package com.dadnavigator.app.domain.usecase.labor

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.ThemeMode
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.repository.TimelineRepository
import com.dadnavigator.app.domain.service.StageTransitionManager
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests manual labor lifecycle transitions and duplicate-protection rules.
 */
class LaborLifecycleUseCasesTest {

    private val stageTransitionManager = StageTransitionManager()

    @Test
    fun `mark labor started switches app stage and creates first labor event`() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val laborRepository = FakeLaborRepository()
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkLaborStartedUseCase(
            settingsRepository,
            laborRepository,
            timelineRepository,
            stageTransitionManager
        )
        val timestamp = Instant.parse("2026-03-27T08:15:00Z")

        val result = useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Начались роды",
            eventDescription = "Схватки стали регулярными",
            timestamp = timestamp
        )

        assertEquals(MarkLaborStartedResult.Started, result)
        assertEquals(AppStage.CONTRACTIONS, settingsRepository.current.appStage)
        assertEquals(timestamp, laborRepository.current.laborStartTime)
        assertEquals(1, timelineRepository.events.size)
        assertEquals(TimelineType.LABOR, timelineRepository.events.single().type)
        assertEquals("Начались роды", timelineRepository.events.single().title)
    }

    @Test
    fun `mark labor started does not overwrite existing start time or duplicate event`() = runTest {
        val existingStart = Instant.parse("2026-03-27T07:50:00Z")
        val settingsRepository = FakeSettingsRepository()
        val laborRepository = FakeLaborRepository(
            LaborSummary(
                laborStartTime = existingStart,
                birthTime = null,
                babyName = null,
                birthWeightGrams = null,
                birthHeightCm = null
            )
        )
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkLaborStartedUseCase(
            settingsRepository,
            laborRepository,
            timelineRepository,
            stageTransitionManager
        )

        val result = useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Начались роды",
            eventDescription = "Повторная попытка",
            timestamp = Instant.parse("2026-03-27T08:15:00Z")
        )

        assertEquals(MarkLaborStartedResult.AlreadyStarted, result)
        assertEquals(AppStage.CONTRACTIONS, settingsRepository.current.appStage)
        assertEquals(existingStart, laborRepository.current.laborStartTime)
        assertTrue("A duplicate labor event should not be created", timelineRepository.events.isEmpty())
    }

    @Test
    fun `mark labor started is blocked after birth`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.AT_HOSPITAL)
        val laborRepository = FakeLaborRepository(
            LaborSummary(
                laborStartTime = null,
                birthTime = Instant.parse("2026-03-27T10:10:00Z"),
                babyName = null,
                birthWeightGrams = null,
                birthHeightCm = null
            )
        )
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkLaborStartedUseCase(
            settingsRepository,
            laborRepository,
            timelineRepository,
            stageTransitionManager
        )

        val result = useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Начались роды",
            eventDescription = "Нельзя после рождения",
            timestamp = Instant.parse("2026-03-27T11:15:00Z")
        )

        assertEquals(MarkLaborStartedResult.BlockedAfterBirth, result)
        assertEquals(AppStage.AT_HOSPITAL, settingsRepository.current.appStage)
        assertNull(laborRepository.current.laborStartTime)
        assertTrue(timelineRepository.events.isEmpty())
    }

    @Test
    fun `mark birth switches stage saves details and creates first birth event`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.CONTRACTIONS)
        val laborRepository = FakeLaborRepository()
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkBirthUseCase(
            settingsRepository,
            laborRepository,
            timelineRepository,
            stageTransitionManager
        )
        val timestamp = Instant.parse("2026-03-27T10:45:00Z")

        useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Ребенок родился",
            eventDescription = "Все прошло спокойно",
            timestamp = timestamp,
            babyName = "Миша",
            birthWeightGrams = 3450,
            birthHeightCm = 52
        )

        assertEquals(AppStage.AT_HOSPITAL, settingsRepository.current.appStage)
        assertEquals(timestamp, laborRepository.current.birthTime)
        assertEquals("Миша", laborRepository.current.babyName)
        assertEquals(3450, laborRepository.current.birthWeightGrams)
        assertEquals(52, laborRepository.current.birthHeightCm)
        assertEquals(1, timelineRepository.events.size)
        assertEquals(TimelineType.BIRTH, timelineRepository.events.single().type)
    }

    @Test
    fun `mark birth preserves existing birth data and avoids duplicate event`() = runTest {
        val existingBirth = Instant.parse("2026-03-27T10:10:00Z")
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.CONTRACTIONS)
        val laborRepository = FakeLaborRepository(
            LaborSummary(
                laborStartTime = Instant.parse("2026-03-27T08:00:00Z"),
                birthTime = existingBirth,
                babyName = "Анна",
                birthWeightGrams = 3300,
                birthHeightCm = 51
            )
        )
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkBirthUseCase(
            settingsRepository,
            laborRepository,
            timelineRepository,
            stageTransitionManager
        )

        useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Ребенок родился",
            eventDescription = "Повторное сохранение",
            timestamp = Instant.parse("2026-03-27T10:45:00Z"),
            babyName = " ",
            birthWeightGrams = null,
            birthHeightCm = null
        )

        assertEquals(AppStage.AT_HOSPITAL, settingsRepository.current.appStage)
        assertEquals(existingBirth, laborRepository.current.birthTime)
        assertEquals("Анна", laborRepository.current.babyName)
        assertEquals(3300, laborRepository.current.birthWeightGrams)
        assertEquals(51, laborRepository.current.birthHeightCm)
        assertTrue("A duplicate birth event should not be created", timelineRepository.events.isEmpty())
    }

    @Test
    fun `mark birth ignores blank optional baby name`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.CONTRACTIONS)
        val laborRepository = FakeLaborRepository()
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkBirthUseCase(
            settingsRepository,
            laborRepository,
            timelineRepository,
            stageTransitionManager
        )

        useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Ребенок родился",
            eventDescription = "",
            babyName = "   ",
            birthWeightGrams = null,
            birthHeightCm = null
        )

        assertNull(laborRepository.current.babyName)
        assertEquals(1, timelineRepository.events.size)
    }

    @Test
    fun `mark arrived home requires birth and hospital stage`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.CONTRACTIONS)
        val laborRepository = FakeLaborRepository()
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkArrivedHomeUseCase(
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            stageTransitionManager = stageTransitionManager,
            addTimelineEventUseCase = AddTimelineEventUseCase(timelineRepository)
        )

        val result = useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Приехали домой",
            eventDescription = ""
        )

        assertEquals(MarkArrivedHomeResult.BirthNotRecorded, result)
        assertEquals(AppStage.CONTRACTIONS, settingsRepository.current.appStage)
        assertTrue(timelineRepository.events.isEmpty())
    }

    @Test
    fun `mark arrived home switches stage only from hospital flow`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.AT_HOSPITAL)
        val laborRepository = FakeLaborRepository(
            LaborSummary(
                laborStartTime = Instant.parse("2026-03-27T08:00:00Z"),
                birthTime = Instant.parse("2026-03-27T10:10:00Z"),
                babyName = null,
                birthWeightGrams = null,
                birthHeightCm = null
            )
        )
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkArrivedHomeUseCase(
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            stageTransitionManager = stageTransitionManager,
            addTimelineEventUseCase = AddTimelineEventUseCase(timelineRepository)
        )

        val result = useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Приехали домой",
            eventDescription = "Домой после выписки"
        )

        assertEquals(MarkArrivedHomeResult.Marked, result)
        assertEquals(AppStage.AT_HOME, settingsRepository.current.appStage)
        assertEquals(1, timelineRepository.events.size)
    }
}

