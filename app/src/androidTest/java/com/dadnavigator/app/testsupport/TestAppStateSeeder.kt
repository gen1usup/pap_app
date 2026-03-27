package com.dadnavigator.app.testsupport

import android.content.Context
import androidx.room.Room
import com.dadnavigator.app.data.local.AppDatabase
import com.dadnavigator.app.data.local.datastore.SettingsDataStore
import com.dadnavigator.app.data.local.entity.ContractionEntity
import com.dadnavigator.app.data.local.entity.ContractionSessionEntity
import com.dadnavigator.app.data.local.entity.LaborSummaryEntity
import com.dadnavigator.app.data.local.entity.SettingsEntity
import com.dadnavigator.app.data.local.entity.UserEntity
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.ThemeMode
import java.time.Instant
import kotlinx.coroutines.runBlocking

/**
 * Seeds target app storage with deterministic state for instrumented tests.
 *
 * The helper intentionally writes final timestamps directly so UI tests can
 * validate 30-90 minute contraction scenarios without waiting in real time.
 */
class TestAppStateSeeder(
    private val context: Context
) {

    fun clearAllData() = runBlocking {
        val database = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()

        try {
            database.clearAllTables()
            SettingsDataStore(context).clear()
        } finally {
            database.close()
        }
    }

    fun seedContractionScenario(
        scenario: ContractionScenario,
        appStage: AppStage = AppStage.LABOR,
        userId: String = DEFAULT_USER_ID
    ) = runBlocking {
        val database = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()

        try {
            database.clearAllTables()

            val settings = Settings(
                userId = userId,
                themeMode = ThemeMode.SYSTEM,
                fatherName = "",
                dueDate = null,
                maternityHospitalAddress = "",
                notificationsEnabled = true,
                appStage = appStage
            )
            SettingsDataStore(context).saveSettings(settings)

            database.userDao().upsertUser(
                UserEntity(
                    id = userId,
                    displayName = "",
                    createdAt = Instant.EPOCH
                )
            )
            database.settingsDao().upsertSettings(
                SettingsEntity(
                    userId = userId,
                    themeMode = ThemeMode.SYSTEM.name,
                    fatherName = "",
                    dueDateEpochDay = null,
                    maternityHospitalAddress = "",
                    notificationsEnabled = true,
                    appStage = appStage.name,
                    updatedAt = Instant.now()
                )
            )
            database.laborDao().upsertLaborSummary(
                LaborSummaryEntity(
                    userId = userId,
                    laborStartTime = null,
                    birthTime = null,
                    babyName = null,
                    birthWeightGrams = null,
                    birthHeightCm = null
                )
            )

            val sessionId = database.contractionDao().insertSession(
                ContractionSessionEntity(
                    userId = userId,
                    startedAt = scenario.sessionStartedAt,
                    endedAt = null
                )
            )

            scenario.contractions.forEach { seed ->
                database.contractionDao().insertContraction(
                    ContractionEntity(
                        sessionId = sessionId,
                        userId = userId,
                        startedAt = seed.startedAt,
                        endedAt = seed.endedAt
                    )
                )
            }
        } finally {
            database.close()
        }
    }

    companion object {
        private const val DATABASE_NAME = "dad_navigator.db"
    }
}

data class ContractionSeed(
    val startedAt: Instant,
    val endedAt: Instant
)

data class ContractionScenario(
    val sessionStartedAt: Instant,
    val contractions: List<ContractionSeed>
) {
    companion object {
        fun monitorIrregular(now: Instant = Instant.now()): ContractionScenario {
            val starts = listOf(
                now.minusSeconds(40 * 60),
                now.minusSeconds(28 * 60),
                now.minusSeconds(26 * 60),
                now.minusSeconds(14 * 60),
                now.minusSeconds(12 * 60),
                now.minusSeconds(2 * 60)
            )
            return fromDurations(starts, listOf(70, 71, 69, 70, 70, 72))
        }

        fun prepareRegular(now: Instant = Instant.now()): ContractionScenario {
            val starts = listOf(
                now.minusSeconds(28 * 60),
                now.minusSeconds(21 * 60),
                now.minusSeconds(14 * 60),
                now.minusSeconds(7 * 60),
                now
            )
            return fromDurations(starts, listOf(50, 52, 50, 49, 51))
        }

        fun goRegular(now: Instant = Instant.now()): ContractionScenario {
            val starts = listOf(
                now.minusSeconds(30 * 60),
                now.minusSeconds(25 * 60),
                now.minusSeconds(20 * 60),
                now.minusSeconds(15 * 60),
                now.minusSeconds(10 * 60),
                now.minusSeconds(5 * 60),
                now
            )
            return fromDurations(starts, listOf(70, 72, 75, 70, 74, 76, 73))
        }

        private fun fromDurations(
            starts: List<Instant>,
            durationSeconds: List<Long>
        ): ContractionScenario {
            require(starts.size == durationSeconds.size) {
                "Starts and durations must contain the same number of elements"
            }
            val contractions = starts.zip(durationSeconds).map { (start, seconds) ->
                ContractionSeed(
                    startedAt = start,
                    endedAt = start.plusSeconds(seconds)
                )
            }
            return ContractionScenario(
                sessionStartedAt = starts.first(),
                contractions = contractions
            )
        }
    }
}
