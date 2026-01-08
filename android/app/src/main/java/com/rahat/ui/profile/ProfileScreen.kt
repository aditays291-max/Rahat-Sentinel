package com.rahat.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// ---------------- DATA MODEL ----------------
data class EmergencyContact(
    val name: String,
    val relation: String,
    val phone: String,
    val id: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    // userSession removed
    userRepo: com.rahat.data.firebase.FirestoreUserRepository,
    narrator: com.rahat.service.Narrator,
    accessibilityPrefs: com.rahat.data.AccessibilityPreferences,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for Name/Phone auto-fill from contact picker
    var pickerName by remember { mutableStateOf("") }
    var pickerPhone by remember { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            val info = getContactInfo(context, it)
            if (info != null) {
                pickerName = info.first
                pickerPhone = info.second
            }
        }
    }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        }
    }
    val isNarratorEnabled by accessibilityPrefs.isNarratorEnabled.collectAsState()
    val narratorVolume by accessibilityPrefs.narratorVolume.collectAsState()
    
    var userName by remember { mutableStateOf("Loading...") }
    var userPhone by remember { mutableStateOf("...") }
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var rId by remember { mutableStateOf<String?>(null) }

    // Fetch from Local DB
    LaunchedEffect(Unit) {
        val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
        launch(kotlinx.coroutines.Dispatchers.IO) {
            val device = db.rahatDao().getDeviceOneShot()
            if (device != null) {
                rId = device.rId
                // Initial Load
                db.rahatDao().getUserProfile(device.rId).collect { profile ->
                    if (profile != null) {
                        userName = profile.name
                        userPhone = profile.phone
                    }
                }
            }
        }
    }

    // 🔥 CONTACT LIST STATE
    // Fetch contacts from Local DB
    var contacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    
    LaunchedEffect(rId) {
        val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
        if (rId != null) {
            db.rahatDao().getContacts(rId!!).collect { entities ->
                contacts = entities.map { 
                    EmergencyContact(it.name, it.relation, it.phone, it.id)
                }
            }
        }
    }
    
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
                        Text(userName, fontWeight = FontWeight.Bold)
                        Text(userPhone, color = Color.Gray)
                    }

                    IconButton(onClick = { 
                        narrator.speakIfEnabled("Edit Name", isNarratorEnabled, narratorVolume)
                        showEditNameDialog = true 
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
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
                         scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                             val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
                             db.rahatDao().deleteContact(contact.id)
                         }
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // ---------------- EDIT NAME DIALOG ----------------
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Enter your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            rId?.let { id ->
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    // 1. Update Local
                                    val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
                                    db.rahatDao().updateUserName(id, newName)
                                    
                                    // 2. Sync Remote (Fire-and-forget)
                                    launch {
                                        try { userRepo.updateUserName(id, newName) } catch(e: Exception) {}
                                    }
                                    
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        userName = newName
                                        showEditNameDialog = false
                                        narrator.speakIfEnabled("Name updated locally", isNarratorEnabled, narratorVolume)
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ---------------- ADD CONTACT DIALOG ----------------
    if (showAddDialog) {

        var name by remember(pickerName) { mutableStateOf(pickerName) }
        var relation by remember { mutableStateOf("") }
        var phone by remember(pickerPhone) { mutableStateOf(pickerPhone) }

        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                pickerName = ""
                pickerPhone = ""
            },
            title = { Text("Add Emergency Contact") },
            text = {
                Column {
                    OutlinedButton(
                        onClick = { 
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                contactPickerLauncher.launch(null)
                            } else {
                                contactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContactPhone, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pick from Contacts")
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contact Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text("Relation (e.g. Father, Friend)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                             rId?.let { id ->
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
                                    val entity = com.rahat.data.local.entity.EmergencyContactEntity(
                                        ownerId = id,
                                        name = name,
                                        relation = relation.ifBlank { "Family" },
                                        phone = phone
                                    )
                                    db.rahatDao().insertContact(entity)
                                    
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        pickerName = ""
                                        pickerPhone = ""
                                        showAddDialog = false
                                        narrator.speakIfEnabled("Contact added", isNarratorEnabled, narratorVolume)
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    pickerName = ""
                    pickerPhone = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getContactInfo(context: android.content.Context, contactUri: Uri): Pair<String, String>? {
    val contentResolver = context.contentResolver
    contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val name = if (nameIndex != -1) cursor.getString(nameIndex) else "Unknown"
            
            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val id = if (idIndex != -1) cursor.getString(idIndex) else null
            
            var phone = ""
            if (id != null) {
                // Secondary query for phone number
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(id),
                    null
                )?.use { phoneCursor ->
                    while (phoneCursor.moveToNext()) {
                        val phoneColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (phoneColumn != -1) {
                            val number = phoneCursor.getString(phoneColumn)
                            if (number.isNotBlank()) {
                                phone = number
                                break // Take the first one found
                            }
                        }
                    }
                }
            }
            return Pair(name, phone)
        }
    }
    return null
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
