package com.rahat.app.ui.nearby

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyHelpScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val services = listOf(
        Triple("Police", "112", Icons.Default.LocalPolice),
        Triple("Ambulance", "108", Icons.Default.LocalHospital),
        Triple("Fire Brigade", "101", Icons.Default.FireTruck),
        Triple("Disaster Helpline", "1078", Icons.Default.Warning)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Help") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text = "Emergency Services",
                fontSize = 18.sp,
                color = Color.Black
            )

            services.forEach { (name, number, icon) ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:$number")
                            )
                            context.startActivity(intent)
                        },
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )

                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Call $number",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Works without internet • Tap to call instantly",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
