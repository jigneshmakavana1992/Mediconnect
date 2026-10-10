package com.example.ui.screens.support

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsHelpdeskScreen(
    currentUser: UserEntity,
    helplineNumber: String,
    complaints: List<ComplaintEntity>,
    selectedComplaint: ComplaintEntity?,
    chatMessages: List<ComplaintChatMessageEntity>,
    onSelectComplaint: (ComplaintEntity?) -> Unit,
    onSubmitComplaint: (category: String, subject: String, description: String, priority: String, onDone: () -> Unit) -> Unit,
    onSendMessage: (complaintId: String, text: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showNewComplaintDialog by remember { mutableStateOf(false) }

    // New Complaint Form State
    var category by remember { mutableStateOf("Audio/Video Call Quality") }
    var priority by remember { mutableStateOf("NORMAL") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Chat input in active complaint
    var chatInput by remember { mutableStateOf("") }

    val userComplaints = remember(complaints, currentUser.id) {
        complaints.filter { it.raisedById == currentUser.id }
    }

    val categories = if (currentUser.role == UserRole.DOCTOR) {
        listOf(
            "Audio/Video Call Quality",
            "Prescription & EMR Sync",
            "Weekly Payment Settlement",
            "Patient Behavior & No-Show",
            "App Technical Glitch",
            "Regulatory / Council Verification",
            "General Assistance"
        )
    } else {
        listOf(
            "Teleconsultation Call Quality",
            "Prescription & Dosage Inquiry",
            "Billing / UPI Payment Issue",
            "Doctor Availability & Scheduling",
            "App Technical Glitch",
            "Medical Records / Vault Sync",
            "General Assistance"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentUser.role == UserRole.DOCTOR) "Doctor Helpdesk & Assistance" else "Patient Care & Helpdesk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Helpline & Complaint Redressal Portal",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MedTealDark
                )
            )
        },
        floatingActionButton = {
            if (selectedComplaint == null) {
                ExtendedFloatingActionButton(
                    onClick = { showNewComplaintDialog = true },
                    containerColor = MedTealPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                    text = { Text("Lodge Complaint", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("lodge_complaint_fab")
                )
            }
        }
    ) { paddingValues ->
        if (selectedComplaint == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MedBackgroundLight)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Official Helpline Card (Configured by Super Admin)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MedTealContainer,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.SupportAgent,
                                                contentDescription = null,
                                                tint = MedTealPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (currentUser.role == UserRole.DOCTOR) "Doctor Assistance Helpline" else "Patient Assistance Helpline",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MedTealDark
                                        )
                                        Text(
                                            text = "Official 24x7 Priority Line (Super Admin Configured)",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Direct Assistance Toll-Free",
                                            fontSize = 10.sp,
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = helplineNumber,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${helplineNumber.replace(" ", "")}")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Dialer unavailable: $helplineNumber", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("call_helpline_button")
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Call Helpline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Title: My Text Complaints
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Text Complaints & Inquiries",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MedTealDark
                        )
                        Text(
                            text = "${userComplaints.size} Lodged",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (userComplaints.isEmpty()) {
                    item {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No Active Complaints", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Have an issue with audio calls, prescriptions, payments, or records? Lodge a text complaint below to receive Chat & Call assistance from our Relationship Manager.",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedButton(
                                    onClick = { showNewComplaintDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lodge a Complaint")
                                }
                            }
                        }
                    }
                } else {
                    items(userComplaints, key = { it.id }) { complaint ->
                        Card(
                            onClick = { onSelectComplaint(complaint) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_complaint_card_${complaint.ticketNumber}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = complaint.ticketNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MedTealDark
                                    )
                                    StatusBadge(status = complaint.status)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = complaint.subject,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = complaint.description,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Assigned RM: ${complaint.assignedRmName}",
                                        fontSize = 10.sp,
                                        color = MedTealPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Chat, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Chat & Track", fontSize = 11.sp, color = MedTealPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // USER COMPLAINT DETAIL & CHAT THREAD WITH RELATIONSHIP MANAGER
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MedBackgroundLight)
                    .padding(paddingValues)
            ) {
                // Header Ticket Info
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
                            Text(
                                text = "Ticket: ${selectedComplaint.ticketNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MedTealDark
                            )
                            StatusBadge(status = selectedComplaint.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedComplaint.subject,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Text(
                            text = selectedComplaint.description,
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Relationship Manager: ${selectedComplaint.assignedRmName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MedTealDark
                                )
                            }
                        }

                        if (selectedComplaint.status == "RESOLVED" && selectedComplaint.resolutionSummary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Resolution: ${selectedComplaint.resolutionSummary}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF14532D)
                                    )
                                }
                            }
                        }
                    }
                }

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == currentUser.id
                        val isCallNote = msg.messageText.startsWith("📞 Call Assistance")

                        if (isCallNote) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg.messageText,
                                        fontSize = 11.sp,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    color = if (isMe) MedTealPrimary else Color.White,
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isMe) 12.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 12.dp
                                    ),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (isMe) "You" else msg.senderName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) Color.White.copy(alpha = 0.85f) else MedTealDark
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.messageText,
                                            fontSize = 12.sp,
                                            color = if (isMe) Color.White else Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                        Text(
                                            text = timeFormat.format(Date(msg.timestamp)),
                                            fontSize = 9.sp,
                                            color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
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
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Reply to Relationship Manager...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("user_complaint_chat_input"),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    onSendMessage(selectedComplaint.id, chatInput)
                                    chatInput = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MedTealPrimary),
                            modifier = Modifier.testTag("user_chat_send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Lodge New Text Complaint
    if (showNewComplaintDialog) {
        Dialog(onDismissRequest = { if (!isSubmitting) showNewComplaintDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lodge Text Complaint",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MedTealDark
                        )
                        IconButton(onClick = { showNewComplaintDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    var expandedCat by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { expandedCat = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false }
                        ) {
                            categories.forEach { catOption ->
                                DropdownMenuItem(
                                    text = { Text(catOption, fontSize = 12.sp) },
                                    onClick = {
                                        category = catOption
                                        expandedCat = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Priority", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("NORMAL", "HIGH", "CRITICAL").forEach { prio ->
                            FilterChip(
                                selected = priority == prio,
                                onClick = { priority = prio },
                                label = { Text(prio, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject Line", fontSize = 12.sp) },
                        placeholder = { Text("Brief title of the issue", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("complaint_subject_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Explanation", fontSize = 12.sp) },
                        placeholder = { Text("Describe what happened, error codes, timing, etc.", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("complaint_description_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showNewComplaintDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (subject.isBlank() || description.isBlank()) {
                                    Toast.makeText(context, "Please enter both subject and description", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSubmitting = true
                                onSubmitComplaint(category, subject, description, priority) {
                                    isSubmitting = false
                                    showNewComplaintDialog = false
                                    subject = ""
                                    description = ""
                                    Toast.makeText(context, "Complaint lodged. Relationship Manager assigned.", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("submit_complaint_button"),
                            enabled = !isSubmitting
                        ) {
                            Text("Submit Complaint")
                        }
                    }
                }
            }
        }
    }
}
