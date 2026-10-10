package com.example.ui.screens.prescription

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PrescriptionEntity
import com.example.ui.DateFilterOption
import com.example.ui.PrescriptionViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen displaying the clinical prescription registry observed from [PrescriptionViewModel].
 * Includes multi-criteria filtering by Patient, Date Range, Specific Date, and Search Query.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionListScreen(
    viewModel: PrescriptionViewModel,
    onBack: () -> Unit = {},
    onPrescriptionSelected: (PrescriptionEntity) -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val uiState by viewModel.uiState.collectAsState()
    val filteredPrescriptions by viewModel.filteredPrescriptions.collectAsState()
    val allPrescriptions by viewModel.allPrescriptions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showDetailDialog by remember { mutableStateOf<PrescriptionEntity?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showPatientPickerDialog by remember { mutableStateOf(false) }
    var patientSearchInDialog by remember { mutableStateOf("") }

    // Distinct patients extracted dynamically from stored prescriptions
    val availablePatients = remember(allPrescriptions) {
        allPrescriptions.map { it.patientName to it.patientId }
            .distinctBy { it.second }
    }

    val isAnyFilterActive = searchQuery.isNotBlank() ||
            uiState.filterPatientId != null ||
            uiState.selectedDateFilter != DateFilterOption.ALL

    val calendarDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        modifier = Modifier.testTag("prescription_list_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Prescription Registry",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = "${filteredPrescriptions.size} of ${allPrescriptions.size} clinical records",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedOnSurfaceVariantLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("prescription_list_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MedTealPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPatientPickerDialog = true },
                        modifier = Modifier.testTag("open_patient_filter_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = "Select Patient",
                            tint = if (uiState.filterPatientId != null) MedTealPrimary else MedOnSurfaceVariantLight
                        )
                    }
                    IconButton(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier.testTag("open_date_picker_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pick Date",
                            tint = if (uiState.selectedDateFilter == DateFilterOption.SPECIFIC_DATE) MedTealPrimary else MedOnSurfaceVariantLight
                        )
                    }
                    if (isAnyFilterActive) {
                        TextButton(
                            onClick = { viewModel.clearFilters() },
                            modifier = Modifier.testTag("clear_all_filters_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAltOff,
                                contentDescription = "Clear Filters",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MedBackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("prescription_search_input"),
                placeholder = {
                    Text("Search patient, ICD-10, medicine, or doctor...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MedTealPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MedTealPrimary,
                    unfocusedBorderColor = MedOutlineLight,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Active Filters Summary Strip (Shown when any filter is active)
            AnimatedVisibility(visible = isAnyFilterActive) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Text(
                            text = "Active Filters:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedOnSurfaceVariantLight
                        )
                    }

                    if (uiState.filterPatientId != null) {
                        item {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.filterByPatient(null) },
                                label = {
                                    Text(
                                        text = "Patient: ${uiState.filterPatientName ?: uiState.filterPatientId}",
                                        fontSize = 11.sp
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove patient filter",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MedTealContainer,
                                    selectedLabelColor = MedOnTealContainer
                                ),
                                modifier = Modifier.testTag("active_filter_patient")
                            )
                        }
                    }

                    if (uiState.selectedDateFilter != DateFilterOption.ALL) {
                        item {
                            val dateLabel = if (uiState.selectedDateFilter == DateFilterOption.SPECIFIC_DATE && uiState.selectedDateEpochMillis != null) {
                                calendarDateFormatter.format(Date(uiState.selectedDateEpochMillis!!))
                            } else {
                                uiState.selectedDateFilter.label
                            }
                            InputChip(
                                selected = true,
                                onClick = { viewModel.filterByDate(DateFilterOption.ALL) },
                                label = {
                                    Text(
                                        text = "Date: $dateLabel",
                                        fontSize = 11.sp
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove date filter",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MedCyanContainer,
                                    selectedLabelColor = MedOnCyanContainer
                                ),
                                modifier = Modifier.testTag("active_filter_date")
                            )
                        }
                    }

                    if (searchQuery.isNotBlank()) {
                        item {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setSearchQuery("") },
                                label = {
                                    Text(
                                        text = "Search: \"$searchQuery\"",
                                        fontSize = 11.sp
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove search query",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MedCoralContainer,
                                    selectedLabelColor = MedCoralTertiary
                                ),
                                modifier = Modifier.testTag("active_filter_search")
                            )
                        }
                    }
                }
            }

            // Date Range Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MedTealPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filter by Date",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedOnSurfaceVariantLight
                        )
                    }

                    TextButton(
                        onClick = { showDatePickerDialog = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.testTag("pick_custom_date_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MedTealPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calendar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedTealPrimary)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DateFilterOption.entries) { option ->
                        val isSelected = uiState.selectedDateFilter == option
                        val labelText = if (option == DateFilterOption.SPECIFIC_DATE && uiState.selectedDateEpochMillis != null) {
                            calendarDateFormatter.format(Date(uiState.selectedDateEpochMillis!!))
                        } else {
                            option.label
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (option == DateFilterOption.SPECIFIC_DATE) {
                                    showDatePickerDialog = true
                                } else {
                                    viewModel.filterByDate(option)
                                }
                            },
                            label = { Text(labelText, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedTealContainer,
                                selectedLabelColor = MedOnTealContainer,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("filter_date_${option.name.lowercase()}")
                        )
                    }
                }
            }

            // Patient Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MedTealPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filter by Patient",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedOnSurfaceVariantLight
                        )
                    }

                    if (availablePatients.size > 3) {
                        TextButton(
                            onClick = { showPatientPickerDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.testTag("all_patients_directory_btn")
                        ) {
                            Text("All Patients (${availablePatients.size})", fontSize = 11.sp, color = MedTealPrimary)
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAllSelected = uiState.filterPatientId == null
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { viewModel.filterByPatient(null) },
                            label = { Text("All Patients", fontSize = 12.sp) },
                            leadingIcon = if (isAllSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedTealContainer,
                                selectedLabelColor = MedOnTealContainer,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("filter_patient_all")
                        )
                    }

                    items(availablePatients) { (patientName, patientId) ->
                        val isSelected = uiState.filterPatientId == patientId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) viewModel.filterByPatient(null)
                                else viewModel.filterByPatient(patientId, patientName)
                            },
                            label = { Text(patientName, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedTealContainer,
                                selectedLabelColor = MedOnTealContainer,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("filter_patient_chip_$patientId")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Prescriptions List
            if (filteredPrescriptions.isEmpty()) {
                EmptyPrescriptionsView(
                    isFiltered = isAnyFilterActive,
                    onResetFilters = { viewModel.clearFilters() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("prescription_list_lazy_column")
                ) {
                    items(
                        items = filteredPrescriptions,
                        key = { it.id }
                    ) { rx ->
                        PrescriptionCard(
                            prescription = rx,
                            onClick = {
                                viewModel.selectPrescription(rx)
                                onPrescriptionSelected(rx)
                            },
                            onVerifyClick = {
                                viewModel.verifySignature(rx)
                            },
                            onQuickView = {
                                showDetailDialog = rx
                            }
                        )
                    }
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDateEpochMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            viewModel.filterBySpecificDate(selectedMillis)
                        }
                        showDatePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                    modifier = Modifier.testTag("confirm_date_picker_btn")
                ) {
                    Text("Apply Filter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select Prescription Date",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }

    // Patient Directory Selector Modal
    if (showPatientPickerDialog) {
        AlertDialog(
            onDismissRequest = {
                showPatientPickerDialog = false
                patientSearchInDialog = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = MedTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filter by Patient", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    OutlinedTextField(
                        value = patientSearchInDialog,
                        onValueChange = { patientSearchInDialog = it },
                        placeholder = { Text("Search patient name or ID...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("patient_dialog_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    val filteredDialogPatients = remember(availablePatients, patientSearchInDialog) {
                        if (patientSearchInDialog.isBlank()) availablePatients
                        else availablePatients.filter {
                            it.first.contains(patientSearchInDialog, ignoreCase = true) ||
                                    it.second.contains(patientSearchInDialog, ignoreCase = true)
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.filterByPatient(null)
                                        showPatientPickerDialog = false
                                        patientSearchInDialog = ""
                                    }
                                    .testTag("dialog_select_all_patients"),
                                color = if (uiState.filterPatientId == null) MedTealContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Checklist, contentDescription = null, tint = MedTealPrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("All Patients (Clear filter)", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        items(filteredDialogPatients) { (patientName, patientId) ->
                            val isSelected = uiState.filterPatientId == patientId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.filterByPatient(patientId, patientName)
                                        showPatientPickerDialog = false
                                        patientSearchInDialog = ""
                                    }
                                    .testTag("dialog_select_patient_$patientId"),
                                color = if (isSelected) MedTealContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(patientName, fontWeight = FontWeight.Bold)
                                        Text("ID: $patientId", fontSize = 11.sp, color = MedOnSurfaceVariantLight)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MedTealPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPatientPickerDialog = false
                        patientSearchInDialog = ""
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Prescription Quick Detail Modal
    if (showDetailDialog != null) {
        val rx = showDetailDialog!!
        AlertDialog(
            onDismissRequest = { showDetailDialog = null },
            confirmButton = {
                Button(
                    onClick = {
                        val current = showDetailDialog
                        showDetailDialog = null
                        if (current != null) {
                            onPrescriptionSelected(current)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
                ) {
                    Text("Open Full Rx Document")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDetailDialog = null }) {
                    Text("Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MedTealPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rx: ${rx.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Patient: ${rx.patientName} (${rx.patientAge} yrs • ${rx.patientGender})", fontWeight = FontWeight.SemiBold)
                    Text("Doctor: ${rx.doctorName} • ${rx.doctorSpeciality}", color = MedOnSurfaceVariantLight, fontSize = 13.sp)
                    Text("Diagnosis: ${rx.diagnosisICD10}", color = MedTealDark, fontWeight = FontWeight.Medium)
                    HorizontalDivider()
                    Text("Vitals: BP ${rx.vitalsBp} • Pulse ${rx.vitalsPulse} • SpO2 ${rx.vitalsSpO2} • Temp ${rx.vitalsTemp}", fontSize = 12.sp)
                    Text("Follow-up: ${rx.followUpText}", fontSize = 12.sp, color = MedCyanSecondary)
                    Surface(
                        color = MedTealContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SHA-256 Validated: ${rx.doctorSignatureHash.take(16)}...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedOnTealContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        )
    }
}

/**
 * Individual Card rendering a single clinical prescription.
 */
@Composable
fun PrescriptionCard(
    prescription: PrescriptionEntity,
    onClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onQuickView: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(prescription.createdAt) {
        dateFormat.format(Date(prescription.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("prescription_card_${prescription.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MedOutlineLight.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Rx ID and Creation Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MedTealContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = prescription.id,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedOnTealContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MedOnSurfaceVariantLight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Patient Name and Demographic Chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MedCyanContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = prescription.patientName.firstOrNull()?.uppercase() ?: "P",
                        fontWeight = FontWeight.Bold,
                        color = MedOnCyanContainer,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prescription.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedOnSurfaceLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${prescription.patientAge} yrs • ${prescription.patientGender} • ${prescription.patientWeightKg} kg",
                        fontSize = 12.sp,
                        color = MedOnSurfaceVariantLight
                    )
                }

                if (prescription.patientAllergies.isNotBlank() && !prescription.patientAllergies.contains("none", ignoreCase = true)) {
                    Surface(
                        color = MedCoralContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Allergies",
                            color = MedCoralTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Diagnosis
            Surface(
                color = MedBackgroundLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MedTealPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prescription.diagnosisICD10,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MedTealDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Doctor Info & Vitals Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Doctor: ${prescription.doctorName}",
                    fontSize = 12.sp,
                    color = MedOnSurfaceVariantLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "BP: ${prescription.vitalsBp}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MedOnSurfaceLight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MedOutlineLight.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MedSuccessContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MedSuccess
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SHA-256 Stamped",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedSuccess
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onQuickView,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Summary", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("view_prescription_${prescription.id}")
                    ) {
                        Text("View Rx", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Friendly empty state for the Prescription List Screen.
 */
@Composable
fun EmptyPrescriptionsView(
    isFiltered: Boolean,
    onResetFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MedTealContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MedTealPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltered) "No Prescriptions Found" else "No Prescriptions in Registry",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MedOnSurfaceLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFiltered)
                "No prescriptions match your current patient, date, or search filter. Try clearing your filters."
            else
                "No clinical prescriptions have been generated yet. Create a prescription during a teleconsultation.",
            style = MaterialTheme.typography.bodyMedium,
            color = MedOnSurfaceVariantLight,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (isFiltered) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onResetFilters,
                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAltOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear All Filters")
            }
        }
    }
}