private class FakeSettingsRepository(
    initialStage: AppStage = AppStage.PREPARING
) : SettingsRepository {

    private val state = MutableStateFlow(
        Settings(
            userId = DEFAULT_USER_ID,
            themeMode = ThemeMode.SYSTEM,
            fatherName = "",
            dueDate = LocalDate.parse("2026-04-15"),
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
        state.value = current.copy(
            fatherName = "",
            dueDate = null,
            maternityHospitalAddress = "",
            appStage = AppStage.PREPARING
        )
    }
}

private class FakeLaborRepository(
    initialSummary: LaborSummary = LaborSummary(
        laborStartTime = null,
        birthTime = null,
        babyName = null,
        birthWeightGrams = null,
        birthHeightCm = null
    )
) : LaborRepository {

    private val state = MutableStateFlow(initialSummary)

    val current: LaborSummary
        get() = state.value

    override fun observeLaborSummary(userId: String): Flow<LaborSummary> = state

    override suspend fun saveLaborSummary(userId: String, summary: LaborSummary) {
        state.value = summary
    }
}

private class FakeTimelineRepository : TimelineRepository {

    val events = mutableListOf<TimelineEvent>()

    override fun observeTimeline(userId: String): Flow<List<TimelineEvent>> = flowOf(events.toList())

    override suspend fun addEvent(event: TimelineEvent) {
        events += event
    }

    override suspend fun addEvent(
        userId: String,
        timestamp: Instant,
        title: String,
        description: String,
        type: TimelineType
    ) {
        events += TimelineEvent(
            id = (events.size + 1).toLong(),
            userId = userId,
            type = type,
            timestamp = timestamp,
            title = title,
            description = description
        )
    }
}
