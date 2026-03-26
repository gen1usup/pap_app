package com.dadnavigator.app.presentation.screen.trackers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.DiaperLog
import com.dadnavigator.app.domain.model.DiaperType
import com.dadnavigator.app.domain.model.FeedingLog
import com.dadnavigator.app.domain.model.FeedingType
import com.dadnavigator.app.domain.model.Note
import com.dadnavigator.app.domain.model.SleepLog
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import com.dadnavigator.app.domain.usecase.tracker.AddDiaperLogUseCase
import com.dadnavigator.app.domain.usecase.tracker.AddFeedingLogUseCase
import com.dadnavigator.app.domain.usecase.tracker.AddNoteUseCase
import com.dadnavigator.app.domain.usecase.tracker.AddSleepLogUseCase
import com.dadnavigator.app.domain.usecase.tracker.ObserveDiaperLogsUseCase
import com.dadnavigator.app.domain.usecase.tracker.ObserveFeedingLogsUseCase
import com.dadnavigator.app.domain.usecase.tracker.ObserveNotesUseCase
import com.dadnavigator.app.domain.usecase.tracker.ObserveSleepLogsUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for postpartum trackers.
 */
@HiltViewModel
class TrackersViewModel @Inject constructor(
    private val observeFeedingLogsUseCase: ObserveFeedingLogsUseCase,
    private val observeDiaperLogsUseCase: ObserveDiaperLogsUseCase,
    private val observeSleepLogsUseCase: ObserveSleepLogsUseCase,
    private val observeNotesUseCase: ObserveNotesUseCase,
    private val addFeedingLogUseCase: AddFeedingLogUseCase,
    private val addDiaperLogUseCase: AddDiaperLogUseCase,
    private val addSleepLogUseCase: AddSleepLogUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val addTimelineEventUseCase: AddTimelineEventUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val formState = MutableStateFlow(TrackersFormState())
    private val selectedTabState = MutableStateFlow(TrackerTab.FEEDING)
    private val errorState = MutableStateFlow<Int?>(null)

    val uiState = userIdState.flatMapLatest { userId ->
        val partialState = combine(
            observeFeedingLogsUseCase(userId),
            observeDiaperLogsUseCase(userId),
            observeSleepLogsUseCase(userId),
            observeNotesUseCase(userId),
            formState
        ) { feeding, diaper, sleep, notes, form ->
            TrackersPartialState(
                feedingLogs = feeding,
                diaperLogs = diaper,
                sleepLogs = sleep,
                notes = notes,
                form = form
            )
        }

        combine(partialState, selectedTabState, errorState) { partial, selectedTab, errorRes ->
            TrackersUiState(
                selectedTab = selectedTab,
                feedingLogs = partial.feedingLogs,
                diaperLogs = partial.diaperLogs,
                sleepLogs = partial.sleepLogs,
                notes = partial.notes,
                feedingDurationInput = partial.form.feedingDurationInput,
                feedingType = partial.form.feedingType,
                diaperType = partial.form.diaperType,
                diaperNotesInput = partial.form.diaperNotesInput,
                sleepDurationInput = partial.form.sleepDurationInput,
                sleepNotesInput = partial.form.sleepNotesInput,
                noteInput = partial.form.noteInput,
                errorRes = errorRes
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrackersUiState()
    )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun setTab(tab: TrackerTab) {
        selectedTabState.value = tab
    }

    fun setFeedingDuration(value: String) {
        formState.update { it.copy(feedingDurationInput = value) }
    }

    fun setFeedingType(type: FeedingType) {
        formState.update { it.copy(feedingType = type) }
    }

    fun setDiaperType(type: DiaperType) {
        formState.update { it.copy(diaperType = type) }
    }

    fun setDiaperNotes(value: String) {
        formState.update { it.copy(diaperNotesInput = value) }
    }

    fun setSleepDuration(value: String) {
        formState.update { it.copy(sleepDurationInput = value) }
    }

    fun setSleepNotes(value: String) {
        formState.update { it.copy(sleepNotesInput = value) }
    }

    fun setNoteText(value: String) {
        formState.update { it.copy(noteInput = value) }
    }

    fun addFeeding() {
        val userId = userIdState.value
        val duration = formState.value.feedingDurationInput.toIntOrNull()
        if (userId.isBlank() || duration == null || duration <= 0) {
            errorState.value = R.string.invalid_number
            return
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val now = Instant.now()
                addFeedingLogUseCase(
                    userId = userId,
                    timestamp = now,
                    durationMinutes = duration,
                    type = formState.value.feedingType
                )
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = now,
                    title = "",
                    description = duration.toString(),
                    type = TimelineType.FEEDING
                )
                formState.update { it.copy(feedingDurationInput = "") }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun addDiaper() {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val now = Instant.now()
                addDiaperLogUseCase(
                    userId = userId,
                    timestamp = now,
                    type = formState.value.diaperType,
                    notes = formState.value.diaperNotesInput
                )
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = now,
                    title = "",
                    description = formState.value.diaperNotesInput,
                    type = TimelineType.DIAPER
                )
                formState.update { it.copy(diaperNotesInput = "") }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun addSleep() {
        val userId = userIdState.value
        val duration = formState.value.sleepDurationInput.toLongOrNull()
        if (userId.isBlank() || duration == null || duration <= 0) {
            errorState.value = R.string.invalid_number
            return
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val end = Instant.now()
                val start = end.minusSeconds(duration * 60)
                addSleepLogUseCase(
                    userId = userId,
                    startTime = start,
                    endTime = end,
                    notes = formState.value.sleepNotesInput
                )
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = end,
                    title = "",
                    description = formState.value.sleepNotesInput,
                    type = TimelineType.SLEEP
                )
                formState.update {
                    it.copy(
                        sleepDurationInput = "",
                        sleepNotesInput = ""
                    )
                }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun addNote() {
        val userId = userIdState.value
        val note = formState.value.noteInput
        if (userId.isBlank() || note.isBlank()) {
            errorState.value = R.string.input_required
            return
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val now = Instant.now()
                addNoteUseCase(
                    userId = userId,
                    timestamp = now,
                    text = note,
                    category = ""
                )
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = now,
                    title = "",
                    description = note,
                    type = TimelineType.NOTE
                )
                formState.update { it.copy(noteInput = "") }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissError() {
        errorState.value = null
    }
}

private data class TrackersFormState(
    val feedingDurationInput: String = "",
    val feedingType: FeedingType = FeedingType.LEFT,
    val diaperType: DiaperType = DiaperType.WET,
    val diaperNotesInput: String = "",
    val sleepDurationInput: String = "",
    val sleepNotesInput: String = "",
    val noteInput: String = ""
)

private data class TrackersPartialState(
    val feedingLogs: List<FeedingLog>,
    val diaperLogs: List<DiaperLog>,
    val sleepLogs: List<SleepLog>,
    val notes: List<Note>,
    val form: TrackersFormState
)
