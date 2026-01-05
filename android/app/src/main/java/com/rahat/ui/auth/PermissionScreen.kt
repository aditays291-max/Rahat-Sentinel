package com.rahat.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionScreen(
    onAllowClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Permissions Required",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To provide you with the best emergency assistance, RAHAT needs access to the following:",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        PermissionItem(
            icon = Icons.Default.LocationOn,
            title = "Location Access",
            desc = "To find nearby help and share your location in emergencies"
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionItem(
            icon = Icons.Default.Call,
            title = "SMS / Call Access",
            desc = "To alert emergency contacts and send distress signals"
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionItem(
            icon = Icons.Default.Wifi,
            title = "Bluetooth & Network",
            desc = "For offline mesh network and nearby device detection"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F6FF))
        ) {
            Text(
                text = "🔒 Your privacy is important. RAHAT is designed for emergency situations only and does not collect personal information for commercial purposes.",
                modifier = Modifier.padding(12.dp),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onAllowClick()   // 👈 IMPORTANT
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Allow Permissions")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                onSkipClick()    // 👈 IMPORTANT
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Skip for now")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF2979FF)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}
