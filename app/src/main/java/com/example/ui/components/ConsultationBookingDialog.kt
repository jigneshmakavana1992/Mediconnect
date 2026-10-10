package com.example.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppointmentEntity
import com.example.data.model.DoctorProfileEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BookingStep {
    SLOT_SELECTION,
    UPI_PAYMENT,
    PAYMENT_PROCESSING,
    PAYMENT_SUCCESS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsultationBookingDialog(
    doctor: DoctorProfileEntity,
    doctorName: String,
    onDismiss: () -> Unit,
    onConfirmBooking: (
        consultationType: String,
        timeSlot: String,
        consultationDate: String,
        complaint: String,
        feeInr: Double,
        upiApp: String,
        upiVpa: String,
        isReminderSet: Boolean,
        reminderTiming: String,
        onSuccess: (AppointmentEntity) -> Unit
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(BookingStep.SLOT_SELECTION) }

    // Slot selection state
    var selectedConsultationType by remember { mutableStateOf("Video Call") } // "Video Call" or "Audio Call"
    var selectedDate by remember { mutableStateOf("Today (24 Sep)") }
    var selectedSlot by remember { mutableStateOf("10:00 AM - 10:30 AM") }
    var complaintText by remember { mutableStateOf("") }
    var isReminderEnabled by remember { mutableStateOf(true) }
    var selectedReminderTiming by remember { mutableStateOf("15 mins before") }

    // UPI Payment state
    var selectedUpiApp by remember { mutableStateOf("Google Pay") }
    var customVpaInput by remember { mutableStateOf("alex.wright@okhdfcbank") }
    var paymentMethodTab by remember { mutableStateOf(0) } // 0: UPI Apps, 1: UPI ID/VPA, 2: Scan QR
    var generatedUtr by remember { mutableStateOf("UPI/2026/894102941") }
    var bookedAppointmentResult by remember { mutableStateOf<AppointmentEntity?>(null) }

    val feeInr = doctor.consultationFee.takeIf { it > 0 } ?: 499.0

    val availableDates = listOf("Today (24 Sep)", "Tomorrow (25 Sep)", "26 Sep", "27 Sep")

    val morningSlots = listOf(
        "09:00 AM - 09:30 AM" to "Filling Fast",
        "09:30 AM - 10:00 AM" to "Available",
        "10:00 AM - 10:30 AM" to "Available",
        "11:00 AM - 11:30 AM" to "Available"
    )

    val afternoonSlots = listOf(
        "02:00 PM - 02:30 PM" to "Available",
        "02:30 PM - 03:00 PM" to "Filling Fast",
        "03:30 PM - 04:00 PM" to "Available",
        "04:00 PM - 04:30 PM" to "Available"
    )

    val eveningSlots = listOf(
        "05:30 PM - 06:00 PM" to "Available",
        "06:00 PM - 06:30 PM" to "Available",
        "07:00 PM - 07:30 PM" to "Available"
    )

    val upiApps = listOf(
        "Google Pay" to "GPay Instant",
        "PhonePe" to "BHIM PhonePe",
        "Paytm" to "Paytm UPI",
        "BHIM UPI" to "NPCI Official",
        "Cred UPI" to "Cred Pay"
    )

    val reminderOptions = listOf("15 mins before", "30 mins before", "1 hour before", "At call time")

    val commonComplaints = listOf("Fever & Chills", "Dry Cough", "Throat Pain", "Pediatric Check", "Stomach Ache", "Follow-up")

    Dialog(
        onDismissRequest = {
            if (currentStep != BookingStep.PAYMENT_PROCESSING) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header Bar
                Surface(
                    color = MedTealDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (currentStep) {
                                    BookingStep.SLOT_SELECTION -> Icons.Default.CalendarMonth
                                    BookingStep.UPI_PAYMENT -> Icons.Default.CurrencyRupee
                                    BookingStep.PAYMENT_PROCESSING -> Icons.Default.HourglassTop
                                    BookingStep.PAYMENT_SUCCESS -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (currentStep) {
                                        BookingStep.SLOT_SELECTION -> "Select Consultation Slot"
                                        BookingStep.UPI_PAYMENT -> "UPI Payment Gateway (₹ INR)"
                                        BookingStep.PAYMENT_PROCESSING -> "Authorizing UPI Payment"
                                        BookingStep.PAYMENT_SUCCESS -> "Consultation Confirmed"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "$doctorName • ${doctor.speciality}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (currentStep != BookingStep.PAYMENT_PROCESSING) {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_close_button")) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }

                // Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentStep) {
                        BookingStep.SLOT_SELECTION -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Consultation Fee Banner in Indian Rupees (₹)
                                item {
                                    Surface(
                                        color = MedTealContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Professional Consultation Fee",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MedTealDark
                                                )
                                                Text(
                                                    text = "₹${feeInr.toInt()} INR",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MedTealPrimary
                                                )
                                                Text(
                                                    text = "Includes digital prescription & follow-up review",
                                                    fontSize = 10.sp,
                                                    color = Color.DarkGray
                                                )
                                            }

                                            Surface(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MedTealPrimary)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Verified, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Verified Doctor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedTealPrimary)
                                                }
                                            }
                                        }
                                    }
                                }

                                // 1. Consultation Call Type Selector (Video Call vs Audio Call)
                                item {
                                    Text("1. Choose Consultation Type", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Video Call Card
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedConsultationType = "Video Call" }
                                                .border(
                                                    width = if (selectedConsultationType == "Video Call") 2.dp else 1.dp,
                                                    color = if (selectedConsultationType == "Video Call") MedTealPrimary else Color.LightGray,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .testTag("select_video_call_option"),
                                            color = if (selectedConsultationType == "Video Call") MedTealContainer.copy(alpha = 0.5f) else Color.White
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Default.Videocam,
                                                    contentDescription = null,
                                                    tint = if (selectedConsultationType == "Video Call") MedTealPrimary else Color.Gray,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Video Call",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (selectedConsultationType == "Video Call") MedTealPrimary else Color.Black
                                                )
                                                Text("Face-to-face HD", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }

                                        // Audio Call Card
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedConsultationType = "Audio Call" }
                                                .border(
                                                    width = if (selectedConsultationType == "Audio Call") 2.dp else 1.dp,
                                                    color = if (selectedConsultationType == "Audio Call") MedCyanSecondary else Color.LightGray,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .testTag("select_audio_call_option"),
                                            color = if (selectedConsultationType == "Audio Call") MedCyanContainer.copy(alpha = 0.5f) else Color.White
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Default.Call,
                                                    contentDescription = null,
                                                    tint = if (selectedConsultationType == "Audio Call") MedCyanSecondary else Color.Gray,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Audio Call",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (selectedConsultationType == "Audio Call") MedCyanSecondary else Color.Black
                                                )
                                                Text("Crystal Clear Voice", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }

                                // 2. Select Date
                                item {
                                    Text("2. Select Consultation Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(availableDates) { dt ->
                                            val isSelected = selectedDate == dt
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .clickable { selectedDate = dt },
                                                color = if (isSelected) MedTealPrimary else Color(0xFFF1F5F9),
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Text(
                                                    text = dt,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else Color.DarkGray,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Select Time Slot
                                item {
                                    Text("3. Select Time Slot", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Morning Slots
                                    Text("Morning (09:00 AM - 12:00 PM)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        morningSlots.forEach { (slot, status) ->
                                            TimeSlotChip(
                                                slot = slot,
                                                badge = status,
                                                isSelected = selectedSlot == slot,
                                                onSelect = { selectedSlot = slot }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Afternoon Slots
                                    Text("Afternoon (02:00 PM - 05:00 PM)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        afternoonSlots.forEach { (slot, status) ->
                                            TimeSlotChip(
                                                slot = slot,
                                                badge = status,
                                                isSelected = selectedSlot == slot,
                                                onSelect = { selectedSlot = slot }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Evening Slots
                                    Text("Evening (05:30 PM - 07:30 PM)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        eveningSlots.forEach { (slot, status) ->
                                            TimeSlotChip(
                                                slot = slot,
                                                badge = status,
                                                isSelected = selectedSlot == slot,
                                                onSelect = { selectedSlot = slot }
                                            )
                                        }
                                    }
                                }

                                // 4. Chief Complaint / Symptoms
                                item {
                                    Text("4. Chief Complaint / Symptoms", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = complaintText,
                                        onValueChange = { complaintText = it },
                                        placeholder = { Text("e.g. Mild fever, dry cough, sore throat since 2 days") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_complaint_input"),
                                        maxLines = 3
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(commonComplaints) { tag ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFF1F5F9),
                                                modifier = Modifier.clickable {
                                                    complaintText = if (complaintText.isBlank()) tag else "$complaintText, $tag"
                                                }
                                            ) {
                                                Text(
                                                    text = "+ $tag",
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 5. Call Reminder to Doctor and Patient
                                item {
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text("Doctor Call Alert & Slot Reminder", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                                        Text("Doctor alerts you directly via SMS & WhatsApp when call begins", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Switch(
                                                    checked = isReminderEnabled,
                                                    onCheckedChange = { isReminderEnabled = it },
                                                    modifier = Modifier.testTag("toggle_reminder_switch")
                                                )
                                            }

                                            if (isReminderEnabled) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text("Send Reminder Notification:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    reminderOptions.forEach { rem ->
                                                        val isSelected = selectedReminderTiming == rem
                                                        Surface(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(16.dp))
                                                                .clickable { selectedReminderTiming = rem },
                                                            color = if (isSelected) MedTealPrimary else Color.White,
                                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MedTealPrimary else Color.LightGray)
                                                        ) {
                                                            Text(
                                                                text = rem,
                                                                fontSize = 10.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isSelected) Color.White else Color.Black,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        BookingStep.UPI_PAYMENT -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Consultation Order Breakdown
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(text = "Consultation Order Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedTealDark)
                                                    Text(text = "$selectedConsultationType • $selectedDate • $selectedSlot", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Surface(
                                                    color = MedTealContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "NPCI Verified",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MedTealPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            HorizontalDivider(color = Color(0xFFE2E8F0))
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Doctor Consultation Fee", fontSize = 12.sp, color = Color.DarkGray)
                                                Text("₹${feeInr.toInt()}.00", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Digital Health Record & EMR Vault", fontSize = 12.sp, color = Color.DarkGray)
                                                Text("₹29.00", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Healthcare GST (18%)", fontSize = 12.sp, color = Color.DarkGray)
                                                Text("₹5.22", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Special Telehealth Subsidy", fontSize = 12.sp, color = MedSuccess)
                                                Text("-₹34.22", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Color(0xFFCBD5E1))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Total Amount Payable", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedTealDark)
                                                Text("₹${feeInr.toInt()}.00 INR", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MedTealPrimary)
                                            }
                                        }
                                    }
                                }

                                // UPI Payment Method Tabs
                                item {
                                    Text("Select UPI Payment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    TabRow(
                                        selectedTabIndex = paymentMethodTab,
                                        containerColor = Color(0xFFF1F5F9),
                                        contentColor = MedTealPrimary
                                    ) {
                                        Tab(
                                            selected = paymentMethodTab == 0,
                                            onClick = { paymentMethodTab = 0 },
                                            text = { Text("UPI Apps", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                        )
                                        Tab(
                                            selected = paymentMethodTab == 1,
                                            onClick = { paymentMethodTab = 1 },
                                            text = { Text("UPI ID (VPA)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                        )
                                        Tab(
                                            selected = paymentMethodTab == 2,
                                            onClick = { paymentMethodTab = 2 },
                                            text = { Text("Scan QR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                        )
                                    }
                                }

                                // Tab Content
                                item {
                                    when (paymentMethodTab) {
                                        0 -> {
                                            // Popular UPI Apps
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                upiApps.forEach { (appName, subtitle) ->
                                                    val isSelected = selectedUpiApp == appName
                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .clickable { selectedUpiApp = appName }
                                                            .border(
                                                                width = if (isSelected) 2.dp else 1.dp,
                                                                color = if (isSelected) MedTealPrimary else Color(0xFFE2E8F0),
                                                                shape = RoundedCornerShape(10.dp)
                                                            )
                                                            .testTag("upi_app_option_${appName.replace(" ", "_")}"),
                                                        color = if (isSelected) MedTealContainer.copy(alpha = 0.4f) else Color.White
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Surface(
                                                                    color = Color(0xFFE0F2FE),
                                                                    shape = CircleShape,
                                                                    modifier = Modifier.size(36.dp)
                                                                ) {
                                                                    Box(contentAlignment = Alignment.Center) {
                                                                        Text(
                                                                            text = "₹",
                                                                            fontSize = 18.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(0xFF0369A1)
                                                                        )
                                                                    }
                                                                }
                                                                Spacer(modifier = Modifier.width(10.dp))
                                                                Column {
                                                                    Text(appName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                    Text(subtitle, fontSize = 10.sp, color = Color.Gray)
                                                                }
                                                            }

                                                            RadioButton(
                                                                selected = isSelected,
                                                                onClick = { selectedUpiApp = appName },
                                                                colors = RadioButtonDefaults.colors(selectedColor = MedTealPrimary)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        1 -> {
                                            // Custom VPA Input
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = customVpaInput,
                                                    onValueChange = { customVpaInput = it },
                                                    label = { Text("Virtual Payment Address (VPA / UPI ID)") },
                                                    placeholder = { Text("e.g. mobile@upi, name@okhdfcbank") },
                                                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MedTealPrimary) },
                                                    trailingIcon = {
                                                        if (customVpaInput.contains("@")) {
                                                            Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = MedSuccess)
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("upi_vpa_input")
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Instant verification supported for GPay, PhonePe, Paytm, BHIM VPAs",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        2 -> {
                                            // Dynamic QR Code
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("Scan QR to Pay ₹${feeInr.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedTealDark)
                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    // Visual Dynamic QR Code Block
                                                    Surface(
                                                        modifier = Modifier
                                                            .size(160.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color.White)
                                                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
                                                        color = Color.White
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(120.dp), tint = Color.Black)
                                                                Text("UPI FASTPAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MedTealDark)
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Scan with any UPI app on your phone", fontSize = 11.sp, color = Color.DarkGray)
                                                    Text("Dynamic UPI Ref: UPI/2026/894102941", fontSize = 10.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        BookingStep.PAYMENT_PROCESSING -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(56.dp),
                                    color = MedTealPrimary,
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("Connecting to NPCI UPI Gateway...", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MedTealDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Authorizing transaction of ₹${feeInr.toInt()} via $selectedUpiApp", fontSize = 13.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Please do not close this window or press back", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        BookingStep.PAYMENT_SUCCESS -> {
                            val apt = bookedAppointmentResult
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Surface(
                                        color = MedSuccessContainer,
                                        shape = CircleShape,
                                        modifier = Modifier.size(70.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(46.dp))
                                        }
                                    }
                                }

                                item {
                                    Text("Payment Confirmed!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MedTealDark)
                                    Text("₹${feeInr.toInt()} successfully paid via $selectedUpiApp", fontSize = 13.sp, color = MedSuccess, fontWeight = FontWeight.SemiBold)
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Official UPI E-Receipt", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("UPI Ref / UTR:", fontSize = 11.sp, color = Color.Gray)
                                                Text(generatedUtr, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Doctor:", fontSize = 11.sp, color = Color.Gray)
                                                Text(doctorName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Date & Slot:", fontSize = 11.sp, color = Color.Gray)
                                                Text("$selectedDate • $selectedSlot", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MedTealPrimary)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Consultation Mode:", fontSize = 11.sp, color = Color.Gray)
                                                Text(selectedConsultationType, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Call Reminder:", fontSize = 11.sp, color = Color.Gray)
                                                Text(
                                                    if (isReminderEnabled) "Active ($selectedReminderTiming)" else "Disabled",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isReminderEnabled) MedSuccess else Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isReminderEnabled) {
                                    item {
                                        Surface(
                                            color = MedTealContainer.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "SMS & WhatsApp reminder set for $selectedReminderTiming • Dr. $doctorName can alert you directly upon call start",
                                                    fontSize = 11.sp,
                                                    color = MedTealDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Bar
                Surface(
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (currentStep) {
                            BookingStep.SLOT_SELECTION -> {
                                TextButton(onClick = onDismiss) {
                                    Text("Cancel", color = Color.DarkGray)
                                }
                                Button(
                                    onClick = { currentStep = BookingStep.UPI_PAYMENT },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    modifier = Modifier.testTag("proceed_to_upi_button")
                                ) {
                                    Text("Proceed to UPI Payment (₹${feeInr.toInt()})")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }

                            BookingStep.UPI_PAYMENT -> {
                                TextButton(onClick = { currentStep = BookingStep.SLOT_SELECTION }) {
                                    Text("Back to Slots", color = Color.DarkGray)
                                }
                                Button(
                                    onClick = {
                                        currentStep = BookingStep.PAYMENT_PROCESSING
                                        coroutineScope.launch {
                                            delay(1600) // Simulated secure NPCI handshake
                                            val utr = "UPI/2026/${(100000000..999999999).random()}"
                                            generatedUtr = utr
                                            val complaint = if (complaintText.isBlank()) "Routine clinical check-up" else complaintText

                                            onConfirmBooking(
                                                selectedConsultationType,
                                                selectedSlot,
                                                selectedDate,
                                                complaint,
                                                feeInr,
                                                selectedUpiApp,
                                                customVpaInput,
                                                isReminderEnabled,
                                                selectedReminderTiming
                                            ) { savedApt ->
                                                bookedAppointmentResult = savedApt
                                                currentStep = BookingStep.PAYMENT_SUCCESS
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    modifier = Modifier.testTag("pay_upi_button")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pay ₹${feeInr.toInt()} via UPI")
                                }
                            }

                            BookingStep.PAYMENT_PROCESSING -> {
                                // Locked state while authorizing
                                Text("Securing payment...", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                            }

                            BookingStep.PAYMENT_SUCCESS -> {
                                Spacer(modifier = Modifier.weight(1f))
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    modifier = Modifier.testTag("finish_booking_button")
                                ) {
                                    Text("Done & View My Consultations")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSlotChip(
    slot: String,
    badge: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MedTealPrimary else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(10.dp)
            )
            .testTag("slot_chip_$slot"),
        color = if (isSelected) MedTealContainer else Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slot,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MedTealDark else Color.Black
            )
            if (badge == "Filling Fast") {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    color = MedCoralContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Fast",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedCoralTertiary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
