package com.rahat

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.rahat.data.repo.AlertRepository
import com.rahat.service.Narrator
import com.rahat.service.NotificationService
import com.rahat.state.MenuAction
import com.rahat.state.UiState
import com.rahat.ui.alert.AlertFeedScreen
import com.rahat.data.repo.MeshRepository
import com.rahat.ui.home.HomeMapScreen
import com.rahat.ui.home.MapViewModel
import com.rahat.ui.home.MapViewModelFactory
import com.rahat.ui.sos.SosInProgressScreen
import com.rahat.ui.sos.SosManager
import com.rahat.ui.sos.SosState
import com.rahat.ui.sos.SosSuccessScreen
import com.rahat.ui.theme.RahatTheme

enum class Screen { SPLASH, ONBOARDING, HOME, ALERT_FEED, PROFILE, SETTINGS, NEARBY_HELP }

@Composable
fun RahatApp() {
    val context = LocalContext.current
    
    // Dependencies
    val alertRepo = remember { AlertRepository() }
    val notificationService = remember { NotificationService(context) }
    val narrator = remember { Narrator(context) }
    val sosManager = remember { SosManager(context, narrator) }
    val userRepo = remember { com.rahat.data.firebase.FirestoreUserRepository() } // Keep for optional sync
    val accessibilityPrefs = remember { com.rahat.data.AccessibilityPreferences(context) }
    val database = remember { com.rahat.data.local.RahatDatabase.getDatabase(context) }
    val nearbyViewModel = remember { com.rahat.ui.nearby.NearbyViewModel() }
    
    // ViewModel
    val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory(alertRepo, userRepo))
    
    
    // Auth State monitoring
    // State
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    val sosState by sosManager.sosState.collectAsState()
    val alerts by mapViewModel.alerts.collectAsState()
    val userLocation = mapViewModel.userLocation.collectAsState()
    
    // Theme Preferences
    val themePrefs = remember { com.rahat.data.ThemePreferences(context) }
    var isDark by remember { mutableStateOf(themePrefs.isDarkMode()) }
    
    // Notifications, Precise Coordinate Sync & Nearby Advertising
    LaunchedEffect(userLocation.value) {
        val loc = userLocation.value
        if (loc != null) {
            // Start Mesh Services
            val locationIntent = Intent(context, com.rahat.service.EmergencyForegroundService::class.java).apply {
                action = com.rahat.service.EmergencyForegroundService.ACTION_START
                putExtra("lat", loc.latitude)
                putExtra("lng", loc.longitude)
            }
            
            val bleMeshIntent = Intent(context, com.rahat.service.EmergencyBleService::class.java).apply {
                putExtra("lat", loc.latitude)
                putExtra("lng", loc.longitude)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(locationIntent)
                context.startForegroundService(bleMeshIntent)
            } else {
                context.startService(locationIntent)
                context.startService(bleMeshIntent)
            }
        }
    }

    RahatTheme(isDarkMode = isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.SPLASH -> {
                    com.rahat.ui.splash.SplashScreen(
                        onSplashFinished = {
                            // Check Local DB for Identity
                            val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
                            val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
                            scope.launch {
                                val device = db.rahatDao().getDeviceOneShot()
                                withContext(Dispatchers.Main) {
                                    if (device != null) {
                                        currentScreen = Screen.HOME
                                    } else {
                                        currentScreen = Screen.ONBOARDING
                                    }
                                }
                            }
                        }
                    )
                }
                Screen.ONBOARDING -> {
                    com.rahat.ui.onboarding.OnboardingScreen(
                        onComplete = { currentScreen = Screen.HOME }
                    )
                }
                Screen.HOME, Screen.ALERT_FEED, Screen.PROFILE, Screen.SETTINGS, Screen.NEARBY_HELP -> {
                     // Only show these specialized screens for SENDING and SENT states
                     if (sosState == SosState.SENDING || sosState == SosState.SENT) {
                        when (sosState) {
                            SosState.SENDING -> SosInProgressScreen(onSosSent = { })
                            SosState.SENT -> SosSuccessScreen { sosManager.reset() }
                            else -> {}
                        }
                      } else {
                         PermissionGate {
                             // Main Content remains visible during IDLE and COUNTDOWN
                            when (currentScreen) {
                                Screen.HOME -> {
                                        HomeMapScreen(
                                            uiState = UiState(),
                                            mapViewModel = mapViewModel,
                                            sosManager = sosManager,
                                            accessibilityPrefs = accessibilityPrefs,
                                            narrator = narrator,
                                            onNearbyHelpClick = { currentScreen = Screen.NEARBY_HELP },
                                            onStatusClick = { currentScreen = Screen.ALERT_FEED },
                                            onMenuAction = { action ->
                                                when(action) {
                                                    MenuAction.LOGOUT -> {
                                                         // "Lock" functionality? Or just Reset?
                                                         // For now, maybe just show toast "Active Mode"
                                                    }
                                                    MenuAction.PROFILE -> currentScreen = Screen.PROFILE
                                                    MenuAction.SETTINGS -> currentScreen = Screen.SETTINGS
                                                    else -> {}
                                                }
                                            },
                                            onOpenAlertFeed = { currentScreen = Screen.ALERT_FEED }
                                        )
                                }
                            Screen.ALERT_FEED -> {
                                val nearbyPeers by MeshRepository.nearbyPeers.collectAsState()
                                AlertFeedScreen(
                                    peers = nearbyPeers,
                                    onBackClick = { currentScreen = Screen.HOME }
                                )
                            }
                            Screen.PROFILE -> {
                                com.rahat.ui.profile.ProfileScreen(
                                    // userSession = userSession,
                                    userRepo = userRepo,
                                    narrator = narrator,
                                    accessibilityPrefs = accessibilityPrefs,
                                    onBack = { currentScreen = Screen.HOME }
                                )
                            }
                            Screen.SETTINGS -> {
                                com.rahat.ui.settings.SettingsScreen(
                                    isDarkMode = isDark,
                                    onDarkModeChange = { enabled ->
                                        isDark = enabled
                                        themePrefs.setDarkMode(enabled)
                                    },
                                    accessibilityPrefs = accessibilityPrefs,
                                    narrator = narrator,
                                    onBack = { currentScreen = Screen.HOME }
                                )
                            }
                            Screen.NEARBY_HELP -> {
                                com.rahat.ui.nearby.NearbyHelpScreen(
                                    viewModel = nearbyViewModel,
                                    userLat = userLocation.value?.latitude ?: 0.0,
                                    userLng = userLocation.value?.longitude ?: 0.0,
                                    onBack = { 
                                        // Service keeps running!
                                        currentScreen = Screen.HOME 
                                    }
                                )
                            }
                            Screen.SPLASH, Screen.ONBOARDING -> {}
                            else -> {}
                           }
                         }
                      }
                 }
                 else -> {}
            }
        }
    }
}

@Composable
fun PermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    var allGranted by remember {
        mutableStateOf(permissions.all {
            androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        })
    }

    if (allGranted) {
        content()
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    null,
                    tint = Color.Red,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "CRITICAL PERMISSIONS MISSING",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "RAHAT requires Bluetooth and Location to discover nearby devices and send SOS signals during disasters.",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("ENABLE IN SETTINGS")
                }
            }
        }
    }
}
