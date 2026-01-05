package com.rahat.app.ui.sos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SosInProgressScreen(
    onSosSent: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500) // simulate SOS sending
        onSosSent()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Color.Red)

            Text(
                text = "Sending SOS…",
                fontSize = 20.sp,
                color = Color.Red
            )

            Text(
                text = "Please stay calm.\nHelp is being notified.",
                fontSize = 14.sp
            )
        }
    }
}
