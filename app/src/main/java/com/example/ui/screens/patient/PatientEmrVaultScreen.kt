package com.example.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrescriptionEntity
import com.example.ui.components.PrescriptionDocumentView
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatientEmrVaultScreen(
    prescriptions: List<PrescriptionEntity>,
    onBack: () -> Unit
) {
    var selectedRx by remember { mutableStateOf<PrescriptionEntity?>(null) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    if (selectedRx != null) {
        PrescriptionDocumentView(
            prescription = selectedRx!!,
            onBack = { selectedRx = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MedBackgroundLight)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("vault_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Patient EMR Digital Vault",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = "Cryptographically verified clinical prescriptions",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = MedTealContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MedTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "All prescriptions are stamped with SHA-256 digital signatures & tamper-proof verification QR codes.",
                                fontSize = 12.sp,
                                color = MedOnTealContainer
                            )
                        }
                    }
                }

                if (prescriptions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No prescriptions found in your EMR vault yet", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(prescriptions) { rx ->
                        Card(
                            onClick = { selectedRx = rx },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vault_rx_item_${rx.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rx.diagnosisICD10,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MedTealDark
                                        )
                                        Text(
                                            text = "Prescribed by: ${rx.doctorName}",
                                            fontSize = 13.sp,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "${rx.doctorQualifications} • ${rx.councilRegistrationNumber}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Surface(
                                        color = MedCyanContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = rx.id,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MedOnCyanContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Date: ${dateFormat.format(Date(rx.createdAt))}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Verified Digital Sign", fontSize = 11.sp, color = MedSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
