package com.dadnavigator.app.domain.usecase.settings

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.ThemeMode
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.service.StageTransitionManager
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SettingsStageGuardUseCasesTest {

    private val transitionManager = StageTransitionManager()

    @Test
    fun `update app stage allows returning to labor after birth`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.BABY_BORN)
        val laborRepository = FakeLaborRepository(
            LaborSummary(
                laborStartTime = Instant.parse("2026-03-27T08:00:00Z"),
                birthTime = Instant.parse("2026-03-27T10:10:00Z"),
                babyName = null,
                birthWeightGrams = null,
                birthHeightCm = null
            )
        )
        val useCase = UpdateAppStageUseCase(
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            stageTransitionManager = transitionManager
        )

        val result = useCase(AppStage.LABOR)

        assertFalse(result.blockedByBirthRecord)
        assertEquals(AppStage.LABOR, settingsRepository.current.appStage)
    }

    @Test
    fun `save settings persists manually selected stage after birth`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.BABY_BORN)
        val laborRepository = FakeLaborRepository(
            LaborSummary(
                laborStartTime = Instant.parse("2026-03-27T08:00:00Z"),
                birthTime = Instant.parse("2026-03-27T10:10:00Z"),
                babyName = "Миша",
                birthWeightGrams = 3450,
                birthHeightCm = 52
            )
        )
        val useCase = SaveSettingsUseCase(
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            stageTransitionManager = transitionManager
        )

        val result = useCase(
            settingsRepository.current.copy(
                fatherName = "Алексей",
                appStage = AppStage.PREPARING
            )
        )

        assertFalse(result.blockedByBirthRecord)
        assertEquals(AppStage.PREPARING, settingsRepository.current.appStage)
        assertEquals("Алексей", settingsRepository.current.fatherName)
    }
}

private class FakeSettingsRepository(
    initialStage: AppStage
) : SettingsRepository {

    private val state = MutableStateFlow(
        Settings(
            userId = DEFAULT_USER_ID,
            themeMode = ThemeMode.SYSTEM,
            fatherName = "",
            dueDate = LocalDate.of(2026, 4, 15),
            maternityHospitalAddress = "",
            notificationsEnabled = true,
            appStage = initialStage
        )
    )

    val current: Settings
        get() = state.value

    override fun observeSettings(): Flow<Settings> = state

    override suspend fun saveSettings(settings: Settings) {
        state.value = settings
    }

    override suspend fun clearAllData() {
        state.value = current.copy(appStage = AppStage.PREPARING)
    }
}

private class FakeLaborRepository(
    initialSummary: LaborSummary
) : LaborRepository {

    private val state = MutableStateFlow(initialSummary)

    override fun observeLaborSummary(userId: String): Flow<LaborSummary> = state

    override suspend fun saveLaborSummary(userId: String, summary: LaborSummary) {
        state.value = summary
    }
}


