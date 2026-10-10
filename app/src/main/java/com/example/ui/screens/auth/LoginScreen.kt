package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.AuthUiState
import com.example.ui.screens.policy.PolicyType
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    authState: AuthUiState,
    onLogin: (email: String, password: String) -> Unit,
    onBiometricLogin: (identifier: String) -> Unit = {},
    onRequestOtp: (identifier: String, onDispatched: (otp: String, target: String) -> Unit) -> Unit = { _, _ -> },
    onVerifyOtp: (identifier: String, enteredOtp: String, expectedOtp: String) -> Unit = { _, _, _ -> },
    onResetPassword: (newPassword: String) -> Unit,
    onNavigateToPatientRegister: () -> Unit,
    isComplaintPortalEnabled: Boolean = true,
    onNavigateToComplaintManagement: () -> Unit = {},
    onNavigateToDoctorJoining: () -> Unit = {},
    onNavigateToPolicy: (PolicyType) -> Unit = {}
) {
    val context = LocalContext.current
    var loginMethodTab by remember { mutableStateOf(0) } // 0: Password, 1: OTP, 2: Fingerprint / Biometric
    val scrollState = rememberScrollState()

    // Password credentials
    var email by remember { mutableStateOf("jigneshmakavana1992@gmail.com") }
    var password by remember { mutableStateOf("admin@98") }
    var passwordVisible by remember { mutableStateOf(false) }

    // OTP credentials
    var otpIdentifier by remember { mutableStateOf("+1 (555) 345-6789") }
    var dispatchedOtp by remember { mutableStateOf<String?>(null) }
    var enteredOtp by remember { mutableStateOf("") }
    var otpTargetRecipient by remember { mutableStateOf("") }
    var otpCountdown by remember { mutableStateOf(0) }
    var isSendingOtp by remember { mutableStateOf(false) }

    // Biometric / Fingerprint State
    var biometricTargetRole by remember { mutableStateOf("DOCTOR") } // DOCTOR, PATIENT, ADMIN
    var biometricIdentifier by remember { mutableStateOf("dr.jenkins@medconnect.pro") }
    var showBiometricScanDialog by remember { mutableStateOf(false) }
    var biometricScanningState by remember { mutableStateOf(0) } // 0: Prompt, 1: Scanning, 2: Verified

    // Password reset dialog state
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }

    // OTP Countdown effect
    LaunchedEffect(otpCountdown) {
        if (otpCountdown > 0) {
            delay(1000)
            otpCountdown -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Upper Section: Upper-Right Corner Complaint Management Portal Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedOutlineLight)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).background(MedSuccess, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Telehealth Online", fontSize = 10.sp, color = MedTealDark, fontWeight = FontWeight.Bold)
                }
            }

            // Upper Right Corner: Complaint Management / RM Portal
            Surface(
                color = if (isComplaintPortalEnabled) Color(0xFFF3E8FF) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isComplaintPortalEnabled) Color(0xFFC084FC) else Color.LightGray),
                modifier = Modifier
                    .clickable {
                        if (isComplaintPortalEnabled) {
                            onNavigateToComplaintManagement()
                        } else {
                            Toast.makeText(context, "Complaint Management Portal is disabled by Super Admin", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .testTag("complaint_management_top_right_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.SupportAgent,
                        contentDescription = "Complaint Management",
                        tint = if (isComplaintPortalEnabled) Color(0xFF7C3AED) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RM Complaint Portal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isComplaintPortalEnabled) Color(0xFF6B21A8) else Color.Gray
                    )
                    if (!isComplaintPortalEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Lock, contentDescription = "Disabled", tint = Color.Gray, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        // Hero Branding Header with Official MediConnect Logo
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mediconnect_logo_white),
                contentDescription = "MediConnect Logo",
                modifier = Modifier
                    .size(150.dp)
                    .padding(6.dp)
            )
        }

        Text(
            text = "Unified Clinical & Patient Portal",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MedTealDark
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-Factor Authentication Mode Tabs (Password, OTP, Fingerprint)
        TabRow(
            selectedTabIndex = loginMethodTab,
            containerColor = Color.White,
            contentColor = MedTealPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MedOutlineLight, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = loginMethodTab == 0,
                onClick = { loginMethodTab = 0 },
                text = { Text("Password", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_password_login")
            )
            Tab(
                selected = loginMethodTab == 1,
                onClick = { loginMethodTab = 1 },
                text = { Text("OTP Login", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_otp_login")
            )
            Tab(
                selected = loginMethodTab == 2,
                onClick = { loginMethodTab = 2 },
                text = { Text("Fingerprint", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_fingerprint_login")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Error Message banner
        if (authState.errorMessage != null) {
            Surface(
                color = MedCoralContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MedCoralTertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authState.errorMessage,
                        fontSize = 12.sp,
                        color = MedCoralTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ==========================================
        // TAB 0: STANDARD PASSWORD LOGIN
        // ==========================================
        if (loginMethodTab == 0) {
            // Quick role evaluation pills
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MedSurfaceLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Quick Select Account:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = email.contains("jigneshmakavana1992"),
                            onClick = {
                                email = "jigneshmakavana1992@gmail.com"
                                password = "admin@98"
                            },
                            label = { Text("Admin", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = email.contains("jenkins"),
                            onClick = {
                                email = "dr.jenkins@medconnect.pro"
                                password = "Doctor@2026"
                            },
                            label = { Text("Doctor", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = email.contains("alex.wright"),
                            onClick = {
                                email = "alex.wright@example.com"
                                password = "Patient@123"
                            },
                            label = { Text("Patient", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Password Credentials",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MedTealDark
                    )
                    Text(
                        text = "Enter your registered email address and security password",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address / Username") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MedTealPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MedTealPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onLogin(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                        enabled = !authState.isAuthenticating
                    ) {
                        if (authState.isAuthenticating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Sign In With Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 1: OTP BASED LOGIN ACCESS (DOCTOR & PATIENT)
        // ==========================================
        if (loginMethodTab == 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OTP Mobile / Email Access",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MedTealDark
                            )
                            Text(
                                text = "Passwordless login for Doctors & Patients",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Surface(
                            color = MedCyanContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Instant SMS OTP",
                                color = MedCyanSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset accounts for Doctor and Patient
                    Text("Select Quick Profile for OTP:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = otpIdentifier.contains("345-6789") || otpIdentifier.contains("jenkins"),
                            onClick = {
                                otpIdentifier = "+1 (555) 345-6789"
                                dispatchedOtp = null
                                enteredOtp = ""
                            },
                            label = { Text("👨‍⚕️ Doctor (+1 555-345-6789)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = otpIdentifier.contains("123-4567") || otpIdentifier.contains("alex"),
                            onClick = {
                                otpIdentifier = "+1 (555) 123-4567"
                                dispatchedOtp = null
                                enteredOtp = ""
                            },
                            label = { Text("🧑 Patient (+1 555-123-4567)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = otpIdentifier,
                        onValueChange = {
                            otpIdentifier = it
                            dispatchedOtp = null
                        },
                        label = { Text("Registered Phone or Email") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MedTealPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_identifier_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated SMS OTP Banner when dispatched
                    AnimatedVisibility(visible = dispatchedOtp != null) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MarkChatRead, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("SMS / Email Gateway Dispatched", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                    }
                                    TextButton(
                                        onClick = { enteredOtp = dispatchedOtp ?: "" },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text("Tap to Auto-Fill", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                    }
                                }
                                Text(
                                    text = "Your MediConnect verification OTP is: ${dispatchedOtp}. Valid for 10 minutes. Sent to $otpTargetRecipient.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }
                    }

                    // OTP Entry Field (When OTP has been requested)
                    if (dispatchedOtp != null) {
                        OutlinedTextField(
                            value = enteredOtp,
                            onValueChange = { if (it.length <= 6) enteredOtp = it },
                            label = { Text("Enter 6-Digit OTP Code") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = MedTealPrimary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entered_otp_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                onVerifyOtp(otpIdentifier, enteredOtp, dispatchedOtp ?: "")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("verify_otp_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                            enabled = enteredOtp.length == 6 && !authState.isAuthenticating
                        ) {
                            if (authState.isAuthenticating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verify OTP & Log In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (otpCountdown > 0) {
                                Text("Resend OTP in ${otpCountdown}s", fontSize = 11.sp, color = Color.Gray)
                            } else {
                                TextButton(
                                    onClick = {
                                        isSendingOtp = true
                                        onRequestOtp(otpIdentifier) { code, target ->
                                            dispatchedOtp = code
                                            otpTargetRecipient = target
                                            otpCountdown = 30
                                            isSendingOtp = false
                                            Toast.makeText(context, "New OTP dispatched to $target", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text("Resend Verification Code", fontSize = 11.sp, color = MedCyanSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Request OTP Button
                        Button(
                            onClick = {
                                isSendingOtp = true
                                onRequestOtp(otpIdentifier) { code, target ->
                                    dispatchedOtp = code
                                    otpTargetRecipient = target
                                    otpCountdown = 30
                                    isSendingOtp = false
                                    Toast.makeText(context, "OTP code sent to $target", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("request_otp_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedCyanSecondary),
                            enabled = otpIdentifier.isNotBlank() && !isSendingOtp
                        ) {
                            if (isSendingOtp) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send 6-Digit OTP Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 2: FINGERPRINT & BIOMETRIC LOGIN ACCESS (DOCTOR & PATIENT)
        // ==========================================
        if (loginMethodTab == 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Touch ID / Fingerprint Access",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MedTealDark
                    )
                    Text(
                        text = "Hardware biometric authentication for instant clinical & patient sign in",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role Selector for Fingerprint Identity
                    Text(
                        text = "Select Account with Enrolled Biometrics:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MedTealDark,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Doctor Fingerprint Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                biometricTargetRole = "DOCTOR"
                                biometricIdentifier = "dr.jenkins@medconnect.pro"
                            }
                            .border(
                                width = if (biometricTargetRole == "DOCTOR") 2.dp else 1.dp,
                                color = if (biometricTargetRole == "DOCTOR") MedTealPrimary else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .testTag("select_doctor_biometric"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (biometricTargetRole == "DOCTOR") MedTealContainer.copy(alpha = 0.4f) else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MedTealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dr. Sarah Jenkins", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                Text("dr.jenkins@medconnect.pro • Paediatrics", fontSize = 11.sp, color = Color.DarkGray)
                            }
                            Surface(
                                color = MedSuccessContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Biometric Enrolled", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MedSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Patient Fingerprint Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                biometricTargetRole = "PATIENT"
                                biometricIdentifier = "alex.wright@example.com"
                            }
                            .border(
                                width = if (biometricTargetRole == "PATIENT") 2.dp else 1.dp,
                                color = if (biometricTargetRole == "PATIENT") MedCyanSecondary else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .testTag("select_patient_biometric"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (biometricTargetRole == "PATIENT") MedCyanContainer.copy(alpha = 0.4f) else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MedCyanSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Alexander Wright", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                Text("alex.wright@example.com • Patient Portal", fontSize = 11.sp, color = Color.DarkGray)
                            }
                            Surface(
                                color = MedSuccessContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Biometric Enrolled", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MedSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Interactive Biometric Sensor Touch Button with Pulse
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.06f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(MedTealContainer)
                            .clickable {
                                showBiometricScanDialog = true
                                biometricScanningState = 0
                            }
                            .border(2.dp, MedTealPrimary, CircleShape)
                            .testTag("fingerprint_sensor_touch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Scan Fingerprint",
                            tint = MedTealPrimary,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Touch Fingerprint Sensor to Log In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MedTealDark
                    )
                    Text(
                        text = "Hardware KeyStore & BiometricPrompt FIDO2 Security",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showBiometricScanDialog = true
                            biometricScanningState = 0
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("launch_biometric_login_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (biometricTargetRole == "DOCTOR") MedTealPrimary else MedCyanSecondary
                        )
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (biometricTargetRole == "DOCTOR") "Sign In as Doctor (Fingerprint)" else "Sign In as Patient (Fingerprint)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Patient registration prompt
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Are you a patient? ", fontSize = 13.sp, color = Color.Gray)
            TextButton(
                onClick = onNavigateToPatientRegister,
                modifier = Modifier.testTag("navigate_patient_register_button")
            ) {
                Text(
                    text = "Register as Patient",
                    fontWeight = FontWeight.Bold,
                    color = MedCyanSecondary
                )
            }
        }

        Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Note: Multi-factor security active. Password, OTP, and Fingerprint biometric authentication supported.",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Doctor Joining Form Button (Aadhaar Verified)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToDoctorJoining() }
                .testTag("doctor_joining_entry_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Doctor Joining Form",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MedTealDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "AADHAAR KYC",
                                    color = Color(0xFF15803D),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Register as consulting practitioner after Aadhaar verification",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Institutional Policies & Terms Center Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_institutional_policies_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MedTealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Institutional Policies & Terms",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MedTealDark
                        )
                    }
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "v2.4 COMPLIANT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Platform intermediary safe-harbor active: MedConnect Pro is strictly not liable for doctor negligence. All consulting doctors must maintain valid Professional Indemnity Insurance.",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Policy Navigation Buttons Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToPolicy(PolicyType.PRIVACY) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("login_policy_privacy_button"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedTealPrimary.copy(alpha = 0.5f))
                    ) {
                        Text("🛡️ Privacy", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MedTealDark)
                    }

                    OutlinedButton(
                        onClick = { onNavigateToPolicy(PolicyType.TERMS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("login_policy_terms_button"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F172A).copy(alpha = 0.5f))
                    ) {
                        Text("⚖️ Terms", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }

                    OutlinedButton(
                        onClick = { onNavigateToPolicy(PolicyType.REFUND) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("login_policy_refund_button"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedCyanSecondary.copy(alpha = 0.5f))
                    ) {
                        Text("💳 Refund", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MedCyanSecondary)
                    }

                    OutlinedButton(
                        onClick = { onNavigateToPolicy(PolicyType.PAYOUT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("login_policy_payout_button"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedSuccess.copy(alpha = 0.5f))
                    ) {
                        Text("💰 Payout", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MedSuccess)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ==========================================
    // BIOMETRIC SCANNING DIALOG (BiometricPrompt Simulation)
    // ==========================================
    if (showBiometricScanDialog) {
        Dialog(onDismissRequest = { showBiometricScanDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Biometric Authentication",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MedTealDark
                    )
                    Text(
                        text = "Touch sensor to sign in as ${if (biometricTargetRole == "DOCTOR") "Dr. Sarah Jenkins" else "Alexander Wright"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (biometricScanningState == 2) MedSuccessContainer else MedTealContainer)
                            .clickable {
                                biometricScanningState = 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (biometricScanningState == 1) {
                            CircularProgressIndicator(color = MedTealPrimary, strokeWidth = 3.dp, modifier = Modifier.size(50.dp))
                        } else if (biometricScanningState == 2) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(44.dp))
                        } else {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(50.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = when (biometricScanningState) {
                            0 -> "Ready. Touch the sensor with your registered finger."
                            1 -> "Scanning fingerprint cryptographic signature..."
                            else -> "Biometric match confirmed! Logging in..."
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (biometricScanningState == 2) MedSuccess else Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showBiometricScanDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                biometricScanningState = 2
                                onBiometricLogin(biometricIdentifier)
                                Toast.makeText(context, "Fingerprint verified! Welcome.", Toast.LENGTH_SHORT).show()
                                showBiometricScanDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                            modifier = Modifier.testTag("confirm_fingerprint_scan_button")
                        ) {
                            Text("Simulate Finger Touch", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // MANDATORY PASSWORD RESET DIALOG
    if (authState.showPasswordResetDialog) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss without resetting */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MedWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("First Login: Password Reset Required", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "For enterprise compliance, your initial temporary credentials must be updated before gaining access to clinical features.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (resetError != null) {
                        Text(text = resetError!!, color = MedCoralTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("New Secure Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_password_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPasswordInput.length < 6) {
                            resetError = "Password must be at least 6 characters"
                        } else if (newPasswordInput != confirmPasswordInput) {
                            resetError = "Passwords do not match"
                        } else {
                            resetError = null
                            onResetPassword(newPasswordInput)
                        }
                    },
                    modifier = Modifier.testTag("submit_password_reset_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
                ) {
                    Text("Update & Proceed")
                }
            }
        )
    }
}
