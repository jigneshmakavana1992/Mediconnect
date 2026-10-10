package com.example.ui.screens.doctor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.CallState
import com.example.ui.components.RapidRxStudio
import com.example.ui.theme.*

@Composable
fun TeleconsultationRoomScreen(
    callState: CallState,
    patientProfile: PatientProfileEntity?,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSplitScreenRx: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEndCall: () -> Unit,
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
    ) -> Unit,
    onPrescriptionCreated: (PrescriptionEntity) -> Unit,
    onSwitchCallMode: (Boolean) -> Unit = {},
    onToggleAudioRoute: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Video/Audio Feed, 1: Split-Screen Rx Pad, 2: SOAP Notes, 3: In-call Chat
    var isConsultationClosed by remember { mutableStateOf(false) }
    var isPrescriptionWritten by remember { mutableStateOf(false) }
    val apt = callState.activeAppointment

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Teleconsultation Top App Bar with Open / Close Consultation Indicator
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Consultation Open (Green) / Closed (Red) Dot Indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = apt?.patientName ?: "Consultation Room",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (callState.isAudioCallMode) MedCyanSecondary else MedTealPrimary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (callState.isAudioCallMode) "AUDIO CALL" else "VIDEO CALL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Room ${apt?.meetingRoomId ?: "749"} • Slot: ${apt?.timeSlot ?: "10:00 AM"} • Encrypted",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Interactive Open / Close Consultation Pill
                    Surface(
                        modifier = Modifier
                            .clickable { isConsultationClosed = !isConsultationClosed }
                            .testTag("consultation_status_indicator_chip"),
                        color = if (isConsultationClosed) Color(0xFF450A0A) else Color(0xFF064E3B),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isConsultationClosed) "CLOSED 🔴" else "OPEN 🟢",
                                color = if (isConsultationClosed) Color(0xFFFCA5A5) else Color(0xFF86EFAC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = Color(0xFF334155),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "LIVE 12:45",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Post Prescription Writing Status Bar with Green / Red Indicator
        AnimatedVisibility(visible = isPrescriptionWritten) {
            Surface(
                color = if (isConsultationClosed) Color(0xFF7F1D1D) else Color(0xFF14532D),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isConsultationClosed) Color(0xFFEF4444) else Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isConsultationClosed)
                                "Prescription Written • Consultation Status: CLOSED 🔴"
                            else
                                "Prescription Written • Consultation Status: REOPENED 🟢",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { isConsultationClosed = !isConsultationClosed },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isConsultationClosed) "Reopen Consultation (🟢)" else "Close Consultation (🔴)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Call Reminder / Notification Pill Banner
        Surface(
            color = Color(0xFF1E3A8A).copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call Alert: Active (Doctor can alert Patient via SMS/WhatsApp)",
                        color = Color(0xFFBFDBFE),
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "UPI: Paid",
                    color = MedSuccess,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Navigation Tabs in call
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (callState.isAudioCallMode) "Audio Call" else "HD Video", fontSize = 12.sp) },
                icon = {
                    Icon(
                        if (callState.isAudioCallMode) Icons.Default.Call else Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Split Rx Pad", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("SOAP Notes", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Live Chat", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // Active Workspace View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> {
                    if (callState.isAudioCallMode) {
                        // Dedicated Audio Call Viewport with Voice Frequency Visualizer
                        AudioConsultationViewport(
                            callState = callState,
                            patientName = apt?.patientName ?: "Alexander Wright",
                            doctorName = apt?.doctorName ?: "Dr. Sarah Jenkins",
                            onSwitchToVideo = { onSwitchCallMode(false) },
                            onToggleAudioRoute = onToggleAudioRoute
                        )
                    } else {
                        // Video Viewport with Telemetry Overlay
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // Main Patient Video Stream Container
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (callState.isCameraOff) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.VideocamOff,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Camera is off", color = Color.Gray, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedButton(
                                            onClick = { onSwitchCallMode(true) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Switch to Audio-Only Call", fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    // Simulated HD Video Feed Graphic
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            color = MedCyanContainer,
                                            shape = CircleShape,
                                            modifier = Modifier.size(96.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MedOnCyanContainer,
                                                    modifier = Modifier.size(60.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = apt?.patientName ?: "Patient Video Feed",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Audio-Video synchronized via WebRTC Mesh",
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                // Patient Vitals Telemetry HUD Overlay
                                Surface(
                                    color = Color(0xCC000000),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = MedCoralTertiary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("76 BPM", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("SpO2 99%", color = MedCyanSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Temp 98.6°F", color = MedWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Switch to Audio Call Quick Action
                                Surface(
                                    color = Color(0xCC1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .clickable { onSwitchCallMode(true) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, tint = MedCyanSecondary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Voice Mode", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Doctor Self-View PiP (Picture in Picture)
                                Surface(
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 140.dp)
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .border(1.dp, MedTealPrimary, RoundedCornerShape(10.dp))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                            Text("Dr. Jenkins (You)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Split-Screen Interactive Prescription Pad inside consultation
                    if (patientProfile != null) {
                        RapidRxStudio(
                            patientProfile = patientProfile,
                            patientName = apt?.patientName ?: "Patient",
                            appointmentId = apt?.id ?: "apt_current",
                            onPrescriptionCreated = { rx ->
                                isPrescriptionWritten = true
                                isConsultationClosed = true
                                onPrescriptionCreated(rx)
                            },
                            onSubmitPrescription = onSubmitPrescription
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading patient clinical profile...", color = Color.White)
                        }
                    }
                }

                2 -> {
                    // SOAP Clinical Notes View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MedBackgroundLight)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Clinical SOAP Notes (In-Session Documentation)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MedTealDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        var subjective by remember { mutableStateOf("Patient presents with dry cough, mild pyrexia for 2 days. Poor appetite.") }
                        var objective by remember { mutableStateOf("Chest clear, no wheezing, throat mildly congested, tympanic temp 98.6°F, SpO2 99%.") }
                        var assessment by remember { mutableStateOf("Acute viral upper respiratory tract infection.") }
                        var plan by remember { mutableStateOf("Symptomatic relief, hydration, paracetamol SOS, saline nasal drops.") }

                        OutlinedTextField(
                            value = subjective,
                            onValueChange = { subjective = it },
                            label = { Text("S - Subjective (Symptoms / Patient History)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = objective,
                            onValueChange = { objective = it },
                            label = { Text("O - Objective (Clinical Examination / Vitals)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = assessment,
                            onValueChange = { assessment = it },
                            label = { Text("A - Assessment (Diagnosis)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = plan,
                            onValueChange = { plan = it },
                            label = { Text("P - Plan (Therapy & Follow-up)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                3 -> {
                    // Live In-Call Encrypted Chat
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MedBackgroundLight)
                            .padding(12.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(callState.inCallChat) { (sender, text) ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (sender.contains("Dr.")) MedTealContainer else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = sender, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedTealDark)
                                        Text(text = text, fontSize = 13.sp, color = Color.Black)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                placeholder = { Text("Type message...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("in_call_chat_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    onSendMessage(messageText)
                                    messageText = ""
                                },
                                modifier = Modifier.testTag("in_call_send_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = MedTealPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Call Control Bar (Bottom)
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Toggle
                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (callState.isMicMuted) MedCoralTertiary else Color(0xFF334155))
                        .testTag("toggle_mic_button")
                ) {
                    Icon(
                        if (callState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = Color.White
                    )
                }

                if (callState.isAudioCallMode) {
                    // Audio Output Route Switcher (Speaker / Earpiece / Bluetooth)
                    IconButton(
                        onClick = onToggleAudioRoute,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                            .testTag("toggle_audio_route_button")
                    ) {
                        Icon(
                            when (callState.audioOutputRoute) {
                                "Speakerphone" -> Icons.Default.VolumeUp
                                "Bluetooth Headset" -> Icons.Default.BluetoothAudio
                                else -> Icons.Default.PhoneInTalk
                            },
                            contentDescription = "Audio Route",
                            tint = Color.White
                        )
                    }

                    // Switch from Audio to Video Call
                    IconButton(
                        onClick = { onSwitchCallMode(false) },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MedTealPrimary)
                            .testTag("switch_to_video_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Enable Video", tint = Color.White)
                    }
                } else {
                    // Camera Toggle
                    IconButton(
                        onClick = onToggleCamera,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (callState.isCameraOff) MedCoralTertiary else Color(0xFF334155))
                            .testTag("toggle_camera_button")
                    ) {
                        Icon(
                            if (callState.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Camera",
                            tint = Color.White
                        )
                    }

                    // Switch from Video to Audio Call
                    IconButton(
                        onClick = { onSwitchCallMode(true) },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                            .testTag("switch_to_audio_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Audio Mode", tint = Color.White)
                    }
                }

                // Quick Rx Studio Switcher
                IconButton(
                    onClick = { selectedTab = if (selectedTab == 1) 0 else 1 },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (selectedTab == 1) MedTealPrimary else Color(0xFF334155))
                        .testTag("toggle_in_call_rx_button")
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = "Rx Pad", tint = Color.White)
                }

                // End Call
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MedCoralTertiary)
                        .testTag("end_call_button")
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AudioConsultationViewport(
    callState: CallState,
    patientName: String,
    doctorName: String,
    onSwitchToVideo: () -> Unit,
    onToggleAudioRoute: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Audio Quality & Security Badges
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MedCyanSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("HD Audio (48kHz Opus) • Noise Cancelled", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Center Calling Visualizer Cards
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Doctor Card
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size((80 * (if (!callState.isMicMuted) pulseScale else 1.0f)).dp)
                            .clip(CircleShape)
                            .background(MedTealContainer)
                            .border(3.dp, MedTealPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(doctorName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("Doctor (Speaking)", color = MedTealPrimary, fontSize = 11.sp)
                }

                // Voice Frequency Waves
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(24.dp, 40.dp, 16.dp, 48.dp, 32.dp, 18.dp, 44.dp, 28.dp)
                    heights.forEachIndexed { i, h ->
                        val animH by infiniteTransition.animateFloat(
                            initialValue = 12f,
                            targetValue = h.value,
                            animationSpec = infiniteRepeatable(
                                animation = tween(350 + (i * 70), easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "wave_$i"
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(animH.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i % 2 == 0) MedCyanSecondary else MedTealPrimary)
                        )
                    }
                }

                // Patient Card
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MedCyanContainer)
                            .border(2.dp, MedCyanSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MedCyanSecondary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(patientName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("Patient (Online)", color = MedSuccess, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Audio Route Chip
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                modifier = Modifier.clickable { onToggleAudioRoute() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (callState.audioOutputRoute) {
                            "Speakerphone" -> Icons.Default.VolumeUp
                            "Bluetooth Headset" -> Icons.Default.BluetoothAudio
                            else -> Icons.Default.PhoneInTalk
                        },
                        contentDescription = null,
                        tint = MedCyanSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Output: ${callState.audioOutputRoute} (Tap to change)", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Action: Switch to Video Call
        Button(
            onClick = onSwitchToVideo,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
            modifier = Modifier.testTag("switch_to_video_call_btn")
        ) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch to Video Call", color = Color.White, fontSize = 13.sp)
        }
    }
}
