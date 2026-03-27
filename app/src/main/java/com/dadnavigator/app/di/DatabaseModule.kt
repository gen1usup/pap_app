package com.dadnavigator.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dadnavigator.app.data.local.AppDatabase
import com.dadnavigator.app.data.local.dao.ChecklistDao
import com.dadnavigator.app.data.local.dao.ContractionDao
import com.dadnavigator.app.data.local.dao.EmergencyContactDao
import com.dadnavigator.app.data.local.dao.LaborDao
import com.dadnavigator.app.data.local.dao.SettingsDao
import com.dadnavigator.app.data.local.dao.TimelineDao
import com.dadnavigator.app.data.local.dao.TrackerDao
import com.dadnavigator.app.data.local.dao.UserDao
import com.dadnavigator.app.data.local.dao.WaterBreakDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides Room database and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val migration1To3 = object : Migration(1, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            recreateSettingsTable(db)
            ensureLaborSummaryHasBabyName(db)
            recreateChecklistsTable(db)
            ensureEmergencyContactsTable(db)
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            recreateSettingsTable(db)
            ensureLaborSummaryHasBabyName(db)
            recreateChecklistsTable(db)
            ensureEmergencyContactsTable(db)
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            recreateSettingsTable(db)
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dad_navigator.db"
        ).addMigrations(
            migration1To3,
            migration2To3,
            migration3To4
        ).build()
    }

    @Provides
    fun provideContractionDao(database: AppDatabase): ContractionDao = database.contractionDao()

    @Provides
    fun provideWaterBreakDao(database: AppDatabase): WaterBreakDao = database.waterBreakDao()

    @Provides
    fun provideTimelineDao(database: AppDatabase): TimelineDao = database.timelineDao()

    @Provides
    fun provideChecklistDao(database: AppDatabase): ChecklistDao = database.checklistDao()

    @Provides
    fun provideTrackerDao(database: AppDatabase): TrackerDao = database.trackerDao()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @Provides
    fun provideLaborDao(database: AppDatabase): LaborDao = database.laborDao()

    @Provides
    fun provideEmergencyContactDao(database: AppDatabase): EmergencyContactDao = database.emergencyContactDao()

    private fun recreateSettingsTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS settings_new (
                userId TEXT NOT NULL,
                themeMode TEXT NOT NULL,
                fatherName TEXT NOT NULL,
                dueDateEpochDay INTEGER,
                maternityHospitalAddress TEXT NOT NULL,
                notificationsEnabled INTEGER NOT NULL,
                appStage TEXT NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(userId)
            )
            """.trimIndent()
        )

        val appStageExpression = if (db.hasColumn("settings", "appStage")) {
            "appStage"
        } else {
            "'PREPARING'"
        }
        val maternityHospitalAddressExpression = if (db.hasColumn("settings", "maternityHospitalAddress")) {
            "maternityHospitalAddress"
        } else {
            "''"
        }

        db.execSQL(
            """
            INSERT INTO settings_new (
                userId,
                themeMode,
                fatherName,
                dueDateEpochDay,
                maternityHospitalAddress,
                notificationsEnabled,
                appStage,
                updatedAt
            )
            SELECT
                userId,
                themeMode,
                fatherName,
                dueDateEpochDay,
                $maternityHospitalAddressExpression,
                notificationsEnabled,
                $appStageExpression,
                updatedAt
            FROM settings
            """.trimIndent()
        )

        db.execSQL("DROP TABLE settings")
        db.execSQL("ALTER TABLE settings_new RENAME TO settings")
    }

    private fun recreateChecklistsTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS checklists_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT NOT NULL,
                title TEXT NOT NULL,
                stage TEXT NOT NULL,
                category TEXT NOT NULL,
                isSystem INTEGER NOT NULL,
                sortOrder INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )

        val titleExpression = if (db.hasColumn("checklists", "title")) {
            "title"
        } else {
            "name"
        }
        val stageExpression = if (db.hasColumn("checklists", "stage")) {
            "stage"
        } else {
            "'PREPARING'"
        }
        val categoryExpression = if (db.hasColumn("checklists", "category")) {
            "category"
        } else {
            titleExpression
        }
        val sortOrderExpression = if (db.hasColumn("checklists", "sortOrder")) {
            "sortOrder"
        } else {
            "0"
        }

        db.execSQL(
            """
            INSERT INTO checklists_new (
                id,
                userId,
                title,
                stage,
                category,
                isSystem,
                sortOrder,
                createdAt
            )
            SELECT
                id,
                userId,
                COALESCE($titleExpression, ''),
                COALESCE($stageExpression, 'PREPARING'),
                COALESCE($categoryExpression, COALESCE($titleExpression, '')),
                isSystem,
                COALESCE($sortOrderExpression, 0),
                createdAt
            FROM checklists
            """.trimIndent()
        )

        db.execSQL("DROP TABLE checklists")
        db.execSQL("ALTER TABLE checklists_new RENAME TO checklists")
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_checklists_userId ON checklists(userId)
            """.trimIndent()
        )
    }

    private fun ensureLaborSummaryHasBabyName(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("labor_summary", "babyName")) {
            db.execSQL(
                """
                ALTER TABLE labor_summary ADD COLUMN babyName TEXT
                """.trimIndent()
            )
        }
    }

    private fun ensureEmergencyContactsTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS emergency_contacts (
                type TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                phone TEXT NOT NULL,
                sortOrder INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun SupportSQLiteDatabase.hasColumn(
        tableName: String,
        columnName: String
    ): Boolean {
        query("PRAGMA table_info($tableName)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == columnName) {
                    return true
                }
            }
        }
        return false
    }
}
