package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.StageInfo
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventsProviderTest {

    private val provider = EventsProvider()

    @Test
    fun `preparation before readiness window keeps prep tools only`() {
        val content = provider.build(
            stageInfo = stageInfo(
                currentStage = AppStage.PREPARING,
                dueDate = LocalDate.of(2026, 5, 1),
                estimatedPregnancyWeek = 34,
                isLaborReadinessWindow = false
            ),
            isContractionRunning = false,
            hasActiveWaterBreak = false,
            laborSummary = emptyLaborSummary()
        )

        assertEquals(2, content.sections.size)
        assertTrue(content.sections.first().actions.contains(EventAction.OpenContractionTimer))
        assertFalse(content.sections.first().actions.contains(EventAction.MarkLaborStarted))
        assertFalse(content.showBirthSummary)
    }

    @Test
    fun `preparation in readiness window exposes labor activation`() {
        val content = provider.build(
            stageInfo = stageInfo(
                currentStage = AppStage.PREPARING,
                dueDate = LocalDate.of(2026, 4, 17),
                estimatedPregnancyWeek = 37,
                isLaborReadinessWindow = true
            ),
            isContractionRunning = false,
            hasActiveWaterBreak = false,
            laborSummary = emptyLaborSummary()
        )

        assertTrue(content.sections.first().actions.contains(EventAction.MarkLaborStarted))
        assertTrue(content.sections.first().actions.contains(EventAction.OpenWaterBreakTimer))
    }

    @Test
    fun `labor stage swaps start and stop action`() {
        val startedContent = provider.build(
            stageInfo = stageInfo(currentStage = AppStage.LABOR),
            isContractionRunning = false,
            hasActiveWaterBreak = false,
            laborSummary = emptyLaborSummary()
        )
        val runningContent = provider.build(
            stageInfo = stageInfo(currentStage = AppStage.LABOR),
            isContractionRunning = true,
            hasActiveWaterBreak = true,
            laborSummary = emptyLaborSummary()
        )

        assertTrue(startedContent.sections.first().actions.contains(EventAction.StartContraction))
        assertFalse(startedContent.sections.first().actions.contains(EventAction.StopContraction))
        assertTrue(runningContent.sections.first().actions.contains(EventAction.StopContraction))
    }

    @Test
    fun `baby born stage exposes birth details and newborn records`() {
        val content = provider.build(
            stageInfo = stageInfo(
                currentStage = AppStage.BABY_BORN,
                birthRecorded = true
            ),
            isContractionRunning = false,
            hasActiveWaterBreak = false,
            laborSummary = emptyLaborSummary().copy(
                birthTime = Instant.parse("2026-03-27T10:00:00Z")
            )
        )

        assertTrue(content.showBirthSummary)
        assertTrue(content.sections.single().actions.contains(EventAction.OpenBirthDetails))
        assertTrue(content.sections.single().actions.contains(EventAction.RecordFeeding))
        assertTrue(content.sections.single().actions.contains(EventAction.RecordWeight))
    }

    @Test
    fun `baby born stage keeps optional water timer access without labor controls`() {
        val content = provider.build(
            stageInfo = stageInfo(currentStage = AppStage.BABY_BORN, birthRecorded = true),
            isContractionRunning = false,
            hasActiveWaterBreak = true,
            laborSummary = emptyLaborSummary().copy(
                birthTime = Instant.parse("2026-03-27T10:00:00Z")
            )
        )

        val actions = content.sections.flatMap { it.actions }
        assertTrue(actions.contains(EventAction.OpenWaterBreakTimer))
        assertTrue(actions.contains(EventAction.RecordFeeding))
        assertTrue(actions.contains(EventAction.RecordSleep))
        assertTrue(actions.contains(EventAction.RecordDiaper))
        assertFalse(actions.contains(EventAction.MarkLaborStarted))
        assertFalse(actions.contains(EventAction.OpenContractionTimer))
    }

    private fun stageInfo(
        currentStage: AppStage,
        dueDate: LocalDate? = null,
        estimatedPregnancyWeek: Int? = null,
        isLaborReadinessWindow: Boolean = false,
        birthRecorded: Boolean = false
    ): StageInfo = StageInfo(
        currentStage = currentStage,
        dueDate = dueDate,
        daysUntilDueDate = null,
        estimatedPregnancyWeek = estimatedPregnancyWeek,
        isDueDateMissing = dueDate == null,
        isLaborReadinessWindow = isLaborReadinessWindow,
        birthRecorded = birthRecorded
    )

    private fun emptyLaborSummary(): LaborSummary = LaborSummary(
        laborStartTime = null,
        birthTime = null,
        babyName = null,
        birthWeightGrams = null,
        birthHeightCm = null
    )
}
