package com.rahat.ui.sos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SosSuccessScreen(
    onBackHome: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "SOS Sent Successfully",
                fontSize = 22.sp,
                color = Color(0xFF2E7D32)
            )

            Text(
                text = "Rescue teams have been alerted.\nStay where you are if possible.",
                fontSize = 14.sp
            )

            Button(
                onClick = onBackHome,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("Back to Home")
            }
        }
    }
}
