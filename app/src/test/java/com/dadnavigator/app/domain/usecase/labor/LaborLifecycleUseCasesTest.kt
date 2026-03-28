package com.dadnavigator.app.domain.usecase.labor

import com.dadnavigator.app.domain.model.ActiveContractionState
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.ContractionSession
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.ThemeMode
import com.dadnavigator.app.domain.model.TimelineEntryType
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.domain.repository.ContractionRepository
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.repository.TimelineRepository
import com.dadnavigator.app.domain.repository.WaterBreakRepository
import com.dadnavigator.app.domain.service.StageTransitionManager
import com.dadnavigator.app.domain.usecase.contraction.FinishContractionUseCase
import com.dadnavigator.app.domain.usecase.contraction.StartContractionSessionUseCase
import com.dadnavigator.app.domain.usecase.contraction.StartContractionUseCase
import com.dadnavigator.app.domain.usecase.contraction.ToggleContractionResult
import com.dadnavigator.app.domain.usecase.contraction.ToggleContractionUseCase
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.AddWaterBreakEventUseCase
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests labor lifecycle transitions and duplicate-protection rules.
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
            eventTitle = "Начались схватки",
            eventDescription = "Схватки стали регулярными",
            timestamp = timestamp
        )

        assertEquals(MarkLaborStartedResult.Started, result)
        assertEquals(AppStage.LABOR, settingsRepository.current.appStage)
        assertEquals(timestamp, laborRepository.current.laborStartTime)
        assertEquals(1, timelineRepository.events.size)
        assertEquals(TimelineType.LABOR, timelineRepository.events.single().type)
        assertEquals("Начались схватки", timelineRepository.events.single().title)
    }

    @Test
    fun `toggle contraction starts session and moves app to labor`() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val laborRepository = FakeLaborRepository()
        val contractionRepository = FakeContractionRepository()
        val timelineRepository = FakeTimelineRepository()
        val toggleUseCase = ToggleContractionUseCase(
            contractionRepository = contractionRepository,
            startContractionSessionUseCase = StartContractionSessionUseCase(contractionRepository),
            startContractionUseCase = StartContractionUseCase(contractionRepository),
            finishContractionUseCase = FinishContractionUseCase(contractionRepository),
            addTimelineEventUseCase = AddTimelineEventUseCase(timelineRepository, settingsRepository),
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            stageTransitionManager = stageTransitionManager
        )

        val result = toggleUseCase(
            userId = DEFAULT_USER_ID,
            sessionId = null,
            activeContractionId = null
        )

        assertEquals(ToggleContractionResult.Started, result)
        assertEquals(AppStage.LABOR, settingsRepository.current.appStage)
        assertNotNull(contractionRepository.activeState.value.session)
        assertNotNull(contractionRepository.activeState.value.activeContraction)
    }

    @Test
    fun `adding water break moves app to labor`() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val laborRepository = FakeLaborRepository()
        val waterBreakRepository = FakeWaterBreakRepository()
        val useCase = AddWaterBreakEventUseCase(
            waterBreakRepository = waterBreakRepository,
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            stageTransitionManager = stageTransitionManager
        )
        val happenedAt = Instant.parse("2026-03-27T09:30:00Z")

        useCase(
            userId = DEFAULT_USER_ID,
            happenedAt = happenedAt,
            color = WaterColor.CLEAR,
            notes = "Прозрачные воды"
        )

        assertEquals(AppStage.LABOR, settingsRepository.current.appStage)
        assertEquals(happenedAt, waterBreakRepository.createdEvent?.happenedAt)
        assertEquals(WaterColor.CLEAR, waterBreakRepository.createdEvent?.color)
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
            eventTitle = "Начались схватки",
            eventDescription = "Повторная попытка",
            timestamp = Instant.parse("2026-03-27T08:15:00Z")
        )

        assertEquals(MarkLaborStartedResult.AlreadyStarted, result)
        assertEquals(AppStage.LABOR, settingsRepository.current.appStage)
        assertEquals(existingStart, laborRepository.current.laborStartTime)
        assertTrue("A duplicate labor event should not be created", timelineRepository.events.isEmpty())
    }

    @Test
    fun `mark labor started is blocked after birth`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.BABY_BORN)
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
            eventTitle = "Начались схватки",
            eventDescription = "Нельзя после рождения",
            timestamp = Instant.parse("2026-03-27T11:15:00Z")
        )

        assertEquals(MarkLaborStartedResult.BlockedAfterBirth, result)
        assertEquals(AppStage.BABY_BORN, settingsRepository.current.appStage)
        assertNull(laborRepository.current.laborStartTime)
        assertTrue(timelineRepository.events.isEmpty())
    }

    @Test
    fun `mark birth switches stage saves details and creates first birth event`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.LABOR)
        val laborRepository = FakeLaborRepository()
        val contractionRepository = FakeContractionRepository()
        val waterBreakRepository = FakeWaterBreakRepository()
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkBirthUseCase(
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            contractionRepository = contractionRepository,
            waterBreakRepository = waterBreakRepository,
            timelineRepository = timelineRepository,
            stageTransitionManager = stageTransitionManager
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

        assertEquals(AppStage.BABY_BORN, settingsRepository.current.appStage)
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
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.LABOR)
        val laborRepository = FakeLaborRepository(
            LaborSummary(
                laborStartTime = Instant.parse("2026-03-27T08:00:00Z"),
                birthTime = existingBirth,
                babyName = "Анна",
                birthWeightGrams = 3300,
                birthHeightCm = 51
            )
        )
        val contractionRepository = FakeContractionRepository()
        val waterBreakRepository = FakeWaterBreakRepository()
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkBirthUseCase(
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            contractionRepository = contractionRepository,
            waterBreakRepository = waterBreakRepository,
            timelineRepository = timelineRepository,
            stageTransitionManager = stageTransitionManager
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

        assertEquals(AppStage.BABY_BORN, settingsRepository.current.appStage)
        assertEquals(existingBirth, laborRepository.current.birthTime)
        assertEquals("Анна", laborRepository.current.babyName)
        assertEquals(3300, laborRepository.current.birthWeightGrams)
        assertEquals(51, laborRepository.current.birthHeightCm)
        assertTrue("A duplicate birth event should not be created", timelineRepository.events.isEmpty())
    }

    @Test
    fun `mark birth ignores blank optional baby name`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.LABOR)
        val laborRepository = FakeLaborRepository()
        val contractionRepository = FakeContractionRepository()
        val waterBreakRepository = FakeWaterBreakRepository()
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkBirthUseCase(
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            contractionRepository = contractionRepository,
            waterBreakRepository = waterBreakRepository,
            timelineRepository = timelineRepository,
            stageTransitionManager = stageTransitionManager
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
    fun `mark birth closes active contraction session and water break`() = runTest {
        val timestamp = Instant.parse("2026-03-27T10:45:00Z")
        val settingsRepository = FakeSettingsRepository(initialStage = AppStage.LABOR)
        val laborRepository = FakeLaborRepository()
        val contractionRepository = FakeContractionRepository(
            initialState = ActiveContractionState(
                session = ContractionSession(
                    id = 7L,
                    userId = DEFAULT_USER_ID,
                    startedAt = Instant.parse("2026-03-27T08:00:00Z"),
                    endedAt = null
                ),
                contractions = listOf(
                    Contraction(
                        id = 9L,
                        sessionId = 7L,
                        userId = DEFAULT_USER_ID,
                        startedAt = Instant.parse("2026-03-27T10:40:00Z"),
                        endedAt = null
                    )
                ),
                activeContraction = Contraction(
                    id = 9L,
                    sessionId = 7L,
                    userId = DEFAULT_USER_ID,
                    startedAt = Instant.parse("2026-03-27T10:40:00Z"),
                    endedAt = null
                )
            )
        )
        val waterBreakRepository = FakeWaterBreakRepository(
            initialActiveEvent = WaterBreakEvent(
                id = 5L,
                userId = DEFAULT_USER_ID,
                happenedAt = Instant.parse("2026-03-27T09:50:00Z"),
                color = WaterColor.CLEAR,
                notes = "",
                closedAt = null
            )
        )
        val timelineRepository = FakeTimelineRepository()
        val useCase = MarkBirthUseCase(
            settingsRepository = settingsRepository,
            laborRepository = laborRepository,
            contractionRepository = contractionRepository,
            waterBreakRepository = waterBreakRepository,
            timelineRepository = timelineRepository,
            stageTransitionManager = stageTransitionManager
        )

        useCase(
            userId = DEFAULT_USER_ID,
            eventTitle = "Ребенок родился",
            eventDescription = "",
            timestamp = timestamp
        )

        assertEquals(timestamp, contractionRepository.finishedContractionAt)
        assertEquals(timestamp, contractionRepository.finishedSessionAt)
        assertEquals(timestamp, waterBreakRepository.closedAt)
        assertNull(contractionRepository.activeState.value.activeContraction)
        assertEquals(timestamp, contractionRepository.activeState.value.session?.endedAt)
        assertEquals(timestamp, waterBreakRepository.activeEvent.value?.closedAt)
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
        type: TimelineType,
        stageAtCreation: AppStage,
        entryType: TimelineEntryType
    ) {
        events += TimelineEvent(
            id = (events.size + 1).toLong(),
            userId = userId,
            type = type,
            timestamp = timestamp,
            title = title,
            description = description,
            stageAtCreation = stageAtCreation,
            entryType = entryType
        )
    }
}

