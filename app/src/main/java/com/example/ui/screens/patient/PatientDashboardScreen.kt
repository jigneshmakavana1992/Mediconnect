package com.example.ui.screens.patient

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
import com.example.data.model.*
import com.example.ui.components.ConsultationBookingDialog
import com.example.ui.screens.policy.PolicyType
import com.example.ui.theme.*

@Composable
fun PatientDashboardScreen(
    patientUser: UserEntity,
    patientProfile: PatientProfileEntity?,
    doctors: List<DoctorProfileEntity>,
    appointments: List<AppointmentEntity>,
    prescriptions: List<PrescriptionEntity>,
    patientHelplineNumber: String = "+91 1800-202-3002",
    onStartCall: (AppointmentEntity) -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToGrowthTracker: () -> Unit,
    onNavigateToFamily: () -> Unit,
    onNavigateToComplaintsHelpdesk: () -> Unit = {},
    onBookConsultation: (doctorId: String, doctorName: String, speciality: String, complaint: String) -> Unit,
    onBookConsultationDetailed: (
        doctorId: String,
        doctorName: String,
        speciality: String,
        complaint: String,
        consultationType: String,
        timeSlot: String,
        consultationDate: String,
        feeInr: Double,
        upiApp: String,
        upiVpa: String,
        isReminderSet: Boolean,
        reminderTiming: String,
        onSuccess: (AppointmentEntity) -> Unit
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onToggleReminder: (appointmentId: String, isReminderSet: Boolean) -> Unit = { _, _ -> },
    onNavigateToPolicy: (PolicyType) -> Unit = {},
    onLogout: () -> Unit
) {
    var showBookingDialog by remember { mutableStateOf(false) }
    var selectedDoctorForBooking by remember { mutableStateOf<DoctorProfileEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Institutional Top App Bar
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
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = patientUser.fullName.take(1),
                                fontWeight = FontWeight.Bold,
                                color = MedTealPrimary,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = patientUser.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = "Patient EMR Vault Active",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onNavigateToPolicy(PolicyType.PRIVACY) },
                        modifier = Modifier.testTag("patient_policies_top_button")
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = "Legal Policies & Terms", tint = MedTealPrimary)
                    }
                    IconButton(
                        onClick = onNavigateToComplaintsHelpdesk,
                        modifier = Modifier.testTag("patient_helpdesk_top_button")
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = "Assistance & Helpdesk", tint = MedTealPrimary)
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("patient_logout_button")) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MedCoralTertiary)
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
            // Patient Assistance Helpline & Complaints Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_helpline_banner")
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
                                Text("Patient Assistance Helpline", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedTealDark)
                                Text(patientHelplineNumber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToComplaintsHelpdesk,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("patient_open_helpdesk_button")
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Text Complaint", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Patient Allergy Safety Warning Pill
            if (patientProfile != null && patientProfile.allergies.isNotBlank()) {
                item {
                    Surface(
                        color = MedCoralContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MedCoralTertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Allergy Alert: ${patientProfile.allergies}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MedCoralTertiary
                                )
                                Text(
                                    text = "Chronic Conditions: ${patientProfile.chronicConditions}",
                                    fontSize = 11.sp,
                                    color = MedTealDark
                                )
                            }
                        }
                    }
                }
            }

            // Quick Hub Navigation Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        onClick = onNavigateToVault,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hub_emr_vault_button"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.FolderShared, contentDescription = null, tint = MedTealPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("EMR Vault", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${prescriptions.size} Prescriptions", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Card(
                        onClick = onNavigateToGrowthTracker,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hub_growth_chart_button"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = MedCyanSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("WHO Growth", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Pediatric Curves", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Card(
                        onClick = onNavigateToFamily,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hub_family_button"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = MedSuccess)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Family", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Dependents", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Institutional Policy, Safety & Refund Guarantee Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_policies_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Patient Safety, Terms & Protection", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedTealDark)
                            }
                            Surface(color = MedSuccessContainer, shape = RoundedCornerShape(4.dp)) {
                                Text("100% NO-SHOW REFUND", color = MedSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Platform Not Liable for Doctor Negligence (Intermediary Safe Harbor). Doctors are independent practitioners with mandatory indemnity insurance. Teleconsultations are not for medical emergencies.",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToPolicy(PolicyType.PRIVACY) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("patient_view_privacy_button"),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MedTealPrimary.copy(alpha = 0.5f))
                            ) {
                                Text("🛡️ Privacy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedTealDark)
                            }

                            OutlinedButton(
                                onClick = { onNavigateToPolicy(PolicyType.TERMS) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("patient_view_terms_button"),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F172A).copy(alpha = 0.5f))
                            ) {
                                Text("⚖️ Terms", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }

                            OutlinedButton(
                                onClick = { onNavigateToPolicy(PolicyType.REFUND) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("patient_view_refund_button"),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MedCyanSecondary.copy(alpha = 0.5f))
                            ) {
                                Text("💳 Refund", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedCyanSecondary)
                            }
                        }
                    }
                }
            }

            // Call Reminder Highlight Card if active upcoming appointment exists
            val upcomingApt = appointments.firstOrNull { it.status == AppointmentStatus.SCHEDULED }
            if (upcomingApt != null) {
                item {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AlarmOn,
                                        contentDescription = null,
                                        tint = MedCyanSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CALL REMINDER ACTIVE",
                                        color = MedCyanSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Surface(
                                    color = if (upcomingApt.consultationType == "Audio Call") MedCyanContainer else MedTealContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = upcomingApt.consultationType.uppercase(),
                                        color = if (upcomingApt.consultationType == "Audio Call") MedOnCyanContainer else MedOnTealContainer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${upcomingApt.doctorName} (${upcomingApt.doctorSpeciality})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Scheduled Slot: ${upcomingApt.timeSlot} • UPI ₹${upcomingApt.consultationFeeInr.toInt()} Paid",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MedCyanSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Doctor alerts you via SMS/WhatsApp prior to call",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onStartCall(upcomingApt) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("join_upcoming_call_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    if (upcomingApt.consultationType == "Audio Call") Icons.Default.Call else Icons.Default.Videocam,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (upcomingApt.consultationType == "Audio Call") "Join Audio Consultation Room" else "Join HD Video Consultation Room",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Upcoming Consultations Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Consultations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedTealDark
                    )
                }
            }

            if (appointments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No upcoming consultations booked.", fontSize = 13.sp, color = Color.Gray)
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = apt.doctorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = apt.doctorSpeciality, fontSize = 12.sp, color = MedTealPrimary)
                                    Text(text = "Complaint: ${apt.chiefComplaint}", fontSize = 12.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Time Slot & Mode
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = apt.timeSlot, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MedTealPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            if (apt.consultationType == "Audio Call") Icons.Default.Call else Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = MedCyanSecondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = apt.consultationType, fontSize = 11.sp, color = MedCyanSecondary)
                                    }
                                    // UPI Payment details
                                    Text(
                                        text = "₹${apt.consultationFeeInr.toInt()} Paid via ${apt.upiApp} • ${apt.paymentStatus}",
                                        fontSize = 11.sp,
                                        color = MedSuccess,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Surface(
                                    color = MedTealContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = apt.status.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MedOnTealContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Reminder row
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (apt.isReminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = if (apt.isReminderSet) MedTealPrimary else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (apt.isReminderSet) "Call Reminder: On (${apt.reminderTiming})" else "Reminder Off",
                                        fontSize = 11.sp,
                                        color = if (apt.isReminderSet) MedTealDark else Color.Gray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Receive Doctor Alert",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = apt.isReminderSet,
                                        onCheckedChange = { onToggleReminder(apt.id, it) },
                                        modifier = Modifier.height(24.dp).testTag("reminder_switch_${apt.id}")
                                    )
                                }
                            }

                            if (apt.status != AppointmentStatus.COMPLETED) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onStartCall(apt) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("patient_join_call_button_${apt.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
                                ) {
                                    Icon(
                                        if (apt.consultationType == "Audio Call") Icons.Default.Call else Icons.Default.Videocam,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (apt.consultationType == "Audio Call") "Join Audio Consultation Room"
                                        else "Join HD Video Consultation Room"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Verified Medical Specialists Directory
            item {
                Text(
                    text = "Verified Medical Council Specialists",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MedTealDark
                )
            }

            items(doctors) { doc ->
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
                                    text = "Specialist Profile",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedCyanSecondary
                                )
                                Text(
                                    text = doc.speciality,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MedTealDark
                                )
                                Text(
                                    text = doc.qualifications,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Council Reg: ${doc.councilRegistrationNumber}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Hospital: ${doc.clinicAffiliation}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Surface(
                                color = MedSuccessContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "₹${doc.consultationFee.toInt()} Fee",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                selectedDoctorForBooking = doc
                                showBookingDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("book_consultation_button_${doc.doctorId}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Book Slot • Video / Audio Call")
                        }
                    }
                }
            }
        }
    }

    // Comprehensive Consultation Booking Dialog with Slots, UPI in INR, and Reminders
    if (showBookingDialog && selectedDoctorForBooking != null) {
        val doc = selectedDoctorForBooking!!
        ConsultationBookingDialog(
            doctor = doc,
            doctorName = "Dr. Sarah Jenkins",
            onDismiss = { showBookingDialog = false },
            onConfirmBooking = { consultationType, timeSlot, consultationDate, complaint, feeInr, upiApp, upiVpa, isReminderSet, reminderTiming, onSuccess ->
                onBookConsultationDetailed(
                    doc.doctorId,
                    "Dr. Sarah Jenkins",
                    doc.speciality,
                    complaint,
                    consultationType,
                    timeSlot,
                    consultationDate,
                    feeInr,
                    upiApp,
                    upiVpa,
                    isReminderSet,
                    reminderTiming
                ) { bookedApt ->
                    onSuccess(bookedApt)
                }
            }
        )
    }
}
