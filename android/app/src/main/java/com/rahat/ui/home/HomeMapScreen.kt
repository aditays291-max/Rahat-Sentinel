package com.rahat.app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.rahat.app.state.MenuAction
import com.rahat.app.state.UiState
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMapScreen(
    uiState: UiState,
    onSosClick: () -> Unit,
    onNearbyHelpClick: () -> Unit,
    onStatusClick: () -> Unit,
    onMenuAction: (MenuAction) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    /* ---------------- PERMISSION ---------------- */

    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    var permissionGranted by remember { mutableStateOf(hasPermission) }

    if (!permissionGranted) {
        LocationPermissionScreen {
            permissionGranted = true
        }
        return
    }

    /* ---------------- GPS ---------------- */

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            5000L
        )
            .setMinUpdateDistanceMeters(10f)
            .build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location = result.lastLocation ?: return
                userLocation = GeoPoint(location.latitude, location.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    LaunchedEffect(Unit) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
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
                onMenuAction = onMenuAction
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
                        StatusIcon(Icons.Default.LocationOn, userLocation != null)
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

                OsmMapView(
                    context = context,
                    userLocation = userLocation
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onNearbyHelpClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nearby Help")
                    }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = onStatusClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34C759)
                        )
                    ) {
                        Text("Status")
                    }
                }

                FloatingActionButton(
                    onClick = onSosClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 88.dp),
                    containerColor = Color(0xFFFF3B30)
                ) {
                    Text("SOS", color = Color.White)
                }
            }
        }
    }
}

/* ---------------- MAP VIEW (FIXED & SMOOTH) ---------------- */

@Composable
fun OsmMapView(
    context: Context,
    userLocation: GeoPoint?
) {
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setUseDataConnection(true)
            isTilesScaledToDpi = true
            setFlingEnabled(true)

            // ❌ Disable default zoom buttons (they were overlapping UI)
            zoomController.setVisibility(
                CustomZoomButtonsController.Visibility.NEVER
            )

            // ✅ Faster first load
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(28.6139, 77.2090)) // Delhi default
        }
    }

    var hasCenteredOnce by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView },
        update = {
            userLocation?.let { point ->

                val marker =
                    mapView.overlays.filterIsInstance<Marker>().firstOrNull()
                        ?: Marker(mapView).also {
                            it.setAnchor(
                                Marker.ANCHOR_CENTER,
                                Marker.ANCHOR_BOTTOM
                            )
                            it.title = "You are here"
                            mapView.overlays.add(it)
                        }

                marker.position = point

                if (!hasCenteredOnce) {
                    mapView.controller.setCenter(point)
                    hasCenteredOnce = true
                }
            }
        }
    )
}

/* ---------------- DRAWER ---------------- */

@Composable
fun DrawerContent(
    onClose: () -> Unit,
    onMenuAction: (MenuAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color.White)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF2962FF),
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
                    Text("Guest User", color = Color.White, fontSize = 18.sp)
                    Text(
                        "+91 98765 43210",
                        color = Color.White.copy(0.9f),
                        fontSize = 14.sp
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        DrawerItem(Icons.Default.Person, "Profile") {
            onMenuAction(MenuAction.PROFILE); onClose()
        }

        DrawerItem(Icons.Default.Settings, "Settings") {
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
