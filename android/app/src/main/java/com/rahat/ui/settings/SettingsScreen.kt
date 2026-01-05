package com.rahat.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {

    var sound by remember { mutableStateOf("Medium") }
    var vibration by remember { mutableStateOf("Medium") }
    var language by remember { mutableStateOf("English") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                            ChoiceChip(it, sound == it) { sound = it }
                        }
                    }

                    Divider(Modifier.padding(vertical = 12.dp))

                    // 📳 VIBRATION
                    Text("Vibration")
                    Spacer(Modifier.height(6.dp))
                    Row {
                        listOf("Off", "Short", "Medium", "Strong").forEach {
                            ChoiceChip(it, vibration == it) { vibration = it }
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
                            onCheckedChange = onDarkModeChange
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
                    }
                    ChoiceChip("हिंदी", language == "Hindi") {
                        language = "Hindi"
                    }
                }
            }
        }
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
