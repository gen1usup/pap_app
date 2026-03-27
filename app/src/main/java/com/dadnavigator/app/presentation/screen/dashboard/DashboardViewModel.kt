package com.dadnavigator.app.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.ActiveContractionState
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.ContractionStats
import com.dadnavigator.app.domain.model.ContractionTrend
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.service.HomeContentBuilder
import com.dadnavigator.app.domain.usecase.checklist.ObserveChecklistsUseCase
import com.dadnavigator.app.domain.usecase.contraction.CalculateContractionStatsUseCase
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkLaborStartedUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkLaborStartedResult
import com.dadnavigator.app.domain.service.StageManager
import com.dadnavigator.app.domain.usecase.contraction.ToggleContractionResult
import com.dadnavigator.app.domain.usecase.contraction.ToggleContractionUseCase
import com.dadnavigator.app.domain.usecase.settings.ObserveSettingsUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveLaborSummaryUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveTimelineUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveActiveWaterBreakUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Builds the contextual home state from app stage, due date and active events.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val observeContractionStateUseCase: ObserveContractionStateUseCase,
    private val observeActiveWaterBreakUseCase: ObserveActiveWaterBreakUseCase,
    private val observeChecklistsUseCase: ObserveChecklistsUseCase,
    private val observeTimelineUseCase: ObserveTimelineUseCase,
    private val observeLaborSummaryUseCase: ObserveLaborSummaryUseCase,
    private val markLaborStartedUseCase: MarkLaborStartedUseCase,
    private val toggleContractionUseCase: ToggleContractionUseCase,
    private val calculateContractionStatsUseCase: CalculateContractionStatsUseCase,
    private val homeContentBuilder: HomeContentBuilder,
    private val stageManager: StageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow(DEFAULT_USER_ID)
    private val infoState = MutableStateFlow<Int?>(null)
    private val errorState = MutableStateFlow<Int?>(null)

    private val ticker = flow {
        while (true) {
            emit(Instant.now())
            delay(1_000)
        }
    }

    val uiState = userIdState
        .flatMapLatest { userId ->
            val dashboardBase = combine(
                observeSettingsUseCase(),
                observeContractionStateUseCase(userId),
                observeActiveWaterBreakUseCase(userId),
                observeChecklistsUseCase(userId),
                observeTimelineUseCase(userId)
            ) { settings, contractionState, waterBreakEvent, checklists, timeline ->
                DashboardBaseState(
                    settings = settings,
                    contractionState = contractionState,
                    isWaterBreakActive = waterBreakEvent?.isActive == true,
                    stageChecklistCompleted = checklists
                        .filter { it.checklist.stage == settings.appStage }
                        .sumOf { it.completedCount },
                    stageChecklistTotal = checklists
                        .filter { it.checklist.stage == settings.appStage }
                        .sumOf { it.totalCount },
                    recentEvents = timeline.take(4)
                )
            }

            combine(
                dashboardBase,
                observeActiveWaterBreakUseCase(userId),
                observeLaborSummaryUseCase(userId),
                ticker
            ) { base, waterBreakEvent, laborSummary, now ->
                val settings = base.settings
                val contractionStats = calculateContractionStatsUseCase(base.contractionState.contractions)
                val stageInfo = stageManager.buildStageInfo(
                    settings = settings,
                    laborSummary = laborSummary,
                    today = LocalDate.now()
                )
                val homeContent = homeContentBuilder.build(
                    settings = settings,
                    stageInfo = stageInfo,
                    laborSummary = laborSummary,
                    hasActiveContractionSession = base.contractionState.session?.isActive == true,
                    hasActiveWaterBreak = base.isWaterBreakActive
                )
                DashboardUiState(
                    fatherName = settings.fatherName,
                    appStage = settings.appStage,
                    dueDate = settings.dueDate,
                    daysUntilDueDate = stageInfo.daysUntilDueDate,
                    hasActiveContractionSession = base.contractionState.session?.isActive == true,
                    contractionSessionId = base.contractionState.session?.id,
                    activeContractionId = base.contractionState.activeContraction?.id,
                    isContractionRunning = base.contractionState.activeContraction != null,
                    currentContractionDuration = base.contractionState.activeContraction?.let {
                        Duration.between(it.startedAt, now)
                    } ?: Duration.ZERO,
                    currentInterval = currentInterval(
                        contractionState = base.contractionState,
                        now = now
                    ),
                    contractionStats = contractionStats,
                    hasActiveWaterBreak = base.isWaterBreakActive,
                    waterBreakElapsed = waterBreakEvent?.elapsed(now),
                    laborSummary = laborSummary,
                    stageChecklistCompletedCount = base.stageChecklistCompleted,
                    stageChecklistTotalCount = base.stageChecklistTotal,
                    recentEvents = base.recentEvents,
                    showDueDateReminder = homeContent.showDueDateReminder,
                    showContractionShortcut = homeContent.showContractionShortcut,
                    showLiveContractionBlock = homeContent.showLiveContractionBlock,
                    showWaterBreakShortcut = homeContent.showWaterBreakShortcut,
                    showBirthDetailsCard = homeContent.showBirthDetailsCard,
                    checklistFirst = homeContent.checklistFirst,
                    showLaborQuickActions = homeContent.showLaborQuickActions,
                    infoRes = null,
                    errorRes = null
                )
            }.combine(infoState) { partial, infoRes ->
                partial.copy(infoRes = infoRes)
            }.combine(errorState) { partial, errorRes ->
                partial.copy(errorRes = errorRes)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun markLaborStarted(eventTitle: String, eventDescription: String) {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                when (markLaborStartedUseCase(
                    userId = userId,
                    eventTitle = eventTitle,
                    eventDescription = eventDescription
                )) {
                    MarkLaborStartedResult.Started -> {
                        infoState.value = R.string.events_labor_started_saved
                    }
                    MarkLaborStartedResult.AlreadyStarted -> {
                        infoState.value = R.string.events_labor_started_already_saved
                    }
                    MarkLaborStartedResult.BlockedAfterBirth -> {
                        infoState.value = R.string.events_labor_start_blocked_after_birth
                    }
                }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun toggleContraction() {
        val userId = userIdState.value
        val currentState = uiState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                infoState.value = when (
                    toggleContractionUseCase(
                        userId = userId,
                        sessionId = currentState.contractionSessionId,
                        activeContractionId = currentState.activeContractionId
                    )
                ) {
                    ToggleContractionResult.Started -> R.string.events_contraction_started
                    ToggleContractionResult.Stopped -> R.string.events_contraction_stopped
                }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissError() {
        infoState.value = null
        errorState.value = null
    }
}

data class DashboardUiState(
    val fatherName: String = "",
    val appStage: AppStage = AppStage.PREPARING,
    val dueDate: LocalDate? = null,
    val daysUntilDueDate: Long? = null,
    val hasActiveContractionSession: Boolean = false,
    val contractionSessionId: Long? = null,
    val activeContractionId: Long? = null,
    val isContractionRunning: Boolean = false,
    val currentContractionDuration: Duration = Duration.ZERO,
    val currentInterval: Duration? = null,
    val contractionStats: ContractionStats = ContractionStats(
        count = 0,
        averageDuration = null,
        averageInterval = null,
        lastInterval = null,
        trend = ContractionTrend.INSUFFICIENT_DATA,
        recommendationLevel = RecommendationLevel.MONITOR
    ),
    val hasActiveWaterBreak: Boolean = false,
    val waterBreakElapsed: Duration? = null,
    val laborSummary: LaborSummary = LaborSummary(
        laborStartTime = null,
        birthTime = null,
        babyName = null,
        birthWeightGrams = null,
        birthHeightCm = null
    ),
    val stageChecklistCompletedCount: Int = 0,
    val stageChecklistTotalCount: Int = 0,
    val recentEvents: List<TimelineEvent> = emptyList(),
    val showDueDateReminder: Boolean = true,
    val showContractionShortcut: Boolean = false,
    val showLiveContractionBlock: Boolean = false,
    val showWaterBreakShortcut: Boolean = false,
    val showBirthDetailsCard: Boolean = false,
    val checklistFirst: Boolean = true,
    val showLaborQuickActions: Boolean = false,
    val infoRes: Int? = null,
    val errorRes: Int? = null
)

private data class DashboardBaseState(
    val settings: Settings,
    val contractionState: ActiveContractionState,
    val isWaterBreakActive: Boolean,
    val stageChecklistCompleted: Int,
    val stageChecklistTotal: Int,
    val recentEvents: List<TimelineEvent>
)

private fun currentInterval(
    contractionState: ActiveContractionState,
    now: Instant
): Duration? {
    val sortedContractions = contractionState.contractions.sortedBy { it.startedAt }
    val activeContraction = contractionState.activeContraction

    return when {
        activeContraction != null -> {
            sortedContractions
                .filterNot { it.id == activeContraction.id }
                .lastOrNull()
                ?.let { previous -> Duration.between(previous.startedAt, activeContraction.startedAt) }
        }

        sortedContractions.isNotEmpty() -> Duration.between(sortedContractions.last().startedAt, now)
        else -> null
    }
}
