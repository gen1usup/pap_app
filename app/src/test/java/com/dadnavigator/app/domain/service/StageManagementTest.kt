package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.ThemeMode
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StageManagementTest {

    private val stageManager = StageManager()
    private val transitionManager = StageTransitionManager()
    private val homeContentBuilder = HomeContentBuilder()

    @Test
    fun `fromStorage keeps compatibility with legacy values`() {
        assertEquals(AppStage.PREPARING, AppStage.fromStorage("PREPARING"))
        assertEquals(AppStage.LABOR, AppStage.fromStorage("LABOR"))
        assertEquals(AppStage.BABY_BORN, AppStage.fromStorage("AFTER_BIRTH"))
        assertEquals(AppStage.BABY_BORN, AppStage.fromStorage("AT_HOME"))
    }

    @Test
    fun `buildStageInfo marks readiness from 37th week`() {
        val today = LocalDate.of(2026, 3, 27)
        val settings = baseSettings(
            dueDate = today.plusDays(21),
            appStage = AppStage.PREPARING
        )

        val info = stageManager.buildStageInfo(
            settings = settings,
            laborSummary = emptyLaborSummary(),
            today = today
        )

        assertEquals(37, info.estimatedPregnancyWeek)
        assertTrue(info.isLaborReadinessWindow)
        assertFalse(info.isDueDateMissing)
    }

    @Test
    fun `buildStageInfo handles missing due date`() {
        val info = stageManager.buildStageInfo(
            settings = baseSettings(dueDate = null, appStage = AppStage.PREPARING),
            laborSummary = emptyLaborSummary(),
            today = LocalDate.of(2026, 3, 27)
        )

        assertTrue(info.isDueDateMissing)
        assertFalse(info.isLaborReadinessWindow)
    }

    @Test
    fun `transition manager returns baby born stage after birth`() {
        assertEquals(AppStage.LABOR, transitionManager.laborStarted(emptyLaborSummary()))
        assertEquals(
            AppStage.BABY_BORN,
            transitionManager.laborStarted(emptyLaborSummary().copy(birthTime = Instant.parse("2026-03-27T10:00:00Z")))
        )
        assertEquals(AppStage.BABY_BORN, transitionManager.babyBorn())
    }

    @Test
    fun `manual stage selection allows returning to earlier stages`() {
        val summary = emptyLaborSummary().copy(
            birthTime = Instant.parse("2026-03-27T10:00:00Z")
        )

        assertTrue(transitionManager.canSelectStage(AppStage.PREPARING, summary))
        assertTrue(transitionManager.canSelectStage(AppStage.LABOR, summary))
        assertTrue(transitionManager.canSelectStage(AppStage.BABY_BORN, summary))
        assertTrue(transitionManager.canSelectStage(AppStage.BABY_BORN, summary))

        val decision = transitionManager.manualSelection(
            targetStage = AppStage.LABOR,
            currentStage = AppStage.BABY_BORN,
            currentSummary = summary
        )

        assertFalse(decision.blockedByBirthRecord)
        assertEquals(AppStage.LABOR, decision.stage)
    }

    @Test
    fun `home content shows reminder when due date is missing`() {
        val settings = baseSettings(dueDate = null, appStage = AppStage.PREPARING)
        val content = homeContentBuilder.build(
            settings = settings,
            stageInfo = stageManager.buildStageInfo(
                settings = settings,
                laborSummary = emptyLaborSummary(),
                today = LocalDate.of(2026, 3, 27)
            ),
            laborSummary = emptyLaborSummary(),
            hasActiveContractionSession = false,
            hasActiveWaterBreak = false
        )

        assertTrue(content.showDueDateReminder)
        assertFalse(content.showDueDateCard)
        assertFalse(content.showContractionShortcut)
    }

    @Test
    fun `home content surfaces contraction shortcut from 37th week`() {
        val settings = baseSettings(
            dueDate = LocalDate.of(2026, 4, 17),
            appStage = AppStage.PREPARING
        )
        val content = homeContentBuilder.build(
            settings = settings,
            stageInfo = stageManager.buildStageInfo(
                settings = settings,
                laborSummary = emptyLaborSummary(),
                today = LocalDate.of(2026, 3, 27)
            ),
            laborSummary = emptyLaborSummary(),
            hasActiveContractionSession = false,
            hasActiveWaterBreak = false
        )

        assertTrue(content.showDueDateCard)
        assertTrue(content.showContractionShortcut)
        assertTrue(content.checklistFirst)
    }

    @Test
    fun `home content hides contraction shortcut in baby born stage`() {
        val settings = baseSettings(
            dueDate = LocalDate.of(2026, 3, 27),
            appStage = AppStage.BABY_BORN
        )
        val content = homeContentBuilder.build(
            settings = settings,
            stageInfo = stageManager.buildStageInfo(
                settings = settings,
                laborSummary = emptyLaborSummary().copy(
                    birthTime = Instant.parse("2026-03-27T10:00:00Z")
                ),
                today = LocalDate.of(2026, 3, 27)
            ),
            laborSummary = emptyLaborSummary(),
            hasActiveContractionSession = true,
            hasActiveWaterBreak = true
        )

        assertFalse(content.showContractionShortcut)
        assertTrue(content.showWaterBreakShortcut)
        assertTrue(content.showBirthDetailsCard)
        assertFalse(content.checklistFirst)
    }

    private fun baseSettings(
        dueDate: LocalDate?,
        appStage: AppStage
    ): Settings = Settings(
        userId = DEFAULT_USER_ID,
        themeMode = ThemeMode.SYSTEM,
        fatherName = "",
        dueDate = dueDate,
        maternityHospitalAddress = "",
        notificationsEnabled = true,
        appStage = appStage
    )

    private fun emptyLaborSummary(): LaborSummary = LaborSummary(
        laborStartTime = null,
        birthTime = null,
        babyName = null,
        birthWeightGrams = null,
        birthHeightCm = null
    )
}





