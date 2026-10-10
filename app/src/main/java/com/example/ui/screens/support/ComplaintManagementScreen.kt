package com.example.ui.screens.support

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ComplaintChatMessageEntity
import com.example.data.model.ComplaintEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintManagementScreen(
    currentRmUser: UserEntity,
    complaints: List<ComplaintEntity>,
    selectedComplaint: ComplaintEntity?,
    chatMessages: List<ComplaintChatMessageEntity>,
    onSelectComplaint: (ComplaintEntity?) -> Unit,
    onSendMessage: (complaintId: String, text: String) -> Unit,
    onLogCallAssistance: (complaintId: String, durationSec: Int, notes: String) -> Unit,
    onUpdateStatus: (complaintId: String, status: String, resolution: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilterTab by remember { mutableStateOf(0) } // 0: All, 1: Doctors, 2: Patients, 3: Open, 4: In Assistance, 5: Resolved
    var searchQuery by remember { mutableStateOf("") }

    // Live Call Assistance Dialog State
    var showCallAssistanceDialog by remember { mutableStateOf(false) }
    var callTimerSeconds by remember { mutableStateOf(0) }
    var isCallActive by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var callNotesInput by remember { mutableStateOf("") }

    // Chat Message Input
    var chatInputText by remember { mutableStateOf("") }

    // Status & Resolution Dialog State
    var showStatusUpdateDialog by remember { mutableStateOf(false) }
    var targetStatus by remember { mutableStateOf("RESOLVED") }
    var resolutionNotesInput by remember { mutableStateOf("") }

    // Call timer tick
    LaunchedEffect(isCallActive) {
        if (isCallActive) {
            while (true) {
                delay(1000)
                callTimerSeconds += 1
            }
        }
    }

    val filteredComplaints = remember(complaints, selectedFilterTab, searchQuery) {
        complaints.filter { comp ->
            val matchRole = when (selectedFilterTab) {
                1 -> comp.raisedByRole == UserRole.DOCTOR
                2 -> comp.raisedByRole == UserRole.PATIENT
                3 -> comp.status == "OPEN"
                4 -> comp.status == "IN_ASSISTANCE"
                5 -> comp.status == "RESOLVED"
                else -> true
            }
            val matchQuery = searchQuery.isBlank() ||
                    comp.ticketNumber.contains(searchQuery, ignoreCase = true) ||
                    comp.raisedByName.contains(searchQuery, ignoreCase = true) ||
                    comp.subject.contains(searchQuery, ignoreCase = true) ||
                    comp.category.contains(searchQuery, ignoreCase = true)
            matchRole && matchQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Relationship Manager Portal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MedSuccessContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "LIVE ASSISTANCE",
                                    color = MedSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "RM: ${currentRmUser.fullName} (${currentRmUser.email})",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedComplaint != null) {
                            onSelectComplaint(null)
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit Portal", tint = MedCoralTertiary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MedTealDark
                )
            )
        }
    ) { paddingValues ->
        if (selectedComplaint == null) {
            // LIST VIEW: All Complaints with Filters and KPIs
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MedBackgroundLight)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // RM KPI Overview Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiStatCard(
                        title = "Total Tickets",
                        value = "${complaints.size}",
                        icon = Icons.Default.ConfirmationNumber,
                        containerColor = Color.White,
                        iconTint = MedTealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "In Assistance",
                        value = "${complaints.count { it.status == "IN_ASSISTANCE" }}",
                        icon = Icons.Default.SupportAgent,
                        containerColor = Color(0xFFEFF6FF),
                        iconTint = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Open / New",
                        value = "${complaints.count { it.status == "OPEN" }}",
                        icon = Icons.Default.WarningAmber,
                        containerColor = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Resolved",
                        value = "${complaints.count { it.status == "RESOLVED" }}",
                        icon = Icons.Default.CheckCircle,
                        containerColor = Color(0xFFDCFCE7),
                        iconTint = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by ticket #, doctor, patient, or subject", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("complaint_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MedTealPrimary,
                        unfocusedBorderColor = MedOutlineLight
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs (Scrollable)
                ScrollableTabRow(
                    selectedTabIndex = selectedFilterTab,
                    containerColor = Color.Transparent,
                    edgePadding = 0.dp,
                    divider = {},
                    indicator = {}
                ) {
                    val tabs = listOf("All (${complaints.size})", "Doctors", "Patients", "Open", "In Assistance", "Resolved")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedFilterTab == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterTab = index },
                            label = { Text(title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.padding(end = 6.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedTealPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color.DarkGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredComplaints.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No complaints found matching this filter", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredComplaints, key = { it.id }) { item ->
                            ComplaintCard(
                                complaint = item,
                                onClick = { onSelectComplaint(item) }
                            )
                        }
                    }
                }
            }
        } else {
            // DETAIL VIEW: Two-Way Chat Assistance & Live Call Assistance Workspace
            ComplaintDetailWorkspace(
                complaint = selectedComplaint,
                chatMessages = chatMessages,
                currentRmUser = currentRmUser,
                chatInputText = chatInputText,
                onChatInputChange = { chatInputText = it },
                onSendChat = {
                    if (chatInputText.isNotBlank()) {
                        onSendMessage(selectedComplaint.id, chatInputText)
                        chatInputText = ""
                    }
                },
                onInitiateCall = {
                    callTimerSeconds = 0
                    isCallActive = true
                    callNotesInput = ""
                    showCallAssistanceDialog = true
                },
                onOpenStatusDialog = {
                    targetStatus = selectedComplaint.status
                    resolutionNotesInput = selectedComplaint.resolutionSummary
                    showStatusUpdateDialog = true
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    // Live Call Assistance Dialog
    if (showCallAssistanceDialog && selectedComplaint != null) {
        Dialog(onDismissRequest = {
            if (!isCallActive) showCallAssistanceDialog = false
        }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = if (isCallActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PhoneInTalk,
                                contentDescription = null,
                                tint = if (isCallActive) Color(0xFF16A34A) else Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isCallActive) "Live Call Assistance Connected" else "Call Assistance Completed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isCallActive) Color(0xFF16A34A) else Color.Black
                    )

                    Text(
                        text = "${selectedComplaint.raisedByName} (${selectedComplaint.raisedByPhone})",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val minutes = callTimerSeconds / 60
                    val seconds = callTimerSeconds % 60
                    val timerStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "⏱ Call Duration: $timerStr",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MedTealDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isCallActive) {
                        // In-Call Controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { isMuted = !isMuted },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (isMuted) MedCoralContainer else Color(0xFFF1F5F9)
                                )
                            ) {
                                Icon(
                                    if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute",
                                    tint = if (isMuted) MedCoralTertiary else Color.DarkGray
                                )
                            }

                            IconButton(
                                onClick = { isSpeakerOn = !isSpeakerOn },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (isSpeakerOn) MedTealContainer else Color(0xFFF1F5F9)
                                )
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Speaker",
                                    tint = if (isSpeakerOn) MedTealPrimary else Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isCallActive = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedCoralTertiary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("end_assistance_call_button")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Call & Record Assistance Notes")
                        }
                    } else {
                        // Call Ended: Enter Call Notes & Log
                        OutlinedTextField(
                            value = callNotesInput,
                            onValueChange = { callNotesInput = it },
                            label = { Text("Call Assistance Notes / Action Items", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Advised doctor to switch to 5GHz Wi-Fi, cleared cache.", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("call_notes_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCallAssistanceDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Discard")
                            }
                            Button(
                                onClick = {
                                    val notes = if (callNotesInput.isNotBlank()) callNotesInput else "Call assistance completed ($timerStr). Complainant briefed."
                                    onLogCallAssistance(selectedComplaint.id, callTimerSeconds, notes)
                                    showCallAssistanceDialog = false
                                    Toast.makeText(context, "Call assistance logged successfully", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_call_log_button")
                            ) {
                                Text("Save & Log Call")
                            }
                        }
                    }
                }
            }
        }
    }

    // Status Update Dialog
    if (showStatusUpdateDialog && selectedComplaint != null) {
        AlertDialog(
            onDismissRequest = { showStatusUpdateDialog = false },
            title = { Text("Update Ticket Status", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select current resolution state for #${selectedComplaint.ticketNumber}:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("OPEN", "IN_ASSISTANCE", "RESOLVED").forEach { statusOption ->
                            FilterChip(
                                selected = targetStatus == statusOption,
                                onClick = { targetStatus = statusOption },
                                label = { Text(statusOption, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resolutionNotesInput,
                        onValueChange = { resolutionNotesInput = it },
                        label = { Text("Resolution Summary / Conclusion") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(selectedComplaint.id, targetStatus, resolutionNotesInput)
                        showStatusUpdateDialog = false
                        Toast.makeText(context, "Ticket status updated to $targetStatus", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ComplaintDetailWorkspace(
    complaint: ComplaintEntity,
    chatMessages: List<ComplaintChatMessageEntity>,
    currentRmUser: UserEntity,
    chatInputText: String,
    onChatInputChange: (String) -> Unit,
    onSendChat: () -> Unit,
    onInitiateCall: () -> Unit,
    onOpenStatusDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Complainant Header & Actions Banner
        Surface(
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = complaint.ticketNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MedTealDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (complaint.raisedByRole == UserRole.DOCTOR) MedTealContainer else Color(0xFFF3E8FF),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (complaint.raisedByRole == UserRole.DOCTOR) "🩺 DOCTOR" else "👤 PATIENT",
                                    color = if (complaint.raisedByRole == UserRole.DOCTOR) MedTealPrimary else Color(0xFF7C3AED),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(status = complaint.status)
                        }
                        Text(
                            text = "${complaint.raisedByName} • ${complaint.raisedByPhone}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }

                    // Action Buttons: Call Assistance & Update Status
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onInitiateCall,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("initiate_call_assistance_button")
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Assist", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenStatusDialog,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("update_ticket_status_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Status", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Complaint Category & Subject Box
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedOutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = MedWarning, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Category: ${complaint.category}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedTealDark
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Priority: ${complaint.priority}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (complaint.priority == "CRITICAL" || complaint.priority == "HIGH") MedCoralTertiary else Color.DarkGray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = complaint.subject,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = complaint.description,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                if (complaint.callAssistanceCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Call Assistance Logged (${complaint.callAssistanceCount} calls). Last: ${complaint.lastCallNotes.take(60)}...",
                            fontSize = 11.sp,
                            color = Color(0xFF15803D),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Two-Way Live Chat Assistance Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Live Chat Assistance with Relationship Manager",
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(chatMessages, key = { it.id }) { msg ->
                val isFromRm = msg.senderRole == UserRole.RELATIONSHIP_MANAGER
                val isCallNote = msg.messageText.startsWith("📞 Call Assistance")

                if (isCallNote) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg.messageText,
                                fontSize = 11.sp,
                                color = Color(0xFF14532D),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isFromRm) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (isFromRm) MedTealPrimary else Color.White,
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isFromRm) 12.dp else 2.dp,
                                bottomEnd = if (isFromRm) 2.dp else 12.dp
                            ),
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = msg.senderName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFromRm) Color.White.copy(alpha = 0.8f) else MedTealDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.messageText,
                                    fontSize = 12.sp,
                                    color = if (isFromRm) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                Text(
                                    text = timeFormat.format(Date(msg.timestamp)),
                                    fontSize = 9.sp,
                                    color = if (isFromRm) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chat Input Box
        Surface(
            color = Color.White,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInputText,
                    onValueChange = onChatInputChange,
                    placeholder = { Text("Type RM assistance message / resolution...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rm_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSendChat,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MedTealPrimary),
                    modifier = Modifier.testTag("rm_chat_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ComplaintCard(
    complaint: ComplaintEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("complaint_card_${complaint.ticketNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = complaint.ticketNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MedTealDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (complaint.raisedByRole == UserRole.DOCTOR) MedTealContainer else Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (complaint.raisedByRole == UserRole.DOCTOR) "DOCTOR" else "PATIENT",
                            color = if (complaint.raisedByRole == UserRole.DOCTOR) MedTealPrimary else Color(0xFF7C3AED),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                StatusBadge(status = complaint.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = complaint.subject,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.Black
            )

            Text(
                text = complaint.description,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By: ${complaint.raisedByName}",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                Text(
                    text = sdf.format(Date(complaint.createdAt)),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "OPEN" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "OPEN")
        "IN_ASSISTANCE" -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), "IN ASSISTANCE")
        "RESOLVED" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "RESOLVED")
        else -> Triple(Color(0xFFF1F5F9), Color.DarkGray, status)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun KpiStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MedOutlineLight),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Text(title, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
        }
    }
}
