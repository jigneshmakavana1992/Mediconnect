package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MedConnectDatabase
import com.example.data.model.*
import com.example.data.repository.MedConnectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val currentDoctorProfile: DoctorProfileEntity? = null,
    val currentPatientProfile: PatientProfileEntity? = null,
    val isAuthenticating: Boolean = false,
    val errorMessage: String? = null,
    val showPasswordResetDialog: Boolean = false,
    val securityNotice: String? = null
)

data class CallState(
    val activeAppointment: AppointmentEntity? = null,
    val isMicMuted: Boolean = false,
    val isCameraOff: Boolean = false,
    val isAudioCallMode: Boolean = false, // True for audio-only consultation
    val audioOutputRoute: String = "Speakerphone", // "Speakerphone", "Earpiece", "Bluetooth Headset"
    val callDurationSeconds: Int = 0,
    val inCallChat: List<Pair<String, String>> = emptyList(), // Sender, Message
    val isSplitScreenRxOpen: Boolean = false
)

class MedConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MedConnectDatabase.getDatabase(application, viewModelScope)
    val prescriptionDao = db.prescriptionDao()
    val prescriptionRepository = com.example.data.repository.PrescriptionRepository(prescriptionDao)
    val repository = MedConnectRepository(db.dao(), prescriptionDao)

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _callState = MutableStateFlow(CallState())
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    // Global Collections
    val doctorProfiles: StateFlow<List<DoctorProfileEntity>> = repository.getAllDoctorProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPrescriptions: StateFlow<List<PrescriptionEntity>> = repository.getAllPrescriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<AppointmentEntity>> = repository.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payoutBatches: StateFlow<List<DoctorPayoutBatchEntity>> = repository.getAllDoctorPayoutBatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featureConfigs: StateFlow<List<AppFeatureConfigEntity>> = repository.getAllFeatureConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Complaints & Relationship Manager Assistance
    val allComplaints: StateFlow<List<ComplaintEntity>> = repository.getAllComplaints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctorJoiningApplications: StateFlow<List<DoctorJoiningApplicationEntity>> = repository.getAllDoctorJoiningApplications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedComplaint = MutableStateFlow<ComplaintEntity?>(null)
    val selectedComplaint: StateFlow<ComplaintEntity?> = _selectedComplaint.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val complaintMessages: StateFlow<List<ComplaintChatMessageEntity>> = _selectedComplaint
        .flatMapLatest { complaint ->
            if (complaint == null) flowOf(emptyList())
            else repository.getMessagesForComplaint(complaint.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Patient Context for Prescribing & EMR
    private val _selectedPatientForRx = MutableStateFlow<PatientProfileEntity?>(null)
    val selectedPatientForRx: StateFlow<PatientProfileEntity?> = _selectedPatientForRx.asStateFlow()

    private val _selectedPatientName = MutableStateFlow("Alexander Wright")
    val selectedPatientName: StateFlow<String> = _selectedPatientName.asStateFlow()

    // Active Prescription being viewed (e.g. In Vault or Print preview)
    private val _selectedPrescription = MutableStateFlow<PrescriptionEntity?>(null)
    val selectedPrescription: StateFlow<PrescriptionEntity?> = _selectedPrescription.asStateFlow()

    init {
        // Automatically check database readiness
        viewModelScope.launch {
            // Check default profiles
            val doc = repository.getDoctorProfile("doc_jenkins_1")
            val pat = repository.getPatientProfile("patient_alex_1")
            if (pat != null) {
                _selectedPatientForRx.value = pat
            }
        }
    }

    // RBAC Permission Checkers
    fun canWritePrescription(): Boolean {
        return _authState.value.currentUser?.role == UserRole.DOCTOR
    }

    fun canProvisionDoctor(): Boolean {
        return _authState.value.currentUser?.role == UserRole.SUPER_ADMIN
    }

    fun isPatient(): Boolean {
        return _authState.value.currentUser?.role == UserRole.PATIENT
    }

    fun isAdmin(): Boolean {
        return _authState.value.currentUser?.role == UserRole.SUPER_ADMIN
    }

    // Authentication Actions
    fun login(email: String, passwordAttempt: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            val result = repository.login(email, passwordAttempt)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                var docProfile: DoctorProfileEntity? = null
                var patProfile: PatientProfileEntity? = null

                if (user.role == UserRole.DOCTOR) {
                    docProfile = repository.getDoctorProfile(user.id)
                } else if (user.role == UserRole.PATIENT) {
                    patProfile = repository.getPatientProfile(user.id)
                    _selectedPatientForRx.value = patProfile
                    _selectedPatientName.value = user.fullName
                }

                _authState.update {
                    it.copy(
                        currentUser = user,
                        currentDoctorProfile = docProfile,
                        currentPatientProfile = patProfile,
                        isAuthenticating = false,
                        showPasswordResetDialog = user.requiresPasswordReset,
                        securityNotice = if (user.requiresPasswordReset) {
                            "FIRST LOGIN SECURITY: You are required to reset your temporary password immediately."
                        } else null
                    )
                }
                onSuccess()
            } else {
                _authState.update {
                    it.copy(
                        isAuthenticating = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
                    )
                }
            }
        }
    }

    fun loginWithBiometrics(identifier: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            val result = repository.loginWithBiometrics(identifier)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                var docProfile: DoctorProfileEntity? = null
                var patProfile: PatientProfileEntity? = null

                if (user.role == UserRole.DOCTOR) {
                    docProfile = repository.getDoctorProfile(user.id)
                } else if (user.role == UserRole.PATIENT) {
                    patProfile = repository.getPatientProfile(user.id)
                    _selectedPatientForRx.value = patProfile
                    _selectedPatientName.value = user.fullName
                }

                _authState.update {
                    it.copy(
                        currentUser = user,
                        currentDoctorProfile = docProfile,
                        currentPatientProfile = patProfile,
                        isAuthenticating = false,
                        showPasswordResetDialog = user.requiresPasswordReset
                    )
                }
                onSuccess()
            } else {
                _authState.update {
                    it.copy(
                        isAuthenticating = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Biometric login failed"
                    )
                }
            }
        }
    }

    fun requestOtp(identifier: String, onOtpDispatched: (otp: String, target: String) -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            val result = repository.generateAndSendOtp(identifier)
            _authState.update { it.copy(isAuthenticating = false) }
            if (result.isSuccess) {
                val (user, otp) = result.getOrThrow()
                val target = user.phone.ifEmpty { user.email }
                onOtpDispatched(otp, target)
            } else {
                _authState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to generate OTP")
                }
            }
        }
    }

    fun verifyOtpAndLogin(identifier: String, enteredOtp: String, expectedOtp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            val result = repository.verifyOtpAndLogin(identifier, enteredOtp, expectedOtp)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                var docProfile: DoctorProfileEntity? = null
                var patProfile: PatientProfileEntity? = null

                if (user.role == UserRole.DOCTOR) {
                    docProfile = repository.getDoctorProfile(user.id)
                } else if (user.role == UserRole.PATIENT) {
                    patProfile = repository.getPatientProfile(user.id)
                    _selectedPatientForRx.value = patProfile
                    _selectedPatientName.value = user.fullName
                }

                _authState.update {
                    it.copy(
                        currentUser = user,
                        currentDoctorProfile = docProfile,
                        currentPatientProfile = patProfile,
                        isAuthenticating = false,
                        showPasswordResetDialog = user.requiresPasswordReset
                    )
                }
                onSuccess()
            } else {
                _authState.update {
                    it.copy(
                        isAuthenticating = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "OTP verification failed"
                    )
                }
            }
        }
    }

    fun resetPassword(newPassword: String, onComplete: () -> Unit) {
        val user = _authState.value.currentUser ?: return
        viewModelScope.launch {
            val result = repository.updatePassword(user.id, newPassword)
            if (result.isSuccess) {
                _authState.update {
                    it.copy(
                        showPasswordResetDialog = false,
                        securityNotice = null,
                        currentUser = user.copy(requiresPasswordReset = false)
                    )
                }
                onComplete()
            } else {
                _authState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message)
                }
            }
        }
    }

    fun registerPatient(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        age: Int,
        gender: String,
        allergies: String,
        chronicConditions: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            val result = repository.registerPatient(
                fullName, email, phone, password, age, gender, allergies, chronicConditions
            )
            if (result.isSuccess) {
                val user = result.getOrThrow()
                val profile = repository.getPatientProfile(user.id)
                _authState.update {
                    it.copy(
                        currentUser = user,
                        currentPatientProfile = profile,
                        isAuthenticating = false
                    )
                }
                onSuccess()
            } else {
                _authState.update {
                    it.copy(
                        isAuthenticating = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        _authState.value = AuthUiState()
        _callState.value = CallState()
        onLoggedOut()
    }

    // Doctor Provisioning
    fun provisionDoctor(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        temporaryPassword: String,
        councilRegNumber: String,
        qualifications: String,
        speciality: String,
        clinicAffiliation: String,
        consultationFee: Double,
        consultationTimings: String,
        onSuccess: () -> Unit
    ) {
        val admin = _authState.value.currentUser
        if (admin?.role != UserRole.SUPER_ADMIN) {
            _authState.update { it.copy(errorMessage = "Unauthorized: Only Super-Admin can provision doctors") }
            return
        }

        viewModelScope.launch {
            val result = repository.provisionDoctor(
                adminEmail = admin.email,
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                temporaryPassword = temporaryPassword,
                councilRegNumber = councilRegNumber,
                qualifications = qualifications,
                speciality = speciality,
                clinicAffiliation = clinicAffiliation,
                consultationFee = consultationFee,
                consultationTimings = consultationTimings,
                signatureLabel = "Dr. $firstName $lastName [NMC-$councilRegNumber]"
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                _authState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    // Prescription Studio
    fun selectPatientForRx(profile: PatientProfileEntity, name: String) {
        _selectedPatientForRx.value = profile
        _selectedPatientName.value = name
    }

    fun submitPrescription(
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
    ) {
        val doctorUser = _authState.value.currentUser
        val doctorProfile = _authState.value.currentDoctorProfile
        val patientProfile = _selectedPatientForRx.value
        val patientName = _selectedPatientName.value

        if (doctorUser == null || doctorProfile == null || patientProfile == null) {
            _authState.update { it.copy(errorMessage = "Missing doctor or patient context for prescription") }
            return
        }

        // Convert medications to simple JSON
        val medJsonBuilder = StringBuilder("[")
        medications.forEachIndexed { i, m ->
            medJsonBuilder.append("""{"id":"${m.id}","name":"${m.name}","dosageForm":"${m.dosageForm}","dosageSchedule":"${m.dosageSchedule}","timing":"${m.timing}","durationDays":${m.durationDays},"totalQuantity":${m.totalQuantity},"instructions":"${m.instructions}"}""")
            if (i < medications.size - 1) medJsonBuilder.append(",")
        }
        medJsonBuilder.append("]")

        viewModelScope.launch {
            val result = repository.issuePrescription(
                appointmentId = appointmentId,
                doctor = doctorUser,
                doctorProfile = doctorProfile,
                patientProfile = patientProfile,
                patientName = patientName,
                diagnosisICD10 = diagnosisICD10,
                vitalsBp = vitalsBp,
                vitalsPulse = vitalsPulse,
                vitalsSpO2 = vitalsSpO2,
                vitalsTemp = vitalsTemp,
                medicationsJson = medJsonBuilder.toString(),
                clinicalAdvice = clinicalAdvice,
                labOrders = labOrders,
                followUpText = followUpText,
                followUpDate = followUpDate
            )

            if (result.isSuccess) {
                val rx = result.getOrThrow()
                _selectedPrescription.value = rx
                onSuccess(rx)
            } else {
                _authState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun setSelectedPrescription(rx: PrescriptionEntity?) {
        _selectedPrescription.value = rx
    }

    // Teleconsultation Room Actions
    fun startCall(appointment: AppointmentEntity) {
        val isAudio = appointment.consultationType.contains("Audio", ignoreCase = true)
        _callState.value = CallState(
            activeAppointment = appointment,
            isMicMuted = false,
            isCameraOff = isAudio,
            isAudioCallMode = isAudio,
            audioOutputRoute = "Speakerphone",
            callDurationSeconds = 0,
            inCallChat = listOf(
                "System" to "Secure WebRTC consultation channel initialized with 256-bit encryption (${if (isAudio) "High-Definition 48kHz Opus Audio" else "1080p HD Video"}).",
                "Dr. Sarah Jenkins" to "Hello Alexander! I have Leo's clinical chart open. How is his temperature today?"
            ),
            isSplitScreenRxOpen = false
        )
    }

    fun switchCallMode(toAudioOnly: Boolean) {
        _callState.update {
            it.copy(
                isAudioCallMode = toAudioOnly,
                isCameraOff = if (toAudioOnly) true else it.isCameraOff
            )
        }
    }

    fun toggleAudioRoute() {
        _callState.update {
            val nextRoute = when (it.audioOutputRoute) {
                "Speakerphone" -> "Earpiece"
                "Earpiece" -> "Bluetooth Headset"
                else -> "Speakerphone"
            }
            it.copy(audioOutputRoute = nextRoute)
        }
    }

    fun toggleMic() {
        _callState.update { it.copy(isMicMuted = !it.isMicMuted) }
    }

    fun toggleCamera() {
        _callState.update { it.copy(isCameraOff = !it.isCameraOff) }
    }

    fun toggleSplitScreenRx() {
        _callState.update { it.copy(isSplitScreenRxOpen = !it.isSplitScreenRxOpen) }
    }

    fun sendInCallMessage(sender: String, message: String) {
        if (message.isBlank()) return
        _callState.update {
            it.copy(inCallChat = it.inCallChat + (sender to message.trim()))
        }
    }

    fun endCall(onEnd: () -> Unit) {
        _callState.value = CallState()
        onEnd()
    }

    fun setAudioCallMode(isAudio: Boolean) {
        switchCallMode(isAudio)
    }

    // Call Reminders & Alerts
    fun toggleAppointmentReminder(appointmentId: String, isReminderSet: Boolean? = null, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.toggleAppointmentReminder(appointmentId, isReminderSet)
            if (res.isSuccess) {
                onComplete(res.getOrDefault(true))
            }
        }
    }

    fun toggleReminder(appointmentId: String, onComplete: (Boolean) -> Unit = {}) {
        toggleAppointmentReminder(appointmentId, null, onComplete)
    }

    fun triggerCallAlert(appointmentId: String, senderRole: String = "DOCTOR", onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.triggerCallAlert(appointmentId, senderRole)
            if (res.isSuccess) {
                onResult(res.getOrThrow())
            } else {
                onResult("Failed to send alert: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    // Growth Chart Tracker
    fun logGrowth(
        patientId: String,
        familyMemberId: String?,
        ageMonths: Int,
        weight: Float,
        height: Float,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.logGrowthRecord(patientId, familyMemberId, ageMonths, weight, height, notes)
            onSuccess()
        }
    }

    // Book Appointment with full Slot, INR UPI Payment, Call Mode & Reminder
    fun bookAppointmentWithDetails(
        doctorId: String,
        doctorName: String,
        doctorSpeciality: String,
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
    ) {
        val user = _authState.value.currentUser ?: return
        viewModelScope.launch {
            val res = repository.bookAppointment(
                doctorId = doctorId,
                doctorName = doctorName,
                doctorSpeciality = doctorSpeciality,
                patientId = user.id,
                patientName = user.fullName,
                appointmentTime = System.currentTimeMillis() + (2L * 3600 * 1000),
                chiefComplaint = complaint,
                consultationType = consultationType,
                timeSlot = timeSlot,
                consultationDate = consultationDate,
                feeInr = feeInr,
                upiApp = upiApp,
                upiVpa = upiVpa,
                isReminderSet = isReminderSet,
                reminderTiming = reminderTiming
            )
            if (res.isSuccess) {
                onSuccess(res.getOrThrow())
            }
        }
    }

    // Backward-compatible bookAppointment
    fun bookAppointment(
        doctorId: String,
        doctorName: String,
        doctorSpeciality: String,
        complaint: String,
        onSuccess: () -> Unit
    ) {
        val user = _authState.value.currentUser ?: return
        viewModelScope.launch {
            repository.bookAppointment(
                doctorId = doctorId,
                doctorName = doctorName,
                doctorSpeciality = doctorSpeciality,
                patientId = user.id,
                patientName = user.fullName,
                appointmentTime = System.currentTimeMillis() + (2L * 3600 * 1000),
                chiefComplaint = complaint
            )
            onSuccess()
        }
    }

    // Doctor Weekly Payment & Settlement (Every Monday after 1-month holding)
    fun processMondayPayout(batchId: String, onResult: (Result<DoctorPayoutBatchEntity>) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.processMondayDisbursement(batchId)
            onResult(result)
        }
    }

    fun updateDoctorBankDetails(
        doctorId: String,
        bankName: String,
        accountNum: String,
        ifsc: String,
        upiVpa: String,
        pan: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.updateDoctorBankDetails(
                doctorId = doctorId,
                bankName = bankName,
                accountNum = accountNum,
                ifsc = ifsc,
                upiVpa = upiVpa,
                pan = pan
            )
            onResult(result.getOrDefault(false))
        }
    }

    // Super Edit: App Feature Flags Control Center (Super Admin)
    fun toggleFeature(key: String, isEnabled: Boolean, onComplete: (Result<AppFeatureConfigEntity>) -> Unit = {}) {
        val adminEmail = _authState.value.currentUser?.email ?: "jigneshmakavana1992@gmail.com"
        val existing = featureConfigs.value.find { it.key == key }
        val value = existing?.configurableValue ?: ""
        viewModelScope.launch {
            val result = repository.updateFeatureConfig(adminEmail, key, isEnabled, value)
            onComplete(result)
        }
    }

    fun updateFeatureConfig(
        key: String,
        isEnabled: Boolean,
        configurableValue: String,
        onComplete: (Result<AppFeatureConfigEntity>) -> Unit = {}
    ) {
        val adminEmail = _authState.value.currentUser?.email ?: "jigneshmakavana1992@gmail.com"
        viewModelScope.launch {
            val result = repository.updateFeatureConfig(adminEmail, key, isEnabled, configurableValue)
            onComplete(result)
        }
    }

    fun addCustomFeature(feature: AppFeatureConfigEntity, onComplete: (Result<Boolean>) -> Unit = {}) {
        val adminEmail = _authState.value.currentUser?.email ?: "jigneshmakavana1992@gmail.com"
        viewModelScope.launch {
            val result = repository.insertCustomFeature(adminEmail, feature)
            onComplete(result)
        }
    }

    // Complaints & RM Management
    fun selectComplaint(complaint: ComplaintEntity?) {
        _selectedComplaint.value = complaint
    }

    fun submitComplaint(
        category: String,
        subject: String,
        description: String,
        priority: String = "NORMAL",
        onComplete: (Result<ComplaintEntity>) -> Unit = {}
    ) {
        val user = _authState.value.currentUser
        if (user == null) {
            onComplete(Result.failure(Exception("You must be logged in to submit a complaint")))
            return
        }
        viewModelScope.launch {
            val result = repository.submitComplaint(user, category, subject, description, priority)
            onComplete(result)
        }
    }

    fun sendComplaintChatMessage(
        complaintId: String,
        text: String,
        onComplete: (Result<ComplaintChatMessageEntity>) -> Unit = {}
    ) {
        val user = _authState.value.currentUser
        if (user == null || text.isBlank()) return
        viewModelScope.launch {
            val result = repository.sendComplaintChatMessage(complaintId, user, text)
            onComplete(result)
        }
    }

    fun logCallAssistance(
        complaintId: String,
        callDurationSec: Int,
        callNotes: String,
        onComplete: (Result<ComplaintEntity>) -> Unit = {}
    ) {
        val user = _authState.value.currentUser
        if (user == null) return
        viewModelScope.launch {
            val result = repository.logCallAssistanceSession(complaintId, user, callDurationSec, callNotes)
            result.onSuccess { updated ->
                if (_selectedComplaint.value?.id == complaintId) {
                    _selectedComplaint.value = updated
                }
            }
            onComplete(result)
        }
    }

    fun updateComplaintStatus(
        complaintId: String,
        status: String,
        resolutionSummary: String,
        onComplete: (Result<ComplaintEntity>) -> Unit = {}
    ) {
        val user = _authState.value.currentUser
        if (user == null) return
        viewModelScope.launch {
            val result = repository.updateComplaintStatus(complaintId, status, resolutionSummary, user.email)
            result.onSuccess { updated ->
                if (_selectedComplaint.value?.id == complaintId) {
                    _selectedComplaint.value = updated
                }
            }
            onComplete(result)
        }
    }

    // Doctor Joining Applications (Aadhaar KYC)
    fun submitDoctorJoining(
        application: DoctorJoiningApplicationEntity,
        onComplete: (Result<DoctorJoiningApplicationEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.submitDoctorJoiningApplication(application)
            onComplete(result)
        }
    }

    fun approveDoctorJoining(
        applicationId: String,
        onComplete: (Result<Boolean>) -> Unit = {}
    ) {
        val admin = _authState.value.currentUser
        val adminEmail = admin?.email ?: "jigneshmakavana1992@gmail.com"
        viewModelScope.launch {
            val result = repository.approveDoctorJoiningApplication(adminEmail, applicationId)
            onComplete(result)
        }
    }
}
