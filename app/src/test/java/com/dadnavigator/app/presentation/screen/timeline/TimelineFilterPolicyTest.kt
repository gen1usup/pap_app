package com.dadnavigator.app.presentation.screen.timeline

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineEntryType
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineFilterPolicyTest {

    @Test
    fun `notes filter keeps only user notes`() {
        val events = listOf(
            event(id = 1, type = TimelineType.CONTRACTION, stage = AppStage.LABOR, entryType = TimelineEntryType.SYSTEM),
            event(id = 2, type = TimelineType.LABOR_NOTE, stage = AppStage.LABOR, entryType = TimelineEntryType.USER_NOTE),
            event(id = 3, type = TimelineType.BABY_NOTE, stage = AppStage.BABY_BORN, entryType = TimelineEntryType.USER_NOTE)
        )

        val filtered = events.filterForTimeline(TimelineFilter.NOTES)

        assertEquals(listOf(2L, 3L), filtered.map { it.id })
    }

    @Test
    fun `stage filters use stage at creation instead of raw event type`() {
        val events = listOf(
            event(id = 1, type = TimelineType.NOTE, stage = AppStage.PREPARING, entryType = TimelineEntryType.USER_NOTE),
            event(id = 2, type = TimelineType.WATER_BREAK, stage = AppStage.LABOR, entryType = TimelineEntryType.SYSTEM),
            event(id = 3, type = TimelineType.BABY_NOTE, stage = AppStage.BABY_BORN, entryType = TimelineEntryType.USER_NOTE)
        )

        assertEquals(listOf(1L), events.filterForTimeline(TimelineFilter.PREPARING).map { it.id })
        assertEquals(listOf(2L), events.filterForTimeline(TimelineFilter.LABOR).map { it.id })
        assertEquals(listOf(3L), events.filterForTimeline(TimelineFilter.BABY_BORN).map { it.id })
    }

    private fun event(
        id: Long,
        type: TimelineType,
        stage: AppStage,
        entryType: TimelineEntryType
    ): TimelineEvent = TimelineEvent(
        id = id,
        userId = "user",
        type = type,
        timestamp = Instant.parse("2026-03-28T10:00:00Z"),
        title = "",
        description = "",
        stageAtCreation = stage,
        entryType = entryType
    )
}

