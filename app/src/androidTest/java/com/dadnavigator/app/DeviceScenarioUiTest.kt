package com.dadnavigator.app

import android.content.Intent
import android.graphics.Rect
import android.widget.EditText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.testsupport.TestAppStateSeeder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end UI tests executed on an emulator or physical device.
 *
 * The suite resets local data before each test and validates documented
 * positive and negative scenarios through the real UI.
 */
@RunWith(AndroidJUnit4::class)
class DeviceScenarioUiTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private val seeder = TestAppStateSeeder(targetContext)
    private val timeoutMs = 15_000L

    private val targetPackage: String
        get() = targetContext.packageName

    @Before
    fun resetAndLaunchApp() {
        device.pressHome()
        seeder.clearAllData()
        launchApp()
    }

    private fun launchApp() {
        val launchIntent = targetContext.packageManager.getLaunchIntentForPackage(targetPackage)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        requireNotNull(launchIntent) { "Launch intent for $targetPackage was not found" }

        targetContext.startActivity(launchIntent)
        assertTrue(
            "Dashboard did not open after app launch",
            waitForText(targetContext.getString(R.string.dashboard_title))
        )
    }

    @Test
    fun homeToChecklistsAndBackViaBottomNavigationWorks() {
        clickText(targetContext.getString(R.string.nav_checklists), minTop = 2_000)

        assertTrue(waitForText(targetContext.getString(R.string.nav_checklists)))
        assertTrue(waitForText(targetContext.resources.getStringArray(R.array.checklist_names)[1]))

        clickText(targetContext.getString(R.string.nav_home), minTop = 2_000)

        assertTrue(waitForText(targetContext.getString(R.string.dashboard_title)))
        assertTrue(waitForText(targetContext.getString(R.string.events_action_labor_started)))
    }

    @Test
    fun drawerStageEntriesOpenStageScreen() {
        openDrawer()
        clickText(targetContext.getString(R.string.app_stage_contractions))

        assertTrue(waitForText(targetContext.getString(R.string.stage_screen_contractions_title)))
        assertTrue(waitForText(targetContext.getString(R.string.stage_screen_activate)))
    }

    @Test
    fun homeChangesForHospitalStageAndHidesContractionShortcut() {
        device.pressHome()
        seeder.seedStage(AppStage.AT_HOSPITAL)
        launchApp()

        assertTrue(waitForText(targetContext.getString(R.string.dashboard_at_hospital_title)))
        assertFalse(
            "Contraction counter shortcut should not stay on after-birth home",
            hasVisibleText(targetContext.getString(R.string.action_contraction_counter))
        )
    }

    @Test
    fun settingsScreenOpensAndShowsDueDateControls() {
        openDrawer()
        clickText(targetContext.getString(R.string.settings_title))

        assertTrue(waitForText(targetContext.getString(R.string.settings_title)))
        assertTrue(waitForText(targetContext.getString(R.string.settings_due_date)))
        assertTrue(waitForText(targetContext.getString(R.string.settings_due_date_hint)))
    }

    @Test
    fun eventsHospitalStageShowsArrivalHomeAndHidesLaborTools() {
        device.pressHome()
        seeder.seedStage(AppStage.AT_HOSPITAL)
        launchApp()

        clickText(targetContext.getString(R.string.nav_events), minTop = 2_000)
        scrollUntilVisible(targetContext.getString(R.string.events_action_arrived_home))

        assertTrue(waitForText(targetContext.getString(R.string.events_action_arrived_home)))
        assertTrue(waitForText(targetContext.getString(R.string.events_action_support)))
        assertFalse(hasVisibleText(targetContext.getString(R.string.action_contraction_counter)))
    }

    @Test
    fun eventsAtHomeStageShowsTrackersAndHidesLaborStart() {
        device.pressHome()
        seeder.seedStage(AppStage.AT_HOME)
        launchApp()

        clickText(targetContext.getString(R.string.nav_events), minTop = 2_000)

        assertTrue(waitForText(targetContext.getString(R.string.events_action_feeding)))
        assertTrue(waitForText(targetContext.getString(R.string.events_action_sleep)))
        assertTrue(waitForText(targetContext.getString(R.string.events_action_diaper)))
        assertFalse(hasVisibleText(targetContext.getString(R.string.events_action_labor_started)))
    }

    fun journalBlankLaborEventShowsValidationMessageAndKeepsEmptyState() {
        clickContentDescription(targetContext.getString(R.string.nav_journal))

        assertTrue(waitForText(targetContext.getString(R.string.timeline_empty_title)))
        clickText(targetContext.getString(R.string.timeline_add_event))
        assertTrue(waitForText(targetContext.getString(R.string.save_event)))

        clickText(targetContext.getString(R.string.save_event))

        assertTrue(waitForText(targetContext.getString(R.string.input_required)))
        assertTrue(waitForText(targetContext.getString(R.string.timeline_empty_title)))
    }

    @Test
    fun helpAndSosRenderCoreScenarioInformation() {
        openDrawer()
        clickText(targetContext.getString(R.string.help_title))

        assertTrue(waitForText(targetContext.getString(R.string.help_maternity_route_title)))
        assertTrue(waitForText(targetContext.getString(R.string.help_maternity_address_label)))

        device.pressBack()
        assertTrue(waitForText(targetContext.getString(R.string.dashboard_title)))

        openDrawer()
        clickText(targetContext.getString(R.string.sos_title))

        assertTrue(waitForText(targetContext.getString(R.string.sos_call_112)))
        assertTrue(waitForText(targetContext.getString(R.string.sos_signs_title)))
        assertTrue(waitForText(targetContext.getString(R.string.sos_manage_contacts)))
    }

    private fun openDrawer() {
        val menuButton = device.wait(
            Until.findObject(By.desc(targetContext.getString(R.string.nav_menu))),
            timeoutMs
        )
        assertNotNull("Drawer menu button was not found", menuButton)
        menuButton!!.click()
        assertTrue(waitForText(targetContext.getString(R.string.app_name)))
    }

    private fun waitForText(text: String, timeout: Long = timeoutMs): Boolean {
        return device.wait(Until.hasObject(By.text(text)), timeout)
    }

    private fun hasVisibleText(text: String): Boolean {
        return device.hasObject(By.text(text))
    }

    private fun clickText(
        text: String,
        maxTop: Int? = null,
        minTop: Int? = null
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val candidate = findTextObject(text, maxTop, minTop)
            if (candidate != null) {
                candidate.click()
                return
            }
            device.waitForIdle()
        }
        throw AssertionError("Could not find clickable text '$text'")
    }

    private fun clickContentDescription(description: String) {
        val candidate = device.wait(
            Until.findObject(By.desc(description)),
            timeoutMs
        )
        requireNotNull(candidate) { "Could not find clickable object with description '$description'" }
        candidate.click()
    }

    private fun setEditTextByIndex(index: Int, value: String) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val fields = device.findObjects(By.clazz(EditText::class.java))
                .filter { it.visibleBounds.height() > 0 }
                .sortedBy { it.visibleBounds.top }
            if (fields.size > index) {
                fields[index].text = value
                return
            }
            device.waitForIdle()
        }
        throw AssertionError("Could not find EditText with index $index")
    }

    private fun scrollUntilVisible(text: String) {
        if (scrollWithUiScrollable(text)) return
        repeat(8) {
            if (hasVisibleText(text)) return
            swipeUp()
        }
        throw AssertionError("Could not scroll to text '$text'")
    }

    private fun scrollWithUiScrollable(text: String): Boolean {
        return runCatching {
            UiScrollable(UiSelector().scrollable(true))
                .setAsVerticalList()
                .scrollTextIntoView(text)
        }.getOrDefault(false)
    }

    private fun swipeUp() {
        device.swipe(
            device.displayWidth / 2,
            (device.displayHeight * 0.82f).toInt(),
            device.displayWidth / 2,
            (device.displayHeight * 0.35f).toInt(),
            20
        )
        device.waitForIdle()
    }

    private fun findTextObject(
        text: String,
        maxTop: Int?,
        minTop: Int?
    ): UiObject2? {
        return device.findObjects(By.text(text))
            .filter { it.visibleBounds.isUsable(maxTop, minTop) }
            .minByOrNull { it.visibleBounds.top }
    }

    private fun Rect.isUsable(maxTop: Int?, minTop: Int?): Boolean {
        if (width() <= 0 || height() <= 0) return false
        if (maxTop != null && top > maxTop) return false
        if (minTop != null && top < minTop) return false
        return true
    }
}
