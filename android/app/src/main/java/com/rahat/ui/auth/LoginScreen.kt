package com.rahat.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onPhoneLoginClick: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.rahat_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Text("Welcome to RAHAT", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
        Text("Disaster Response Coordination", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
        
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onPhoneLoginClick,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Login with Phone Number")
        }
        
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Login with Azure AD")
        }
        
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Continue as Guest")
        }
    }
}
