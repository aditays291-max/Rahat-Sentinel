package com.rahat.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ---------------- CUSTOM INFO WINDOW ---------------- */

@Composable
fun CustomInfoWindow(
    alert: com.rahat.data.model.Alert,
    userLocation: org.osmdroid.util.GeoPoint?,
    onDismiss: () -> Unit
) {
    // Whoosh animation
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300)
    )
    
    // Calculate distance
    val distance = if (userLocation != null) {
        val res = FloatArray(1)
        android.location.Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            alert.lat, alert.lon, res
        )
        if (res[0] > 1000) String.format("%.1f km", res[0] / 1000) else "${res[0].toInt()} m"
    } else {
        "--"
    }
    
    // Severity color
    val severityColor = when(alert.severity) {
        com.rahat.data.model.AlertSeverity.CRITICAL -> Color(0xFFFF3B30)
        com.rahat.data.model.AlertSeverity.HIGH -> Color(0xFFFF9500)
        com.rahat.data.model.AlertSeverity.MEDIUM -> Color(0xFFFFCC00)
        com.rahat.data.model.AlertSeverity.LOW -> Color(0xFF34C759)
        else -> Color.Gray
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 80.dp)  // Extra bottom padding to clear buttons
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header with severity badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = severityColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = alert.severity.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Distance info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$distance away",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Alert details
                Text(
                    text = alert.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
