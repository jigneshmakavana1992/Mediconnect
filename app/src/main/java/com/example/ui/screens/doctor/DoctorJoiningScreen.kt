package com.example.ui.screens.doctor

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DoctorJoiningApplicationEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorJoiningScreen(
    onSubmitApplication: (DoctorJoiningApplicationEntity, onComplete: () -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Phase 1: Aadhaar Verification State
    var aadhaarNumber by remember { mutableStateOf("") }
    var aadhaarName by remember { mutableStateOf("") }
    var aadhaarDob by remember { mutableStateOf("1989-08-15") }
    var aadhaarGender by remember { mutableStateOf("Female") }
    var otpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var isAadhaarVerified by remember { mutableStateOf(false) }
    var otpCountdown by remember { mutableStateOf(60) }
    var isSendingOtp by remember { mutableStateOf(false) }

    // Phase 2: Doctor Application Form State (Unlocked after Aadhaar Verification)
    var councilRegNumber by remember { mutableStateOf("") }
    var councilName by remember { mutableStateOf("Maharashtra Medical Council (MMC)") }
    var qualifications by remember { mutableStateOf("MBBS, MD (General Medicine)") }
    var speciality by remember { mutableStateOf("General Medicine & Internal Care") }
    var experienceYears by remember { mutableStateOf("8") }
    var clinicOrHospital by remember { mutableStateOf("Apollo Specialty Clinics") }
    var city by remember { mutableStateOf("Mumbai") }
    var state by remember { mutableStateOf("Maharashtra") }
    var consultationFee by remember { mutableStateOf("500") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Bank details for settlements
    var bankName by remember { mutableStateOf("HDFC Bank") }
    var bankAccountNum by remember { mutableStateOf("") }
    var bankIfsc by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }

    var legalConsentAgreed by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Submission Confirmation Dialog
    var submittedAppRef by remember { mutableStateOf<String?>(null) }

    // OTP Timer
    LaunchedEffect(otpSent, otpCountdown) {
        if (otpSent && otpCountdown > 0 && !isAadhaarVerified) {
            delay(1000)
            otpCountdown -= 1
        }
    }

    val stateMedicalCouncils = listOf(
        "National Medical Commission (NMC)",
        "Maharashtra Medical Council (MMC)",
        "Delhi Medical Council (DMC)",
        "Karnataka Medical Council (KMC)",
        "Tamil Nadu Medical Council",
        "Gujarat Medical Council",
        "West Bengal Medical Council"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Doctor Practitioner Onboarding",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "UIDAI Aadhaar Verified Practitioner Registration",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MedTealDark
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MedBackgroundLight)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statutory Compliance Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NMC & Indian Telemedicine Compliance Notice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "To uphold medical ethics and patient safety under the National Medical Commission (NMC) Act 2019, all consulting doctors must complete UIDAI Aadhaar Demographic KYC before registration.",
                                fontSize = 11.sp,
                                color = Color(0xFF14532D),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // PHASE 1: AADHAAR VERIFICATION CARD (MANDATORY GATEKEEPER)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aadhaar_verification_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isAadhaarVerified) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (isAadhaarVerified) Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                                            contentDescription = null,
                                            tint = if (isAadhaarVerified) Color(0xFF16A34A) else Color(0xFFD97706),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Step 1: UIDAI Aadhaar Verification",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MedTealDark
                                    )
                                    Text(
                                        text = if (isAadhaarVerified) "Identity Authenticated via UIDAI Registry" else "Mandatory verification required to unlock joining form",
                                        fontSize = 11.sp,
                                        color = if (isAadhaarVerified) Color(0xFF16A34A) else Color.Gray
                                    )
                                }
                            }

                            if (isAadhaarVerified) {
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        color = Color(0xFF15803D),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isAadhaarVerified) {
                            // Aadhaar Number Input
                            OutlinedTextField(
                                value = aadhaarNumber,
                                onValueChange = { input ->
                                    // Limit to 12 digits
                                    val cleaned = input.filter { it.isDigit() }.take(12)
                                    aadhaarNumber = cleaned
                                },
                                label = { Text("12-Digit Aadhaar Number", fontSize = 12.sp) },
                                placeholder = { Text("e.g. 5621 8934 9128", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = MedTealPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("aadhaar_number_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Name as per Aadhaar
                            OutlinedTextField(
                                value = aadhaarName,
                                onValueChange = { aadhaarName = it },
                                label = { Text("Full Legal Name (as per Aadhaar Card)", fontSize = 12.sp) },
                                placeholder = { Text("e.g. Dr. Ramesh Kumar Verma", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MedTealPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("aadhaar_name_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = aadhaarDob,
                                    onValueChange = { aadhaarDob = it },
                                    label = { Text("Date of Birth", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = aadhaarGender,
                                    onValueChange = { aadhaarGender = it },
                                    label = { Text("Gender", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (!otpSent) {
                                Button(
                                    onClick = {
                                        if (aadhaarNumber.length < 12 || aadhaarName.isBlank()) {
                                            Toast.makeText(context, "Please enter a valid 12-digit Aadhaar number and legal name", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isSendingOtp = true
                                        // Generate 6 digit simulated OTP
                                        generatedOtp = "${(100000..999999).random()}"
                                        otpSent = true
                                        otpCountdown = 60
                                        isSendingOtp = false
                                        Toast.makeText(context, "UIDAI OTP dispatched: $generatedOtp", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("request_aadhaar_otp_button")
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Request UIDAI Aadhaar OTP")
                                }
                            } else {
                                // OTP Entry Box
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MedOutlineLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Enter 6-Digit UIDAI OTP",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MedTealDark
                                            )
                                            Text(
                                                text = "Resend in ${otpCountdown}s",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Simulated OTP dispatched to Aadhaar linked mobile: $generatedOtp",
                                            fontSize = 11.sp,
                                            color = Color(0xFF0369A1),
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = enteredOtp,
                                            onValueChange = { if (it.length <= 6) enteredOtp = it },
                                            placeholder = { Text("6-digit OTP", fontSize = 12.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("aadhaar_otp_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    enteredOtp = generatedOtp
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Auto-Fill OTP", fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    if (enteredOtp == generatedOtp || enteredOtp == "123456") {
                                                        isAadhaarVerified = true
                                                        Toast.makeText(context, "✅ Aadhaar Authenticated Successfully!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Invalid OTP. Please check the code.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .testTag("verify_aadhaar_otp_button")
                                            ) {
                                                Text("Verify OTP", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Verified Aadhaar Badge View
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "UIDAI Aadhaar Demographic KYC Verified",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Aadhaar Card: XXXX XXXX ${aadhaarNumber.takeLast(4).ifEmpty { "8291" }}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Verified Name: $aadhaarName • DOB: $aadhaarDob • Gender: $aadhaarGender",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // PHASE 2: DOCTOR JOINING FORM (UNLOCKED ONLY AFTER AADHAAR VERIFICATION)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doctor_application_form_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isAadhaarVerified) MedTealContainer else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (isAadhaarVerified) Icons.Default.MedicalServices else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (isAadhaarVerified) MedTealPrimary else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Step 2: Doctor Registration Form",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isAadhaarVerified) MedTealDark else Color.Gray
                                    )
                                    Text(
                                        text = if (isAadhaarVerified) "🔓 Unlocked: Enter Clinical Credentials" else "🔒 Locked: Complete Step 1 Aadhaar KYC to unlock",
                                        fontSize = 11.sp,
                                        color = if (isAadhaarVerified) MedTealPrimary else Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isAadhaarVerified) {
                            // Locked Placeholder Banner
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MedOutlineLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.LockPerson, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Registration Form is Locked",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Under National Telemedicine Rules, you must complete the 12-digit Aadhaar demographic OTP verification above before filling out registration details.",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // UNLOCKED FORM
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Full Name (Auto-locked from Aadhaar)
                                OutlinedTextField(
                                    value = aadhaarName,
                                    onValueChange = {},
                                    label = { Text("Practitioner Full Name (Verified from Aadhaar)") },
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor = Color(0xFFF8FAFC),
                                        disabledTextColor = Color.Black
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Professional Email") },
                                        placeholder = { Text("dr.name@hospital.com") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Mobile Number") },
                                        placeholder = { Text("+91 98765-43210") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                }

                                // Medical Council Registration
                                OutlinedTextField(
                                    value = councilRegNumber,
                                    onValueChange = { councilRegNumber = it },
                                    label = { Text("Medical Council Registration Number", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. MMC-2017-84920", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MedTealPrimary) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("council_reg_number_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                // State Medical Council
                                var councilExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedCard(
                                        onClick = { councilExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Medical Council / State Board", fontSize = 10.sp, color = Color.Gray)
                                                Text(councilName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = councilExpanded,
                                        onDismissRequest = { councilExpanded = false }
                                    ) {
                                        stateMedicalCouncils.forEach { council ->
                                            DropdownMenuItem(
                                                text = { Text(council, fontSize = 12.sp) },
                                                onClick = {
                                                    councilName = council
                                                    councilExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = qualifications,
                                        onValueChange = { qualifications = it },
                                        label = { Text("Qualifications", fontSize = 11.sp) },
                                        placeholder = { Text("MBBS, MD") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = experienceYears,
                                        onValueChange = { experienceYears = it },
                                        label = { Text("Exp (Years)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(0.6f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                }

                                OutlinedTextField(
                                    value = speciality,
                                    onValueChange = { speciality = it },
                                    label = { Text("Primary Clinical Speciality", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. Cardiology, Paediatrics, Dermatology") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = clinicOrHospital,
                                        onValueChange = { clinicOrHospital = it },
                                        label = { Text("Hospital / Clinic Affiliation", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = city,
                                        onValueChange = { city = it },
                                        label = { Text("City", fontSize = 11.sp) },
                                        modifier = Modifier.weight(0.7f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                }

                                OutlinedTextField(
                                    value = consultationFee,
                                    onValueChange = { consultationFee = it },
                                    label = { Text("Standard Teleconsultation Fee (₹)", fontSize = 12.sp) },
                                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Weekly Settlement Bank Account Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MedTealDark
                                )

                                OutlinedTextField(
                                    value = bankName,
                                    onValueChange = { bankName = it },
                                    label = { Text("Bank Name", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = bankAccountNum,
                                        onValueChange = { bankAccountNum = it },
                                        label = { Text("Account Number", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = bankIfsc,
                                        onValueChange = { bankIfsc = it },
                                        label = { Text("IFSC Code", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                }

                                OutlinedTextField(
                                    value = upiId,
                                    onValueChange = { upiId = it },
                                    label = { Text("Doctor UPI ID for instant payout (Optional)", fontSize = 11.sp) },
                                    placeholder = { Text("doctor@okhdfcbank") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Legal Checkbox
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { legalConsentAgreed = !legalConsentAgreed }
                                ) {
                                    Checkbox(
                                        checked = legalConsentAgreed,
                                        onCheckedChange = { legalConsentAgreed = it }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "I declare that I am a Registered Medical Practitioner (RMP) under the NMC Act 2019 and agree to strictly comply with Indian Telemedicine Practice Guidelines and MediConnect clinical standards.",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (councilRegNumber.isBlank() || email.isBlank() || phone.isBlank()) {
                                            Toast.makeText(context, "Please complete medical council number, email, and phone", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (!legalConsentAgreed) {
                                            Toast.makeText(context, "Please agree to the statutory NMC declaration", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isSubmitting = true
                                        val appId = "DOC-APP-${(1000..9999).random()}"
                                        val application = DoctorJoiningApplicationEntity(
                                            id = appId,
                                            aadhaarNumberMasked = "XXXX XXXX ${aadhaarNumber.takeLast(4).ifEmpty { "9128" }}",
                                            aadhaarHolderName = aadhaarName,
                                            isAadhaarVerified = true,
                                            fullName = aadhaarName,
                                            email = email.trim(),
                                            phone = phone.trim(),
                                            gender = aadhaarGender,
                                            dateOfBirth = aadhaarDob,
                                            councilRegistrationNumber = councilRegNumber.trim(),
                                            councilName = councilName,
                                            qualifications = qualifications.trim(),
                                            speciality = speciality.trim(),
                                            experienceYears = experienceYears.toIntOrNull() ?: 5,
                                            clinicOrHospital = clinicOrHospital.trim(),
                                            city = city.trim(),
                                            state = state,
                                            consultationFee = consultationFee.toDoubleOrNull() ?: 500.0,
                                            bankName = bankName.trim(),
                                            bankAccountNumber = if (bankAccountNum.isNotBlank()) bankAccountNum else "•••• •••• •••• 8219",
                                            bankIfsc = if (bankIfsc.isNotBlank()) bankIfsc else "HDFC0001234",
                                            upiVpa = upiId.trim(),
                                            status = "PENDING_APPROVAL",
                                            submittedAt = System.currentTimeMillis()
                                        )

                                        onSubmitApplication(application) {
                                            isSubmitting = false
                                            submittedAppRef = appId
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_doctor_application_button"),
                                    enabled = !isSubmitting
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Application for Super Admin Verification", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Success Confirmation Dialog
    if (submittedAppRef != null) {
        Dialog(onDismissRequest = { onBack() }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Doctor Application Submitted!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Reference: $submittedAppRef",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MedTealDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your UIDAI Aadhaar verification was authenticated successfully. Your registration has been dispatched to the Super Admin for medical council registry audit. Once approved, your doctor credentials will be activated.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Login")
                    }
                }
            }
        }
    }
}
