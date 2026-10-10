package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    SUPER_ADMIN,
    DOCTOR,
    PATIENT,
    RELATIONSHIP_MANAGER
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String,
    val role: UserRole,
    val requiresPasswordReset: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "doctor_profiles")
data class DoctorProfileEntity(
    @PrimaryKey
    val doctorId: String, // foreign key to users.id
    val councilRegistrationNumber: String, // e.g. NMC/GMC-88291
    val qualifications: String, // e.g. MBBS, MD (Paediatrics)
    val speciality: String, // e.g. Paediatric Medicine, Cardiology
    val clinicAffiliation: String, // e.g. Apex Health Institute & Research
    val contactDetails: String,
    val consultationFee: Double = 499.0, // Fee in Indian Rupees (₹)
    val consultationTimings: String = "Mon-Fri 09:00 - 17:00",
    val digitalSignatureSvg: String = "Dr. Certified Digital Signature",
    val signatureHash: String = "",
    val isVerified: Boolean = true,
    val bankName: String = "HDFC Bank Ltd",
    val bankAccountNumber: String = "•••• •••• •••• 8912",
    val bankIfsc: String = "HDFC0001042",
    val doctorUpiVpa: String = "dr.jenkins@okicici",
    val panNumber: String = "ABCDE1234F",
    val payoutCycle: String = "Weekly (Every Monday after 1-month holding)"
)

enum class PayoutBatchStatus {
    HOLDING_ESCROW, // Within 30-day quality & refund hold window
    MATURED_QUEUED, // 30-day hold completed, queued for upcoming Monday disbursement
    DISBURSED // Successfully paid to Doctor's Bank / UPI on Monday
}

@Entity(tableName = "doctor_payout_batches")
data class DoctorPayoutBatchEntity(
    @PrimaryKey
    val id: String, // e.g. "BATCH-2026-W38"
    val doctorId: String,
    val doctorName: String,
    val scheduledMondayDate: String, // e.g. "Monday, Sep 28, 2026"
    val scheduledMondayTimestamp: Long,
    val cohortPeriodLabel: String, // e.g. "Aug 17 - Aug 23, 2026 (Matured after 30-day hold)"
    val consultationCount: Int,
    val grossEarningsInr: Double,
    val platformFeePercent: Double = 10.0,
    val platformFeeInr: Double,
    val tdsPercent: Double = 10.0,
    val tdsInr: Double,
    val netPayoutInr: Double,
    val payoutStatus: PayoutBatchStatus = PayoutBatchStatus.MATURED_QUEUED,
    val holdingDaysRemaining: Int = 0, // 0 if matured, >0 if still in 30-day hold
    val payoutMethod: String = "NEFT / Direct Bank Credit", // or "UPI Instant Payout"
    val bankName: String = "HDFC Bank Ltd",
    val bankAccountMasked: String = "•••• •••• •••• 8912",
    val bankIfsc: String = "HDFC0001042",
    val upiVpa: String = "dr.jenkins@okicici",
    val utrReference: String = "",
    val disbursedDate: String = "",
    val notes: String = "Official weekly settlement after 30-day escrow hold"
)

@Entity(tableName = "patient_profiles")
data class PatientProfileEntity(
    @PrimaryKey
    val patientId: String, // foreign key to users.id
    val dateOfBirth: String = "1994-05-12",
    val age: Int = 32,
    val gender: String = "Male",
    val bloodGroup: String = "O+",
    val weightKg: Double = 68.5,
    val allergies: String = "Penicillin, Sulfa drugs",
    val chronicConditions: String = "Mild Asthma, Seasonal Rhinitis",
    val emergencyContact: String = "+91 98765-43210"
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey
    val id: String,
    val patientId: String,
    val name: String,
    val relationship: String, // e.g. Child, Spouse, Parent
    val dateOfBirth: String,
    val ageInMonths: Int,
    val gender: String,
    val allergies: String = "None known"
)

@Entity(tableName = "growth_records")
data class GrowthRecordEntity(
    @PrimaryKey
    val id: String,
    val patientId: String,
    val familyMemberId: String? = null,
    val recordedDate: Long = System.currentTimeMillis(),
    val ageInMonths: Int,
    val weightKg: Float,
    val heightCm: Float,
    val headCircumferenceCm: Float? = null,
    val whoWeightPercentile: Int, // e.g. 50th percentile
    val whoHeightPercentile: Int,
    val clinicalNotes: String = "Normal developmental growth curve"
)

