package com.example.ui.screens.doctor

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
import com.example.data.model.AppointmentEntity
import com.example.data.model.AppointmentStatus
import com.example.data.model.DoctorProfileEntity
import com.example.data.model.UserEntity
import com.example.ui.screens.policy.PolicyType
import com.example.ui.theme.*

@Composable
fun DoctorDashboardScreen(
    doctorUser: UserEntity,
    doctorProfile: DoctorProfileEntity?,
    appointments: List<AppointmentEntity>,
    doctorHelplineNumber: String = "+91 1800-202-3001",
    onStartCall: (AppointmentEntity) -> Unit,
    onOpenRxStudio: (patientName: String, appointmentId: String) -> Unit,
    onToggleReminder: (appointmentId: String, isReminderSet: Boolean) -> Unit = { _, _ -> },
    onTriggerCallAlert: (appointmentId: String) -> Unit = {},
    onNavigateToComplaintsHelpdesk: () -> Unit = {},
    onNavigateToPrescriptionRegistry: () -> Unit = {},
    onNavigateToPolicy: (PolicyType) -> Unit = {},
    onLogout: () -> Unit
) {
    var alertSentPatientName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Institutional Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MedTealContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedTealPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = doctorUser.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = doctorProfile?.speciality ?: "Clinical Specialist",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onNavigateToPolicy(PolicyType.TERMS) },
                        modifier = Modifier.testTag("doctor_policies_top_button")
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = "Legal & Indemnity Policies", tint = MedTealPrimary)
                    }
                    IconButton(
                        onClick = onNavigateToPrescriptionRegistry,
                        modifier = Modifier.testTag("doctor_prescriptions_top_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = "Prescription Registry", tint = MedTealPrimary)
                    }
                    IconButton(
                        onClick = onNavigateToComplaintsHelpdesk,
                        modifier = Modifier.testTag("doctor_helpdesk_top_button")
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = "Assistance & Helpdesk", tint = MedTealPrimary)
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("doctor_logout_button")) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MedCoralTertiary)
                    }
                }
            }
        }

        // Alert Feedback Banner
        if (alertSentPatientName != null) {
            Surface(
                color = MedTealPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔔 Call reminder sent to $alertSentPatientName (SMS & In-App Notification)", color = Color.White, fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = { alertSentPatientName = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Doctor Assistance Helpline & Complaints Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doctor_helpline_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Doctor Assistance Helpline", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedTealDark)
                                Text(doctorHelplineNumber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToComplaintsHelpdesk,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("doctor_open_helpdesk_button")
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Helpdesk / Complaints", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Institutional Compliance & Indemnity Notice
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doctor_indemnity_notice_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MedCyanSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MANDATORY PROFESSIONAL INDEMNITY", color = MedCyanSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("IT ACT SEC 79 SAFE HARBOR", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Platform Not Liable for Doctor Negligence: MedConnect Pro functions as a technology intermediary conduit. Consulting practitioners are required to independently procure and maintain active Professional Indemnity Medical Malpractice Insurance.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToPolicy(PolicyType.TERMS) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("doctor_view_terms_button"),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Text("⚖️ Terms & Indemnity", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onNavigateToPolicy(PolicyType.PAYOUT) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("doctor_view_payout_policy_button"),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MedSuccess),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MedSuccess.copy(alpha = 0.5f))
                            ) {
                                Text("💰 Payout Policy", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Regulatory Credentials Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Regulatory Council Credentials",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MedTealDark
                            )
                            Surface(
                                color = MedSuccessContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verified Practitioner", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Council Reg No: ${doctorProfile?.councilRegistrationNumber ?: "NMC-2018-84920"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            text = "Degrees: ${doctorProfile?.qualifications ?: "MBBS, MD"}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Affiliation: ${doctorProfile?.clinicAffiliation ?: "Apex Healthcare"}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Consultation Timings: ${doctorProfile?.consultationTimings ?: "Mon-Sat 09:00 - 18:00"}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MedTealContainer),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Today's Queue", fontSize = 11.sp, color = MedOnTealContainer)
                            Text("${appointments.size} Patients", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedTealPrimary)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateToPrescriptionRegistry)
                            .testTag("doctor_emr_prescriptions_card"),
                        colors = CardDefaults.cardColors(containerColor = MedCyanContainer),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("EMR Prescriptions", fontSize = 11.sp, color = MedOnCyanContainer)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Registry", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedCyanSecondary)
                                Icon(Icons.Default.ArrowForward, contentDescription = "Open Registry", modifier = Modifier.size(16.dp), tint = MedCyanSecondary)
                            }
                        }
                    }
                }
            }

            // Clinical Teleconsultation Standards & Quality Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MedTealContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = MedTealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Institutional Quality Protocol Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MedTealDark
                            )
                            Text(
                                text = "HD Video/Audio Streams Enabled • Minimum 3-min consultation standard",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Surface(
                            color = MedSuccessContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "READY",
                                color = MedSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Live Queue Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Teleconsultation Queue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedTealDark
                    )
                    Text(
                        text = "WebRTC & Opus Ready",
                        fontSize = 12.sp,
                        color = MedSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (appointments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No pending appointments in queue", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(appointments) { apt ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                        text = apt.patientName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Chief Complaint: ${apt.chiefComplaint}",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "Slot: ${apt.timeSlot}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedTealPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = if (apt.consultationType == "Audio Call") MedCyanContainer else MedTealContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = apt.consultationType,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (apt.consultationType == "Audio Call") MedOnCyanContainer else MedOnTealContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Teleconsultation Room: ${apt.meetingRoomId}",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal
                                    )
                                }

                                Surface(
                                    color = if (apt.status == AppointmentStatus.SCHEDULED) MedTealContainer else MedSuccessContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = apt.status.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (apt.status == AppointmentStatus.SCHEDULED) MedTealPrimary else MedSuccess,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Reminder control bar for doctor
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MedTealPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reminder: ${if (apt.isReminderSet) "Scheduled (${apt.reminderTiming})" else "Disabled"}",
                                        fontSize = 11.sp,
                                        color = MedTealDark
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        onTriggerCallAlert(apt.id)
                                        alertSentPatientName = apt.patientName
                                    },
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("alert_patient_button_${apt.id}"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.RingVolume, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Alert Patient", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onStartCall(apt) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("start_call_button_${apt.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        if (apt.consultationType == "Audio Call") Icons.Default.Call else Icons.Default.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (apt.consultationType == "Audio Call") "Join Audio Call" else "Join Video Call",
                                        fontSize = 13.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onOpenRxStudio(apt.patientName, apt.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_rx_studio_button_${apt.id}"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rapid Rx Pad", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
