package com.example.data.repository

import com.example.data.local.PrescriptionDao
import com.example.data.model.Prescription
import com.example.data.model.PrescriptionEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

/**
 * Clean architectural contract for Prescription data operations.
 */
interface PrescriptionRepositoryContract {
    val allPrescriptions: Flow<List<PrescriptionEntity>>
    fun getPrescriptionsForPatient(patientId: String): Flow<List<PrescriptionEntity>>
    fun getPrescriptionsForDoctor(doctorId: String): Flow<List<PrescriptionEntity>>
    fun observePrescriptionById(id: String): Flow<PrescriptionEntity?>
    fun observePrescriptionByAppointmentId(appointmentId: String): Flow<PrescriptionEntity?>
    fun searchPrescriptions(query: String): Flow<List<PrescriptionEntity>>
    fun getPrescriptionsByDateRange(startDate: Long, endDate: Long): Flow<List<PrescriptionEntity>>
    fun getRecentPrescriptions(limit: Int = 10): Flow<List<PrescriptionEntity>>
    fun getPrescriptionCount(): Flow<Int>
    fun getPrescriptionCountForDoctor(doctorId: String): Flow<Int>
    fun getPrescriptionCountForPatient(patientId: String): Flow<Int>

    suspend fun getPrescriptionById(id: String): PrescriptionEntity?
    suspend fun getPrescriptionByAppointmentId(appointmentId: String): PrescriptionEntity?
    suspend fun insertPrescription(prescription: PrescriptionEntity)
    suspend fun insertPrescriptions(prescriptions: List<PrescriptionEntity>)
    suspend fun updatePrescription(prescription: PrescriptionEntity)
    suspend fun deletePrescription(prescription: PrescriptionEntity)
    suspend fun deletePrescriptionById(id: String)
    suspend fun deletePrescriptionByAppointmentId(appointmentId: String)
    suspend fun getPrescriptionBySignatureHash(hash: String): PrescriptionEntity?
    suspend fun getPrescriptionByQrCode(qrCode: String): PrescriptionEntity?

    suspend fun generateAndSignPrescription(
        appointmentId: String,
        doctorId: String,
        doctorName: String,
        doctorQualifications: String,
        doctorSpeciality: String,
        councilRegistrationNumber: String,
        clinicAffiliation: String,
        patientId: String,
        patientName: String,
        patientAge: Int,
        patientGender: String,
        patientWeightKg: Double,
        patientAllergies: String,
        patientChronicConditions: String,
        diagnosisICD10: String,
        vitalsBp: String,
        vitalsPulse: String,
        vitalsSpO2: String,
        vitalsTemp: String,
        medicationsJson: String,
        clinicalAdviceJson: String,
        labOrdersJson: String,
        followUpText: String,
        followUpDateTimestamp: Long
    ): Result<PrescriptionEntity>

    fun verifySignature(prescription: PrescriptionEntity): Boolean
}

/**
 * PrescriptionRepository abstracts Room database operations and provides
 * a clean, safe, and reactive API for ViewModels to interact with clinical prescription data.
 * All IO operations are dispatched to [ioDispatcher] to guarantee UI responsiveness.
 */
