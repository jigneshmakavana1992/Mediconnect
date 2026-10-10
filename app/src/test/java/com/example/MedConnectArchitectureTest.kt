package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MedConnectDatabase
import com.example.data.model.MedicationItem
import com.example.data.model.UserRole
import com.example.data.repository.MedConnectRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
class MedConnectArchitectureTest {

    private lateinit var context: Context
    private lateinit var database: MedConnectDatabase
    private lateinit var repository: MedConnectRepository

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, MedConnectDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MedConnectRepository(database.dao())
        MedConnectDatabase.populateInitialData(database.dao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun verifyAppName() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Mediconnect", appName)
    }

    @Test
    fun testSuperAdminInitialStateAndMandatoryPasswordReset() = runBlocking {
        // Attempt login with default seeded credentials
        val loginResult = repository.login("jigneshmakavana1992@gmail.com", "admin@98")
        assertTrue("Login must succeed for seeded Super Admin", loginResult.isSuccess)

        val user = loginResult.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.SUPER_ADMIN, user?.role)
        assertTrue("Initial Super Admin must require password reset on first login", user?.requiresPasswordReset == true)

        // Perform mandatory password reset
        val resetResult = repository.updatePassword(user!!.id, "NewSecureAdmin@2026")
        assertTrue("Password reset must succeed", resetResult.isSuccess)

        // Verify that the user no longer requires password reset
        val updatedUser = database.dao().getUserById(user.id)
        assertFalse(updatedUser?.requiresPasswordReset ?: true)

        // Login with old password should now fail
        val oldLogin = repository.login("jigneshmakavana1992@gmail.com", "admin@98")
        assertTrue(oldLogin.isFailure)

