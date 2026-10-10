package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MedConnectDatabase
import com.example.data.local.PrescriptionDao
import com.example.data.model.Prescription
import com.example.data.model.PrescriptionEntity
import com.example.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrescriptionDaoTest {

    private lateinit var context: Context
    private lateinit var database: MedConnectDatabase
    private lateinit var prescriptionDao: PrescriptionDao
    private lateinit var prescriptionRepository: PrescriptionRepository

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, MedConnectDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        prescriptionDao = database.prescriptionDao()
        prescriptionRepository = PrescriptionRepository(prescriptionDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createSamplePrescription(
        id: String = "RX-2026-${(1000..9999).random()}",
        appointmentId: String = "apt_${UUID.randomUUID()}",
        doctorId: String = "doc_jenkins_1",
        doctorName: String = "Dr. Sarah Jenkins",
        patientId: String = "patient_alex_1",
        patientName: String = "Alexander Wright",
        diagnosis: String = "J06.9 - Acute Upper Respiratory Infection",
        signatureHash: String = "hash_${UUID.randomUUID()}"
    ): Prescription {
        return Prescription(
            id = id,
            appointmentId = appointmentId,
            doctorId = doctorId,
            doctorName = doctorName,
            doctorQualifications = "MBBS, MD (Paediatrics)",
            doctorSpeciality = "Paediatric Medicine",
            councilRegistrationNumber = "NMC-2018-84920",
            clinicAffiliation = "Metro Children's Specialty Health Center",
            patientId = patientId,
            patientName = patientName,
            patientAge = 32,
            patientGender = "Male",
            patientWeightKg = 72.4,
            patientAllergies = "Penicillin",
            patientChronicConditions = "Mild Asthma",
            diagnosisICD10 = diagnosis,
            vitalsBp = "120/80 mmHg",
            vitalsPulse = "74 bpm",
            vitalsSpO2 = "99%",
            vitalsTemp = "98.6°F",
            medicationsJson = """[{"id":"m1","name":"Paracetamol 650mg","dosageForm":"Tab","dosageSchedule":"SOS","timing":"After Food","durationDays":3,"totalQuantity":6,"instructions":"For fever"}]""",
            clinicalAdviceJson = "Adequate rest and warm fluid intake",
            labOrdersJson = "None",
            followUpText = "Review in 5 days",
            followUpDateTimestamp = System.currentTimeMillis() + 5 * 24 * 3600 * 1000L,
            doctorSignatureHash = signatureHash,
            verificationQrUrl = "https://mediconnect.pro/verify/rx/$id?hash=${signatureHash.take(16)}",
            createdAt = System.currentTimeMillis()
        )
    }

    @Test
    fun verifyPrescriptionEntityAnnotationAndTableName() {
        val cursor = database.openHelper.readableDatabase.query("SELECT name FROM sqlite_master WHERE type='table' AND name='prescriptions'")
        assertTrue("Table 'prescriptions' must exist in database", cursor.moveToFirst())
        assertEquals("prescriptions", cursor.getString(0))
        cursor.close()
    }

    @Test
    fun testInsertAndRetrievePrescriptionById() = runBlocking {
        val rx = createSamplePrescription(id = "RX-TEST-001")
        prescriptionDao.insertPrescription(rx)

        val retrieved = prescriptionDao.getPrescriptionById("RX-TEST-001")
        assertNotNull("Inserted prescription must be retrievable by ID", retrieved)
        assertEquals(rx.id, retrieved?.id)
        assertEquals(rx.doctorName, retrieved?.doctorName)
        assertEquals(rx.patientName, retrieved?.patientName)
        assertEquals(rx.diagnosisICD10, retrieved?.diagnosisICD10)
        assertEquals(rx.doctorSignatureHash, retrieved?.doctorSignatureHash)
    }

    @Test
    fun testObservePrescriptionByIdFlow() = runBlocking {
        val rx = createSamplePrescription(id = "RX-FLOW-001")
        prescriptionDao.insertPrescription(rx)

        val observed = prescriptionDao.observePrescriptionById("RX-FLOW-001").first()
        assertNotNull(observed)
        assertEquals("RX-FLOW-001", observed?.id)
    }

    @Test
    fun testPrescriptionByAppointmentId() = runBlocking {
        val appointmentId = "apt_teleconsult_99"
        val rx = createSamplePrescription(id = "RX-APT-01", appointmentId = appointmentId)
        prescriptionDao.insertPrescription(rx)

        val retrieved = prescriptionDao.getPrescriptionByAppointmentId(appointmentId)
        assertNotNull("Prescription must be retrievable by appointment ID", retrieved)
        assertEquals("RX-APT-01", retrieved?.id)
        assertEquals(appointmentId, retrieved?.appointmentId)
    }

    @Test
    fun testObservePrescriptionsForDoctorAndPatient() = runBlocking {
        val rxDoc1 = createSamplePrescription(id = "RX-D1-1", doctorId = "doc_1", patientId = "pat_1")
        val rxDoc2 = createSamplePrescription(id = "RX-D1-2", doctorId = "doc_1", patientId = "pat_2")
        val rxDocOther = createSamplePrescription(id = "RX-D2-1", doctorId = "doc_2", patientId = "pat_1")

        prescriptionDao.insertPrescription(rxDoc1)
        prescriptionDao.insertPrescription(rxDoc2)
        prescriptionDao.insertPrescription(rxDocOther)

        val doc1Prescriptions = prescriptionDao.getPrescriptionsForDoctor("doc_1").first()
        assertEquals(2, doc1Prescriptions.size)

        val pat1Prescriptions = prescriptionDao.getPrescriptionsForPatient("pat_1").first()
        assertEquals(2, pat1Prescriptions.size)

        val pat2Prescriptions = prescriptionDao.getPrescriptionsForPatient("pat_2").first()
        assertEquals(1, pat2Prescriptions.size)
        assertEquals("RX-D1-2", pat2Prescriptions.first().id)
    }

    @Test
    fun testSearchPrescriptions() = runBlocking {
        val rx1 = createSamplePrescription(
            id = "RX-SEARCH-1",
            patientName = "Siddharth Roy",
            diagnosis = "I10 - Essential Hypertension"
        )
        val rx2 = createSamplePrescription(
            id = "RX-SEARCH-2",
            patientName = "Pooja Hegde",
            diagnosis = "E11.9 - Type 2 Diabetes Mellitus"
        )
        prescriptionDao.insertPrescription(rx1)
        prescriptionDao.insertPrescription(rx2)

        val searchHypertension = prescriptionDao.searchPrescriptions("Hypertension").first()
        assertEquals(1, searchHypertension.size)
        assertEquals("RX-SEARCH-1", searchHypertension.first().id)

        val searchPooja = prescriptionDao.searchPrescriptions("Pooja").first()
        assertEquals(1, searchPooja.size)
        assertEquals("RX-SEARCH-2", searchPooja.first().id)
    }

    @Test
    fun testPrescriptionVerificationBySignatureHashAndQrCode() = runBlocking {
        val targetHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val rx = createSamplePrescription(id = "RX-VERIFY-1", signatureHash = targetHash)
        prescriptionDao.insertPrescription(rx)

        val verifiedByHash = prescriptionDao.getPrescriptionBySignatureHash(targetHash)
        assertNotNull("Must find prescription by cryptographic signature hash", verifiedByHash)
        assertEquals("RX-VERIFY-1", verifiedByHash?.id)

        val verifiedByQr = prescriptionDao.getPrescriptionByQrCode("RX-VERIFY-1")
        assertNotNull("Must find prescription by QR code query", verifiedByQr)
        assertEquals("RX-VERIFY-1", verifiedByQr?.id)
    }

    @Test
    fun testUpdateAndDeletePrescription() = runBlocking {
        val rx = createSamplePrescription(id = "RX-MUTATE-1", diagnosis = "Initial Diagnosis")
        prescriptionDao.insertPrescription(rx)

        val updated = rx.copy(diagnosisICD10 = "Updated Final Diagnosis")
        prescriptionDao.updatePrescription(updated)

        val fetchedUpdated = prescriptionDao.getPrescriptionById("RX-MUTATE-1")
        assertEquals("Updated Final Diagnosis", fetchedUpdated?.diagnosisICD10)

        // Delete by ID
        prescriptionDao.deletePrescriptionById("RX-MUTATE-1")
        val deleted = prescriptionDao.getPrescriptionById("RX-MUTATE-1")
        assertNull("Prescription must be deleted", deleted)
    }

    @Test
    fun testBatchInsertAndCounts() = runBlocking {
        val batch = listOf(
            createSamplePrescription(id = "RX-BATCH-1", doctorId = "doc_batch"),
            createSamplePrescription(id = "RX-BATCH-2", doctorId = "doc_batch"),
            createSamplePrescription(id = "RX-BATCH-3", doctorId = "doc_batch")
        )
        prescriptionDao.insertPrescriptions(batch)

        val totalCount = prescriptionDao.getPrescriptionCount().first()
        assertEquals(3, totalCount)

        val docCount = prescriptionDao.getPrescriptionCountForDoctor("doc_batch").first()
        assertEquals(3, docCount)

        val recent = prescriptionDao.getRecentPrescriptions(2).first()
        assertEquals(2, recent.size)
    }

    @Test
    fun testPrescriptionRepositoryAbstraction() = runBlocking {
        val rx = createSamplePrescription(id = "RX-REPO-1")
        prescriptionRepository.insertPrescription(rx)

        val fetched = prescriptionRepository.getPrescriptionById("RX-REPO-1")
        assertNotNull("Repository must retrieve inserted prescription", fetched)
        assertEquals(rx.id, fetched?.id)

        val count = prescriptionRepository.getPrescriptionCount().first()
        assertEquals(1, count)
    }
}