enum class AppointmentStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey
    val id: String,
    val doctorId: String,
    val doctorName: String,
    val doctorSpeciality: String,
    val patientId: String,
    val patientName: String,
    val familyMemberId: String? = null,
    val appointmentTime: Long,
    val consultationType: String = "Video Call", // "Video Call" or "Audio Call"
    val chiefComplaint: String,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val meetingRoomId: String = "",
    val timeSlot: String = "10:00 AM - 10:30 AM",
    val consultationDate: String = "Today",
    val consultationFeeInr: Double = 499.0,
    val paymentStatus: String = "PAID_UPI", // "PAID_UPI", "PENDING"
    val upiTransactionRef: String = "UPI/2026/894102941",
    val upiApp: String = "Google Pay",
    val upiVpa: String = "patient@okhdfcbank",
    val isReminderSet: Boolean = true,
    val reminderTiming: String = "15 mins before",
    val reminderDoctorSent: Boolean = true,
    val reminderPatientSent: Boolean = true,
    val callDurationSeconds: Int = 380, // Minimum 3 mins (180s) required to qualify
    val patientRating: Float = 4.8f, // 1.0 to 5.0 stars
    val callQuality: String = "HD (1080p Crystal Clear)", // HD / Good (720p) / Standard / Poor
    val networkQuality: String = "Excellent (<45ms, 0% Loss)", // Excellent / Good / Fair / Poor
    val doctorBadgeTier: String = "PLATINUM", // PLATINUM, GOLD, SILVER, UNQUALIFIED
    val remunerationInr: Double = 150.0 // Auto-calculated based on badge
)

enum class DoctorBadgeTier(
    val badgeName: String,
    val defaultFeeInr: Double,
    val minScoreThreshold: Int,
    val badgeColorHex: Long
) {
    PLATINUM("Platinum", 150.0, 85, 0xFF7C3AED), // 150 per case
    GOLD("Gold", 130.0, 70, 0xFFD97706),       // 130 per case
    SILVER("Silver", 120.0, 50, 0xFF475569),     // 120 per case
    UNQUALIFIED("Unqualified", 0.0, 0, 0xFFDC2626) // < 3 mins or < 50 pts
}

data class CaseRemunerationEvaluation(
    val appointmentId: String,
    val doctorId: String,
    val doctorName: String,
    val patientName: String,
    val consultationType: String,
    val durationSeconds: Int,
    val durationMinutes: Double,
    val isDurationQualified: Boolean, // >= 180 seconds (3 mins)
    val patientRating: Float,
    val callQuality: String,
    val networkQuality: String,
    val ratingScore: Int, // max 40
    val durationScore: Int, // max 30
    val callQualityScore: Int, // max 15
    val networkScore: Int, // max 15
    val totalScore: Int, // max 100
    val badgeTier: DoctorBadgeTier,
    val remunerationInr: Double,
    val evaluationNotes: String
)

object DoctorRemunerationCalculator {
    fun evaluateCase(
        apt: AppointmentEntity,
        platinumRate: Double = 150.0,
        goldRate: Double = 130.0,
        silverRate: Double = 120.0
    ): CaseRemunerationEvaluation {
        val durationSec = apt.callDurationSeconds
        val durationMin = durationSec / 60.0
        val isQualified = durationSec >= 180 // Mandatory minimum 3 minutes

        // Parameter 1: Rating (Max 40 pts)
        val ratingScore = when {
            apt.patientRating >= 4.8f -> 40
            apt.patientRating >= 4.3f -> 34
            apt.patientRating >= 3.8f -> 26
            apt.patientRating >= 3.0f -> 18
            else -> 10
        }

        // Parameter 2: Duration (Max 30 pts) - Minimum 3 mins mandatory
        val durationScore = if (!isQualified) {
            0
        } else {
            when {
                durationSec >= 480 -> 30 // >= 8 mins
                durationSec >= 300 -> 25 // 5 - 7 mins
                else -> 20 // 3 - 4 mins
            }
        }

        // Parameter 3: Call Quality (Video or Audio Call) (Max 15 pts)
        val qualityLower = apt.callQuality.lowercase()
        val qualityScore = when {
            qualityLower.contains("hd") || qualityLower.contains("1080p") || qualityLower.contains("crystal") -> 15
            qualityLower.contains("720p") || qualityLower.contains("good") -> 12
            qualityLower.contains("fair") || qualityLower.contains("standard") -> 8
            else -> 4
        }

        // Parameter 4: Network Quality (Max 15 pts)
        val networkLower = apt.networkQuality.lowercase()
        val networkScore = when {
            networkLower.contains("excellent") || networkLower.contains("<45ms") || networkLower.contains("<50ms") -> 15
            networkLower.contains("good") -> 12
            networkLower.contains("fair") -> 8
            else -> 4
        }

        val totalScore = if (!isQualified) {
            0
        } else {
            (ratingScore + durationScore + qualityScore + networkScore).coerceIn(0, 100)
        }

        val badge = when {
            !isQualified -> DoctorBadgeTier.UNQUALIFIED
            totalScore >= 85 -> DoctorBadgeTier.PLATINUM
            totalScore >= 70 -> DoctorBadgeTier.GOLD
            totalScore >= 50 -> DoctorBadgeTier.SILVER
            else -> DoctorBadgeTier.UNQUALIFIED
        }

        val remuneration = when (badge) {
            DoctorBadgeTier.PLATINUM -> platinumRate
            DoctorBadgeTier.GOLD -> goldRate
            DoctorBadgeTier.SILVER -> silverRate
            DoctorBadgeTier.UNQUALIFIED -> 0.0
        }

        val notes = if (!isQualified) {
            "DISQUALIFIED: Consultation duration of ${String.format(java.util.Locale.US, "%.1f", durationMin)} mins fails mandatory 3-minute minimum threshold."
        } else {
            "QUALIFIED: ${badge.badgeName} badge awarded with $totalScore/100 score. Remuneration: ₹${remuneration.toInt()}."
        }

        return CaseRemunerationEvaluation(
            appointmentId = apt.id,
            doctorId = apt.doctorId,
            doctorName = apt.doctorName,
            patientName = apt.patientName,
            consultationType = apt.consultationType,
            durationSeconds = durationSec,
            durationMinutes = durationMin,
            isDurationQualified = isQualified,
            patientRating = apt.patientRating,
            callQuality = apt.callQuality,
            networkQuality = apt.networkQuality,
            ratingScore = ratingScore,
            durationScore = durationScore,
            callQualityScore = qualityScore,
            networkScore = networkScore,
            totalScore = totalScore,
            badgeTier = badge,
            remunerationInr = remuneration,
            evaluationNotes = notes
        )
    }
}

