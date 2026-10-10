package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        UserEntity::class,
        DoctorProfileEntity::class,
        PatientProfileEntity::class,
        FamilyMemberEntity::class,
        GrowthRecordEntity::class,
        AppointmentEntity::class,
        PrescriptionEntity::class,
        AuditLogEntity::class,
        DoctorPayoutBatchEntity::class,
        AppFeatureConfigEntity::class,
        ComplaintEntity::class,
        ComplaintChatMessageEntity::class,
        DoctorJoiningApplicationEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MedConnectDatabase : RoomDatabase() {
    abstract fun dao(): MedConnectDao
    abstract fun prescriptionDao(): PrescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: MedConnectDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `doctor_payout_batches` (
                        `id` TEXT NOT NULL,
                        `doctorId` TEXT NOT NULL,
                        `doctorName` TEXT NOT NULL,
                        `scheduledMondayDate` TEXT NOT NULL,
                        `scheduledMondayTimestamp` INTEGER NOT NULL,
                        `cohortPeriodLabel` TEXT NOT NULL,
                        `consultationCount` INTEGER NOT NULL,
                        `grossEarningsInr` REAL NOT NULL,
                        `platformFeePercent` REAL NOT NULL,
                        `platformFeeInr` REAL NOT NULL,
                        `tdsPercent` REAL NOT NULL,
                        `tdsInr` REAL NOT NULL,
                        `netPayoutInr` REAL NOT NULL,
                        `payoutStatus` TEXT NOT NULL,
                        `holdingDaysRemaining` INTEGER NOT NULL,
                        `payoutMethod` TEXT NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `bankAccountMasked` TEXT NOT NULL,
                        `bankIfsc` TEXT NOT NULL,
                        `upiVpa` TEXT NOT NULL,
                        `utrReference` TEXT NOT NULL,
                        `disbursedDate` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `app_feature_configs` (
                        `key` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `configurableValue` TEXT NOT NULL,
                        `valueLabel` TEXT NOT NULL,
                        `lastModifiedTimestamp` INTEGER NOT NULL,
                        `lastModifiedBy` TEXT NOT NULL,
                        PRIMARY KEY(`key`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `complaints` (
                        `id` TEXT NOT NULL,
                        `ticketNumber` TEXT NOT NULL,
                        `raisedById` TEXT NOT NULL,
                        `raisedByName` TEXT NOT NULL,
                        `raisedByRole` TEXT NOT NULL,
                        `raisedByPhone` TEXT NOT NULL,
                        `raisedByEmail` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `priority` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `assignedRmId` TEXT NOT NULL,
                        `assignedRmName` TEXT NOT NULL,
                        `callAssistanceCount` INTEGER NOT NULL,
                        `lastCallTimestamp` INTEGER NOT NULL,
                        `lastCallNotes` TEXT NOT NULL,
                        `resolutionSummary` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `complaint_chat_messages` (
                        `id` TEXT NOT NULL,
                        `complaintId` TEXT NOT NULL,
                        `senderId` TEXT NOT NULL,
                        `senderName` TEXT NOT NULL,
                        `senderRole` TEXT NOT NULL,
                        `messageText` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `doctor_joining_applications` (
                        `id` TEXT NOT NULL,
                        `aadhaarNumberMasked` TEXT NOT NULL,
                        `aadhaarHolderName` TEXT NOT NULL,
                        `isAadhaarVerified` INTEGER NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `gender` TEXT NOT NULL,
                        `dateOfBirth` TEXT NOT NULL,
                        `councilRegistrationNumber` TEXT NOT NULL,
                        `councilName` TEXT NOT NULL,
                        `qualifications` TEXT NOT NULL,
                        `speciality` TEXT NOT NULL,
                        `experienceYears` INTEGER NOT NULL,
                        `clinicOrHospital` TEXT NOT NULL,
                        `city` TEXT NOT NULL,
                        `state` TEXT NOT NULL,
                        `consultationFee` REAL NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `bankAccountNumber` TEXT NOT NULL,
                        `bankIfsc` TEXT NOT NULL,
                        `upiVpa` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `submittedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5
        )

        fun getDatabase(context: Context, scope: CoroutineScope): MedConnectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedConnectDatabase::class.java,
                    "medconnect_pro_db"
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.dao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: MedConnectDao) {
            val now = System.currentTimeMillis()

            // 1. Super-Admin Bootstrap Account
            // Admin Email: jigneshmakavana1992@gmail.com
            // Default Password: admin@98 (Requires immediate password reset on first login!)
            val adminUser = UserEntity(
                id = "admin_root_1",
                email = "jigneshmakavana1992@gmail.com",
                passwordHash = hashPassword("admin@98"),
                fullName = "Jignesh Makavana (Super Admin)",
                phone = "+1 (800) 555-0199",
                role = UserRole.SUPER_ADMIN,
                requiresPasswordReset = true,
                isActive = true,
                createdAt = now
            )
            dao.insertUser(adminUser)

            // 2. Initial Admin-Provisioned Doctor
            val docUser = UserEntity(
                id = "doc_jenkins_1",
                email = "dr.jenkins@medconnect.pro",
                passwordHash = hashPassword("Doctor@2026"),
                fullName = "Dr. Sarah Jenkins",
                phone = "+1 (555) 345-6789",
                role = UserRole.DOCTOR,
                requiresPasswordReset = false,
                isActive = true,
                createdAt = now
            )
            dao.insertUser(docUser)

            val docProfile = DoctorProfileEntity(
                doctorId = docUser.id,
                councilRegistrationNumber = "NMC-2018-84920",
                qualifications = "MBBS, MD (Paediatrics), DCH",
                speciality = "Paediatric Medicine & Child Health",
                clinicAffiliation = "Metro Children's Specialty Health Center",
                contactDetails = "OPD Desk: +91 98765-01234 | Room 402",
                consultationFee = 499.0, // Indian Rupees (₹)
                consultationTimings = "Mon - Sat 09:00 - 18:00",
                digitalSignatureSvg = "Dr. Sarah Jenkins, MD [Digital Sign Validated]",
                signatureHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                isVerified = true
            )
            dao.insertDoctorProfile(docProfile)

            // 3. Initial Patient Account
            val patientUser = UserEntity(
                id = "patient_alex_1",
                email = "alex.wright@example.com",
                passwordHash = hashPassword("Patient@123"),
                fullName = "Alexander Wright",
                phone = "+1 (555) 890-1234",
                role = UserRole.PATIENT,
                requiresPasswordReset = false,
                isActive = true,
                createdAt = now
            )
            dao.insertUser(patientUser)

            val patientProfile = PatientProfileEntity(
                patientId = patientUser.id,
                dateOfBirth = "1992-08-14",
                age = 34,
                gender = "Male",
                bloodGroup = "O+",
                weightKg = 72.4,
                allergies = "Penicillin, Sulfa Antibiotics",
                chronicConditions = "Mild Seasonal Asthma",
                emergencyContact = "+1 (555) 998-1122"
            )
            dao.insertPatientProfile(patientProfile)

            // 4. Family Dependent: Pediatric Son Leo Wright (18 Months)
            val childMember = FamilyMemberEntity(
                id = "family_leo_1",
                patientId = patientUser.id,
                name = "Leo Wright (Son)",
                relationship = "Child",
                dateOfBirth = "2025-03-10",
                ageInMonths = 18,
                gender = "Male",
                allergies = "None Known"
            )
            dao.insertFamilyMember(childMember)

            // WHO Pediatric Growth Curve Records for Leo
            val growth1 = GrowthRecordEntity(
                id = "growth_1",
                patientId = patientUser.id,
                familyMemberId = childMember.id,
                recordedDate = now - 90L * 24 * 3600 * 1000,
                ageInMonths = 15,
                weightKg = 10.3f,
                heightCm = 80.2f,
                headCircumferenceCm = 47.0f,
                whoWeightPercentile = 52,
                whoHeightPercentile = 55,
                clinicalNotes = "Healthy milestone progression, normal motor development"
            )
            val growth2 = GrowthRecordEntity(
                id = "growth_2",
                patientId = patientUser.id,
                familyMemberId = childMember.id,
                recordedDate = now,
                ageInMonths = 18,
                weightKg = 11.2f,
                heightCm = 83.5f,
                headCircumferenceCm = 47.8f,
                whoWeightPercentile = 54,
                whoHeightPercentile = 58,
                clinicalNotes = "Active, eating well, immunization up-to-date"
            )
            dao.insertGrowthRecord(growth1)
            dao.insertGrowthRecord(growth2)

            // 5. Active Teleconsultation Appointment Ready in Queue
            val apt = AppointmentEntity(
                id = "apt_101",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                doctorSpeciality = "Paediatric Medicine & Child Health",
                patientId = patientUser.id,
                patientName = "Alexander Wright (re: Leo Wright)",
                familyMemberId = childMember.id,
                appointmentTime = now,
                consultationType = "Video Call",
                chiefComplaint = "Acute dry cough & mild fever for 2 days, poor sleep at night",
                status = AppointmentStatus.SCHEDULED,
                meetingRoomId = "MED-PRO-ROOM-749",
                timeSlot = "10:00 AM - 10:30 AM",
                consultationDate = "Today",
                consultationFeeInr = 499.0,
                paymentStatus = "PAID_UPI",
                upiTransactionRef = "UPI/2026/894102941",
                upiApp = "Google Pay",
                upiVpa = "alex.wright@oksbi",
                isReminderSet = true,
                reminderTiming = "15 mins before",
                reminderDoctorSent = true,
                reminderPatientSent = true,
                callDurationSeconds = 480,
                patientRating = 4.9f,
                callQuality = "HD (1080p Crystal Clear)",
                networkQuality = "Excellent (<45ms, 0% Loss)",
                doctorBadgeTier = "PLATINUM",
                remunerationInr = 150.0
            )
            dao.insertAppointment(apt)

            // Completed Cases Evaluated for Doctor Remuneration (Platinum, Gold, Silver, and <3 min Disqualified)
            val case1 = AppointmentEntity(
                id = "case_201_plat",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                doctorSpeciality = "Paediatric Medicine & Child Health",
                patientId = patientUser.id,
                patientName = "Rahul Sharma",
                appointmentTime = now - (2L * 24 * 3600 * 1000),
                consultationType = "Video Call",
                chiefComplaint = "Follow-up for asthma nebulization review & chest clearance",
                status = AppointmentStatus.COMPLETED,
                meetingRoomId = "MED-ROOM-201",
                timeSlot = "11:00 AM - 11:30 AM",
                consultationDate = "Sep 22, 2026",
                consultationFeeInr = 499.0,
                paymentStatus = "PAID_UPI",
                upiTransactionRef = "UPI/2026/99410283",
                upiApp = "Google Pay",
                upiVpa = "rahul.sharma@okaxis",
                callDurationSeconds = 620, // 10 mins 20 secs (> 3 min threshold)
                patientRating = 5.0f,
                callQuality = "HD (1080p Crystal Clear)",
                networkQuality = "Excellent (<40ms, 0% Loss)",
                doctorBadgeTier = "PLATINUM",
                remunerationInr = 150.0
            )
            dao.insertAppointment(case1)

            val case2 = AppointmentEntity(
                id = "case_202_plat",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                doctorSpeciality = "Paediatric Medicine & Child Health",
                patientId = patientUser.id,
                patientName = "Priya Patel",
                appointmentTime = now - (3L * 24 * 3600 * 1000),
                consultationType = "Audio Call",
                chiefComplaint = "Dosage confirmation for infant teething drops and mild rash",
                status = AppointmentStatus.COMPLETED,
                meetingRoomId = "MED-ROOM-202",
                timeSlot = "02:30 PM - 03:00 PM",
                consultationDate = "Sep 21, 2026",
                consultationFeeInr = 499.0,
                paymentStatus = "PAID_UPI",
                upiTransactionRef = "UPI/2026/88301923",
                upiApp = "PhonePe",
                upiVpa = "priyapatel@ybl",
                callDurationSeconds = 390, // 6 mins 30 secs
                patientRating = 4.6f,
                callQuality = "Crystal Hi-Fi Audio",
                networkQuality = "Good (55ms latency)",
                doctorBadgeTier = "PLATINUM",
                remunerationInr = 150.0
            )
            dao.insertAppointment(case2)

            val case3 = AppointmentEntity(
                id = "case_203_gold",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                doctorSpeciality = "Paediatric Medicine & Child Health",
                patientId = patientUser.id,
                patientName = "Amit Verma",
                appointmentTime = now - (4L * 24 * 3600 * 1000),
                consultationType = "Video Call",
                chiefComplaint = "Seasonal allergic rhinitis with morning sneezing",
                status = AppointmentStatus.COMPLETED,
                meetingRoomId = "MED-ROOM-203",
                timeSlot = "04:00 PM - 04:30 PM",
                consultationDate = "Sep 20, 2026",
                consultationFeeInr = 499.0,
                paymentStatus = "PAID_UPI",
                upiTransactionRef = "UPI/2026/77192031",
                upiApp = "Paytm",
                upiVpa = "amit.verma@paytm",
                callDurationSeconds = 340, // 5 mins 40 secs
                patientRating = 4.1f,
                callQuality = "Good (720p)",
                networkQuality = "Good (75ms latency)",
                doctorBadgeTier = "GOLD",
                remunerationInr = 130.0
            )
            dao.insertAppointment(case3)

            val case4 = AppointmentEntity(
                id = "case_204_silver",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                doctorSpeciality = "Paediatric Medicine & Child Health",
                patientId = patientUser.id,
                patientName = "Sunita Rao",
                appointmentTime = now - (5L * 24 * 3600 * 1000),
                consultationType = "Audio Call",
                chiefComplaint = "Dietary transition inquiry for 1-year toddler",
                status = AppointmentStatus.COMPLETED,
                meetingRoomId = "MED-ROOM-204",
                timeSlot = "10:30 AM - 11:00 AM",
                consultationDate = "Sep 19, 2026",
                consultationFeeInr = 499.0,
                paymentStatus = "PAID_UPI",
                upiTransactionRef = "UPI/2026/66102914",
                upiApp = "Google Pay",
                upiVpa = "sunita.rao@okhdfcbank",
                callDurationSeconds = 210, // 3 mins 30 secs (barely meets 3 min rule)
                patientRating = 3.8f,
                callQuality = "Standard Audio",
                networkQuality = "Fair (125ms latency)",
                doctorBadgeTier = "SILVER",
                remunerationInr = 120.0
            )
            dao.insertAppointment(case4)

            val case5 = AppointmentEntity(
                id = "case_205_unqualified",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                doctorSpeciality = "Paediatric Medicine & Child Health",
                patientId = patientUser.id,
                patientName = "Vikram Desai",
                appointmentTime = now - (6L * 24 * 3600 * 1000),
                consultationType = "Video Call",
                chiefComplaint = "Quick question on vaccination date confirmation",
                status = AppointmentStatus.COMPLETED,
                meetingRoomId = "MED-ROOM-205",
                timeSlot = "05:00 PM - 05:30 PM",
                consultationDate = "Sep 18, 2026",
                consultationFeeInr = 499.0,
                paymentStatus = "PAID_UPI",
                upiTransactionRef = "UPI/2026/55291823",
                upiApp = "BHIM UPI",
                upiVpa = "vikram@upi",
                callDurationSeconds = 110, // 1 min 50 secs -> FAILS 3-MINUTE MANDATORY RULE!
                patientRating = 4.8f,
                callQuality = "HD (1080p)",
                networkQuality = "Excellent (<40ms)",
                doctorBadgeTier = "UNQUALIFIED",
                remunerationInr = 0.0
            )
            dao.insertAppointment(case5)

            // 6. Existing EMR Prescription Record in Vault
            val rx = PrescriptionEntity(
                id = "RX-2026-9041",
                appointmentId = "apt_100_prev",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                doctorQualifications = "MBBS, MD (Paediatrics), DCH",
                doctorSpeciality = "Paediatric Medicine",
                councilRegistrationNumber = "NMC-2018-84920",
                clinicAffiliation = "Metro Children's Specialty Health Center",
                patientId = patientUser.id,
                patientName = "Leo Wright",
                patientAge = 1,
                patientGender = "Male",
                patientWeightKg = 11.2,
                patientAllergies = "No known drug allergies",
                patientChronicConditions = "None",
                diagnosisICD10 = "J06.9 - Acute Upper Respiratory Infection, unspecified",
                vitalsBp = "N/A (Pediatric)",
                vitalsPulse = "110 bpm",
                vitalsSpO2 = "98%",
                vitalsTemp = "99.4°F",
                medicationsJson = """
                    [
                      {"id":"m1","name":"Syrup Paracetamol (120mg/5ml)","dosageForm":"Syrup","dosageSchedule":"SOS","timing":"After Food","durationDays":3,"totalQuantity":1,"instructions":"5ml if temperature > 100°F"},
                      {"id":"m2","name":"Saline Nasal Drops (0.9%)","dosageForm":"Drops","dosageSchedule":"TDS","timing":"Before Food","durationDays":5,"totalQuantity":1,"instructions":"2 drops in each nostril before sleep & feeding"}
                    ]
                """.trimIndent(),
                clinicalAdviceJson = "Warm fluids, steam inhalation, raise head while sleeping",
                labOrdersJson = "None required at this stage",
                followUpText = "Review in 3 days if fever persists",
                followUpDateTimestamp = now + (3L * 24 * 3600 * 1000),
                doctorSignatureHash = "f9a463c208c1a7925e51086bf87a10f994821e25d2b7042a98f5ceec54e8db03",
                verificationQrUrl = "https://medconnect.pro/verify/rx/RX-2026-9041?hash=f9a463c2",
                createdAt = now - (14L * 24 * 3600 * 1000)
            )
            dao.insertPrescription(rx)

            // 7. Initial Platform Audit Trail Entry
            val audit = AuditLogEntity(
                id = "audit_init_1",
                action = "SYSTEM_INITIALIZED",
                performedByEmail = "system@medconnect.pro",
                userRole = "SYSTEM",
                targetResource = "PLATFORM_CORE",
                details = "Root Super-Admin jigneshmakavana1992@gmail.com provisioned. Strict RBAC policies activated.",
                timestamp = now
            )
            dao.insertAuditLog(audit)

            // 8. Weekly Monday Payout Batches (with 1-Month / 30-Day Escrow Hold)
            val pastMonday1 = DoctorPayoutBatchEntity(
                id = "BATCH-2026-W36",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                scheduledMondayDate = "Monday, Sep 14, 2026",
                scheduledMondayTimestamp = now - (10L * 24 * 3600 * 1000),
                cohortPeriodLabel = "Aug 3 - Aug 9, 2026 (Matured after 30-day hold)",
                consultationCount = 52,
                grossEarningsInr = 31000.0,
                platformFeePercent = 10.0,
                platformFeeInr = 3100.0,
                tdsPercent = 10.0,
                tdsInr = 3100.0,
                netPayoutInr = 24800.0,
                payoutStatus = PayoutBatchStatus.DISBURSED,
                holdingDaysRemaining = 0,
                payoutMethod = "NEFT Direct Bank Credit",
                bankName = "HDFC Bank Ltd",
                bankAccountMasked = "•••• •••• •••• 8912",
                bankIfsc = "HDFC0001042",
                upiVpa = "dr.jenkins@okicici",
                utrReference = "UTR/NEFT/20260914/6620194",
                disbursedDate = "Sep 14, 2026 11:20 AM",
                notes = "Weekly Monday disbursement executed successfully via NPCI NEFT gateway."
            )

            val pastMonday2 = DoctorPayoutBatchEntity(
                id = "BATCH-2026-W37",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                scheduledMondayDate = "Monday, Sep 21, 2026",
                scheduledMondayTimestamp = now - (3L * 24 * 3600 * 1000),
                cohortPeriodLabel = "Aug 10 - Aug 16, 2026 (Matured after 30-day hold)",
                consultationCount = 44,
                grossEarningsInr = 26500.0,
                platformFeePercent = 10.0,
                platformFeeInr = 2650.0,
                tdsPercent = 10.0,
                tdsInr = 2650.0,
                netPayoutInr = 21200.0,
                payoutStatus = PayoutBatchStatus.DISBURSED,
                holdingDaysRemaining = 0,
                payoutMethod = "NEFT Direct Bank Credit",
                bankName = "HDFC Bank Ltd",
                bankAccountMasked = "•••• •••• •••• 8912",
                bankIfsc = "HDFC0001042",
                upiVpa = "dr.jenkins@okicici",
                utrReference = "UTR/NEFT/20260921/4491023",
                disbursedDate = "Sep 21, 2026 10:45 AM",
                notes = "Weekly Monday disbursement executed successfully via NPCI NEFT gateway."
            )

            // Current Matured Cohort Queued for upcoming Monday
            val upcomingMondayBatch = DoctorPayoutBatchEntity(
                id = "BATCH-2026-W38",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                scheduledMondayDate = "Monday, Sep 28, 2026",
                scheduledMondayTimestamp = now + (4L * 24 * 3600 * 1000),
                cohortPeriodLabel = "Aug 17 - Aug 23, 2026 (Matured after 30-day hold)",
                consultationCount = 48,
                grossEarningsInr = 29000.0,
                platformFeePercent = 10.0,
                platformFeeInr = 2900.0,
                tdsPercent = 10.0,
                tdsInr = 2900.0,
                netPayoutInr = 23200.0,
                payoutStatus = PayoutBatchStatus.MATURED_QUEUED,
                holdingDaysRemaining = 0,
                payoutMethod = "NEFT Direct Bank Credit",
                bankName = "HDFC Bank Ltd",
                bankAccountMasked = "•••• •••• •••• 8912",
                bankIfsc = "HDFC0001042",
                upiVpa = "dr.jenkins@okicici",
                utrReference = "QUEUED-SETTLEMENT-W38",
                disbursedDate = "Pending Monday Execution",
                notes = "30-day holding completed. Scheduled for automatic bank credit on Monday, Sep 28, 2026."
            )

            // Cohorts currently under 30-Day Escrow / Quality Holding Period
            val escrowBatch1 = DoctorPayoutBatchEntity(
                id = "BATCH-2026-W39",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                scheduledMondayDate = "Monday, Oct 05, 2026",
                scheduledMondayTimestamp = now + (11L * 24 * 3600 * 1000),
                cohortPeriodLabel = "Aug 24 - Aug 30, 2026 (Under 30-day hold)",
                consultationCount = 54,
                grossEarningsInr = 32000.0,
                platformFeePercent = 10.0,
                platformFeeInr = 3200.0,
                tdsPercent = 10.0,
                tdsInr = 3200.0,
                netPayoutInr = 25600.0,
                payoutStatus = PayoutBatchStatus.HOLDING_ESCROW,
                holdingDaysRemaining = 5,
                payoutMethod = "NEFT Direct Bank Credit",
                bankName = "HDFC Bank Ltd",
                bankAccountMasked = "•••• •••• •••• 8912",
                bankIfsc = "HDFC0001042",
                upiVpa = "dr.jenkins@okicici",
                utrReference = "ESCROW-HOLD-30D",
                disbursedDate = "Holding Window Active (5 days remaining)",
                notes = "Held in 30-day escrow. Matures on Oct 3, releases on Monday, Oct 5."
            )

            val escrowBatch2 = DoctorPayoutBatchEntity(
                id = "BATCH-2026-W40",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                scheduledMondayDate = "Monday, Oct 12, 2026",
                scheduledMondayTimestamp = now + (18L * 24 * 3600 * 1000),
                cohortPeriodLabel = "Aug 31 - Sep 6, 2026 (Under 30-day hold)",
                consultationCount = 61,
                grossEarningsInr = 36500.0,
                platformFeePercent = 10.0,
                platformFeeInr = 3650.0,
                tdsPercent = 10.0,
                tdsInr = 3650.0,
                netPayoutInr = 29200.0,
                payoutStatus = PayoutBatchStatus.HOLDING_ESCROW,
                holdingDaysRemaining = 12,
                payoutMethod = "NEFT Direct Bank Credit",
                bankName = "HDFC Bank Ltd",
                bankAccountMasked = "•••• •••• •••• 8912",
                bankIfsc = "HDFC0001042",
                upiVpa = "dr.jenkins@okicici",
                utrReference = "ESCROW-HOLD-30D",
                disbursedDate = "Holding Window Active (12 days remaining)",
                notes = "Held in 30-day escrow. Matures on Oct 10, releases on Monday, Oct 12."
            )

            val escrowBatch3 = DoctorPayoutBatchEntity(
                id = "BATCH-2026-W41",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                scheduledMondayDate = "Monday, Oct 19, 2026",
                scheduledMondayTimestamp = now + (25L * 24 * 3600 * 1000),
                cohortPeriodLabel = "Sep 7 - Sep 13, 2026 (Under 30-day hold)",
                consultationCount = 48,
                grossEarningsInr = 28500.0,
                platformFeePercent = 10.0,
                platformFeeInr = 2850.0,
                tdsPercent = 10.0,
                tdsInr = 2850.0,
                netPayoutInr = 22800.0,
                payoutStatus = PayoutBatchStatus.HOLDING_ESCROW,
                holdingDaysRemaining = 19,
                payoutMethod = "NEFT Direct Bank Credit",
                bankName = "HDFC Bank Ltd",
                bankAccountMasked = "•••• •••• •••• 8912",
                bankIfsc = "HDFC0001042",
                upiVpa = "dr.jenkins@okicici",
                utrReference = "ESCROW-HOLD-30D",
                disbursedDate = "Holding Window Active (19 days remaining)",
                notes = "Held in 30-day escrow. Matures on Oct 17, releases on Monday, Oct 19."
            )

            val escrowBatch4 = DoctorPayoutBatchEntity(
                id = "BATCH-2026-W42",
                doctorId = docUser.id,
                doctorName = "Dr. Sarah Jenkins",
                scheduledMondayDate = "Monday, Oct 26, 2026",
                scheduledMondayTimestamp = now + (32L * 24 * 3600 * 1000),
                cohortPeriodLabel = "Sep 14 - Sep 20, 2026 (Under 30-day hold)",
                consultationCount = 40,
                grossEarningsInr = 24000.0,
                platformFeePercent = 10.0,
                platformFeeInr = 2400.0,
                tdsPercent = 10.0,
                tdsInr = 2400.0,
                netPayoutInr = 19200.0,
                payoutStatus = PayoutBatchStatus.HOLDING_ESCROW,
                holdingDaysRemaining = 26,
                payoutMethod = "NEFT Direct Bank Credit",
                bankName = "HDFC Bank Ltd",
                bankAccountMasked = "•••• •••• •••• 8912",
                bankIfsc = "HDFC0001042",
                upiVpa = "dr.jenkins@okicici",
                utrReference = "ESCROW-HOLD-30D",
                disbursedDate = "Holding Window Active (26 days remaining)",
                notes = "Held in 30-day escrow. Matures on Oct 24, releases on Monday, Oct 26."
            )

            dao.insertDoctorPayoutBatches(
                listOf(
                    pastMonday1,
                    pastMonday2,
                    upcomingMondayBatch,
                    escrowBatch1,
                    escrowBatch2,
                    escrowBatch3,
                    escrowBatch4
                )
            )

            // 9. Super Admin App Feature Configurations (Editable & Toggleable in Super Edit section)
            val defaultFeatures = listOf(
                AppFeatureConfigEntity(
                    key = "FEATURE_DOCTOR_WEEKLY_PAYMENT",
                    category = "PAYMENTS",
                    title = "Doctor Weekly Monday Settlement",
                    description = "Automated disbursement executed every Monday after a 1-month (30-day) escrow hold.",
                    isEnabled = true,
                    configurableValue = "30",
                    valueLabel = "Holding Days"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_PLATFORM_FEE",
                    category = "PAYMENTS",
                    title = "Platform Technology Service Fee",
                    description = "Percentage deducted from gross consultation fees for technology hosting & maintenance.",
                    isEnabled = true,
                    configurableValue = "10",
                    valueLabel = "Fee %"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_TDS_WITHHOLDING",
                    category = "PAYMENTS",
                    title = "TDS Tax Deduction u/s 194J",
                    description = "Tax Deducted at Source percentage withheld and remitted to Income Tax Dept with Form 16A.",
                    isEnabled = true,
                    configurableValue = "10",
                    valueLabel = "TDS %"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_DOCTOR_CALL_ALERTS",
                    category = "TELECONSULTATION",
                    title = "Doctor-to-Patient Call Alert Dispatch",
                    description = "Restricts outbound call alerts to verified practitioners to prevent patient spam.",
                    isEnabled = true,
                    configurableValue = "SMS+WhatsApp+InApp",
                    valueLabel = "Dispatch Channels"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_AUDIO_CALLS",
                    category = "TELECONSULTATION",
                    title = "Audio Consultation Telehealth Calling",
                    description = "Allows doctors and patients to conduct consultations over audio-only WebRTC Opus stream.",
                    isEnabled = true,
                    configurableValue = "Opus-HD-32kbps",
                    valueLabel = "Bitrate / Codec"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_UPI_GATEWAY",
                    category = "PAYMENTS",
                    title = "UPI Indian Rupees (₹) Payment Flow",
                    description = "Direct UPI payment intent generation (Google Pay, PhonePe, Paytm, BHIM) for consultations.",
                    isEnabled = true,
                    configurableValue = "499",
                    valueLabel = "Base Fee (₹)"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_RAPID_RX",
                    category = "EMR_RX",
                    title = "RapidRx Studio EMR & QR Validation",
                    description = "Multi-drug dosage schedule builder with SHA-256 digital signature and verifiable QR code.",
                    isEnabled = true,
                    configurableValue = "SHA-256",
                    valueLabel = "Hash Standard"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_PEDIATRIC_GROWTH",
                    category = "PATIENT_CARE",
                    title = "WHO Pediatric Growth Curve Tracker",
                    description = "Percentile curves and Z-scores for height, weight, and head circumference monitoring.",
                    isEnabled = true,
                    configurableValue = "WHO-2006",
                    valueLabel = "Norms Reference"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_PUBLIC_REGISTRATION",
                    category = "SECURITY",
                    title = "Patient Public Self-Registration",
                    description = "Enables new patients to register and create immediate EMR profiles online.",
                    isEnabled = true,
                    configurableValue = "Open",
                    valueLabel = "Access Mode"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_CLINICAL_AUDIT_TRAIL",
                    category = "SECURITY",
                    title = "Immutable Clinical Audit Logging",
                    description = "Records cryptographic audit events for all logins, prescriptions, appointments, and payments.",
                    isEnabled = true,
                    configurableValue = "Strict-Tamper-Evident",
                    valueLabel = "Logging Policy"
                ),
                AppFeatureConfigEntity(
                    key = "CONFIG_DOCTOR_HELPLINE",
                    category = "SUPPORT",
                    title = "Doctor Assistance Helpline Number",
                    description = "Dedicated practitioner emergency assistance and escalation helpline (Filled by Super Admin).",
                    isEnabled = true,
                    configurableValue = "+91 1800-202-3001",
                    valueLabel = "Helpline Number"
                ),
                AppFeatureConfigEntity(
                    key = "CONFIG_PATIENT_HELPLINE",
                    category = "SUPPORT",
                    title = "Patient Assistance Helpline Number",
                    description = "24x7 patient care, prescription, and teleconsultation assistance helpline (Filled by Super Admin).",
                    isEnabled = true,
                    configurableValue = "+91 1800-202-3002",
                    valueLabel = "Helpline Number"
                ),
                AppFeatureConfigEntity(
                    key = "FEATURE_COMPLAINT_MANAGEMENT_PORTAL",
                    category = "SUPPORT",
                    title = "Complaint Management Portal (RM Access)",
                    description = "Enables upper right login access for Relationship Managers to provide Chat & Call assistance.",
                    isEnabled = true,
                    configurableValue = "Enabled",
                    valueLabel = "Portal Status"
                )
            )
            dao.insertFeatureConfigs(defaultFeatures)

            // 10. Relationship Manager Bootstrap Account
            val rmUser = UserEntity(
                id = "rm_ananya_1",
                email = "rm.support@medconnect.pro",
                passwordHash = hashPassword("rm@123"),
                fullName = "Ananya Roy",
                phone = "+91 98222-33445",
                role = UserRole.RELATIONSHIP_MANAGER,
                requiresPasswordReset = false,
                isActive = true,
                createdAt = now
            )
            dao.insertUser(rmUser)

            // 11. Initial Seed Complaints (1 Doctor complaint, 1 Patient complaint)
            val docComplaint = ComplaintEntity(
                id = "CMP-2026-D101",
                ticketNumber = "TKT-8402",
                raisedById = docUser.id,
                raisedByName = "Dr. Sarah Jenkins",
                raisedByRole = UserRole.DOCTOR,
                raisedByPhone = docUser.phone,
                raisedByEmail = docUser.email,
                category = "Teleconsultation Call Quality",
                subject = "Intermittent Audio Echo during Peak Evening Consultations",
                description = "During yesterday evening's 6:30 PM consultations, there was an occasional audio loop/echo when the patient spoke. Network was tested at 85 Mbps. Kindly investigate server-side echo cancellation filter.",
                priority = "HIGH",
                status = "IN_ASSISTANCE",
                assignedRmId = rmUser.id,
                assignedRmName = "Ananya Roy (Senior RM)",
                callAssistanceCount = 1,
                lastCallTimestamp = now - 3600000L,
                lastCallNotes = "Called Dr. Jenkins. Guided her to enable WebRTC AEC-3 Noise Suppression toggle in audio settings.",
                createdAt = now - 7200000L,
                updatedAt = now - 3600000L
            )

            val patientComplaint = ComplaintEntity(
                id = "CMP-2026-P201",
                ticketNumber = "TKT-9108",
                raisedById = patientUser.id,
                raisedByName = "Alexander Wright",
                raisedByRole = UserRole.PATIENT,
                raisedByPhone = patientUser.phone,
                raisedByEmail = patientUser.email,
                category = "Prescription & EMR Sync",
                subject = "Request for Clarification on Pediatric Syrup Dosage Timing",
                description = "Dr. Jenkins prescribed Paracetamol Paediatric syrup for my son Leo. Wanted to confirm if the 5ml dose should be administered strictly after feeding or if it can be taken on empty stomach.",
                priority = "NORMAL",
                status = "OPEN",
                assignedRmId = rmUser.id,
                assignedRmName = "Ananya Roy (Senior RM)",
                callAssistanceCount = 0,
                lastCallTimestamp = 0L,
                lastCallNotes = "",
                createdAt = now - 1800000L,
                updatedAt = now - 1800000L
            )

            dao.insertComplaints(listOf(docComplaint, patientComplaint))

            // Seed initial chat messages
            dao.insertComplaintChatMessages(
                listOf(
                    ComplaintChatMessageEntity(
                        id = "msg_1",
                        complaintId = docComplaint.id,
                        senderId = docUser.id,
                        senderName = docUser.fullName,
                        senderRole = UserRole.DOCTOR,
                        messageText = "Hello Support team, the echo was prominent between 6:30 PM and 7:00 PM.",
                        timestamp = now - 7000000L
                    ),
                    ComplaintChatMessageEntity(
                        id = "msg_2",
                        complaintId = docComplaint.id,
                        senderId = rmUser.id,
                        senderName = rmUser.fullName,
                        senderRole = UserRole.RELATIONSHIP_MANAGER,
                        messageText = "Hello Dr. Jenkins, Relationship Manager Ananya here. I have reviewed the call telemetry. We are initiating a brief Call Assistance to optimize your local buffer.",
                        timestamp = now - 4000000L
                    ),
                    ComplaintChatMessageEntity(
                        id = "msg_3",
                        complaintId = patientComplaint.id,
                        senderId = patientUser.id,
                        senderName = patientUser.fullName,
                        senderRole = UserRole.PATIENT,
                        messageText = "Please confirm the dose timing for Leo Wright.",
                        timestamp = now - 1700000L
                    )
                )
            )

            // 12. Initial Doctor Joining Application (Aadhaar Verified)
            val sampleJoiningApp = DoctorJoiningApplicationEntity(
                id = "DOC-APP-7731",
                aadhaarNumberMasked = "XXXX XXXX 8291",
                aadhaarHolderName = "Dr. Meera Nambiar",
                isAadhaarVerified = true,
                fullName = "Dr. Meera Nambiar",
                email = "dr.meera@medconnect.pro",
                phone = "+91 98334-11223",
                gender = "Female",
                dateOfBirth = "1987-11-20",
                councilRegistrationNumber = "MMC-2015-098231",
                councilName = "Maharashtra Medical Council (MMC)",
                qualifications = "MBBS, MD (General Medicine), DNB",
                speciality = "Internal Medicine & Diabetology",
                experienceYears = 9,
                clinicOrHospital = "Lilavati Hospital & Research Centre",
                city = "Mumbai",
                state = "Maharashtra",
                consultationFee = 650.0,
                bankName = "State Bank of India",
                bankAccountNumber = "•••• •••• •••• 5591",
                bankIfsc = "SBIN0001824",
                upiVpa = "dr.meera@oksbi",
                status = "PENDING_APPROVAL",
                submittedAt = now - 86400000L
            )
            dao.insertDoctorJoiningApplication(sampleJoiningApp)
        }
    }
}
