package com.example

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PrescriptionDatabase
import com.example.data.model.Prescription
import com.example.data.repository.PrescriptionRepository
import com.example.ui.PrescriptionViewModel
import com.example.ui.screens.prescription.PrescriptionListScreen
import com.example.ui.theme.MedConnectProTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class PrescriptionListScreenRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

        val now = System.currentTimeMillis()
        val oneDayMillis = 86400000L

        val rx1 = Prescription(
            id = "RX-UI-1",
            appointmentId = "apt_1",
            doctorId = "doc_1",
            doctorName = "Dr. Jenkins",
            doctorQualifications = "MBBS",
            doctorSpeciality = "Paediatrics",
            councilRegistrationNumber = "NMC-1",
            clinicAffiliation = "Clinic",
            patientId = "pat_alice",
            patientName = "Alice Morgan",
            patientAge = 29,
            patientGender = "Female",
            patientWeightKg = 59.0,
            patientAllergies = "None",
            patientChronicConditions = "None",
            diagnosisICD10 = "J06.9 - Acute Upper Respiratory",
            vitalsBp = "120/80",
            vitalsPulse = "72",
            vitalsSpO2 = "98%",
            vitalsTemp = "98.6°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "Drink fluids",
            labOrdersJson = "None",
            followUpText = "5 days",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hash1",
            verificationQrUrl = "url1",
            createdAt = now
        )

        val rx2 = Prescription(
            id = "RX-UI-2",
            appointmentId = "apt_2",
            doctorId = "doc_2",
            doctorName = "Dr. Roy",
            doctorQualifications = "MD",
            doctorSpeciality = "Cardiology",
            councilRegistrationNumber = "NMC-2",
            clinicAffiliation = "Heart Clinic",
            patientId = "pat_bob",
            patientName = "Bob Vance",
            patientAge = 52,
            patientGender = "Male",
            patientWeightKg = 84.0,
            patientAllergies = "Penicillin",
            patientChronicConditions = "Hypertension",
            diagnosisICD10 = "I10 - Essential Hypertension",
            vitalsBp = "138/88",
            vitalsPulse = "76",
            vitalsSpO2 = "97%",
            vitalsTemp = "98.4°F",
            medicationsJson = "[]",
            clinicalAdviceJson = "Low salt diet",
            labOrdersJson = "ECG",
            followUpText = "2 weeks",
            followUpDateTimestamp = 0L,
            doctorSignatureHash = "hash2",
            verificationQrUrl = "url2",
            createdAt = now - (10 * oneDayMillis)
        )

        runBlocking {
            repository.insertPrescriptions(listOf(rx1, rx2))
            viewModel.filteredPrescriptions.first { it.size == 2 }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun testPrescriptionListRendersPrescriptionCards() {
        composeTestRule.setContent {
            MedConnectProTheme {
                PrescriptionListScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onPrescriptionSelected = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("prescription_list_screen").assertExists()
        composeTestRule.onNodeWithTag("prescription_card_RX-UI-1").assertExists()

        // Scroll to and verify second card
        composeTestRule.onNodeWithTag("prescription_list_lazy_column")
            .performScrollToNode(hasTestTag("prescription_card_RX-UI-2"))
        composeTestRule.onNodeWithTag("prescription_card_RX-UI-2").assertExists()
    }

    @Test
    fun testFilterByPatientInteraction() {
        composeTestRule.setContent {
            MedConnectProTheme {
                PrescriptionListScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onPrescriptionSelected = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Filter by Patient Alice
        composeTestRule.onNodeWithTag("filter_patient_chip_pat_alice").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("prescription_card_RX-UI-1").assertExists()
        composeTestRule.onNodeWithTag("prescription_card_RX-UI-2").assertDoesNotExist()

        // Reset to All Patients
        composeTestRule.onNodeWithTag("filter_patient_all").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("prescription_card_RX-UI-1").assertExists()
    }

    @Test
    fun testFilterByDateInteraction() {
        composeTestRule.setContent {
            MedConnectProTheme {
                PrescriptionListScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onPrescriptionSelected = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Filter Today: rx1 is today, rx2 is 10 days ago
        composeTestRule.onNodeWithTag("filter_date_today").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("prescription_card_RX-UI-1").assertExists()
        composeTestRule.onNodeWithTag("prescription_card_RX-UI-2").assertDoesNotExist()

        // Reset to All Dates
        composeTestRule.onNodeWithTag("filter_date_all").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("prescription_card_RX-UI-1").assertExists()
    }

    @Test
    fun testSelectPrescriptionAndBackNavigation() {
        var selectedRxId: String? = null
        var backPressed = false

        composeTestRule.setContent {
            MedConnectProTheme {
                PrescriptionListScreen(
                    viewModel = viewModel,
                    onBack = { backPressed = true },
                    onPrescriptionSelected = { rx -> selectedRxId = rx.id }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click card directly to select prescription
        composeTestRule.onNodeWithTag("prescription_card_RX-UI-1").performClick()
        assertEquals("RX-UI-1", selectedRxId)

        // Top bar back button
        composeTestRule.onNodeWithTag("prescription_list_back_button").performClick()
        assertTrue(backPressed)
    }
}
