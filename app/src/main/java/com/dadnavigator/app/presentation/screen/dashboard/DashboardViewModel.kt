package com.dadnavigator.app.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.usecase.checklist.ObserveChecklistsUseCase
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkLaborStartedUseCase
import com.dadnavigator.app.domain.usecase.settings.ObserveSettingsUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveLaborSummaryUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveTimelineUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveActiveWaterBreakUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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
                val daysUntilDueDate = settings.dueDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }
                val showContractionShortcut = when (settings.appStage) {
                    AppStage.PREPARING -> {
                        base.hasActiveContractionSession || (daysUntilDueDate != null && daysUntilDueDate <= 21)
                    }
                    AppStage.LABOR -> true
                    AppStage.AFTER_BIRTH -> false
                }
                DashboardUiState(
                    fatherName = settings.fatherName,
                    appStage = settings.appStage,
                    dueDate = settings.dueDate,
                    daysUntilDueDate = daysUntilDueDate,
                    hasActiveContractionSession = base.hasActiveContractionSession,
                    hasActiveWaterBreak = base.isWaterBreakActive,
                    waterBreakElapsed = waterBreakEvent?.elapsed(now),
                    laborSummary = laborSummary,
                    stageChecklistCompletedCount = base.stageChecklistCompleted,
                    stageChecklistTotalCount = base.stageChecklistTotal,
                    recentEvents = base.recentEvents,
                    showContractionShortcut = showContractionShortcut,
                    showWaterBreakShortcut = settings.appStage == AppStage.LABOR || waterBreakEvent?.isActive == true,
                    showBirthDetailsCard = settings.appStage == AppStage.AFTER_BIRTH &&
                        (laborSummary.babyName.isNullOrBlank() ||
                            laborSummary.birthWeightGrams == null ||
                            laborSummary.birthHeightCm == null),
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
    val showContractionShortcut: Boolean = false,
    val showWaterBreakShortcut: Boolean = false,
    val showBirthDetailsCard: Boolean = false,
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
