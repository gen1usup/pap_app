package com.dadnavigator.app

import android.content.Intent
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
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
        clickBottomNav(targetContext.getString(R.string.nav_checklists))

        assertTrue(waitForText(targetContext.getString(R.string.nav_checklists)))
        assertTrue(waitForText(targetContext.getString(R.string.checklist_create_title)))

        clickBottomNav(targetContext.getString(R.string.nav_home))

        assertTrue(waitForText(targetContext.getString(R.string.dashboard_title)))
    }

    @Test
    fun drawerStageEntriesOpenStageScreen() {
        openDrawer()
        clickText(targetContext.getString(R.string.app_stage_labor))

        assertTrue(waitForText(targetContext.getString(R.string.stage_screen_labor_title)))
        assertTrue(waitForText(targetContext.getString(R.string.stage_screen_activate)))
    }

    @Test
    fun babyCardIsVisibleOnHomeAfterBirthAndContractionShortcutIsHidden() {
        device.pressHome()
        seeder.seedStage(AppStage.BABY_BORN)
        launchApp()

        assertTrue(waitForText(targetContext.getString(R.string.baby_card_overline)))
        assertTrue(waitForText(targetContext.getString(R.string.baby_open_action)))
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
    fun eventsBabyBornStageHidesLaborTools() {
        device.pressHome()
        seeder.seedStage(AppStage.BABY_BORN)
        launchApp()

        clickBottomNav(targetContext.getString(R.string.nav_events))

        assertTrue(waitForText(targetContext.getString(R.string.events_title)))
        assertFalse(hasVisibleText(targetContext.getString(R.string.action_contraction_counter)))
    }

    @Test
    fun eventsBabyBornStageOpensFromBottomNavigation() {
        device.pressHome()
        seeder.seedStage(AppStage.BABY_BORN)
        launchApp()

        clickBottomNav(targetContext.getString(R.string.nav_events))

        assertTrue(waitForText(targetContext.getString(R.string.events_title)))
    }

    @Test
    fun helpAndContactsRenderCoreScenarioInformation() {
        openDrawer()
        clickText(targetContext.getString(R.string.help_title))

        assertTrue(waitForText(targetContext.getString(R.string.help_title)))
        assertTrue(waitForText(targetContext.getString(R.string.app_stage_preparing)))

        device.pressBack()
        assertTrue(waitForText(targetContext.getString(R.string.dashboard_title)))

        clickBottomNav(targetContext.getString(R.string.nav_contacts))

        assertTrue(waitForText(targetContext.getString(R.string.emergency_contacts_title)))
        assertTrue(waitForText(targetContext.getString(R.string.contact_type_ambulance_full)))
        assertTrue(waitForText(targetContext.getString(R.string.contact_type_maternity)))
        assertTrue(waitForText(targetContext.getString(R.string.emergency_contacts_add_action)))
    }

    private fun clickBottomNav(text: String) {
        clickText(text, minTop = bottomNavMinTop())
    }

    private fun bottomNavMinTop(): Int = (device.displayHeight * 0.82f).toInt()

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

    private fun findTextObject(
        text: String,
        maxTop: Int?,
        minTop: Int?
    ): UiObject2? {
        return device.findObjects(By.text(text))
            .filter { it.visibleBounds.isUsable(maxTop, minTop) }
            .maxByOrNull { it.visibleBounds.top }
    }

    private fun Rect.isUsable(maxTop: Int?, minTop: Int?): Boolean {
        if (width() <= 0 || height() <= 0) return false
        if (maxTop != null && top > maxTop) return false
        if (minTop != null && top < minTop) return false
        return true
    }
}