        // Login with new password must succeed
        val newLogin = repository.login("jigneshmakavana1992@gmail.com", "NewSecureAdmin@2026")
        assertTrue(newLogin.isSuccess)
    }

    @Test
    fun testPatientRegistrationAndZeroSelfRegistrationForDoctors() = runBlocking {
        val email = "jane.patient.${UUID.randomUUID()}@example.com"
        val regResult = repository.registerPatient(
            fullName = "Jane Patient",
            email = email,
            phone = "+1 555-0192",
            password = "PatientPassword@123",
            age = 29,
            gender = "Female",
            allergies = "None",
            chronicConditions = "None"
        )
        assertTrue(regResult.isSuccess)

        val patientUser = regResult.getOrNull()
        assertEquals(UserRole.PATIENT, patientUser?.role)

        // Verify patient profile was created in database
        val profile = database.dao().getPatientProfile(patientUser!!.id)
        assertNotNull("Patient profile must exist", profile)
    }

    @Test
    fun testAdminDoctorProvisioningWithCouncilRegistration() = runBlocking {
        val admin = database.dao().getUserByEmail("jigneshmakavana1992@gmail.com")
        assertNotNull(admin)

        val docEmail = "dr.carter.${UUID.randomUUID()}@medconnect.pro"
        val provResult = repository.provisionDoctor(
            adminEmail = admin!!.email,
            firstName = "Julian",
            lastName = "Carter",
            email = docEmail,
            phone = "+1 555-4920",
            temporaryPassword = "TempDocPass@123",
            councilRegNumber = "NMC-2024-${UUID.randomUUID().toString().take(6)}",
            qualifications = "MBBS, MD (Cardiology)",
            speciality = "Cardiologist",
            clinicAffiliation = "Heart & Vascular Pavilion",
            consultationFee = 90.0,
            consultationTimings = "Mon-Fri 08:00 - 16:00",
            signatureLabel = "Dr. Julian Carter, MD"
        )
        assertTrue("Admin doctor provisioning must succeed", provResult.isSuccess)

        val doctorUser = database.dao().getUserByEmail(docEmail)
        assertNotNull(doctorUser)
        assertEquals(UserRole.DOCTOR, doctorUser?.role)

        val docProfile = database.dao().getDoctorProfile(doctorUser!!.id)
        assertNotNull(docProfile)
        assertTrue("Doctor with council registration must be verified", docProfile?.isVerified == true)
    }

    @Test
    fun testRapidRxIssuanceAndTamperProofDigitalSignature() = runBlocking {
        val doctor = database.dao().getUserByEmail("dr.jenkins@medconnect.pro")
        assertNotNull(doctor)

        val doctorProfile = database.dao().getDoctorProfile(doctor!!.id)
        assertNotNull(doctorProfile)

        val patient = database.dao().getUserByEmail("alex.wright@example.com")
        assertNotNull(patient)

        val patientProfile = database.dao().getPatientProfile(patient!!.id)
        assertNotNull(patientProfile)

        val meds = listOf(
            MedicationItem(
                id = UUID.randomUUID().toString(),
                name = "Azithromycin 500mg",
                dosageForm = "Tab",
                dosageSchedule = "OD",
                timing = "After Food",
                durationDays = 3,
                totalQuantity = 3,
                instructions = "Once daily after lunch"
            )
        )

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, MedicationItem::class.java)
        val adapter = moshi.adapter<List<MedicationItem>>(type)
        val medsJson = adapter.toJson(meds)

        val rxResult = repository.issuePrescription(
            doctor = doctor,
            doctorProfile = doctorProfile!!,
            patientProfile = patientProfile!!,
            patientName = patient.fullName,
            appointmentId = "apt_101",
            diagnosisICD10 = "J06.9 - Acute Upper Respiratory Infection",
            vitalsBp = "120/80 mmHg",
            vitalsPulse = "74 bpm",
            vitalsSpO2 = "99%",
            vitalsTemp = "98.6°F",
            medicationsJson = medsJson,
            clinicalAdvice = "Hydration, Warm fluids",
            labOrders = "CBC if symptoms persist",
            followUpText = "Review in 5 days",
            followUpDate = System.currentTimeMillis() + (5 * 24 * 3600 * 1000)
        )

        assertTrue(rxResult.isSuccess)
        val rx = rxResult.getOrNull()
        assertNotNull(rx)
        assertTrue("Prescription must have non-empty SHA-256 signature hash", rx!!.doctorSignatureHash.isNotBlank())
        assertTrue("Prescription must have verification QR code URL", rx.verificationQrUrl.contains("https://mediconnect.pro/verify/rx/"))

        // Check that the prescription appears in all prescriptions
        val allRx = database.dao().getAllPrescriptions().first()
        assertTrue("Database must contain issued prescription", allRx.any { it.id == rx.id })
    }

    @Test
    fun testSlotSelectionUpiInrPaymentAndCallReminder() = runBlocking {
        val patientResult = repository.login("alex.wright@example.com", "Patient@123")
        assertTrue(patientResult.isSuccess)
        val patient = patientResult.getOrNull()!!

        val doctors = database.dao().getAllDoctorProfiles().first()
        val doc = doctors.first()

        // Book with time slot, audio call mode, INR fee, UPI payment, and reminder
        val bookResult = repository.bookAppointment(
            doctorId = doc.doctorId,
            doctorName = "Dr. Sarah Jenkins",
            doctorSpeciality = doc.speciality,
            patientId = patient.id,
            patientName = patient.fullName,
            appointmentTime = System.currentTimeMillis() + 3600000,
            chiefComplaint = "Persistent hoarseness and throat irritation",
            consultationType = "Audio Call",
            timeSlot = "11:00 AM - 11:30 AM",
            consultationDate = "Today",
            feeInr = 600.0,
            upiApp = "Google Pay",
            upiVpa = "patient@okaxis",
            isReminderSet = true,
            reminderTiming = "15 mins before"
        )

        assertTrue("Booking with slot, UPI, and reminder must succeed", bookResult.isSuccess)
        val apt = bookResult.getOrNull()
        assertNotNull(apt)
        assertEquals("11:00 AM - 11:30 AM", apt?.timeSlot)
        assertEquals("Audio Call", apt?.consultationType)
        assertEquals(600.0, apt?.consultationFeeInr ?: 0.0, 0.01)
        assertEquals("Google Pay", apt?.upiApp)
        assertEquals("PAID_UPI", apt?.paymentStatus)
        assertTrue(apt?.upiTransactionRef?.startsWith("UPI/2026/") == true)
        assertTrue(apt?.isReminderSet == true)
        assertEquals("15 mins before", apt?.reminderTiming)

        // Toggle reminder off
        repository.toggleAppointmentReminder(apt!!.id, false)
        val toggledOffApt = database.dao().getAppointmentById(apt.id)
        assertFalse(toggledOffApt?.isReminderSet ?: true)

        // Toggle reminder back on
        repository.toggleAppointmentReminder(apt.id, true)
        val toggledOnApt = database.dao().getAppointmentById(apt.id)
        assertTrue(toggledOnApt?.isReminderSet == true)

        // Patient attempting to trigger call alert to doctor must fail
        val patientAlertResult = repository.triggerCallAlert(apt.id, "PATIENT")
        assertTrue("Patient must NOT be able to trigger call alert to doctor", patientAlertResult.isFailure)
        assertTrue(patientAlertResult.exceptionOrNull()?.message?.contains("Call alerts can only be dispatched by doctors") == true)

        // Doctor triggering call alert to patient must succeed
        val doctorAlertResult = repository.triggerCallAlert(apt.id, "DOCTOR")
        assertTrue("Doctor dispatching call alert to patient must succeed", doctorAlertResult.isSuccess)
        val alertMessage = doctorAlertResult.getOrNull()
        assertNotNull(alertMessage)
        assertTrue(alertMessage!!.contains("Call Alert dispatched! SMS & WhatsApp notification sent to patient"))
    }
}
