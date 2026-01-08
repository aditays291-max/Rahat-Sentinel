package com.rahat.ui.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.data.model.PeerState
import android.util.Log

/**
 * Senior Architect Implementation: AlertFeedScreen
 * 
 * MANDATORY FIX: This screen now displays REAL nearby devices via BLE.
 * All fake/demo disaster alerts have been removed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertFeedScreen(
    peers: List<PeerState>,
    onBackClick: () -> Unit
) {
    // Audit Log for list update
    remember(peers.size) {
        Log.i("ALERT_FEED", "RAHAT_UI_REPORT: Nearby device list updated with ${peers.size} entries")
    }

    // Sort by Signal Level (Strongest first) and filter only SOS (HIGH severity)
    val sortedPeers = remember(peers) {
        peers.filter { it.severity == "HIGH" }
             .sortedBy { it.signalLevel.ordinal }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency (Nearby Devices)") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Icon(
                        Icons.Default.Bluetooth, 
                        contentDescription = null, 
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        if (sortedPeers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Scanning for nearby devices...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF9F9F9)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedPeers) { peer ->
                    PeerAlertCard(peer)
                }
            }
        }
    }
}

@Composable
fun PeerAlertCard(peer: PeerState) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Signal: ${peer.signalLevel}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Trend: ${peer.signalTrend}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when(peer.signalTrend) {
                        com.rahat.data.model.SignalTrend.APPROACHING -> Color(0xFF2E7D32)
                        com.rahat.data.model.SignalTrend.RECEDING -> Color(0xFFD32F2F)
                        else -> Color.Gray
                    }
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = peer.severity,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                if (peer.severity == "HIGH") {
                    Icon(
                        Icons.Default.Warning, 
                        contentDescription = null, 
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
