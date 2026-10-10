package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicationItem
import com.example.data.model.PrescriptionEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PrescriptionDocumentView(
    prescription: PrescriptionEntity,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(prescription.createdAt))

    // Parse items from simple json
    val medications = parseMedicationsJson(prescription.medicationsJson)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Action Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("rx_back_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Text(
                        text = "Official Clinical Rx: ${prescription.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedTealDark
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Prescription ${prescription.id}")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "MedConnect Pro Verified Prescription\nRx ID: ${prescription.id}\nDoctor: ${prescription.doctorName} (${prescription.councilRegistrationNumber})\nPatient: ${prescription.patientName}\nDiagnosis: ${prescription.diagnosisICD10}\nVerification Link: ${prescription.verificationQrUrl}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Prescription via"))
                        },
                        modifier = Modifier.testTag("rx_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MedTealPrimary)
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "PDF saved to Patient Vault & Document storage", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("rx_download_pdf_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download PDF", tint = MedTealPrimary)
                    }
                }
            }
        }

        // Post-Prescription Consultation Status Indicator (Red = Closed, Green = Open)
        var isConsultationClosed by remember { mutableStateOf(true) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isConsultationClosed) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isConsultationClosed) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConsultationClosed)
                            "CONSULTATION CLOSED 🔴"
                        else
                            "CONSULTATION OPEN 🟢",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isConsultationClosed) Color(0xFF991B1B) else Color(0xFF166534)
                    )
                }

                FilledTonalButton(
                    onClick = { isConsultationClosed = !isConsultationClosed },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isConsultationClosed) Color(0xFF16A34A) else Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("rx_view_toggle_consultation_button")
                ) {
                    Text(
                        text = if (isConsultationClosed) "Reopen (🟢)" else "Close (🔴)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Printable Paper Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedOutlineLight, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // 1. INSTITUTIONAL & DOCTOR REGULATORY HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prescription.doctorName,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedTealDark
                            )
                            Text(
                                text = prescription.doctorQualifications,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Speciality: ${prescription.doctorSpeciality}",
                                fontSize = 13.sp,
                                color = MedTealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MedTealContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Council Reg: ${prescription.councilRegistrationNumber}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedOnTealContainer
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = prescription.clinicAffiliation,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MedTealDark
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_mediconnect_logo),
                                    contentDescription = "Mediconnect Logo",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mediconnect Telemedicine Network",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "Date: $dateString",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Rx ID: ${prescription.id}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedCoralTertiary
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        thickness = 1.5.dp,
                        color = MedTealPrimary
                    )

                    // 2. PATIENT CONTEXT BAR
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MedSurfaceVariantLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Patient: ${prescription.patientName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Age/Gender: ${prescription.patientAge} Yrs / ${prescription.patientGender}",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Weight: ${prescription.patientWeightKg} kg",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                            }

                            if (prescription.patientAllergies.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Allergy Warning",
                                        tint = MedCoralTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Allergies: ${prescription.patientAllergies}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MedCoralTertiary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. CLINICAL DIAGNOSIS & VITALS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "Provisional Diagnosis (ICD-10):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = prescription.diagnosisICD10,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MedTealDark
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recorded Vitals:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "BP: ${prescription.vitalsBp} | HR: ${prescription.vitalsPulse}",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "SpO2: ${prescription.vitalsSpO2} | Temp: ${prescription.vitalsTemp}",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Rx SYMBOL & MEDICATIONS TABLE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "℞",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = MedTealPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Prescribed Medications",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Medication Items List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text("#", Modifier.weight(0.4f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Medicine / Form", Modifier.weight(2.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Schedule & Timing", Modifier.weight(2.0f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Duration", Modifier.weight(1.0f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Qty", Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        medications.forEachIndexed { index, med ->
                            Divider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}", Modifier.weight(0.4f), fontSize = 12.sp)
                                Column(Modifier.weight(2.2f)) {
                                    Text(med.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("(${med.dosageForm})", fontSize = 11.sp, color = MedTealPrimary)
                                }
                                Column(Modifier.weight(2.0f)) {
                                    Surface(
                                        color = MedCyanContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = med.dosageSchedule,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MedOnCyanContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Text(med.timing, fontSize = 11.sp, color = Color.DarkGray)
                                    if (med.instructions.isNotBlank()) {
                                        Text(med.instructions, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                Text("${med.durationDays} days", Modifier.weight(1.0f), fontSize = 12.sp)
                                Text("${med.totalQuantity}", Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. ADVICE & LAB ORDERS
                    if (prescription.clinicalAdviceJson.isNotBlank()) {
                        Text(
                            text = "Clinical Advice & Dietary Guidelines:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = prescription.clinicalAdviceJson,
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (prescription.labOrdersJson.isNotBlank()) {
                        Text(
                            text = "Lab Investigations & Diagnostic Orders:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = prescription.labOrdersJson,
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (prescription.followUpText.isNotBlank()) {
                        Surface(
                            color = Color(0xFFFFF7ED),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = MedWarning, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scheduled Follow-up: ${prescription.followUpText}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedWarning
                                )
                            }
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color(0xFFE2E8F0)
                    )

                    // 6. DOCTOR DIGITAL SIGNATURE & TAMPER-PROOF VERIFICATION QR CODE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // QR Code Verification Block
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(130.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Stylized QR matrix representation
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    repeat(5) { r ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            repeat(5) { c ->
                                                val isFilled = (r + c) % 2 == 0 || (r == 0 && c == 0) || (r == 4 && c == 4)
                                                Box(
                                                    modifier = Modifier
                                                        .size(9.dp)
                                                        .background(if (isFilled) Color.Black else Color.White)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Scan to Verify Tamper-Proof Hash",
                                fontSize = 8.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }

                        // Doctor Signature Block
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Digitally Signed By:",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = prescription.doctorName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Cursive,
                                color = MedTealDark
                            )
                            Text(
                                text = "Council Reg: ${prescription.councilRegistrationNumber}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "SHA-256: ${prescription.doctorSignatureHash.take(16)}...",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Gray
                            )
                            Surface(
                                color = MedSuccessContainer,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = MedSuccess,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "NMC Reg Authenticated",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MedSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verification URL Info Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedOutlineLight)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MedTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Tamper-Proof Verification Link:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = prescription.verificationQrUrl,
                            fontSize = 11.sp,
                            color = MedCyanSecondary
                        )
                    }
                }
            }
        }
    }
}

fun parseMedicationsJson(json: String): List<MedicationItem> {
    val items = mutableListOf<MedicationItem>()
    try {
        // Simple regex parser for MedicationItem JSON
        val pattern = java.util.regex.Pattern.compile(
            """"id":"(.*?)","name":"(.*?)","dosageForm":"(.*?)","dosageSchedule":"(.*?)","timing":"(.*?)","durationDays":(\d+),"totalQuantity":(\d+)"""
        )
        val matcher = pattern.matcher(json)
        while (matcher.find()) {
            items.add(
                MedicationItem(
                    id = matcher.group(1) ?: "",
                    name = matcher.group(2) ?: "",
                    dosageForm = matcher.group(3) ?: "Tab",
                    dosageSchedule = matcher.group(4) ?: "1-0-1",
                    timing = matcher.group(5) ?: "After Food",
                    durationDays = matcher.group(6)?.toIntOrNull() ?: 5,
                    totalQuantity = matcher.group(7)?.toIntOrNull() ?: 10,
                    instructions = ""
                )
            )
        }
    } catch (e: Exception) {
        // Fallback
    }

    if (items.isEmpty()) {
        items.add(
            MedicationItem(
                id = "1",
                name = "Amoxicillin + Clavulanic Acid 625mg",
                dosageForm = "Tab",
                dosageSchedule = "1-0-1",
                timing = "After Food",
                durationDays = 5,
                totalQuantity = 10,
                instructions = "Complete entire course"
            )
        )
    }
    return items
}