private class FakeContractionRepository(
    initialState: ActiveContractionState = ActiveContractionState(
        session = null,
        contractions = emptyList(),
        activeContraction = null
    )
) : ContractionRepository {

    val activeState = MutableStateFlow(initialState)
    private var nextSessionId = 100L
    private var nextContractionId = 200L
    var finishedContractionAt: Instant? = null
    var finishedSessionAt: Instant? = null

    override fun observeActiveState(userId: String): Flow<ActiveContractionState> = activeState

    override fun observeSessionHistory(userId: String): Flow<List<ContractionSession>> = flowOf(
        activeState.value.session?.let(::listOf) ?: emptyList()
    )

    override suspend fun startSession(userId: String, startedAt: Instant): Long {
        val session = ContractionSession(
            id = nextSessionId++,
            userId = userId,
            startedAt = startedAt,
            endedAt = null
        )
        activeState.value = activeState.value.copy(session = session)
        return session.id
    }

    override suspend fun finishSession(sessionId: Long, endedAt: Instant) {
        finishedSessionAt = endedAt
        activeState.value = activeState.value.copy(
            session = activeState.value.session?.copy(endedAt = endedAt)
        )
    }

    override suspend fun startContraction(sessionId: Long, userId: String, startedAt: Instant): Long {
        val contraction = Contraction(
            id = nextContractionId++,
            sessionId = sessionId,
            userId = userId,
            startedAt = startedAt,
            endedAt = null
        )
        activeState.value = activeState.value.copy(
            contractions = activeState.value.contractions + contraction,
            activeContraction = contraction
        )
        return contraction.id
    }

    override suspend fun finishContraction(contractionId: Long, endedAt: Instant) {
        finishedContractionAt = endedAt
        activeState.value = activeState.value.copy(
            contractions = activeState.value.contractions.map { contraction ->
                if (contraction.id == contractionId) contraction.copy(endedAt = endedAt) else contraction
            },
            activeContraction = null
        )
    }

    override suspend fun deleteContraction(contractionId: Long) {
        activeState.value = activeState.value.copy(
            contractions = activeState.value.contractions.filterNot { it.id == contractionId },
            activeContraction = activeState.value.activeContraction?.takeUnless { it.id == contractionId }
        )
    }
}

