package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PrescriptionDao
import com.example.data.local.PrescriptionDatabase
import com.example.data.model.Prescription
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
class PrescriptionDatabaseTest {

    private lateinit var context: Context
    private lateinit var database: PrescriptionDatabase
    private lateinit var dao: PrescriptionDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, PrescriptionDatabase::class.java)
            .addMigrations(*PrescriptionDatabase.MIGRATION_STRATEGY)
            .allowMainThreadQueries()
            .build()
        dao = database.prescriptionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testPrescriptionDaoAbstractMethodAccess() {
        val prescriptionDao = database.prescriptionDao()
        assertNotNull("abstract fun prescriptionDao() must return valid DAO instance", prescriptionDao)
    }

    @Test
    fun testPrescriptionsTableAndSchemaMigrationStrategy() {
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='prescriptions'"
        )
        assertTrue("Table 'prescriptions' must be created by database setup", cursor.moveToFirst())
        assertEquals("prescriptions", cursor.getString(0))
        cursor.close()

        // Verify MIGRATION_1_2 definition and execution
        val migration = PrescriptionDatabase.MIGRATION_1_2
        assertEquals(1, migration.startVersion)
        assertEquals(2, migration.endVersion)
        assertNotNull("Migration strategy array must not be empty", PrescriptionDatabase.MIGRATION_STRATEGY)
        assertTrue(PrescriptionDatabase.MIGRATION_STRATEGY.contains(migration))

        // Execute migration on SQLite database to ensure valid DDL syntax
        val db = database.openHelper.writableDatabase
        migration.migrate(db)

        // Verify created index exists
        val indexCursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='index_prescriptions_patientId'"
        )
        assertTrue("Index 'index_prescriptions_patientId' must exist after migration", indexCursor.moveToFirst())
        indexCursor.close()
    }

    @Test
    fun testPrescriptionDatabaseOperations() = runBlocking {
        val rx = Prescription(
            id = "RX-ENGINE-101",
            appointmentId = "apt_${UUID.randomUUID()}",
            doctorId = "doc_jenkins_1",
            doctorName = "Dr. Sarah Jenkins",
            doctorQualifications = "MBBS, MD (Paediatrics)",
            doctorSpeciality = "Paediatric Medicine",
            councilRegistrationNumber = "NMC-2018-84920",
            clinicAffiliation = "Metro Children's Specialty Health Center",
            patientId = "patient_alex_1",
            patientName = "Alexander Wright",
            patientAge = 32,
            patientGender = "Male",
            patientWeightKg = 72.4,
            patientAllergies = "Penicillin",
            patientChronicConditions = "Mild Asthma",
            diagnosisICD10 = "J06.9 - Acute Upper Respiratory Infection",
            vitalsBp = "120/80 mmHg",
            vitalsPulse = "74 bpm",
            vitalsSpO2 = "99%",
            vitalsTemp = "98.6°F",
            medicationsJson = """[{"id":"m1","name":"Paracetamol 650mg","dosageForm":"Tab","dosageSchedule":"SOS","timing":"After Food","durationDays":3,"totalQuantity":6,"instructions":"For fever"}]""",
            clinicalAdviceJson = "Adequate hydration and rest",
            labOrdersJson = "None",
            followUpText = "Review in 5 days",
            followUpDateTimestamp = System.currentTimeMillis() + 5 * 24 * 3600 * 1000L,
            doctorSignatureHash = "f9a463c208c1a7925e51086bf87a10f994821e25d2b7042a98f5ceec54e8db03",
            verificationQrUrl = "https://mediconnect.pro/verify/rx/RX-ENGINE-101?hash=f9a463c2",
            createdAt = System.currentTimeMillis()
        )

        dao.insertPrescription(rx)

        val retrieved = dao.getPrescriptionById("RX-ENGINE-101")
        assertNotNull(retrieved)
        assertEquals("RX-ENGINE-101", retrieved?.id)
        assertEquals("Dr. Sarah Jenkins", retrieved?.doctorName)

        val count = dao.getPrescriptionCount().first()
        assertEquals(1, count)
    }

    @Test
    fun testSingletonDatabaseInitialization() {
        val instance1 = PrescriptionDatabase.getDatabase(context)
        val instance2 = PrescriptionDatabase.getDatabase(context)
        assertSame("getDatabase() must return singleton instance", instance1, instance2)
        assertNotNull(instance1.prescriptionDao())
    }
}
