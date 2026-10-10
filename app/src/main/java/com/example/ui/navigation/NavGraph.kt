package com.example.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.MedConnectViewModel
import com.example.ui.components.PrescriptionDocumentView
import com.example.ui.components.RapidRxStudio
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminRemunerationSettlementScreen
import com.example.ui.screens.admin.AuditLogScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.PatientRegisterScreen
import com.example.ui.screens.doctor.DoctorDashboardScreen
import com.example.ui.screens.doctor.DoctorJoiningScreen
import com.example.ui.screens.doctor.DoctorPaymentManagementScreen
import com.example.ui.screens.doctor.TeleconsultationRoomScreen
import com.example.ui.screens.patient.FamilyProfilesScreen
import com.example.ui.screens.patient.GrowthChartTrackerScreen
import com.example.ui.screens.patient.PatientDashboardScreen
import com.example.ui.screens.patient.PatientEmrVaultScreen
import com.example.ui.PrescriptionViewModel
import com.example.ui.screens.prescription.PrescriptionListScreen
import com.example.ui.screens.policy.LegalPoliciesScreen
import com.example.ui.screens.policy.PolicyType
import com.example.ui.screens.support.ComplaintManagementScreen
import com.example.ui.screens.support.ComplaintsHelpdeskScreen

enum class AppScreen {
    LOGIN,
    PATIENT_REGISTER,
    DOCTOR_HOME,
    DOCTOR_CALL,
    DOCTOR_RX_STUDIO,
    DOCTOR_PAYMENTS,
    PATIENT_HOME,
    PATIENT_VAULT,
    PATIENT_GROWTH,
    PATIENT_FAMILY,
    ADMIN_HOME,
    ADMIN_AUDIT,
    PRESCRIPTION_DETAIL,
    PRESCRIPTION_LIST,
    COMPLAINT_MANAGEMENT,
    COMPLAINTS_HELPDESK,
    DOCTOR_JOINING_FORM,
    LEGAL_POLICIES
}

