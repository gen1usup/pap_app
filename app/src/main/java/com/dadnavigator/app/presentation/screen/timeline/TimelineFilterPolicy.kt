package com.dadnavigator.app.presentation.screen.timeline

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineEntryType
import com.dadnavigator.app.domain.model.TimelineEvent

internal fun List<TimelineEvent>.filterForTimeline(filter: TimelineFilter): List<TimelineEvent> = when (filter) {
    TimelineFilter.ALL -> this
    TimelineFilter.PREPARING -> filter { it.stageAtCreation == AppStage.PREPARING }
    TimelineFilter.LABOR -> filter { it.stageAtCreation == AppStage.LABOR }
    TimelineFilter.BABY_BORN -> filter { it.stageAtCreation == AppStage.BABY_BORN }
    TimelineFilter.NOTES -> filter { it.entryType == TimelineEntryType.USER_NOTE }
}