data class MedicationItem(
    val id: String,
    val name: String,
    val dosageForm: String, // Tab, Syrup, Cap, Inj, Inhaler, Drops
    val dosageSchedule: String, // 1-0-1, OD, BD, TDS, SOS
    val timing: String, // After Food, Before Food, With Food, Bedtime
    val durationDays: Int,
    val totalQuantity: Int,
    val instructions: String = ""
)

// Prescription entity is defined in Prescription.kt to support the clinical prescription generation engine

@Entity(tableName = "clinical_audit_logs")
data class AuditLogEntity(
    @PrimaryKey
    val id: String,
    val action: String, // e.g. "DOCTOR_PROVISIONED", "PRESCRIPTION_GENERATED", "PASSWORD_RESET", "CONSULTATION_STARTED"
    val performedByEmail: String,
    val userRole: String,
    val targetResource: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_feature_configs")
data class AppFeatureConfigEntity(
    @PrimaryKey
    val key: String, // e.g. "FEATURE_DOCTOR_WEEKLY_PAYMENT", "FEATURE_DOCTOR_CALL_ALERTS", etc.
    val category: String, // "PAYMENTS", "TELECONSULTATION", "EMR_RX", "PATIENT_CARE", "SECURITY"
    val title: String,
    val description: String,
    val isEnabled: Boolean = true,
    val configurableValue: String = "",
    val valueLabel: String = "",
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val lastModifiedBy: String = "jigneshmakavana1992@gmail.com"
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey
    val id: String, // e.g. "CMP-2026-101"
    val ticketNumber: String, // e.g. "TKT-8901"
    val raisedById: String,
    val raisedByName: String,
    val raisedByRole: UserRole, // DOCTOR or PATIENT
    val raisedByPhone: String,
    val raisedByEmail: String,
    val category: String, // e.g. "Teleconsultation Call Quality", "Prescription & EMR Sync", "Weekly Payment Settlement", "Patient Behavior & Conduct", "App Technical Glitch", "Billing / UPI Refund"
    val subject: String,
    val description: String,
    val priority: String = "NORMAL", // "NORMAL", "HIGH", "CRITICAL"
    val status: String = "OPEN", // "OPEN", "IN_ASSISTANCE", "RESOLVED"
    val assignedRmId: String = "rm_1",
    val assignedRmName: String = "Ananya Roy (Senior RM)",
    val callAssistanceCount: Int = 0,
    val lastCallTimestamp: Long = 0L,
    val lastCallNotes: String = "",
    val resolutionSummary: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "complaint_chat_messages")
data class ComplaintChatMessageEntity(
    @PrimaryKey
    val id: String,
    val complaintId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: UserRole,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "doctor_joining_applications")
data class DoctorJoiningApplicationEntity(
    @PrimaryKey
    val id: String, // e.g. "DOC-APP-92182"
    val aadhaarNumberMasked: String, // "XXXX XXXX 8291"
    val aadhaarHolderName: String,
    val isAadhaarVerified: Boolean = true,
    val fullName: String,
    val email: String,
    val phone: String,
    val gender: String = "Female",
    val dateOfBirth: String = "1988-07-14",
    val councilRegistrationNumber: String,
    val councilName: String,
    val qualifications: String,
    val speciality: String,
    val experienceYears: Int,
    val clinicOrHospital: String,
    val city: String,
    val state: String = "Maharashtra",
    val consultationFee: Double = 499.0,
    val bankName: String = "State Bank of India",
    val bankAccountNumber: String = "•••• •••• •••• 4421",
    val bankIfsc: String = "SBIN0001824",
    val upiVpa: String = "",
    val status: String = "PENDING_APPROVAL", // "PENDING_APPROVAL", "APPROVED", "REJECTED"
    val submittedAt: Long = System.currentTimeMillis()
)
