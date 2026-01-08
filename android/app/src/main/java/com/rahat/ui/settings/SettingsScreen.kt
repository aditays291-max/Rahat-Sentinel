package com.rahat.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.data.AccessibilityPreferences
import com.rahat.service.Narrator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    accessibilityPrefs: AccessibilityPreferences,
    narrator: Narrator,
    onBack: () -> Unit
) {
    val isNarratorEnabled by accessibilityPrefs.isNarratorEnabled.collectAsState()
    val narratorVolume by accessibilityPrefs.narratorVolume.collectAsState()
    
    var sound by remember { mutableStateOf("Medium") }
    var vibration by remember { mutableStateOf("Medium") }
    var language by remember { mutableStateOf("English") }
    
    var showAccessibilityMenu by remember { mutableStateOf(false) }
    var showNarratorWarning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        narrator.speakIfEnabled("Back to home", isNarratorEnabled, narratorVolume)
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* SOS later */ },
                containerColor = Color(0xFF2ECC71)
            ) {
                Text("SOS", color = Color.White)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // ================= ACCESSIBILITY =================
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().clickable { 
                    showAccessibilityMenu = !showAccessibilityMenu 
                    narrator.speakIfEnabled("Accessibility Options", isNarratorEnabled, narratorVolume)
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Accessibility Features", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Icon(
                            imageVector = if (showAccessibilityMenu) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = showAccessibilityMenu) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            
                            // Narrator Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Narrator")
                                Switch(
                                    checked = isNarratorEnabled,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            showNarratorWarning = true
                                        } else {
                                            accessibilityPrefs.setNarratorEnabled(false)
                                            narrator.speak("Narrator disabled")
                                        }
                                    }
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Volume Slider
                            Text("Narrator Volume")
                            Slider(
                                value = narratorVolume,
                                onValueChange = { accessibilityPrefs.setNarratorVolume(it) },
                                onValueChangeFinished = {
                                    narrator.speakIfEnabled("Volume adjusted", isNarratorEnabled, narratorVolume)
                                },
                                valueRange = 0f..1f
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ================= GENERAL =================
            Text("General", fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {

                    // 🔊 SOUND
                    Text("Sound")
                    Spacer(Modifier.height(6.dp))
                    Row {
                        listOf("Mute", "Low", "Medium", "High").forEach {
                            ChoiceChip(it, sound == it) { 
                                sound = it
                                narrator.speakIfEnabled("Sound set to $it", isNarratorEnabled, narratorVolume)
                            }
                        }
                    }

                    Divider(Modifier.padding(vertical = 12.dp))

                    // 📳 VIBRATION
                    Text("Vibration")
                    Spacer(Modifier.height(6.dp))
                    Row {
                        listOf("Off", "Short", "Medium", "Strong").forEach {
                            ChoiceChip(it, vibration == it) { 
                                vibration = it
                                narrator.speakIfEnabled("Vibration set to $it", isNarratorEnabled, narratorVolume)
                            }
                        }
                    }

                    Divider(Modifier.padding(vertical = 12.dp))

                    // 🌙 DARK MODE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dark Mode")
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { 
                                onDarkModeChange(it)
                                narrator.speakIfEnabled("Dark mode ${if(it) "enabled" else "disabled"}", isNarratorEnabled, narratorVolume)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ================= LANGUAGE =================
            Text("Language", fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChoiceChip("English", language == "English") {
                        language = "English"
                        narrator.speakIfEnabled("Language set to English", isNarratorEnabled, narratorVolume)
                    }
                    ChoiceChip("हिंदी", language == "Hindi") {
                        language = "Hindi"
                        narrator.speakIfEnabled("Language set to Hindi", isNarratorEnabled, narratorVolume)
                    }
                }
            }
        }
    }

    if (showNarratorWarning) {
        AlertDialog(
            onDismissRequest = { showNarratorWarning = false },
            title = { Text("Turn on Narrator?") },
            text = { Text("Warning: Pressing 'Confirm' will turn on the narrator. Everything you touch will be spoken aloud to assist you. Proceed?") },
            confirmButton = {
                Button(onClick = {
                    accessibilityPrefs.setNarratorEnabled(true)
                    showNarratorWarning = false
                    narrator.speak("Narrator enabled. I will read out the screen for you.", 1.0f)
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNarratorWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF2962FF) else Color(0xFFF1F1F1),
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (selected) Color.White else Color.Black,
            fontSize = 13.sp
        )
    }
}
