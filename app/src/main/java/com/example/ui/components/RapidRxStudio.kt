package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicationItem
import com.example.data.model.PatientProfileEntity
import com.example.data.model.PrescriptionEntity
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RapidRxStudio(
    patientProfile: PatientProfileEntity,
    patientName: String,
    appointmentId: String,
    onPrescriptionCreated: (PrescriptionEntity) -> Unit,
    onSubmitPrescription: (
        appointmentId: String,
        diagnosisICD10: String,
        vitalsBp: String,
        vitalsPulse: String,
        vitalsSpO2: String,
        vitalsTemp: String,
        medications: List<MedicationItem>,
        clinicalAdvice: String,
        labOrders: String,
        followUpText: String,
        followUpDate: Long,
        onSuccess: (PrescriptionEntity) -> Unit
    ) -> Unit
) {
    val scrollState = rememberScrollState()

    // ICD-10 catalog
    val icd10Catalog = listOf(
        "J06.9 - Acute Upper Respiratory Infection",
        "J45.9 - Asthma, unspecified",
        "I10 - Essential (Primary) Hypertension",
        "E11.9 - Type 2 Diabetes Mellitus without complications",
        "K21.9 - Gastro-esophageal Reflux Disease (GERD)",
        "A09 - Infectious Gastroenteritis and Colitis",
        "R50.9 - Fever, unspecified",
        "M54.5 - Low Back Pain",
        "N39.0 - Urinary Tract Infection, site not specified"
    )

    var selectedIcd10 by remember { mutableStateOf(icd10Catalog[0]) }
    var showIcd10Dropdown by remember { mutableStateOf(false) }

    // Vitals
    var vitalsBp by remember { mutableStateOf("120/80 mmHg") }
    var vitalsPulse by remember { mutableStateOf("76 bpm") }
    var vitalsSpO2 by remember { mutableStateOf("99%") }
    var vitalsTemp by remember { mutableStateOf("98.6°F") }

    // Medication items
    val medicationList = remember {
        mutableStateListOf(
            MedicationItem(
                id = UUID.randomUUID().toString(),
                name = "Amoxicillin + Clavulanate 625mg",
                dosageForm = "Tab",
                dosageSchedule = "1-0-1",
                timing = "After Food",
                durationDays = 5,
                totalQuantity = 10,
                instructions = "Complete course"
            ),
            MedicationItem(
                id = UUID.randomUUID().toString(),
                name = "Paracetamol 650mg",
                dosageForm = "Tab",
                dosageSchedule = "SOS",
                timing = "After Food",
                durationDays = 3,
                totalQuantity = 6,
                instructions = "If temp > 100°F"
            )
        )
    }

    // New medication builder inputs
    var medNameQuery by remember { mutableStateOf("") }
    var selectedForm by remember { mutableStateOf("Tab") }
    var selectedSchedule by remember { mutableStateOf("1-0-1") } // 1-0-1, OD, BD, TDS, SOS
    var selectedTiming by remember { mutableStateOf("After Food") }
    var durationDays by remember { mutableStateOf(5) }

    // Auto-calculate quantity
    val autoQuantity = remember(selectedSchedule, durationDays) {
        val dailyDoses = when (selectedSchedule) {
            "1-0-1" -> 2
            "OD" -> 1
            "BD" -> 2
            "TDS" -> 3
            "SOS" -> 2
            else -> 2
        }
        dailyDoses * durationDays
    }

    // Common Drug Catalog suggestions
    val commonDrugs = listOf(
        "Azithromycin 500mg" to "Tab",
        "Cefixime 200mg" to "Tab",
        "Pantoprazole 40mg" to "Tab",
        "Levocetirizine 5mg" to "Tab",
        "Ibuprofen 400mg" to "Tab",
        "Salbutamol Inhaler" to "Inhaler",
        "Cough Expectorant Syrup" to "Syrup",
        "Normal Saline Nasal Drops" to "Drops"
    )

    // Clinical Advice tags
    val adviceOptions = listOf(
        "Adequate Hydration (3L/day)",
        "Steam Inhalation 2x/day",
        "Warm Saline Gargles",
        "Avoid Cold & Oily Food",
        "Salt & Sugar Restriction",
        "Bed Rest for 48 Hours"
    )
    val selectedAdvice = remember { mutableStateListOf("Adequate Hydration (3L/day)", "Steam Inhalation 2x/day") }

    // Lab Orders tags
    val labOptions = listOf(
        "CBC (Complete Blood Count)",
        "Chest X-Ray (PA View)",
        "HbA1c / Fasting Blood Sugar",
        "Serum Creatinine & Electrolytes",
        "Urine Routine & Microscopy",
        "Lipid Profile"
    )
    val selectedLabs = remember { mutableStateListOf<String>() }

    // Follow-up
    var followUpDays by remember { mutableStateOf(7) }
    var followUpText by remember { mutableStateOf("Review after 7 days or SOS if symptoms worsen") }

    var isPrescriptionSubmitted by remember { mutableStateOf(false) }
    var isConsultationClosed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Clinical Rapid Rx Studio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MedTealDark
                )
                Text(
                    text = "Zero-latency, 1-click clinical prescription writer",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Surface(
                color = MedTealContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rapid Flow Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedOnTealContainer
                    )
                }
            }
        }

        // Open/Close Consultation Indicator with Green/Red status
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = if (isConsultationClosed) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isConsultationClosed) "CONSULTATION CLOSED (🔴 RED)" else "CONSULTATION OPEN (🟢 GREEN)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isConsultationClosed) Color(0xFF991B1B) else Color(0xFF166534)
                        )
                        Text(
                            text = if (isConsultationClosed)
                                "Prescription written & dispatched. Session marked concluded."
                            else
                                "Doctor is currently in session & prescribing medications.",
                            fontSize = 10.sp,
                            color = if (isConsultationClosed) Color(0xFFB91C1C) else Color(0xFF15803D)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { isConsultationClosed = !isConsultationClosed },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isConsultationClosed) Color(0xFFDC2626) else Color(0xFF16A34A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("toggle_consultation_status_button")
                ) {
                    Text(
                        text = if (isConsultationClosed) "Reopen (🟢)" else "Close (🔴)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. PATIENT CONTEXT BAR
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MedCyanContainer,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = patientName.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = MedOnCyanContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = patientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "${patientProfile.age} Yrs • ${patientProfile.gender} • ${patientProfile.weightKg} kg • Blood Group: ${patientProfile.bloodGroup}",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                // Allergies Alert Pill
                if (patientProfile.allergies.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MedCoralContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MedCoralTertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CRITICAL ALLERGIES: ${patientProfile.allergies}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedCoralTertiary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. DIAGNOSIS (ICD-10) & VITALS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Provisional Diagnosis (ICD-10 Standard)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MedTealDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedIcd10,
                        onValueChange = { selectedIcd10 = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("icd10_input"),
                        trailingIcon = {
                            IconButton(onClick = { showIcd10Dropdown = !showIcd10Dropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        },
                        label = { Text("Search / Pick ICD-10 Code") }
                    )

                    DropdownMenu(
                        expanded = showIcd10Dropdown,
                        onDismissRequest = { showIcd10Dropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        icd10Catalog.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, fontSize = 13.sp) },
                                onClick = {
                                    selectedIcd10 = item
                                    showIcd10Dropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Clinical Vitals",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MedTealDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = vitalsBp,
                        onValueChange = { vitalsBp = it },
                        label = { Text("BP", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vitals_bp_input")
                    )
                    OutlinedTextField(
                        value = vitalsPulse,
                        onValueChange = { vitalsPulse = it },
                        label = { Text("Pulse", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vitals_pulse_input")
                    )
                    OutlinedTextField(
                        value = vitalsSpO2,
                        onValueChange = { vitalsSpO2 = it },
                        label = { Text("SpO2", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vitals_spo2_input")
                    )
                    OutlinedTextField(
                        value = vitalsTemp,
                        onValueChange = { vitalsTemp = it },
                        label = { Text("Temp", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vitals_temp_input")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. SMART MEDICATION SELECTOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Smart Medication Builder (With Dose Shortcuts)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MedTealDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quick suggestions chips
                Text(text = "Quick Select:", fontSize = 11.sp, color = Color.Gray)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(commonDrugs) { (name, form) ->
                        SuggestionChip(
                            onClick = {
                                medNameQuery = name
                                selectedForm = form
                            },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Medicine Name Input
                OutlinedTextField(
                    value = medNameQuery,
                    onValueChange = { medNameQuery = it },
                    label = { Text("Medicine Brand / Generic Name") },
                    placeholder = { Text("e.g. Azithromycin 500mg") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_name_input"),
                    leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null, tint = MedTealPrimary) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dosage Form Selector
                Text(text = "Dosage Form:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Tab", "Syrup", "Cap", "Inj", "Drops", "Inhaler").forEach { form ->
                        FilterChip(
                            selected = selectedForm == form,
                            onClick = { selectedForm = form },
                            label = { Text(form, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dosage Schedule Shortcuts: 1-0-1, OD, BD, TDS, SOS
                Text(text = "Dosage Schedule Shortcut:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1-0-1", "OD", "BD", "TDS", "SOS").forEach { sched ->
                        FilterChip(
                            selected = selectedSchedule == sched,
                            onClick = { selectedSchedule = sched },
                            label = { Text(sched, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Timing & Duration Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timing (Before / After food)
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(text = "Meal Timing:", fontSize = 11.sp, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = selectedTiming == "After Food",
                                onClick = { selectedTiming = "After Food" },
                                label = { Text("After Food", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = selectedTiming == "Before Food",
                                onClick = { selectedTiming = "Before Food" },
                                label = { Text("Before Food", fontSize = 10.sp) }
                            )
                        }
                    }

                    // Duration Counter
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Duration: $durationDays Days", fontSize = 11.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (durationDays > 1) durationDays-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                            }
                            Text(
                                text = "$durationDays",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { durationDays++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase")
                            }
                        }
                    }

                    // Calculated Total Quantity
                    Surface(
                        color = MedTealContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Qty", fontSize = 10.sp, color = MedOnTealContainer)
                            Text(text = "$autoQuantity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MedTealPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Medication Button
                Button(
                    onClick = {
                        if (medNameQuery.isNotBlank()) {
                            medicationList.add(
                                MedicationItem(
                                    id = UUID.randomUUID().toString(),
                                    name = medNameQuery.trim(),
                                    dosageForm = selectedForm,
                                    dosageSchedule = selectedSchedule,
                                    timing = selectedTiming,
                                    durationDays = durationDays,
                                    totalQuantity = autoQuantity,
                                    instructions = if (selectedSchedule == "SOS") "As needed" else "Standard course"
                                )
                            )
                            medNameQuery = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_medication_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add To Prescription Pad")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prescribed List Table Preview
                Text(
                    text = "Current Prescription Items (${medicationList.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MedTealDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                medicationList.forEachIndexed { index, med ->
                    Surface(
                        color = MedSurfaceVariantLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}. ${med.name} (${med.dosageForm})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Dose: ${med.dosageSchedule} • ${med.timing} • ${med.durationDays} Days • Total: ${med.totalQuantity} units",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                            IconButton(
                                onClick = { medicationList.removeAt(index) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MedCoralTertiary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. CLINICAL ADVICE & LAB ORDERS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Clinical Advice Checklist:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MedTealDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    adviceOptions.forEach { adv ->
                        val isSelected = selectedAdvice.contains(adv)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedAdvice.remove(adv) else selectedAdvice.add(adv)
                            },
                            label = { Text(adv, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Diagnostic & Lab Investigations:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MedTealDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    labOptions.forEach { lab ->
                        val isSelected = selectedLabs.contains(lab)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedLabs.remove(lab) else selectedLabs.add(lab)
                            },
                            label = { Text(lab, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. FOLLOW-UP DATE PICKER
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Scheduled Follow-Up Review:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MedTealDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3 to "+3 Days", 7 to "+1 Week", 14 to "+2 Weeks").forEach { (days, label) ->
                        FilterChip(
                            selected = followUpDays == days,
                            onClick = {
                                followUpDays = days
                                followUpText = "Review in $days days or SOS if needed"
                            },
                            label = { Text(label, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = followUpText,
                    onValueChange = { followUpText = it },
                    label = { Text("Follow-up Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. SUBMIT BUTTON & POST-PRESCRIPTION CONSULTATION STATUS
        Button(
            onClick = {
                val followUpTimestamp = System.currentTimeMillis() + (followUpDays.toLong() * 24 * 3600 * 1000)
                val adviceStr = selectedAdvice.joinToString(", ")
                val labStr = selectedLabs.joinToString(", ")

                onSubmitPrescription(
                    appointmentId,
                    selectedIcd10,
                    vitalsBp,
                    vitalsPulse,
                    vitalsSpO2,
                    vitalsTemp,
                    medicationList.toList(),
                    adviceStr,
                    labStr,
                    followUpText,
                    followUpTimestamp
                ) { rx ->
                    isPrescriptionSubmitted = true
                    isConsultationClosed = true // Consultation automatically closes upon prescription submission!
                    onPrescriptionCreated(rx)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("sign_dispatch_rx_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Digitally Sign & Dispatch Prescription",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isPrescriptionSubmitted) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_rx_consultation_status_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConsultationClosed) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isConsultationClosed)
                                        "CONSULTATION CLOSED 🔴"
                                    else
                                        "CONSULTATION OPEN 🟢",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isConsultationClosed) Color(0xFF991B1B) else Color(0xFF166534)
                                )
                                Text(
                                    text = if (isConsultationClosed)
                                        "Prescription generated. Teleconsultation session is formally closed."
                                    else
                                        "Consultation active. Reopened for patient follow-up discussions.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Button(
                            onClick = { isConsultationClosed = !isConsultationClosed },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConsultationClosed) Color(0xFF16A34A) else Color(0xFFDC2626)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("post_rx_toggle_consultation_button")
                        ) {
                            Text(
                                text = if (isConsultationClosed) "Reopen (🟢)" else "Close (🔴)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
