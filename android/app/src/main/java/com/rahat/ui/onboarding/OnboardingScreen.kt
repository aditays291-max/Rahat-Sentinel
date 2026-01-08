package com.rahat.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rahat.data.local.RahatDatabase
import com.rahat.data.local.entity.DeviceEntity
import com.rahat.data.local.entity.UserProfileEntity
import com.rahat.security.IdentityManager
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Rahat", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enter your details to get started with secure, offline emergency response.")
        Spacer(modifier = Modifier.height(32.dp))
        
        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Phone field
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                loading = true
                scope.launch {
                    val db = RahatDatabase.getDatabase(context)
                    val idManager = IdentityManager(context)
                    
                    // 1. Generate Identity
                    val secret = idManager.getOrCreateDeviceSecret()
                    val rId = idManager.generateEphemeralIdAtOffset(secret, IdentityManager.TIME_WINDOW_MS, 0L)
                    // Ensure secret exists
                    
                    // 2. Save to Local DB
                    db.rahatDao().insertDevice(DeviceEntity(rId, System.currentTimeMillis()))
                    db.rahatDao().insertUserProfile(UserProfileEntity(rId, phone, name, "{}"))
                    
                    // 3. Sync to Firestore (fire-and-forget)
                    try {
                        val userRepo = com.rahat.data.firebase.FirestoreUserRepository()
                        userRepo.createUser(rId, name, phone)
                        android.util.Log.d("Onboarding", "User synced to Firestore: $rId")
                    } catch (e: Exception) {
                        android.util.Log.e("Onboarding", "Firestore sync failed: ${e.message}")
                        // Continue anyway - offline-first
                    }
                    
                    // 4. Complete
                    loading = false
                    onComplete()
                }
            },
            enabled = name.isNotEmpty() && phone.isNotEmpty() && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Initializing..." else "Start Rahat")
        }
    }
}
