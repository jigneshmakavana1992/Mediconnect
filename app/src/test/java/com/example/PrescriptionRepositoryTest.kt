package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PrescriptionDatabase
import com.example.data.model.Prescription
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
class PrescriptionRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: PrescriptionDatabase
    private lateinit var repository: PrescriptionRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, PrescriptionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PrescriptionRepository(database.prescriptionDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun samplePrescription(
        id: String = "RX-${UUID.randomUUID()}",
        doctorId: String = "doc_101",
        patientId: String = "pat_202",
        patientName: String = "Emily Watson",
        appointmentId: String = "apt_303"
    ): Prescription {
        return Prescription(
            id = id,
            appointmentId = appointmentId,
            doctorId = doctorId,
            doctorName = "Dr. Michael Chen",
            doctorQualifications = "MBBS, MD (Medicine)",
            doctorSpeciality = "General Medicine",
            councilRegistrationNumber = "NMC-2015-77881",
            clinicAffiliation = "City Central Care",
            patientId = patientId,
            patientName = patientName,
            patientAge = 28,
            patientGender = "Female",
            patientWeightKg = 58.0,
            patientAllergies = "Sulfa",
            patientChronicConditions = "None",
            diagnosisICD10 = "K21.9 - Gastro-esophageal Reflux Disease",
            vitalsBp = "118/76 mmHg",
            vitalsPulse = "72 bpm",
            vitalsSpO2 = "98%",
            vitalsTemp = "98.4°F",
            medicationsJson = """[{"id":"m1","name":"Pantoprazole 40mg","dosageForm":"Tab","dosageSchedule":"OD","timing":"Before Food","durationDays":14,"totalQuantity":14,"instructions":"Before breakfast"}]""",
            clinicalAdviceJson = "Avoid spicy and oily food",
            labOrdersJson = "None",
            followUpText = "Review in 2 weeks",
            followUpDateTimestamp = System.currentTimeMillis() + 14 * 24 * 3600 * 1000L,
            doctorSignatureHash = "f9a463c208c1a7925e51086bf87a10f994821e25d2b7042a98f5ceec54e8db03",
            verificationQrUrl = "https://mediconnect.pro/verify/rx/$id?hash=f9a463c2",
            createdAt = System.currentTimeMillis()
        )
    }

    @Test
    fun testGenerateAndSignPrescriptionApi() = runBlocking {
        val result = repository.generateAndSignPrescription(
            appointmentId = "apt_live_call_88",
            doctorId = "doc_sarah_1",
            doctorName = "Dr. Sarah Jenkins",
            doctorQualifications = "MBBS, MD (Paediatrics)",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-2018-84920",
            clinicAffiliation = "Children Specialty Care",
            patientId = "pat_leo_1",
            patientName = "Leo Wright",
            patientAge = 2,
            patientGender = "Male",
            patientWeightKg = 12.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "J06.9 - Acute Upper Respiratory Infection",
            vitalsBp = "100/65 mmHg",
            vitalsPulse = "95 bpm",
            vitalsSpO2 = "99%",
            vitalsTemp = "99.1°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "Rest",
            labOrdersJson = "None",
            followUpText = "Review in 3 days",
            followUpDateTimestamp = System.currentTimeMillis() + 3 * 24 * 3600 * 1000L
        )

        assertTrue("generateAndSignPrescription must return success", result.isSuccess)
        val rx = result.getOrNull()
        assertNotNull(rx)
        assertTrue("Generated ID must start with RX-", rx!!.id.startsWith("RX-"))
        assertTrue("Signature hash must be non-empty SHA-256", rx.doctorSignatureHash.length == 64)
        assertTrue("QR verification URL must be formatted properly", rx.verificationQrUrl.contains("/verify/rx/"))
        assertTrue("Signature verification must pass", repository.verifySignature(rx))

        // Check that repository can retrieve the generated prescription
        val fetched = repository.getPrescriptionById(rx.id)
        assertNotNull(fetched)
        assertEquals(rx.id, fetched?.id)
        assertEquals("Leo Wright", fetched?.patientName)
    }

    @Test
    fun testRepositoryReactiveFlowsAndLookups() = runBlocking {
        val rx1 = samplePrescription(id = "RX-FLOW-1", doctorId = "doc_A", patientId = "pat_X", appointmentId = "apt_1")
        val rx2 = samplePrescription(id = "RX-FLOW-2", doctorId = "doc_A", patientId = "pat_Y", appointmentId = "apt_2")
        val rx3 = samplePrescription(id = "RX-FLOW-3", doctorId = "doc_B", patientId = "pat_X", appointmentId = "apt_3")

        repository.insertPrescriptions(listOf(rx1, rx2, rx3))

        // Total count
        val totalCount = repository.getPrescriptionCount().first()
        assertEquals(3, totalCount)

        // Count for doctor A
        val docACount = repository.getPrescriptionCountForDoctor("doc_A").first()
        assertEquals(2, docACount)

        // Count for patient X
        val patXCount = repository.getPrescriptionCountForPatient("pat_X").first()
        assertEquals(2, patXCount)

        // All prescriptions list
        val all = repository.allPrescriptions.first()
        assertEquals(3, all.size)

        // Query by appointment ID
        val aptPrescription = repository.getPrescriptionByAppointmentId("apt_2")
        assertNotNull(aptPrescription)
        assertEquals("RX-FLOW-2", aptPrescription?.id)

        // Observe by appointment ID flow
        val observedApt = repository.observePrescriptionByAppointmentId("apt_1").first()
        assertNotNull(observedApt)
        assertEquals("RX-FLOW-1", observedApt?.id)
    }

    @Test
    fun testUpdateAndDeleteOperations() = runBlocking {
        val rx = samplePrescription(id = "RX-MUTATE")
        repository.insertPrescription(rx)

        val updated = rx.copy(followUpText = "Immediate Review Required")
        repository.updatePrescription(updated)

        val retrievedUpdated = repository.getPrescriptionById("RX-MUTATE")
        assertEquals("Immediate Review Required", retrievedUpdated?.followUpText)

        // Delete by ID
        repository.deletePrescriptionById("RX-MUTATE")
        val afterDelete = repository.getPrescriptionById("RX-MUTATE")
        assertNull(afterDelete)
    }

    @Test
    fun testSearchAndRecentQueries() = runBlocking {
        val rx = samplePrescription(
            id = "RX-SEARCH-TEST",
            patientName = "Siddharth Roy",
            doctorId = "doc_cardio"
        )
        repository.insertPrescription(rx)

        val searchResult = repository.searchPrescriptions("Siddharth").first()
        assertEquals(1, searchResult.size)
        assertEquals("RX-SEARCH-TEST", searchResult.first().id)

        val recent = repository.getRecentPrescriptions(5).first()
        assertTrue(recent.isNotEmpty())
    }
}
