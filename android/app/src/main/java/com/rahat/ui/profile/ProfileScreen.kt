package com.rahat.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------- DATA MODEL ----------------
data class EmergencyContact(
    val name: String,
    val relation: String,
    val phone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onAddContact: () -> Unit = {},
    onDeleteContact: (String) -> Unit = {}
) {

    // 🔥 CONTACT LIST STATE
    var contacts by remember {
        mutableStateOf(
            listOf(
                EmergencyContact("Rajesh Kumar", "Father", "+91 98765 43210"),
                EmergencyContact("Sunita Kumar", "Mother", "+91 98765 43211")
            )
        )
    }

    // 🔥 ADD DIALOG STATE
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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

            // 👤 USER CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF2979FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Guest User", fontWeight = FontWeight.Bold)
                        Text("+91 98765 43210", color = Color.Gray)
                    }

                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(Modifier.height(24.dp))

            // 🚨 HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Emergency Family Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                TextButton(onClick = { showAddDialog = true }) {
                    Text("+ Add")
                }
            }

            Spacer(Modifier.height(12.dp))

            // 📞 CONTACT LIST
            contacts.forEach { contact ->
                EmergencyContactItem(
                    name = contact.name,
                    relation = contact.relation,
                    phone = contact.phone,
                    onDelete = {
                        contacts = contacts.filter { it != contact }
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // ---------------- ADD CONTACT DIALOG ----------------
    if (showAddDialog) {

        var name by remember { mutableStateOf("") }
        var relation by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Emergency Contact") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text("Relation") },
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            contacts = contacts + EmergencyContact(
                                name = name,
                                relation = relation.ifBlank { "Family" },
                                phone = phone
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ---------------- CONTACT ITEM ----------------
@Composable
fun EmergencyContactItem(
    name: String,
    relation: String,
    phone: String,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(relation) }
                    )
                }
                Text(phone, color = Color.Gray)
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}
