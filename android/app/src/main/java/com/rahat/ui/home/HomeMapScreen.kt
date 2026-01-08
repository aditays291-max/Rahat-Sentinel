package com.rahat.ui.home

import android.Manifest
import android.os.Build
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.input.pointer.pointerInput
import com.rahat.service.Narrator
import com.rahat.data.AccessibilityPreferences
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import com.google.android.gms.location.*
import com.rahat.state.MenuAction
import com.rahat.state.UiState
import com.rahat.ui.home.MapViewModel
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import android.animation.ValueAnimator
import android.animation.TypeEvaluator
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.rahat.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeMapScreen(
    uiState: UiState,
    mapViewModel: MapViewModel,
    onNearbyHelpClick: () -> Unit,
    onStatusClick: () -> Unit,
    onMenuAction: (MenuAction) -> Unit,
    onOpenAlertFeed: () -> Unit,
    sosManager: com.rahat.ui.sos.SosManager,
    accessibilityPrefs: AccessibilityPreferences,
    narrator: Narrator
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Permission Launcher
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        val callGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
        if (!smsGranted || !callGranted) {
            // Optional: Show toast or rationale
        }
    }

    LaunchedEffect(Unit) {
        val permissionsNeeded = mutableListOf<String>()
        val currentPermissions = listOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        currentPermissions.forEach { perm ->
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(perm)
            }
        }

        // Android 12+ BLE Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blePermissions = listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            blePermissions.forEach { perm ->
                if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(perm)
                }
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toTypedArray())
        }
    }
    val isNarratorEnabled by accessibilityPrefs.isNarratorEnabled.collectAsState()
    val narratorVolume by accessibilityPrefs.narratorVolume.collectAsState()
    
    val sosState by sosManager.sosState.collectAsState()
    val sosCountdown by sosManager.countdown.collectAsState()
    
    // Fix OSMDroid Configuration
    LaunchedEffect(Unit) {
        org.osmdroid.config.Configuration.getInstance().load(
            context,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        )
        // Set User Agent to avoid blocking (important for OSM servers)
        org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
    }
    
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    
    // Collect from ViewModel
    val userLocation = mapViewModel.userLocation.collectAsState()
    val alerts by mapViewModel.alerts.collectAsState()
    val nearbyPeers by mapViewModel.nearbyPeers.collectAsState()
    
    var showDebugOverlay by remember { mutableStateOf(false) }

    /* ---------------- PERMISSION ---------------- */

    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    var permissionGranted by remember { mutableStateOf(hasPermission) }

    // MANDATORY FIX: Do NOT block map rendering on permission
    // We will show the map and center on last known or default if permission denied
    val showPermissionOverlay = !permissionGranted
    
    // One-time centering gate
    var shouldCenter by remember { mutableStateOf(true) }

    /* ---------------- GPS ---------------- */

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }



    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Senior preference for emergency
            3000L
        )
            .setMinUpdateDistanceMeters(2f)
            .setWaitForAccurateLocation(false) // MANDATORY: Get SOMETHING immediately
            .build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location = result.lastLocation ?: return
                mapViewModel.onLocationUpdated(location)
            }
        }
    }

    // @SuppressLint("MissingPermission") // Moved to function level if needed, or ignored as check is done above.
    // The previous check `if (!permissionGranted) return` guarantees permission.
    // But Lint might not know.
    // We can use @SuppressLint on the function HomeMapScreen or suppress differently.
    // For now, removing it from expression.
    LaunchedEffect(Unit) {
        // MANDATORY FIX: Get immediate coarse location for instant map load
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    android.util.Log.i("HOME_MAP", "RAHAT_UI_REPORT: Self location acquired (Last Known): (${loc.latitude}, ${loc.longitude})")
                    mapViewModel.onLocationUpdated(loc)
                    // Immediate center on last known
                    shouldCenter = true 
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            android.util.Log.e("HOME_MAP", "Permission lost during start: ${e.message}")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /* ---------------- UI ---------------- */

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            DrawerContent(
                onClose = { scope.launch { drawerState.close() } },
                onMenuAction = onMenuAction,
                narrator = narrator,
                isNarratorEnabled = isNarratorEnabled,
                narratorVolume = narratorVolume
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Rahat") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            narrator.speakIfEnabled("Open Alert Feed", isNarratorEnabled, narratorVolume)
                            onOpenAlertFeed()
                        }) {
                             Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                        StatusIcon(Icons.Default.LocationOn, userLocation.value != null)
                        StatusIcon(Icons.Default.Wifi, true)
                        StatusIcon(Icons.Default.Bluetooth, false)
                    }
                )
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Debug Toggle (Hidden in Production normally)
                if (androidx.compose.ui.platform.LocalInspectionMode.current || true) {
                    Box(Modifier.align(Alignment.TopStart).padding(8.dp)) {
                        IconButton(onClick = { showDebugOverlay = !showDebugOverlay }) {
                            Icon(Icons.Default.BugReport, "Debug", tint = Color.Gray)
                        }
                    }
                }

                val acknowledgedAlerts = remember { mutableStateListOf<String>() }
                var selectedAlert by remember { mutableStateOf<com.rahat.data.model.Alert?>(null) }

                Box(modifier = Modifier.fillMaxSize()) {
                    OsmMapView(
                        userLocation = userLocation,
                        alerts = alerts,
                        nearbyPeers = nearbyPeers,
                        acknowledgedAlertIds = emptyList(),
                        onAlertAck = {},
                        onAlertClick = { selectedAlert = it },
                        shouldCenter = shouldCenter,
                        onCentered = { shouldCenter = false }
                    )
                    
                    // Emergency Panel (HIGH severity list)
                    EmergencyPanel(
                        peers = nearbyPeers,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                    )
                    
                    // Bottom buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedAlert = null  // Auto-dismiss info window
                                onNearbyHelpClick()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Nearby Help")
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = {
                                selectedAlert = null  // Auto-dismiss info window
                                onStatusClick()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF34C759)
                            )
                        ) {
                            Text("Status")
                        }
                    }

                    // Countdown Overlay
                    if (sosState == com.rahat.ui.sos.SosState.COUNTDOWN) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = sosCountdown.toString(),
                                    color = Color.White,
                                    fontSize = 120.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "HOLD TO SEND SOS",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "RELEASE TO CANCEL",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // SOS Button using custom Box to avoid FAB event consumption
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 24.dp, bottom = 88.dp)
                            .size(64.dp)
                            .background(Color(0xFFFF3B30), shape = CircleShape)
                            .pointerInput(isNarratorEnabled, narratorVolume) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown()
                                        // Start countdown immediately on press
                                        selectedAlert = null
                                        // Start countdown immediately on press
                                        selectedAlert = null
                                        val name = "User" // TODO: Get from DB preferrably
                                        val loc = userLocation.value
                                        val coords = if (loc != null) "Lat: ${loc.latitude}, Lng: ${loc.longitude}" else "Lat: 21.1458, Lng: 79.0882"
                                        
                                        android.util.Log.d("HomeMapScreen", "SOS Button Pressed - starting countdown")
                                        sosManager.startCountdown(isNarratorEnabled, narratorVolume, name, coords)
                                        
                                        val waitResult = withTimeoutOrNull(5000) {
                                            waitForUpOrCancellation()
                                        }
                                        
                                        if (waitResult != null) {
                                            android.util.Log.d("HomeMapScreen", "SOS Button Released early - cancelling")
                                            sosManager.cancelCountdown()
                                        } else {
                                            android.util.Log.d("HomeMapScreen", "SOS Button held for 5s - waiting for trigger")
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SOS", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    // Custom Info Window Overlay - positioned above buttons
                    selectedAlert?.let { alert ->
                        CustomInfoWindow(
                            alert = alert,
                            userLocation = userLocation.value,
                            onDismiss = { selectedAlert = null }
                        )
                    }

                // Permission Overlay (Non-blocking)
                if (showPermissionOverlay) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { 
                            // Re-trigger permission check
                            onMenuAction(MenuAction.SETTINGS) // Fallback to settings
                        }) {
                            Text("Enable Location for Real-time Tracking")
                        }
                    }
                }

                // Debug Overlay
                if (showDebugOverlay) {
                    DebugOverlay(nearbyPeers, Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}
}