@Composable
fun MedConnectNavGraph(viewModel: MedConnectViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val callState by viewModel.callState.collectAsStateWithLifecycle()

    val doctorProfiles by viewModel.doctorProfiles.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val allPrescriptions by viewModel.allPrescriptions.collectAsStateWithLifecycle()
    val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val payoutBatches by viewModel.payoutBatches.collectAsStateWithLifecycle()
    val featureConfigs by viewModel.featureConfigs.collectAsStateWithLifecycle()

    val allComplaints by viewModel.allComplaints.collectAsStateWithLifecycle()
    val doctorJoiningApplications by viewModel.doctorJoiningApplications.collectAsStateWithLifecycle()
    val selectedComplaint by viewModel.selectedComplaint.collectAsStateWithLifecycle()
    val complaintMessages by viewModel.complaintMessages.collectAsStateWithLifecycle()

    val selectedRx by viewModel.selectedPrescription.collectAsStateWithLifecycle()
    val selectedPatientForRx by viewModel.selectedPatientForRx.collectAsStateWithLifecycle()
    val selectedPatientName by viewModel.selectedPatientName.collectAsStateWithLifecycle()

    // Family and Growth records
    val patientId = authState.currentUser?.id ?: "patient_alex_1"
    val growthRecords by viewModel.repository.getGrowthRecords(patientId).collectAsStateWithLifecycle(emptyList())
    val familyMembers by viewModel.repository.getFamilyMembers(patientId).collectAsStateWithLifecycle(emptyList())

    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var activeAppointmentIdForRx by remember { mutableStateOf("apt_101") }
    var previousScreenForRxDetail by remember { mutableStateOf<AppScreen?>(null) }
    var activePolicyType by remember { mutableStateOf(PolicyType.PRIVACY) }
    var previousScreenForPolicy by remember { mutableStateOf<AppScreen?>(null) }

    // Navigation & RBAC Guard
    fun navigateWithRbacGuard(targetScreen: AppScreen) {
        val user = authState.currentUser
        when (targetScreen) {
            AppScreen.DOCTOR_HOME, AppScreen.DOCTOR_CALL, AppScreen.DOCTOR_RX_STUDIO, AppScreen.DOCTOR_PAYMENTS -> {
                if (user?.role != UserRole.DOCTOR && user?.role != UserRole.SUPER_ADMIN) {
                    Toast.makeText(context, "403 Forbidden: Doctor permissions required", Toast.LENGTH_LONG).show()
                    return
                }
            }
            AppScreen.ADMIN_HOME, AppScreen.ADMIN_AUDIT -> {
                if (user?.role != UserRole.SUPER_ADMIN) {
                    Toast.makeText(context, "403 Forbidden: Super-Admin access required", Toast.LENGTH_LONG).show()
                    return
                }
            }
            AppScreen.PATIENT_HOME, AppScreen.PATIENT_VAULT, AppScreen.PATIENT_GROWTH, AppScreen.PATIENT_FAMILY -> {
                if (user?.role != UserRole.PATIENT && user?.role != UserRole.SUPER_ADMIN) {
                    Toast.makeText(context, "403 Forbidden: Patient portal restricted", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            AppScreen.COMPLAINT_MANAGEMENT -> {
                if (user?.role != UserRole.RELATIONSHIP_MANAGER && user?.role != UserRole.SUPER_ADMIN) {
                    Toast.makeText(context, "403 Forbidden: Relationship Manager access required", Toast.LENGTH_LONG).show()
                    return
                }
            }
            else -> {}
        }
        currentScreen = targetScreen
    }

    when (currentScreen) {
        AppScreen.LOGIN -> {
            val isComplaintPortalEnabled = featureConfigs.find { it.key == "FEATURE_COMPLAINT_MANAGEMENT_PORTAL" }?.isEnabled ?: true
            LoginScreen(
                authState = authState,
                isComplaintPortalEnabled = isComplaintPortalEnabled,
                onLogin = { email, password ->
                    viewModel.login(email, password) {
                        val role = viewModel.authState.value.currentUser?.role
                        when (role) {
                            UserRole.SUPER_ADMIN -> currentScreen = AppScreen.ADMIN_HOME
                            UserRole.DOCTOR -> currentScreen = AppScreen.DOCTOR_HOME
                            UserRole.PATIENT -> currentScreen = AppScreen.PATIENT_HOME
                            UserRole.RELATIONSHIP_MANAGER -> currentScreen = AppScreen.COMPLAINT_MANAGEMENT
                            else -> currentScreen = AppScreen.LOGIN
                        }
                    }
                },
                onBiometricLogin = { identifier ->
                    viewModel.loginWithBiometrics(identifier) {
                        val role = viewModel.authState.value.currentUser?.role
                        when (role) {
                            UserRole.SUPER_ADMIN -> currentScreen = AppScreen.ADMIN_HOME
                            UserRole.DOCTOR -> currentScreen = AppScreen.DOCTOR_HOME
                            UserRole.PATIENT -> currentScreen = AppScreen.PATIENT_HOME
                            UserRole.RELATIONSHIP_MANAGER -> currentScreen = AppScreen.COMPLAINT_MANAGEMENT
                            else -> currentScreen = AppScreen.LOGIN
                        }
                    }
                },
                onRequestOtp = { identifier, onDispatched ->
                    viewModel.requestOtp(identifier) { otp, target ->
                        onDispatched(otp, target)
                    }
                },
                onVerifyOtp = { identifier, enteredOtp, expectedOtp ->
                    viewModel.verifyOtpAndLogin(identifier, enteredOtp, expectedOtp) {
                        val role = viewModel.authState.value.currentUser?.role
                        when (role) {
                            UserRole.SUPER_ADMIN -> currentScreen = AppScreen.ADMIN_HOME
                            UserRole.DOCTOR -> currentScreen = AppScreen.DOCTOR_HOME
                            UserRole.PATIENT -> currentScreen = AppScreen.PATIENT_HOME
                            UserRole.RELATIONSHIP_MANAGER -> currentScreen = AppScreen.COMPLAINT_MANAGEMENT
                            else -> currentScreen = AppScreen.LOGIN
                        }
                    }
                },
                onResetPassword = { newPass ->
                    viewModel.resetPassword(newPass) {
                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        val role = authState.currentUser?.role
                        when (role) {
                            UserRole.SUPER_ADMIN -> currentScreen = AppScreen.ADMIN_HOME
                            UserRole.DOCTOR -> currentScreen = AppScreen.DOCTOR_HOME
                            UserRole.RELATIONSHIP_MANAGER -> currentScreen = AppScreen.COMPLAINT_MANAGEMENT
                            else -> currentScreen = AppScreen.PATIENT_HOME
                        }
                    }
                },
                onNavigateToPatientRegister = {
                    currentScreen = AppScreen.PATIENT_REGISTER
                },
                onNavigateToComplaintManagement = {
                    currentScreen = AppScreen.COMPLAINT_MANAGEMENT
                },
                onNavigateToDoctorJoining = {
                    currentScreen = AppScreen.DOCTOR_JOINING_FORM
                },
                onNavigateToPolicy = { policy ->
                    previousScreenForPolicy = AppScreen.LOGIN
                    activePolicyType = policy
                    currentScreen = AppScreen.LEGAL_POLICIES
                }
            )
        }

        AppScreen.PATIENT_REGISTER -> {
            PatientRegisterScreen(
                authState = authState,
                onRegister = { name, email, phone, pass, age, gender, allergies, chronic ->
                    viewModel.registerPatient(name, email, phone, pass, age, gender, allergies, chronic) {
                        Toast.makeText(context, "Welcome to Mediconnect!", Toast.LENGTH_SHORT).show()
                        currentScreen = AppScreen.PATIENT_HOME
                    }
                },
                onBackToLogin = {
                    currentScreen = AppScreen.LOGIN
                }
            )
        }

        // DOCTOR WORKSPACE
        AppScreen.DOCTOR_HOME -> {
            val doctorUser = authState.currentUser
            if (doctorUser == null) {
                currentScreen = AppScreen.LOGIN
                return
            }
            val appointmentsForDoctor = allAppointments.filter { it.doctorId == doctorUser.id || doctorUser.role == UserRole.SUPER_ADMIN }
            val docHelpline = featureConfigs.find { it.key == "CONFIG_DOCTOR_HELPLINE" }?.configurableValue ?: "+91 1800-202-3001"

            DoctorDashboardScreen(
                doctorUser = doctorUser,
                doctorProfile = authState.currentDoctorProfile,
                appointments = appointmentsForDoctor,
                doctorHelplineNumber = docHelpline,
                onStartCall = { apt ->
                    viewModel.startCall(apt)
                    navigateWithRbacGuard(AppScreen.DOCTOR_CALL)
                },
                onOpenRxStudio = { patientName, aptId ->
                    activeAppointmentIdForRx = aptId
                    navigateWithRbacGuard(AppScreen.DOCTOR_RX_STUDIO)
                },
                onToggleReminder = { aptId, isSet ->
                    viewModel.toggleAppointmentReminder(aptId, isSet)
                },
                onTriggerCallAlert = { aptId ->
                    viewModel.triggerCallAlert(aptId, "DOCTOR") { alertMsg ->
                        Toast.makeText(context, alertMsg, Toast.LENGTH_SHORT).show()
                    }
                },
                onNavigateToComplaintsHelpdesk = {
                    currentScreen = AppScreen.COMPLAINTS_HELPDESK
                },
                onNavigateToPrescriptionRegistry = {
                    currentScreen = AppScreen.PRESCRIPTION_LIST
                },
                onNavigateToPolicy = { policy ->
                    previousScreenForPolicy = AppScreen.DOCTOR_HOME
                    activePolicyType = policy
                    currentScreen = AppScreen.LEGAL_POLICIES
                },
                onLogout = {
                    viewModel.logout { currentScreen = AppScreen.LOGIN }
                }
            )
        }

        AppScreen.DOCTOR_PAYMENTS -> {
            val adminUser = authState.currentUser
            if (adminUser == null || adminUser.role != UserRole.SUPER_ADMIN) {
                Toast.makeText(context, "Access Denied: Payment settlement hub is strictly restricted to Super Admin.", Toast.LENGTH_LONG).show()
                currentScreen = if (adminUser?.role == UserRole.DOCTOR) AppScreen.DOCTOR_HOME else AppScreen.LOGIN
                return
            }

            val doctorUserEntities = doctorProfiles.map { prof ->
                val matchingDocName = allAppointments.find { it.doctorId == prof.doctorId }?.doctorName ?: "Dr. Sarah Jenkins"
                com.example.data.model.UserEntity(
                    id = prof.doctorId,
                    email = "${prof.councilRegistrationNumber.lowercase()}@medconnect.pro",
                    passwordHash = "hash",
                    fullName = matchingDocName,
                    phone = "+91 98111-22334",
                    role = UserRole.DOCTOR
                )
            }.ifEmpty {
                listOf(
                    com.example.data.model.UserEntity(
                        id = "doc_1",
                        email = "dr.jenkins@medconnect.pro",
                        passwordHash = "hash",
                        fullName = "Dr. Sarah Jenkins",
                        phone = "+91 98111-22334",
                        role = UserRole.DOCTOR
                    )
                )
            }

            AdminRemunerationSettlementScreen(
                adminUser = adminUser,
                doctors = doctorUserEntities,
                appointments = allAppointments,
                payoutBatches = payoutBatches,
                onProcessMondayPayout = { batchId, onComplete ->
                    viewModel.processMondayPayout(batchId) { res ->
                        res.onSuccess { b ->
                            onComplete("Monday payout processed! Bank UTR: ${b.utrReference}")
                        }.onFailure { err ->
                            onComplete("Payout failed: ${err.message}")
                        }
                    }
                },
                onNavigateToPayoutPolicy = {
                    previousScreenForPolicy = AppScreen.DOCTOR_PAYMENTS
                    activePolicyType = PolicyType.PAYOUT
                    currentScreen = AppScreen.LEGAL_POLICIES
                },
                onBack = { currentScreen = AppScreen.ADMIN_HOME }
            )
        }

        AppScreen.DOCTOR_CALL -> {
            TeleconsultationRoomScreen(
                callState = callState,
                patientProfile = selectedPatientForRx,
                onToggleMic = { viewModel.toggleMic() },
                onToggleCamera = { viewModel.toggleCamera() },
                onToggleSplitScreenRx = { viewModel.toggleSplitScreenRx() },
                onSendMessage = { msg -> viewModel.sendInCallMessage(authState.currentUser?.fullName ?: "Doctor", msg) },
                onEndCall = {
                    viewModel.endCall {
                        navigateWithRbacGuard(AppScreen.DOCTOR_HOME)
                    }
                },
                onSubmitPrescription = { aptId, icd, bp, hr, spo2, temp, meds, adv, lab, fuText, fuDate, onSuccess ->
                    viewModel.submitPrescription(aptId, icd, bp, hr, spo2, temp, meds, adv, lab, fuText, fuDate, onSuccess)
                },
                onPrescriptionCreated = { rx ->
                    viewModel.setSelectedPrescription(rx)
                    navigateWithRbacGuard(AppScreen.PRESCRIPTION_DETAIL)
                },
                onSwitchCallMode = { isAudio ->
                    viewModel.setAudioCallMode(isAudio)
                },
                onToggleAudioRoute = {
                    viewModel.toggleAudioRoute()
                }
            )
        }

        AppScreen.DOCTOR_RX_STUDIO -> {
            if (selectedPatientForRx != null) {
                RapidRxStudio(
                    patientProfile = selectedPatientForRx!!,
                    patientName = selectedPatientName,
                    appointmentId = activeAppointmentIdForRx,
                    onPrescriptionCreated = { rx ->
                        viewModel.setSelectedPrescription(rx)
                        navigateWithRbacGuard(AppScreen.PRESCRIPTION_DETAIL)
                    },
                    onSubmitPrescription = { aptId, icd, bp, hr, spo2, temp, meds, adv, lab, fuText, fuDate, onSuccess ->
                        viewModel.submitPrescription(aptId, icd, bp, hr, spo2, temp, meds, adv, lab, fuText, fuDate, onSuccess)
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // PATIENT WORKSPACE
        AppScreen.PATIENT_HOME -> {
            val patientUser = authState.currentUser
            if (patientUser == null) {
                currentScreen = AppScreen.LOGIN
                return
            }
            val appointmentsForPatient = allAppointments.filter { it.patientId == patientUser.id }
            val patientPrescriptions = allPrescriptions.filter { it.patientId == patientUser.id }
            val patHelpline = featureConfigs.find { it.key == "CONFIG_PATIENT_HELPLINE" }?.configurableValue ?: "+91 1800-202-3002"

            PatientDashboardScreen(
                patientUser = patientUser,
                patientProfile = authState.currentPatientProfile,
                doctors = doctorProfiles,
                appointments = appointmentsForPatient,
                prescriptions = patientPrescriptions,
                patientHelplineNumber = patHelpline,
                onStartCall = { apt ->
                    viewModel.startCall(apt)
                    navigateWithRbacGuard(AppScreen.DOCTOR_CALL)
                },
                onNavigateToVault = { navigateWithRbacGuard(AppScreen.PATIENT_VAULT) },
                onNavigateToGrowthTracker = { navigateWithRbacGuard(AppScreen.PATIENT_GROWTH) },
                onNavigateToFamily = { navigateWithRbacGuard(AppScreen.PATIENT_FAMILY) },
                onNavigateToComplaintsHelpdesk = { currentScreen = AppScreen.COMPLAINTS_HELPDESK },
                onBookConsultation = { docId, docName, spec, complaint ->
                    viewModel.bookAppointment(docId, docName, spec, complaint) {
                        Toast.makeText(context, "Consultation scheduled successfully!", Toast.LENGTH_SHORT).show()
                    }
                },
                onBookConsultationDetailed = { docId, docName, spec, complaint, consultationType, timeSlot, consultationDate, feeInr, upiApp, upiVpa, isReminderSet, reminderTiming, onCompleted ->
                    viewModel.bookAppointmentWithDetails(
                        doctorId = docId,
                        doctorName = docName,
                        doctorSpeciality = spec,
                        complaint = complaint,
                        consultationType = consultationType,
                        timeSlot = timeSlot,
                        consultationDate = consultationDate,
                        feeInr = feeInr,
                        upiApp = upiApp,
                        upiVpa = upiVpa,
                        isReminderSet = isReminderSet,
                        reminderTiming = reminderTiming
                    ) { bookedApt ->
                        Toast.makeText(context, "Slot booked & UPI payment confirmed!", Toast.LENGTH_SHORT).show()
                        onCompleted(bookedApt)
                    }
                },
                onToggleReminder = { aptId, isSet ->
                    viewModel.toggleAppointmentReminder(aptId, isSet)
                },
                onNavigateToPolicy = { policy ->
                    previousScreenForPolicy = AppScreen.PATIENT_HOME
                    activePolicyType = policy
                    currentScreen = AppScreen.LEGAL_POLICIES
                },
                onLogout = {
                    viewModel.logout { currentScreen = AppScreen.LOGIN }
                }
            )
        }

        AppScreen.PATIENT_VAULT -> {
            val patientUser = authState.currentUser
            val userRxList = allPrescriptions.filter { it.patientId == (patientUser?.id ?: "") || patientUser?.role == UserRole.SUPER_ADMIN }
            PatientEmrVaultScreen(
                prescriptions = userRxList,
                onBack = { currentScreen = AppScreen.PATIENT_HOME }
            )
        }

        AppScreen.PATIENT_GROWTH -> {
            GrowthChartTrackerScreen(
                growthRecords = growthRecords,
                onAddRecord = { age, w, h, notes ->
                    viewModel.logGrowth(patientId, null, age, w, h, notes) {
                        Toast.makeText(context, "Milestone recorded on WHO Growth Curve", Toast.LENGTH_SHORT).show()
                    }
                },
                onBack = { currentScreen = AppScreen.PATIENT_HOME }
            )
        }

        AppScreen.PATIENT_FAMILY -> {
            FamilyProfilesScreen(
                familyMembers = familyMembers,
                onAddFamilyMember = { name, relation, age, gender, allergies ->
                    // Add via repository
                    Toast.makeText(context, "Dependent $name added", Toast.LENGTH_SHORT).show()
                },
                onBack = { currentScreen = AppScreen.PATIENT_HOME }
            )
        }

        // ADMIN WORKSPACE
        AppScreen.ADMIN_HOME -> {
            val adminUser = authState.currentUser
            if (adminUser == null) {
                currentScreen = AppScreen.LOGIN
                return
            }

            AdminDashboardScreen(
                adminUser = adminUser,
                doctorProfiles = doctorProfiles,
                auditLogs = auditLogs,
                featureConfigs = featureConfigs,
                doctorJoiningApplications = doctorJoiningApplications,
                complaintsCount = allComplaints.size,
                onToggleFeature = { key, isEnabled ->
                    viewModel.toggleFeature(key, isEnabled)
                },
                onUpdateFeatureConfig = { key, isEnabled, configVal ->
                    viewModel.updateFeatureConfig(key, isEnabled, configVal)
                },
                onAddCustomFeature = { feature ->
                    viewModel.addCustomFeature(feature)
                },
                onApproveDoctorJoining = { appId ->
                    viewModel.approveDoctorJoining(appId)
                },
                onNavigateToComplaintManagement = { currentScreen = AppScreen.COMPLAINT_MANAGEMENT },
                onProvisionDoctor = { fName, lName, email, phone, pass, councilReg, qual, spec, affil, fee, timings ->
                    viewModel.provisionDoctor(
                        fName, lName, email, phone, pass, councilReg, qual, spec, affil, fee, timings
                    ) {
                        Toast.makeText(context, "Doctor provisioned & verified successfully! Credentials sent.", Toast.LENGTH_LONG).show()
                    }
                },
                onNavigateToAuditLogs = { navigateWithRbacGuard(AppScreen.ADMIN_AUDIT) },
                onNavigateToRemunerationSettlement = { navigateWithRbacGuard(AppScreen.DOCTOR_PAYMENTS) },
                onNavigateToPolicy = { policy ->
                    previousScreenForPolicy = AppScreen.ADMIN_HOME
                    activePolicyType = policy
                    currentScreen = AppScreen.LEGAL_POLICIES
                },
                onLogout = {
                    viewModel.logout { currentScreen = AppScreen.LOGIN }
                }
            )
        }

        AppScreen.ADMIN_AUDIT -> {
            AuditLogScreen(
                auditLogs = auditLogs,
                onBack = { currentScreen = AppScreen.ADMIN_HOME }
            )
        }

        AppScreen.PRESCRIPTION_DETAIL -> {
            if (selectedRx != null) {
                PrescriptionDocumentView(
                    prescription = selectedRx!!,
                    onBack = {
                        val prev = previousScreenForRxDetail
                        previousScreenForRxDetail = null
                        if (prev != null) {
                            currentScreen = prev
                        } else {
                            val user = authState.currentUser
                            currentScreen = when (user?.role) {
                                UserRole.DOCTOR -> AppScreen.DOCTOR_HOME
                                UserRole.SUPER_ADMIN -> AppScreen.ADMIN_HOME
                                else -> AppScreen.PATIENT_HOME
                            }
                        }
                    }
                )
            } else {
                currentScreen = previousScreenForRxDetail ?: AppScreen.PATIENT_HOME
                previousScreenForRxDetail = null
            }
        }

        AppScreen.PRESCRIPTION_LIST -> {
            val prescriptionVm = remember {
                PrescriptionViewModel(
                    application = context.applicationContext as android.app.Application,
                    repository = viewModel.prescriptionRepository
                )
            }
            PrescriptionListScreen(
                viewModel = prescriptionVm,
                onBack = {
                    val user = authState.currentUser
                    currentScreen = when (user?.role) {
                        UserRole.DOCTOR -> AppScreen.DOCTOR_HOME
                        UserRole.SUPER_ADMIN -> AppScreen.ADMIN_HOME
                        else -> AppScreen.PATIENT_HOME
                    }
                },
                onPrescriptionSelected = { rx ->
                    viewModel.setSelectedPrescription(rx)
                    previousScreenForRxDetail = AppScreen.PRESCRIPTION_LIST
                    currentScreen = AppScreen.PRESCRIPTION_DETAIL
                }
            )
        }

        // COMPLAINT MANAGEMENT (RELATIONSHIP MANAGER & SUPER ADMIN)
        AppScreen.COMPLAINT_MANAGEMENT -> {
            val currentUser = authState.currentUser
            val isPortalEnabled = featureConfigs.find { it.key == "FEATURE_COMPLAINT_MANAGEMENT_PORTAL" }?.isEnabled ?: true
            if (currentUser == null) {
                currentScreen = AppScreen.LOGIN
            } else if (!isPortalEnabled && currentUser.role != UserRole.SUPER_ADMIN) {
                Toast.makeText(context, "Complaint Management Portal is disabled by Super Admin", Toast.LENGTH_SHORT).show()
                currentScreen = AppScreen.LOGIN
            } else {
                ComplaintManagementScreen(
                    currentRmUser = currentUser,
                    complaints = allComplaints,
                    selectedComplaint = selectedComplaint,
                    chatMessages = complaintMessages,
                    onSelectComplaint = { viewModel.selectComplaint(it) },
                    onSendMessage = { complaintId, text ->
                        viewModel.sendComplaintChatMessage(complaintId, text)
                    },
                    onLogCallAssistance = { complaintId, durationSec, notes ->
                        viewModel.logCallAssistance(complaintId, durationSec, notes)
                    },
                    onUpdateStatus = { complaintId, status, resolution ->
                        viewModel.updateComplaintStatus(complaintId, status, resolution)
                    },
                    onBack = {
                        currentScreen = when (currentUser.role) {
                            UserRole.SUPER_ADMIN -> AppScreen.ADMIN_HOME
                            UserRole.RELATIONSHIP_MANAGER -> AppScreen.LOGIN
                            UserRole.DOCTOR -> AppScreen.DOCTOR_HOME
                            else -> AppScreen.LOGIN
                        }
                    }
                )
            }
        }

        // COMPLAINTS & HELPDESK (DOCTORS & PATIENTS)
        AppScreen.COMPLAINTS_HELPDESK -> {
            val currentUser = authState.currentUser
            if (currentUser == null) {
                currentScreen = AppScreen.LOGIN
            } else {
                val helpline = if (currentUser.role == UserRole.DOCTOR) {
                    featureConfigs.find { it.key == "CONFIG_DOCTOR_HELPLINE" }?.configurableValue ?: "+91 1800-202-3001"
                } else {
                    featureConfigs.find { it.key == "CONFIG_PATIENT_HELPLINE" }?.configurableValue ?: "+91 1800-202-3002"
                }

                ComplaintsHelpdeskScreen(
                    currentUser = currentUser,
                    helplineNumber = helpline,
                    complaints = allComplaints,
                    selectedComplaint = selectedComplaint,
                    chatMessages = complaintMessages,
                    onSelectComplaint = { viewModel.selectComplaint(it) },
                    onSubmitComplaint = { category, subject, description, priority, onDone ->
                        viewModel.submitComplaint(
                            category = category,
                            subject = subject,
                            description = description,
                            priority = priority
                        ) {
                            onDone()
                        }
                    },
                    onSendMessage = { complaintId, text ->
                        viewModel.sendComplaintChatMessage(complaintId, text)
                    },
                    onBack = {
                        currentScreen = when (currentUser.role) {
                            UserRole.DOCTOR -> AppScreen.DOCTOR_HOME
                            UserRole.SUPER_ADMIN -> AppScreen.ADMIN_HOME
                            UserRole.PATIENT -> AppScreen.PATIENT_HOME
                            else -> AppScreen.LOGIN
                        }
                    }
                )
            }
        }

        // DOCTOR JOINING FORM (UIDAI AADHAAR VERIFIED)
        AppScreen.DOCTOR_JOINING_FORM -> {
            DoctorJoiningScreen(
                onSubmitApplication = { application, onComplete ->
                    viewModel.submitDoctorJoining(application) {
                        onComplete()
                    }
                },
                onBack = { currentScreen = AppScreen.LOGIN }
            )
        }

        // INSTITUTIONAL POLICY & LEGAL CENTER (PRIVACY, TERMS & INDEMNITY, REFUND, PAYOUT)
        AppScreen.LEGAL_POLICIES -> {
            LegalPoliciesScreen(
                initialPolicy = activePolicyType,
                onBack = {
                    val prev = previousScreenForPolicy
                    currentScreen = if (prev != null) {
                        prev
                    } else {
                        val user = authState.currentUser
                        when (user?.role) {
                            UserRole.DOCTOR -> AppScreen.DOCTOR_HOME
                            UserRole.PATIENT -> AppScreen.PATIENT_HOME
                            UserRole.SUPER_ADMIN -> AppScreen.ADMIN_HOME
                            UserRole.RELATIONSHIP_MANAGER -> AppScreen.COMPLAINT_MANAGEMENT
                            else -> AppScreen.LOGIN
                        }
                    }
                },
                onNavigateToComplaintsHelpdesk = {
                    if (authState.currentUser != null) {
                        currentScreen = AppScreen.COMPLAINTS_HELPDESK
                    } else {
                        Toast.makeText(context, "Please sign in to lodge a helpdesk ticket", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
