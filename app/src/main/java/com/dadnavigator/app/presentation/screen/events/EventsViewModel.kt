package com.dadnavigator.app.presentation.screen.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.ActiveContractionState
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.service.EventAction
import com.dadnavigator.app.domain.service.EventsProvider
import com.dadnavigator.app.domain.service.StageManager
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.contraction.ToggleContractionResult
import com.dadnavigator.app.domain.usecase.contraction.ToggleContractionUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkArrivedHomeUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkBirthUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkLaborStartedUseCase
import com.dadnavigator.app.domain.usecase.settings.ObserveSettingsUseCase
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveLaborSummaryUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveActiveWaterBreakUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordinates stage-aware actions and quick records for the Events feature.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventsViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val observeContractionStateUseCase: ObserveContractionStateUseCase,
    private val observeActiveWaterBreakUseCase: ObserveActiveWaterBreakUseCase,
    private val observeLaborSummaryUseCase: ObserveLaborSummaryUseCase,
    private val addTimelineEventUseCase: AddTimelineEventUseCase,
    private val markLaborStartedUseCase: MarkLaborStartedUseCase,
    private val markBirthUseCase: MarkBirthUseCase,
    private val markArrivedHomeUseCase: MarkArrivedHomeUseCase,
    private val toggleContractionUseCase: ToggleContractionUseCase,
    private val stageManager: StageManager,
    private val eventsProvider: EventsProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val birthFormState = MutableStateFlow(BirthFormState())
    private val quickRecordState = MutableStateFlow(QuickRecordState())
    private val infoState = MutableStateFlow<Int?>(null)
    private val errorState = MutableStateFlow<Int?>(null)

    val uiState = userIdState.flatMapLatest { userId ->
        combine(
            observeSettingsUseCase(),
            observeContractionStateUseCase(userId),
            observeActiveWaterBreakUseCase(userId),
            observeLaborSummaryUseCase(userId)
        ) { settings, contractionState, waterBreak, laborSummary ->
            ObservedEventsData(
                settings = settings,
                contractionState = contractionState,
                waterBreak = waterBreak,
                laborSummary = laborSummary
            )
        }.combine(birthFormState) { observed, birthForm ->
            observed to birthForm
        }.combine(quickRecordState) { (observed, birthForm), quickRecord ->
            val stageInfo = stageManager.buildStageInfo(
                settings = observed.settings,
                laborSummary = observed.laborSummary
            )
            EventsUiState(
                appStage = observed.settings.appStage,
                hasActiveContractionSession = observed.contractionState.session?.isActive == true,
                contractionSessionId = observed.contractionState.session?.id,
                isContractionRunning = observed.contractionState.activeContraction != null,
                activeContractionId = observed.contractionState.activeContraction?.id,
                hasActiveWaterBreak = observed.waterBreak?.isActive == true,
                content = eventsProvider.build(
                    stageInfo = stageInfo,
                    isContractionRunning = observed.contractionState.activeContraction != null,
                    hasActiveWaterBreak = observed.waterBreak?.isActive == true,
                    laborSummary = observed.laborSummary
                ),
                laborSummary = observed.laborSummary,
                showBirthSheet = birthForm.showBirthSheet,
                showQuickRecordSheet = quickRecord.showQuickRecordSheet,
                activeQuickRecordAction = quickRecord.action,
                quickRecordTitleInput = quickRecord.title,
                quickRecordDescriptionInput = quickRecord.description,
                babyNameInput = birthForm.babyName,
                weightInput = birthForm.weightInput,
                heightInput = birthForm.heightInput
            )
        }.combine(infoState) { partial, infoRes ->
            partial.copy(infoRes = infoRes)
        }.combine(errorState) { partial, errorRes ->
            partial.copy(errorRes = errorRes)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EventsUiState()
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
                infoState.value = R.string.events_labor_started_saved
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
                val result = toggleContractionUseCase(
                    userId = userId,
                    sessionId = currentState.contractionSessionId,
                    activeContractionId = currentState.activeContractionId
                )
                infoState.value = when (result) {
                    ToggleContractionResult.Started -> R.string.events_contraction_started
                    ToggleContractionResult.Stopped -> R.string.events_contraction_stopped
                }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun addJourneyEvent(title: String, description: String = "") {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = Instant.now(),
                    title = title,
                    description = description,
                    type = TimelineType.LABOR
                )
                infoState.value = R.string.timeline_event_saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun markArrivedHome(eventTitle: String, eventDescription: String = "") {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                markArrivedHomeUseCase(
                    userId = userId,
                    eventTitle = eventTitle,
                    eventDescription = eventDescription
                )
                infoState.value = R.string.events_arrived_home_saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun showQuickRecordSheet(action: EventAction) {
        val config = quickRecordConfig(action)
        quickRecordState.value = QuickRecordState(
            showQuickRecordSheet = true,
            action = action,
            title = config.defaultTitle,
            description = ""
        )
    }

    fun hideQuickRecordSheet() {
        quickRecordState.value = QuickRecordState()
    }

    fun updateQuickRecordTitle(value: String) {
        quickRecordState.update { it.copy(title = value) }
    }

    fun updateQuickRecordDescription(value: String) {
        quickRecordState.update { it.copy(description = value) }
    }

    fun saveQuickRecord() {
        val userId = userIdState.value
        val quickRecord = quickRecordState.value
        if (userId.isBlank()) return

        val action = quickRecord.action ?: return
        val config = quickRecordConfig(action)
        val title = if (config.titleEditable) quickRecord.title.trim() else config.defaultTitle

        if (config.requireTitle && title.isBlank()) {
            errorState.value = R.string.input_required
            return
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = Instant.now(),
                    title = title,
                    description = quickRecord.description.trim(),
                    type = config.timelineType
                )
                quickRecordState.value = QuickRecordState()
                infoState.value = R.string.timeline_event_saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun showBirthSheet(summary: LaborSummary) {
        birthFormState.value = BirthFormState(
            showBirthSheet = true,
            babyName = summary.babyName.orEmpty(),
            weightInput = summary.birthWeightGrams?.toString().orEmpty(),
            heightInput = summary.birthHeightCm?.toString().orEmpty()
        )
    }

    fun hideBirthSheet() {
        birthFormState.update { it.copy(showBirthSheet = false) }
    }

    fun updateBabyName(value: String) {
        birthFormState.update { it.copy(babyName = value) }
    }

    fun updateWeight(value: String) {
        birthFormState.update { it.copy(weightInput = value) }
    }

    fun updateHeight(value: String) {
        birthFormState.update { it.copy(heightInput = value) }
    }

    fun markBirth(eventTitle: String) {
        markBirthInternal(
            eventTitle = eventTitle,
            includeOptionalDetails = true
        )
    }

    fun markBirthWithoutDetails(eventTitle: String) {
        markBirthInternal(
            eventTitle = eventTitle,
            includeOptionalDetails = false
        )
    }

    private fun markBirthInternal(
        eventTitle: String,
        includeOptionalDetails: Boolean
    ) {
        val userId = userIdState.value
        if (userId.isBlank()) return

        val form = birthFormState.value
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val weight = if (includeOptionalDetails && form.weightInput.isNotBlank()) {
                    form.weightInput.toIntOrNull()
                } else {
                    null
                }
                val height = if (includeOptionalDetails && form.heightInput.isNotBlank()) {
                    form.heightInput.toIntOrNull()
                } else {
                    null
                }

                if (includeOptionalDetails && form.weightInput.isNotBlank() && weight == null) {
                    errorState.value = R.string.invalid_number
                    return@runCatching
                }
                if (includeOptionalDetails && form.heightInput.isNotBlank() && height == null) {
                    errorState.value = R.string.invalid_number
                    return@runCatching
                }

                val description = buildString {
                    if (includeOptionalDetails && form.babyName.isNotBlank()) {
                        append("Имя: ")
                        append(form.babyName.trim())
                    }
                    if (includeOptionalDetails && weight != null) {
                        if (isNotBlank()) append(" • ")
                        append("Вес: ")
                        append(weight)
                        append(" г")
                    }
                    if (includeOptionalDetails && height != null) {
                        if (isNotBlank()) append(" • ")
                        append("Рост: ")
                        append(height)
                        append(" см")
                    }
                }

                markBirthUseCase(
                    userId = userId,
                    eventTitle = eventTitle,
                    eventDescription = description,
                    babyName = form.babyName.takeIf { includeOptionalDetails },
                    birthWeightGrams = weight,
                    birthHeightCm = height
                )
                birthFormState.value = BirthFormState()
                infoState.value = R.string.events_birth_saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun clearMessages() {
        infoState.value = null
        errorState.value = null
    }

    private fun quickRecordConfig(action: EventAction): QuickRecordConfig = when (action) {
        EventAction.RecordBagReady -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Сумка собрана"
        )
        EventAction.RecordTestDrive -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Пробный выезд"
        )
        EventAction.RecordPreparationNote -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Заметка",
            titleEditable = true,
            requireTitle = true
        )
        EventAction.RecordLaborNote -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Заметка во время родов",
            titleEditable = true,
            requireTitle = true
        )
        EventAction.RecordHospitalNote -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Заметка из роддома",
            titleEditable = true,
            requireTitle = true
        )
        EventAction.RecordSupportAction -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Поддержка мамы"
        )
        EventAction.RecordPhotoNote -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Фото"
        )
        EventAction.RecordFeeding -> QuickRecordConfig(
            timelineType = TimelineType.FEEDING,
            defaultTitle = "Кормление"
        )
        EventAction.RecordSleep -> QuickRecordConfig(
            timelineType = TimelineType.SLEEP,
            defaultTitle = "Сон"
        )
        EventAction.RecordDiaper -> QuickRecordConfig(
            timelineType = TimelineType.DIAPER,
            defaultTitle = "Подгузник"
        )
        EventAction.RecordTemperature -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Температура"
        )
        EventAction.RecordWeight -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Вес"
        )
        EventAction.RecordHomeNote -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Домашняя заметка",
            titleEditable = true,
            requireTitle = true
        )
        else -> QuickRecordConfig(
            timelineType = TimelineType.NOTE,
            defaultTitle = "Заметка"
        )
    }
}

private data class BirthFormState(
    val showBirthSheet: Boolean = false,
    val babyName: String = "",
    val weightInput: String = "",
    val heightInput: String = ""
)

private data class ObservedEventsData(
    val settings: Settings,
    val contractionState: ActiveContractionState,
    val waterBreak: WaterBreakEvent?,
    val laborSummary: LaborSummary
)

private data class QuickRecordState(
    val showQuickRecordSheet: Boolean = false,
    val action: EventAction? = null,
    val title: String = "",
    val description: String = ""
)

private data class QuickRecordConfig(
    val timelineType: TimelineType,
    val defaultTitle: String,
    val titleEditable: Boolean = false,
    val requireTitle: Boolean = false
)