@Composable
fun DebugOverlay(peers: List<com.rahat.data.model.PeerState>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.Black.copy(0.8f))
            .padding(8.dp)
    ) {
        LazyColumn {
            item {
                Text("DEBUG: MESH PEERS", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            items(peers) { peer ->
                Text(
                    "ID: ${peer.rId} | Signal: ${peer.signalLevel} | Trend: ${peer.signalTrend} | Seen: ${System.currentTimeMillis() - peer.lastSeen}ms",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/* ---------------- MAP VIEW (FIXED & SMOOTH) ---------------- */

/* ---------------- MAP VIEW (SMOOTH & REUSABLE) ---------------- */

@Composable
fun OsmMapView(
    userLocation: androidx.compose.runtime.State<org.osmdroid.util.GeoPoint?>,
    alerts: List<com.rahat.data.model.Alert>,
    nearbyPeers: List<com.rahat.data.model.PeerState>,
    acknowledgedAlertIds: List<String>,
    onAlertAck: (String) -> Unit,
    onAlertClick: (com.rahat.data.model.Alert) -> Unit,
    shouldCenter: Boolean,
    onCentered: () -> Unit
) {
    val context = LocalContext.current
    val peerMarkers = remember { mutableMapOf<String, org.osmdroid.views.overlay.Marker>() }
    // Random positions for peers without location
    val peerAngles = remember { mutableMapOf<String, Double>() }

    AndroidView(factory = { ctx ->
        org.osmdroid.views.MapView(ctx).apply {
            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(18.0)
            setBuiltInZoomControls(false)
            android.util.Log.i("HOME_MAP", "RAHAT_UI_REPORT: Map initialized immediately on screen load")
        }
    }, modifier = Modifier.fillMaxSize(), update = { mapView ->
        // One-time centering
        if (shouldCenter) {
            userLocation.value?.let { geo ->
                mapView.controller.animateTo(geo)
                onCentered()
            }
        }

        mapView.overlays.clear()
        
        // 1. User Marker (BLUE)
        userLocation.value?.let { geo ->
            val userMarker = org.osmdroid.views.overlay.Marker(mapView).apply {
                position = geo
                icon = createSimpleIcon(context, android.graphics.Color.BLUE)
                setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                setOnMarkerClickListener { _, _ -> true } 
            }
            mapView.overlays.add(userMarker)
        }

        // 2. Alert Markers
        alerts.forEach { alert ->
            val marker = org.osmdroid.views.overlay.Marker(mapView).apply {
                position = org.osmdroid.util.GeoPoint(alert.lat, alert.lon)
                icon = createSleekMarker(context, getSeverityColorInt(alert.severity.name))
                setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                title = alert.severity.name
                setOnMarkerClickListener { _, _ -> 
                    onAlertClick(alert)
                    true 
                }
            }
            mapView.overlays.add(marker)
        }

        // 3. Peer Markers (ORANGE)
        val now = System.currentTimeMillis()
        nearbyPeers.forEach { peer ->
            val isStale = now - peer.lastSeen > 30000 
            val marker = peerMarkers.getOrPut(peer.rId) {
                org.osmdroid.views.overlay.Marker(mapView).apply {
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                }
            }
            
            val userLoc = userLocation.value
            if (peer.latitude != null && peer.longitude != null) {
                marker.position = org.osmdroid.util.GeoPoint(peer.latitude, peer.longitude)
            } else if (userLoc != null) {
                // Approximate distance rings for peers without GPS
                val distanceMeters = when (peer.signalLevel) {
                    com.rahat.data.model.SignalLevel.VERY_STRONG -> 10.0
                    com.rahat.data.model.SignalLevel.STRONG -> 30.0
                    com.rahat.data.model.SignalLevel.MODERATE -> 70.0
                    com.rahat.data.model.SignalLevel.WEAK -> 150.0
                }
                val angle = peerAngles.getOrPut(peer.rId) { Math.random() * 2 * Math.PI }
                // Rough coordinate offset (approx 0.00001 degrees per meter)
                val latOffset = (distanceMeters * Math.cos(angle)) / 111111.0
                val lngOffset = (distanceMeters * Math.sin(angle)) / (111111.0 * Math.cos(Math.toRadians(userLoc.latitude)))
                marker.position = org.osmdroid.util.GeoPoint(userLoc.latitude + latOffset, userLoc.longitude + lngOffset)
            }
            
            val alpha = if (isStale) 60 else 255
            marker.icon = createPeerIcon(context, android.graphics.Color.parseColor("#FF8C00"), alpha, peer.signalLevel)
            marker.title = "${peer.name} (${peer.signalTrend})"
            mapView.overlays.add(marker)
            android.util.Log.v("HOME_MAP", "MAP_MARKER_RENDER: Peer ${peer.rId} at ${marker.position.latitude}, ${marker.position.longitude}")
        }
        
        mapView.invalidate()
    })
}

@Composable
fun EmergencyPanel(peers: List<com.rahat.data.model.PeerState>, modifier: Modifier) {
    val highPeers = peers.filter { it.severity == "HIGH" }.take(3)
    if (highPeers.isEmpty()) return

    Column(
        modifier = modifier
            .width(200.dp)
            .background(Color.Black.copy(0.7f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("Nearby Emergency", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        highPeers.forEach { peer ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Color(0xFFFF8C00), CircleShape))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(peer.name, color = Color.White, fontSize = 12.sp)
                    Text(
                        "${peer.signalLevel} • ${peer.signalTrend}",
                        color = Color.White.copy(0.7f),
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

private fun createSimpleIcon(context: android.content.Context, color: Int): android.graphics.drawable.Drawable {
    val size = 50
    val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val c = android.graphics.Canvas(bmp)
    val p = android.graphics.Paint().apply { isAntiAlias = true; this.color = color }
    c.drawCircle(size/2f, size/2f, size/2f, p.apply { alpha = 50 })
    c.drawCircle(size/2f, size/2f, size/4f, p.apply { alpha = 255 })
    return android.graphics.drawable.BitmapDrawable(context.resources, bmp)
}

fun getSeverityColorInt(sev: String): Int {
    return when (sev.uppercase()) {
        "CRITICAL" -> android.graphics.Color.RED
        "HIGH" -> android.graphics.Color.parseColor("#FF8C00")
        "MEDIUM" -> android.graphics.Color.YELLOW
        else -> android.graphics.Color.GREEN
    }
}

private fun createPeerIcon(context: android.content.Context, color: Int, alpha: Int, level: com.rahat.data.model.SignalLevel): android.graphics.drawable.Drawable {
    val baseSize = 56
    val size = when(level) {
        com.rahat.data.model.SignalLevel.VERY_STRONG -> 64
        com.rahat.data.model.SignalLevel.STRONG -> 56
        com.rahat.data.model.SignalLevel.MODERATE -> 48
        com.rahat.data.model.SignalLevel.WEAK -> 40
    }
    val bmp = android.graphics.Bitmap.createBitmap(baseSize, baseSize, android.graphics.Bitmap.Config.ARGB_8888)
    val c = android.graphics.Canvas(bmp)
    val p = android.graphics.Paint().apply { isAntiAlias = true; this.color = color; this.alpha = alpha }
    c.drawCircle(baseSize/2f, baseSize/2f, size/2f, p.apply { this.alpha = alpha / 4 })
    c.drawCircle(baseSize/2f, baseSize/2f, size/4f, p.apply { this.alpha = alpha })
    return android.graphics.drawable.BitmapDrawable(context.resources, bmp)
}

private fun createSleekMarker(context: android.content.Context, color: Int): android.graphics.drawable.Drawable {
    val size = 64
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply { isAntiAlias = true }
    paint.color = android.graphics.Color.BLACK
    paint.alpha = 40
    canvas.drawCircle(size/2f, size/2f + 2f, size/2.5f, paint)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size/2f, size/2f, size/2.5f, paint)
    paint.color = color
    canvas.drawCircle(size/2f, size/2f, size/3.8f, paint)
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}

/* ---------------- DRAWER ---------------- */

@Composable
fun DrawerContent(
    onClose: () -> Unit,
    onMenuAction: (MenuAction) -> Unit,
    narrator: Narrator,
    isNarratorEnabled: Boolean,
    narratorVolume: Float
) {
    val context = LocalContext.current
    var displayName by remember { mutableStateOf("Rahat User") }
    
    // Fetch Name from DB
    LaunchedEffect(Unit) {
        val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
        val device = db.rahatDao().getDeviceOneShot()
        device?.let {
            db.rahatDao().getUserProfile(it.rId).collect { profile ->
                profile?.let { p ->
                    displayName = p.name
                }
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color.Black else Color.White
    val headerColor = if (isDark) Color.Gray else Color(0xFF2962FF)
    val textColor = if (isDark) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(backgroundColor)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    headerColor,
                    RoundedCornerShape(bottomStart = 24.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Icon(
                        Icons.Default.AccountCircle,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        displayName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Active Mode",
                        color = Color.White.copy(0.9f),
                        fontSize = 14.sp
                    )
                }

                IconButton(onClick = {
                    narrator.speakIfEnabled("Close Menu", isNarratorEnabled, narratorVolume)
                    onClose()
                }) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        DrawerItem(Icons.Default.Person, "Profile", textColor) {
            narrator.speakIfEnabled("Open Profile", isNarratorEnabled, narratorVolume)
            onMenuAction(MenuAction.PROFILE); onClose()
        }

        DrawerItem(Icons.Default.Settings, "Settings", textColor) {
            narrator.speakIfEnabled("Open Settings", isNarratorEnabled, narratorVolume)
            onMenuAction(MenuAction.SETTINGS); onClose()
        }

        Spacer(modifier = Modifier.weight(1f))

        DrawerItem(Icons.Default.ExitToApp, "Logout", Color.Red) {
            onMenuAction(MenuAction.LOGOUT); onClose()
        }
    }
}

/* ---------------- SMALL UI ---------------- */

@Composable
fun DrawerItem(
    icon: ImageVector,
    title: String,
    color: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color)
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = color)
    }
}

@Composable
fun StatusIcon(icon: ImageVector, enabled: Boolean) {
    Icon(
        icon,
        contentDescription = null,
        tint = if (enabled) Color(0xFF2979FF) else Color.Gray,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}
