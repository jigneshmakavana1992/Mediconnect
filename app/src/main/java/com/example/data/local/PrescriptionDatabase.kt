package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Prescription

/**
 * Room Database for the Clinical Prescription Generation Engine (RapidRx Studio).
 * Provides an abstract method to access [PrescriptionDao] and defines explicit migration
 * strategies for initial database setup and subsequent schema evolutions.
 */
@Database(
    entities = [Prescription::class],
    version = 2,
    exportSchema = false
)
abstract class PrescriptionDatabase : RoomDatabase() {

    /**
     * Abstract method to access the Prescriptions DAO.
     */
    abstract fun prescriptionDao(): PrescriptionDao

    companion object {
        const val DATABASE_NAME = "prescriptions_engine_db"

        @Volatile
        private var INSTANCE: PrescriptionDatabase? = null

        /**
         * Migration strategy from version 1 to 2:
         * Adds performance indexes on foreign and search keys (patientId, doctorId, appointmentId, createdAt)
         * to support low-latency clinical lookups and audit trails.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_prescriptions_patientId` ON `prescriptions` (`patientId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_prescriptions_doctorId` ON `prescriptions` (`doctorId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_prescriptions_appointmentId` ON `prescriptions` (`appointmentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_prescriptions_createdAt` ON `prescriptions` (`createdAt`)")
            }
        }

        /**
         * Array of all defined migration strategies for the database setup.
         */
        val MIGRATION_STRATEGY: Array<Migration> = arrayOf(MIGRATION_1_2)

        /**
         * Returns the singleton instance of [PrescriptionDatabase].
         * Configures the database builder with explicit migrations and destructive fallback
         * as a safety strategy for corrupted or mismatched initial setups.
         */
        fun getDatabase(context: Context): PrescriptionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrescriptionDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*MIGRATION_STRATEGY)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Type alias for AppDatabase to satisfy general Room persistence references.
 */
typealias AppDatabase = PrescriptionDatabase