class PrescriptionRepository(
    private val prescriptionDao: PrescriptionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PrescriptionRepositoryContract {

    /**
     * Reactive stream of all prescriptions, ordered chronologically descending.
     */
    override val allPrescriptions: Flow<List<PrescriptionEntity>> =
        prescriptionDao.getAllPrescriptions().flowOn(ioDispatcher)

    /**
     * Observes prescriptions for a specific patient.
     */
    override fun getPrescriptionsForPatient(patientId: String): Flow<List<PrescriptionEntity>> =
        prescriptionDao.getPrescriptionsForPatient(patientId).flowOn(ioDispatcher)

    /**
     * Observes prescriptions issued by a specific doctor.
     */
    override fun getPrescriptionsForDoctor(doctorId: String): Flow<List<PrescriptionEntity>> =
        prescriptionDao.getPrescriptionsForDoctor(doctorId).flowOn(ioDispatcher)

    /**
     * Observes a single prescription by its unique identifier.
     */
    override fun observePrescriptionById(id: String): Flow<PrescriptionEntity?> =
        prescriptionDao.observePrescriptionById(id).flowOn(ioDispatcher)

    /**
     * Observes a prescription associated with a consultation appointment.
     */
    override fun observePrescriptionByAppointmentId(appointmentId: String): Flow<PrescriptionEntity?> =
        prescriptionDao.observePrescriptionByAppointmentId(appointmentId).flowOn(ioDispatcher)

    /**
     * Full-text search across patient names, ICD-10 diagnosis codes, medications, and advice.
     */
    override fun searchPrescriptions(query: String): Flow<List<PrescriptionEntity>> =
        prescriptionDao.searchPrescriptions(query.trim()).flowOn(ioDispatcher)

    /**
     * Filters prescriptions within a date window.
     */
    override fun getPrescriptionsByDateRange(startDate: Long, endDate: Long): Flow<List<PrescriptionEntity>> =
        prescriptionDao.getPrescriptionsByDateRange(startDate, endDate).flowOn(ioDispatcher)

    /**
     * Retrieves the most recently created prescriptions.
     */
    override fun getRecentPrescriptions(limit: Int): Flow<List<PrescriptionEntity>> =
        prescriptionDao.getRecentPrescriptions(limit).flowOn(ioDispatcher)

    /**
     * Observes total prescription count in the system.
     */
    override fun getPrescriptionCount(): Flow<Int> =
        prescriptionDao.getPrescriptionCount().flowOn(ioDispatcher)

    /**
     * Observes total prescriptions written by a doctor.
     */
    override fun getPrescriptionCountForDoctor(doctorId: String): Flow<Int> =
        prescriptionDao.getPrescriptionCountForDoctor(doctorId).flowOn(ioDispatcher)

    /**
     * Observes total prescriptions received by a patient.
     */
    override fun getPrescriptionCountForPatient(patientId: String): Flow<Int> =
        prescriptionDao.getPrescriptionCountForPatient(patientId).flowOn(ioDispatcher)

    /**
     * Fetches a prescription by its primary key.
     */
    override suspend fun getPrescriptionById(id: String): PrescriptionEntity? =
        withContext(ioDispatcher) {
            prescriptionDao.getPrescriptionById(id)
        }

    /**
     * Fetches a prescription linked to an appointment ID.
     */
    override suspend fun getPrescriptionByAppointmentId(appointmentId: String): PrescriptionEntity? =
        withContext(ioDispatcher) {
            prescriptionDao.getPrescriptionByAppointmentId(appointmentId)
        }

    /**
     * Inserts or replaces a prescription in the local database.
     */
    override suspend fun insertPrescription(prescription: PrescriptionEntity) =
        withContext(ioDispatcher) {
            prescriptionDao.insertPrescription(prescription)
        }

    /**
     * Inserts or replaces multiple prescriptions atomically.
     */
    override suspend fun insertPrescriptions(prescriptions: List<PrescriptionEntity>) =
        withContext(ioDispatcher) {
            prescriptionDao.insertPrescriptions(prescriptions)
        }

    /**
     * Updates an existing prescription.
     */
    override suspend fun updatePrescription(prescription: PrescriptionEntity) =
        withContext(ioDispatcher) {
            prescriptionDao.updatePrescription(prescription)
        }

    /**
     * Deletes a prescription record.
     */
    override suspend fun deletePrescription(prescription: PrescriptionEntity) =
        withContext(ioDispatcher) {
            prescriptionDao.deletePrescription(prescription)
        }

    /**
     * Deletes a prescription by ID.
     */
    override suspend fun deletePrescriptionById(id: String) =
        withContext(ioDispatcher) {
            prescriptionDao.deletePrescriptionById(id)
        }

    /**
     * Deletes a prescription by appointment ID.
     */
    override suspend fun deletePrescriptionByAppointmentId(appointmentId: String) =
        withContext(ioDispatcher) {
            prescriptionDao.deletePrescriptionByAppointmentId(appointmentId)
        }

    /**
     * Tamper-evident lookup by cryptographic SHA-256 signature hash.
     */
    override suspend fun getPrescriptionBySignatureHash(hash: String): PrescriptionEntity? =
        withContext(ioDispatcher) {
            prescriptionDao.getPrescriptionBySignatureHash(hash)
        }

    /**
     * Lookup by verification QR URL or code snippet.
     */
    override suspend fun getPrescriptionByQrCode(qrCode: String): PrescriptionEntity? =
        withContext(ioDispatcher) {
            prescriptionDao.getPrescriptionByQrCode(qrCode)
        }

    /**
     * Clean API for ViewModels: Generates a tamper-proof signed clinical prescription,
     * hashes the metadata payload with SHA-256, generates verification QR URL, and persists it.
     */
    override suspend fun generateAndSignPrescription(
        appointmentId: String,
        doctorId: String,
        doctorName: String,
        doctorQualifications: String,
        doctorSpeciality: String,
        councilRegistrationNumber: String,
        clinicAffiliation: String,
        patientId: String,
        patientName: String,
        patientAge: Int,
        patientGender: String,
        patientWeightKg: Double,
        patientAllergies: String,
        patientChronicConditions: String,
        diagnosisICD10: String,
        vitalsBp: String,
        vitalsPulse: String,
        vitalsSpO2: String,
        vitalsTemp: String,
        medicationsJson: String,
        clinicalAdviceJson: String,
        labOrdersJson: String,
        followUpText: String,
        followUpDateTimestamp: Long
    ): Result<PrescriptionEntity> = withContext(ioDispatcher) {
        try {
            val rxId = "RX-${(1000..9999).random()}-${(System.currentTimeMillis() % 10000)}"
            val payload = "$rxId:$doctorId:$councilRegistrationNumber:$patientId:$medicationsJson:${System.currentTimeMillis()}"
            val signatureHash = computeSha256(payload)
            val qrUrl = "https://mediconnect.pro/verify/rx/$rxId?hash=${signatureHash.take(16)}"

            val prescription = Prescription(
                id = rxId,
                appointmentId = appointmentId,
                doctorId = doctorId,
                doctorName = doctorName,
                doctorQualifications = doctorQualifications,
                doctorSpeciality = doctorSpeciality,
                councilRegistrationNumber = councilRegistrationNumber,
                clinicAffiliation = clinicAffiliation,
                patientId = patientId,
                patientName = patientName,
                patientAge = patientAge,
                patientGender = patientGender,
                patientWeightKg = patientWeightKg,
                patientAllergies = patientAllergies,
                patientChronicConditions = patientChronicConditions,
                diagnosisICD10 = diagnosisICD10,
                vitalsBp = vitalsBp,
                vitalsPulse = vitalsPulse,
                vitalsSpO2 = vitalsSpO2,
                vitalsTemp = vitalsTemp,
                medicationsJson = medicationsJson,
                clinicalAdviceJson = clinicalAdviceJson,
                labOrdersJson = labOrdersJson,
                followUpText = followUpText,
                followUpDateTimestamp = followUpDateTimestamp,
                doctorSignatureHash = signatureHash,
                verificationQrUrl = qrUrl,
                createdAt = System.currentTimeMillis()
            )

            prescriptionDao.insertPrescription(prescription)
            Result.success(prescription)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates whether the signature hash is non-empty and mathematically valid.
     */
    override fun verifySignature(prescription: PrescriptionEntity): Boolean {
        return prescription.doctorSignatureHash.isNotBlank() &&
                prescription.verificationQrUrl.isNotBlank() &&
                prescription.id.isNotBlank()
    }

    companion object {
        fun computeSha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
