package com.dadnavigator.app.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.service.HomeContentBuilder
import com.dadnavigator.app.domain.usecase.checklist.ObserveChecklistsUseCase
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkLaborStartedUseCase
import com.dadnavigator.app.domain.service.StageManager
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
    private val homeContentBuilder: HomeContentBuilder,
    private val stageManager: StageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow(DEFAULT_USER_ID)
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
                    hasActiveContractionSession = contractionState.session?.isActive == true,
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
                val stageInfo = stageManager.buildStageInfo(
                    settings = settings,
                    laborSummary = laborSummary,
                    today = LocalDate.now()
                )
                val homeContent = homeContentBuilder.build(
                    settings = settings,
                    stageInfo = stageInfo,
                    laborSummary = laborSummary,
                    hasActiveContractionSession = base.hasActiveContractionSession,
                    hasActiveWaterBreak = base.isWaterBreakActive
                )
                DashboardUiState(
                    fatherName = settings.fatherName,
                    appStage = settings.appStage,
                    dueDate = settings.dueDate,
                    daysUntilDueDate = stageInfo.daysUntilDueDate,
                    hasActiveContractionSession = base.hasActiveContractionSession,
                    hasActiveWaterBreak = base.isWaterBreakActive,
                    waterBreakElapsed = waterBreakEvent?.elapsed(now),
                    laborSummary = laborSummary,
                    stageChecklistCompletedCount = base.stageChecklistCompleted,
                    stageChecklistTotalCount = base.stageChecklistTotal,
                    recentEvents = base.recentEvents,
                    showDueDateReminder = homeContent.showDueDateReminder,
                    showContractionShortcut = homeContent.showContractionShortcut,
                    showWaterBreakShortcut = homeContent.showWaterBreakShortcut,
                    showBirthDetailsCard = homeContent.showBirthDetailsCard,
                    checklistFirst = homeContent.checklistFirst,
                    errorRes = null
                )
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
                markLaborStartedUseCase(
                    userId = userId,
                    eventTitle = eventTitle,
                    eventDescription = eventDescription
                )
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissError() {
        errorState.value = null
    }
}

data class DashboardUiState(
    val fatherName: String = "",
    val appStage: AppStage = AppStage.PREPARING,
    val dueDate: LocalDate? = null,
    val daysUntilDueDate: Long? = null,
    val hasActiveContractionSession: Boolean = false,
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
    val showWaterBreakShortcut: Boolean = false,
    val showBirthDetailsCard: Boolean = false,
    val checklistFirst: Boolean = true,
    val errorRes: Int? = null
)

private data class DashboardBaseState(
    val settings: Settings,
    val hasActiveContractionSession: Boolean,
    val isWaterBreakActive: Boolean,
    val stageChecklistCompleted: Int,
    val stageChecklistTotal: Int,
    val recentEvents: List<TimelineEvent>
)
