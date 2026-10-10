package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PrescriptionDatabase
import com.example.data.model.MedicationItem
import com.example.data.model.Prescription
import com.example.data.repository.PrescriptionRepository
import com.example.ui.PrescriptionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrescriptionViewModelTest {

    private lateinit var application: Application
    private lateinit var database: PrescriptionDatabase
    private lateinit var repository: PrescriptionRepository
    private lateinit var viewModel: PrescriptionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        application = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(application, PrescriptionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PrescriptionRepository(database.prescriptionDao(), Dispatchers.Unconfined)
        viewModel = PrescriptionViewModel(application, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun testInitialUiState() = runBlocking {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.selectedPrescription)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun testCreatePrescriptionAsynchronousWorkflow() = runBlocking {
        var callbackInvoked = false
        var createdRxId: String? = null

        val job = viewModel.createPrescription(
            appointmentId = "apt_test_101",
            doctorId = "doc_sarah_1",
            doctorName = "Dr. Sarah Jenkins",
            doctorQualifications = "MBBS, MD (Paediatrics)",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-2018-84920",
            clinicAffiliation = "Children Care Clinic",
            patientId = "pat_alex_1",
            patientName = "Alexander Wright",
            patientAge = 32,
            patientGender = "Male",
            patientWeightKg = 72.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "J06.9 - Acute Upper Respiratory Infection",
            vitalsBp = "120/80 mmHg",
            vitalsPulse = "74 bpm",
            vitalsSpO2 = "99%",
            vitalsTemp = "98.6°F",
            medications = listOf(
                MedicationItem(
                    id = "med_1",
                    name = "Amoxicillin 500mg",
                    dosageForm = "Tab",
                    dosageSchedule = "1-0-1",
                    timing = "After Food",
                    durationDays = 5,
                    totalQuantity = 10,
                    instructions = "Complete full course"
                )
            ),
            clinicalAdvice = "Hydration and bed rest",
            labOrders = "None",
            followUpText = "Review in 5 days",
            followUpDateTimestamp = System.currentTimeMillis() + 5 * 24 * 3600 * 1000L,
            onSuccess = { rx ->
                callbackInvoked = true
                createdRxId = rx.id
            }
        )

        job.join()

        assertTrue("onSuccess callback must be invoked", callbackInvoked)
        assertNotNull(createdRxId)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.lastGeneratedPrescription)
        assertEquals(createdRxId, state.lastGeneratedPrescription?.id)
        assertEquals(true, state.isSignatureValid)
        assertNotNull(state.successMessage)

        // Verify repository direct lookup
        val inDb = repository.getPrescriptionById(createdRxId!!)
        assertNotNull("Must exist in database", inDb)
        assertEquals(createdRxId, inDb?.id)
    }

    @Test
    fun testSearchAndFilterStateFlows() = runBlocking {
        val rx1 = Prescription(
            id = "RX-SEARCH-1",
            appointmentId = "apt_1",
            doctorId = "doc_1",
            doctorName = "Dr. Jenkins",
            doctorQualifications = "MBBS",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-1",
            clinicAffiliation = "Clinic",
            patientId = "pat_A",
            patientName = "Rahul Sharma",
            patientAge = 25,
            patientGender = "Male",
            patientWeightKg = 65.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "J45.9 - Asthma, unspecified",
            vitalsBp = "120/80",
            vitalsPulse = "72",
            vitalsSpO2 = "98%",
            vitalsTemp = "98.4°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "Inhaler review",
            labOrdersJson = "None",
            followUpText = "1 week",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hash1",
            verificationQrUrl = "url1",
            createdAt = System.currentTimeMillis()
        )
        val rx2 = Prescription(
            id = "RX-SEARCH-2",
            appointmentId = "apt_2",
            doctorId = "doc_2",
            doctorName = "Dr. Roy",
            doctorQualifications = "MD",
            doctorSpeciality = "Cardiology",
            councilRegistrationNumber = "NMC-2",
            clinicAffiliation = "Heart Clinic",
            patientId = "pat_B",
            patientName = "Priya Sen",
            patientAge = 40,
            patientGender = "Female",
            patientWeightKg = 60.0,
            patientAllergies = "None",
            patientChronicConditions = "Hypertension",
            diagnosisICD10 = "I10 - Essential Hypertension",
            vitalsBp = "135/85",
            vitalsPulse = "76",
            vitalsSpO2 = "99%",
            vitalsTemp = "98.6°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "Low sodium",
            labOrdersJson = "Lipid profile",
            followUpText = "1 month",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hash2",
            verificationQrUrl = "url2",
            createdAt = System.currentTimeMillis()
        )

        repository.insertPrescriptions(listOf(rx1, rx2))

        // Set search query
        viewModel.setSearchQuery("Asthma")
        assertEquals("Asthma", viewModel.searchQuery.value)
        assertEquals("Asthma", viewModel.uiState.value.searchQuery)

        // Filter by patient
        viewModel.filterByPatient("pat_A", "Rahul Sharma")
        assertEquals("pat_A", viewModel.uiState.value.filterPatientId)
        assertEquals("Rahul Sharma", viewModel.uiState.value.filterPatientName)

        // Clear filters
        viewModel.clearFilters()
        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.uiState.value.filterPatientId)
        assertNull(viewModel.uiState.value.filterPatientName)
        assertNull(viewModel.uiState.value.filterDoctorId)
        assertEquals(com.example.ui.DateFilterOption.ALL, viewModel.uiState.value.selectedDateFilter)
        assertNull(viewModel.uiState.value.selectedDateEpochMillis)
    }

    @Test
    fun testPatientFilteringFlow() = runBlocking {
        val now = System.currentTimeMillis()
        val rxPatient1 = Prescription(
            id = "RX-PAT-1",
            appointmentId = "apt_1",
            doctorId = "doc_1",
            doctorName = "Dr. Jenkins",
            doctorQualifications = "MBBS",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-1",
            clinicAffiliation = "Clinic",
            patientId = "PATIENT_ALPHA",
            patientName = "Alice Wonderland",
            patientAge = 28,
            patientGender = "Female",
            patientWeightKg = 58.0,
            patientAllergies = "Penicillin",
            patientChronicConditions = "None",
            diagnosisICD10 = "R05 - Cough",
            vitalsBp = "118/76",
            vitalsPulse = "70",
            vitalsSpO2 = "99%",
            vitalsTemp = "98.4°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "Rest",
            labOrdersJson = "None",
            followUpText = "3 days",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hashA",
            verificationQrUrl = "urlA",
            createdAt = now
        )
        val rxPatient2 = Prescription(
            id = "RX-PAT-2",
            appointmentId = "apt_2",
            doctorId = "doc_1",
            doctorName = "Dr. Jenkins",
            doctorQualifications = "MBBS",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-1",
            clinicAffiliation = "Clinic",
            patientId = "PATIENT_BETA",
            patientName = "Bob Builder",
            patientAge = 45,
            patientGender = "Male",
            patientWeightKg = 82.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "M54.5 - Low back pain",
            vitalsBp = "124/82",
            vitalsPulse = "74",
            vitalsSpO2 = "98%",
            vitalsTemp = "98.6°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "Physiotherapy",
            labOrdersJson = "X-ray lumbar",
            followUpText = "10 days",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hashB",
            verificationQrUrl = "urlB",
            createdAt = now
        )

        repository.insertPrescriptions(listOf(rxPatient1, rxPatient2))

        // Wait for Room Flow emission
        val initial = viewModel.filteredPrescriptions.first { it.size == 2 }
        assertEquals(2, initial.size)

        // When filtering by PATIENT_ALPHA
        viewModel.filterByPatient("PATIENT_ALPHA", "Alice Wonderland")
        assertEquals("PATIENT_ALPHA", viewModel.uiState.value.filterPatientId)
        assertEquals("Alice Wonderland", viewModel.uiState.value.filterPatientName)

        val filtered = viewModel.filteredPrescriptions.first { it.size == 1 && it[0].id == "RX-PAT-1" }
        assertEquals(1, filtered.size)
        assertEquals("RX-PAT-1", filtered[0].id)
        assertEquals("Alice Wonderland", filtered[0].patientName)

        // When switching to PATIENT_BETA
        viewModel.filterByPatient("PATIENT_BETA", "Bob Builder")
        val filteredBeta = viewModel.filteredPrescriptions.first { it.size == 1 && it[0].id == "RX-PAT-2" }
        assertEquals(1, filteredBeta.size)
        assertEquals("RX-PAT-2", filteredBeta[0].id)
        assertEquals("Bob Builder", filteredBeta[0].patientName)

        // When clearing patient filter
        viewModel.filterByPatient(null)
        val allAfterClear = viewModel.filteredPrescriptions.first { it.size == 2 }
        assertEquals(2, allAfterClear.size)
    }

    @Test
    fun testDateFilteringOptions() = runBlocking {
        val now = System.currentTimeMillis()
        val oneDayMillis = 86400000L

        // Prescription created today
        val rxToday = Prescription(
            id = "RX-DATE-TODAY",
            appointmentId = "apt_today",
            doctorId = "doc_1",
            doctorName = "Dr. Jenkins",
            doctorQualifications = "MBBS",
            doctorSpeciality = "General",
            councilRegistrationNumber = "NMC-1",
            clinicAffiliation = "Clinic",
            patientId = "pat_1",
            patientName = "Today Patient",
            patientAge = 30,
            patientGender = "Male",
            patientWeightKg = 70.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "J00",
            vitalsBp = "120/80",
            vitalsPulse = "72",
            vitalsSpO2 = "98%",
            vitalsTemp = "98.6°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "",
            labOrdersJson = "",
            followUpText = "",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hashToday",
            verificationQrUrl = "url",
            createdAt = now
        )

        // Prescription created 3 days ago (within 7 days)
        val rx3DaysAgo = rxToday.copy(
            id = "RX-DATE-3DAYS",
            createdAt = now - (3 * oneDayMillis)
        )

        // Prescription created 15 days ago (within 30 days, but > 7 days)
        val rx15DaysAgo = rxToday.copy(
            id = "RX-DATE-15DAYS",
            createdAt = now - (15 * oneDayMillis)
        )

        // Prescription created 45 days ago (> 30 days)
        val rx45DaysAgo = rxToday.copy(
            id = "RX-DATE-45DAYS",
            createdAt = now - (45 * oneDayMillis)
        )

        repository.insertPrescriptions(listOf(rxToday, rx3DaysAgo, rx15DaysAgo, rx45DaysAgo))

        // Wait for all 4 to emit
        val allInitial = viewModel.filteredPrescriptions.first { it.size == 4 }
        assertEquals(4, allInitial.size)

        // DateFilterOption.TODAY -> only rxToday
        viewModel.filterByDate(com.example.ui.DateFilterOption.TODAY)
        val todayResults = viewModel.filteredPrescriptions.first { it.size == 1 && it[0].id == "RX-DATE-TODAY" }
        assertEquals(1, todayResults.size)
        assertEquals("RX-DATE-TODAY", todayResults[0].id)

        // DateFilterOption.LAST_7_DAYS -> rxToday and rx3DaysAgo
        viewModel.filterByDate(com.example.ui.DateFilterOption.LAST_7_DAYS)
        val sevenDaysResults = viewModel.filteredPrescriptions.first { it.size == 2 }
        assertEquals(2, sevenDaysResults.size)
        assertTrue(sevenDaysResults.any { it.id == "RX-DATE-TODAY" })
        assertTrue(sevenDaysResults.any { it.id == "RX-DATE-3DAYS" })

        // DateFilterOption.LAST_30_DAYS -> rxToday, rx3DaysAgo, rx15DaysAgo
        viewModel.filterByDate(com.example.ui.DateFilterOption.LAST_30_DAYS)
        val thirtyDaysResults = viewModel.filteredPrescriptions.first { it.size == 3 }
        assertEquals(3, thirtyDaysResults.size)
        assertFalse(thirtyDaysResults.any { it.id == "RX-DATE-45DAYS" })

        // Filter by Specific Date (15 days ago)
        val specificDateTarget = now - (15 * oneDayMillis)
        viewModel.filterBySpecificDate(specificDateTarget)
        val specificResults = viewModel.filteredPrescriptions.first { it.size == 1 && it[0].id == "RX-DATE-15DAYS" }
        assertEquals(1, specificResults.size)
        assertEquals("RX-DATE-15DAYS", specificResults[0].id)

        // Reset
        viewModel.clearFilters()
        val allReset = viewModel.filteredPrescriptions.first { it.size == 4 }
        assertEquals(4, allReset.size)
    }

    @Test
    fun testSelectPrescriptionAndSignatureVerification() = runBlocking {
        val rx = Prescription(
            id = "RX-VERIFY",
            appointmentId = "apt_99",
            doctorId = "doc_1",
            doctorName = "Dr. Jenkins",
            doctorQualifications = "MBBS",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-1",
            clinicAffiliation = "Clinic",
            patientId = "pat_A",
            patientName = "Test Patient",
            patientAge = 30,
            patientGender = "Male",
            patientWeightKg = 70.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "None",
            vitalsBp = "120/80",
            vitalsPulse = "72",
            vitalsSpO2 = "98%",
            vitalsTemp = "98.4°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "",
            labOrdersJson = "",
            followUpText = "",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "valid_sha256_hash",
            verificationQrUrl = "https://mediconnect.pro/verify/rx/RX-VERIFY?hash=123",
            createdAt = System.currentTimeMillis()
        )

        viewModel.selectPrescription(rx)
        val state = viewModel.uiState.value
        assertEquals("RX-VERIFY", state.selectedPrescription?.id)
        assertEquals(true, state.isSignatureValid)
    }

    @Test
    fun testDeletePrescriptionWorkflow() = runBlocking {
        val rx = Prescription(
            id = "RX-DELETE-ME",
            appointmentId = "apt_del",
            doctorId = "doc_1",
            doctorName = "Dr. Jenkins",
            doctorQualifications = "MBBS",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-1",
            clinicAffiliation = "Clinic",
            patientId = "pat_A",
            patientName = "Delete Patient",
            patientAge = 30,
            patientGender = "Male",
            patientWeightKg = 70.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "None",
            vitalsBp = "120/80",
            vitalsPulse = "72",
            vitalsSpO2 = "98%",
            vitalsTemp = "98.4°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "",
            labOrdersJson = "",
            followUpText = "",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hash",
            verificationQrUrl = "url",
            createdAt = System.currentTimeMillis()
        )

        repository.insertPrescription(rx)
        assertNotNull(repository.getPrescriptionById("RX-DELETE-ME"))

        val job = viewModel.deletePrescription("RX-DELETE-ME")
        job.join()

        val state = viewModel.uiState.value
        assertEquals("Prescription deleted", state.successMessage)
        assertNull(repository.getPrescriptionById("RX-DELETE-ME"))
    }

    @Test
    fun testUpdatePrescriptionWorkflow() = runBlocking {
        val rx = Prescription(
            id = "RX-UPDATE-ME",
            appointmentId = "apt_upd",
            doctorId = "doc_1",
            doctorName = "Dr. Jenkins",
            doctorQualifications = "MBBS",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-1",
            clinicAffiliation = "Clinic",
            patientId = "pat_A",
            patientName = "Update Patient",
            patientAge = 30,
            patientGender = "Male",
            patientWeightKg = 70.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "Initial Diagnosis",
            vitalsBp = "120/80",
            vitalsPulse = "72",
            vitalsSpO2 = "98%",
            vitalsTemp = "98.4°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "",
            labOrdersJson = "",
            followUpText = "Original follow-up",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hash",
            verificationQrUrl = "url",
            createdAt = System.currentTimeMillis()
        )

        repository.insertPrescription(rx)

        val updatedRx = rx.copy(followUpText = "Revised urgent follow-up")
        val job = viewModel.updatePrescription(updatedRx)
        job.join()

        val state = viewModel.uiState.value
        assertEquals("Prescription updated successfully", state.successMessage)
        assertEquals("Revised urgent follow-up", state.selectedPrescription?.followUpText)

        val inDb = repository.getPrescriptionById("RX-UPDATE-ME")
        assertEquals("Revised urgent follow-up", inDb?.followUpText)
    }
}
