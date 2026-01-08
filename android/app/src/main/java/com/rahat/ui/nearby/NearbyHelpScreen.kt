package com.rahat.ui.nearby

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.data.model.PeerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyHelpScreen(
    viewModel: NearbyViewModel,
    userLat: Double,
    userLng: Double,
    onBack: () -> Unit
) {
    val devices by viewModel.nearbyDevices.collectAsState()
    val scope = rememberCoroutineScope()

    // Permission Handling
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

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        val allGranted = map.values.all { it }
        // Service should be running already or started by Home
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions.toTypedArray())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Help (P2P)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                actions = {
                    Icon(Icons.Default.Bluetooth, null, tint = Color.Blue, modifier = Modifier.padding(end = 16.dp))
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (devices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Scanning for nearby devices...", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(devices) { device ->
                        NearbyDeviceItem(device)
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyDeviceItem(device: PeerState) {
    val severityColor = when (device.severity.uppercase()) {
        "CRITICAL" -> Color.Red
        "HIGH" -> Color(0xFFFF9800)
        else -> Color.Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Signal: ${device.signalLevel}", fontSize = 14.sp, color = Color.Gray)
                Text("Trend: ${device.signalTrend}", fontSize = 12.sp, color = Color.Gray)
                if (device.latitude != null && device.longitude != null) {
                    Text("Coords: ${device.latitude}, ${device.longitude}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = severityColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        device.severity,
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (device.severity.uppercase() == "CRITICAL" || device.severity.uppercase() == "HIGH") {
                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
