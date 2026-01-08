package com.rahat.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.auth.FirebaseAuthManager
import com.rahat.data.firebase.FirestoreUserRepository
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

enum class AuthStep { PHONE, OTP, NAME }

@Composable
fun PhoneLoginScreen(
    firebaseAuthManager: com.rahat.auth.FirebaseAuthManager,
    onLoginSuccess: (String, String, String) -> Unit, // phone, name, rId
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val userRepo = remember { FirestoreUserRepository() }
    
    var step by remember { mutableStateOf(AuthStep.PHONE) }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", fontSize = 32.sp, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))

        when (step) {
            AuthStep.PHONE -> {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !loading,
                    placeholder = { Text("+91XXXXXXXXXX") }
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !loading
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (phoneNumber.isBlank()) {
                            error = "Phone number is required"
                            return@Button
                        }
                        loading = true
                        error = null
                        firebaseAuthManager.sendOtp(
                            activity = context as Activity,
                            phoneNumber = phoneNumber,
                            onCodeSent = {
                                loading = false
                                step = AuthStep.OTP
                            },
                            onError = {
                                loading = false
                                error = it
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Send OTP")
                    }
                }
            }
            AuthStep.OTP -> {
                Text("Enter OTP sent to $phoneNumber")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("6-Digit OTP") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !loading
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (otp.length < 6) {
                            error = "Enter valid OTP"
                            return@Button
                        }
                        loading = true
                        error = null
                        firebaseAuthManager.verifyOtp(
                            code = otp,
                            onSuccess = { uid ->
                                scope.launch {
                                    try {
                                        val existingUser = userRepo.getUser(uid)
                                        loading = false
                                        if (existingUser != null) {
                                            onLoginSuccess(phoneNumber, existingUser.name, existingUser.rId)
                                        } else {
                                            step = AuthStep.NAME
                                        }
                                    } catch (e: Exception) {
                                        loading = false
                                        error = "Failed to check user status"
                                    }
                                }
                            },
                            onError = {
                                loading = false
                                error = it
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Verify OTP")
                    }
                }
            }
            AuthStep.NAME -> {
                Text("Complete your profile")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it},
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            error = "Name is required"
                            return@Button
                        }
                        
                        loading = true
                        error = null
                        
                        scope.launch {
                            try {
                                val uid = firebaseAuthManager.getCurrentUserId() ?: ""
                                userRepo.createUser(
                                    rId = uid,
                                    name = name,
                                    phone = phoneNumber,
                                    email = email
                                )
                                loading = false
                                onLoginSuccess(phoneNumber, name, uid)
                            } catch (e: Exception) {
                                loading = false
                                error = e.message ?: "Failed to create user"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Finish")
                    }
                }
            }
        }
        
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack, enabled = !loading) {
            Text("Back")
        }
    }
}
