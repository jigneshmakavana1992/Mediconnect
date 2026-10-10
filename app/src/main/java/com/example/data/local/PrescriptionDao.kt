package com.example.data.local

import androidx.room.*
import com.example.data.model.Prescription
import com.example.data.model.PrescriptionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [Prescription] entities.
 * Supports the high-speed clinical prescription generation engine (RapidRx Studio)
 * with atomic insertion, retrieval, reactive updates, tamper verification, and audit search.
 */
@Dao
interface PrescriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescription: PrescriptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptions(prescriptions: List<PrescriptionEntity>)

    @Update
    suspend fun updatePrescription(prescription: PrescriptionEntity)

    @Delete
    suspend fun deletePrescription(prescription: PrescriptionEntity)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deletePrescriptionById(id: String)

    @Query("DELETE FROM prescriptions WHERE appointmentId = :appointmentId")
    suspend fun deletePrescriptionByAppointmentId(appointmentId: String)

    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    suspend fun getPrescriptionById(id: String): PrescriptionEntity?

    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    fun observePrescriptionById(id: String): Flow<PrescriptionEntity?>

    @Query("SELECT * FROM prescriptions ORDER BY createdAt DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions ORDER BY createdAt DESC")
    suspend fun getPrescriptionList(): List<PrescriptionEntity>

    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getPrescriptionsForPatient(patientId: String): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE doctorId = :doctorId ORDER BY createdAt DESC")
    fun getPrescriptionsForDoctor(doctorId: String): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE appointmentId = :appointmentId LIMIT 1")
    suspend fun getPrescriptionByAppointmentId(appointmentId: String): PrescriptionEntity?

    @Query("SELECT * FROM prescriptions WHERE appointmentId = :appointmentId LIMIT 1")
    fun observePrescriptionByAppointmentId(appointmentId: String): Flow<PrescriptionEntity?>

    @Query("SELECT * FROM prescriptions WHERE doctorSignatureHash = :hash LIMIT 1")
    suspend fun getPrescriptionBySignatureHash(hash: String): PrescriptionEntity?

    @Query("SELECT * FROM prescriptions WHERE verificationQrUrl LIKE '%' || :qrCode || '%' LIMIT 1")
    suspend fun getPrescriptionByQrCode(qrCode: String): PrescriptionEntity?

    @Query("SELECT * FROM prescriptions WHERE patientName LIKE '%' || :query || '%' OR diagnosisICD10 LIKE '%' || :query || '%' OR medicationsJson LIKE '%' || :query || '%' OR clinicalAdviceJson LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPrescriptions(query: String): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getPrescriptionsByDateRange(startDate: Long, endDate: Long): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPrescriptions(limit: Int = 10): Flow<List<PrescriptionEntity>>

    @Query("SELECT COUNT(*) FROM prescriptions")
    fun getPrescriptionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM prescriptions WHERE doctorId = :doctorId")
    fun getPrescriptionCountForDoctor(doctorId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM prescriptions WHERE patientId = :patientId")
    fun getPrescriptionCountForPatient(patientId: String): Flow<Int>
}
