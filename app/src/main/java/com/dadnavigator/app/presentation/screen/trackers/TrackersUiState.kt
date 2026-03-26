package com.dadnavigator.app.presentation.screen.trackers

import com.dadnavigator.app.domain.model.DiaperLog
import com.dadnavigator.app.domain.model.DiaperType
import com.dadnavigator.app.domain.model.FeedingLog
import com.dadnavigator.app.domain.model.FeedingType
import com.dadnavigator.app.domain.model.Note
import com.dadnavigator.app.domain.model.SleepLog

/**
 * Tracker tabs.
 */
enum class TrackerTab {
    FEEDING,
    DIAPER,
    SLEEP,
    NOTES
}

/**
 * UI state for trackers screen.
 */
data class TrackersUiState(
    val selectedTab: TrackerTab = TrackerTab.FEEDING,
    val feedingLogs: List<FeedingLog> = emptyList(),
    val diaperLogs: List<DiaperLog> = emptyList(),
    val sleepLogs: List<SleepLog> = emptyList(),
    val notes: List<Note> = emptyList(),
    val feedingDurationInput: String = "",
    val feedingType: FeedingType = FeedingType.LEFT,
    val diaperType: DiaperType = DiaperType.WET,
    val diaperNotesInput: String = "",
    val sleepDurationInput: String = "",
    val sleepNotesInput: String = "",
    val noteInput: String = "",
    val errorRes: Int? = null
)
