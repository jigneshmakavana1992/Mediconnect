package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedConnectDao : PrescriptionDao {

    // Users & Auth
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR phone = :identifier LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role LIMIT 1")
    suspend fun getFirstUserByRole(role: UserRole): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Doctor Profiles
    @Query("SELECT * FROM doctor_profiles WHERE doctorId = :doctorId LIMIT 1")
    suspend fun getDoctorProfile(doctorId: String): DoctorProfileEntity?

    @Query("SELECT * FROM doctor_profiles")
    fun getAllDoctorProfiles(): Flow<List<DoctorProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorProfile(profile: DoctorProfileEntity)

    @Update
    suspend fun updateDoctorProfile(profile: DoctorProfileEntity)

    // Patient Profiles
    @Query("SELECT * FROM patient_profiles WHERE patientId = :patientId LIMIT 1")
    suspend fun getPatientProfile(patientId: String): PatientProfileEntity?

    @Query("SELECT * FROM patient_profiles WHERE patientId = :patientId LIMIT 1")
    fun observePatientProfile(patientId: String): Flow<PatientProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatientProfile(profile: PatientProfileEntity)

    // Family Members
    @Query("SELECT * FROM family_members WHERE patientId = :patientId")
    fun getFamilyMembers(patientId: String): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberEntity)

    // Growth Records
    @Query("SELECT * FROM growth_records WHERE patientId = :patientId ORDER BY recordedDate ASC")
    fun getGrowthRecords(patientId: String): Flow<List<GrowthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthRecord(record: GrowthRecordEntity)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY appointmentTime ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY appointmentTime ASC")
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY appointmentTime DESC")
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: String): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    // Prescriptions operations are inherited from PrescriptionDao

    // Audit Logs
    @Query("SELECT * FROM clinical_audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Doctor Weekly Payment & Payout Batches
    @Query("SELECT * FROM doctor_payout_batches WHERE doctorId = :doctorId ORDER BY scheduledMondayTimestamp DESC")
    fun getDoctorPayoutBatches(doctorId: String): Flow<List<DoctorPayoutBatchEntity>>

    @Query("SELECT * FROM doctor_payout_batches ORDER BY scheduledMondayTimestamp DESC")
    fun getAllDoctorPayoutBatches(): Flow<List<DoctorPayoutBatchEntity>>

    @Query("SELECT * FROM doctor_payout_batches WHERE id = :id LIMIT 1")
    suspend fun getDoctorPayoutBatchById(id: String): DoctorPayoutBatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorPayoutBatch(batch: DoctorPayoutBatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorPayoutBatches(batches: List<DoctorPayoutBatchEntity>)

    @Update
    suspend fun updateDoctorPayoutBatch(batch: DoctorPayoutBatchEntity)

    // Super Edit: App Feature Flags & Configuration
    @Query("SELECT * FROM app_feature_configs ORDER BY category ASC, title ASC")
    fun getAllFeatureConfigs(): Flow<List<AppFeatureConfigEntity>>

    @Query("SELECT * FROM app_feature_configs WHERE `key` = :key LIMIT 1")
    suspend fun getFeatureConfigByKey(key: String): AppFeatureConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatureConfig(config: AppFeatureConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatureConfigs(configs: List<AppFeatureConfigEntity>)

    @Update
    suspend fun updateFeatureConfig(config: AppFeatureConfigEntity)

    // Complaints & RM Assistance
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE raisedById = :userId ORDER BY createdAt DESC")
    fun getComplaintsForUser(userId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id LIMIT 1")
    suspend fun getComplaintById(id: String): ComplaintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    // Complaint Chat Messages
    @Query("SELECT * FROM complaint_chat_messages WHERE complaintId = :complaintId ORDER BY timestamp ASC")
    fun getMessagesForComplaint(complaintId: String): Flow<List<ComplaintChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaintChatMessage(message: ComplaintChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaintChatMessages(messages: List<ComplaintChatMessageEntity>)

    // Doctor Joining Applications (Aadhaar KYC)
    @Query("SELECT * FROM doctor_joining_applications ORDER BY submittedAt DESC")
    fun getAllDoctorJoiningApplications(): Flow<List<DoctorJoiningApplicationEntity>>

    @Query("SELECT * FROM doctor_joining_applications WHERE id = :id LIMIT 1")
    suspend fun getDoctorJoiningApplicationById(id: String): DoctorJoiningApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorJoiningApplication(app: DoctorJoiningApplicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorJoiningApplications(apps: List<DoctorJoiningApplicationEntity>)

    @Update
    suspend fun updateDoctorJoiningApplication(app: DoctorJoiningApplicationEntity)
}
