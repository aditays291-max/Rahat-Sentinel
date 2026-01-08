package com.rahat.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rahat.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import kotlinx.coroutines.delay



@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        // 🔁 Logo scale animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )

        // ⏳ Splash duration
        delay(2000)

        // ➡️ Go to Home
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // White background
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.rahat_logo),
                contentDescription = "Rahat Logo",
                modifier = Modifier
                    .size(300.dp)
                    .scale(scale.value)
            )
            // Removed Text "RAHAT" if the logo already contains the text or user wants "only the logo in the middle"
            // The prompt said: "only the logo in the middle(the blue part)shows".
            // If the jpg has white background, it will blend.
        }
    }
}