private class FakeWaterBreakRepository(
    initialActiveEvent: WaterBreakEvent? = null
) : WaterBreakRepository {

    val activeEvent = MutableStateFlow(initialActiveEvent)
    var createdEvent: WaterBreakEvent? = null
    var closedAt: Instant? = null

    override fun observeActiveEvent(userId: String): Flow<WaterBreakEvent?> = activeEvent

    override fun observeHistory(userId: String): Flow<List<WaterBreakEvent>> = flowOf(
        activeEvent.value?.let(::listOf) ?: emptyList()
    )

    override suspend fun createEvent(
        userId: String,
        happenedAt: Instant,
        color: WaterColor,
        notes: String
    ): Long {
        createdEvent = WaterBreakEvent(
            id = 1L,
            userId = userId,
            happenedAt = happenedAt,
            color = color,
            notes = notes,
            closedAt = null
        )
        activeEvent.value = createdEvent
        return 1L
    }

    override suspend fun closeActiveEvent(userId: String, closedAt: Instant) {
        this.closedAt = closedAt
        activeEvent.value = activeEvent.value?.copy(closedAt = closedAt)
    }

    override suspend fun deleteEvent(eventId: Long) {
        if (activeEvent.value?.id == eventId) {
            activeEvent.value = null
        }
        if (createdEvent?.id == eventId) {
            createdEvent = null
        }
    }
}




