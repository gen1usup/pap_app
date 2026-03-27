package com.dadnavigator.app

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.dadnavigator.app.testsupport.ContractionScenario
import com.dadnavigator.app.testsupport.TestAppStateSeeder
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Validates contraction recommendations against seeded historical data.
 *
 * These tests intentionally prepare synthetic timestamps in local storage so
 * emulator runs can cover 20-90 minute labor windows in a few seconds.
 */
@RunWith(AndroidJUnit4::class)
class ContractionAnalyticsUiTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private val timeoutMs = 15_000L
    private val seeder = TestAppStateSeeder(targetContext)

    private val targetPackage: String
        get() = targetContext.packageName

    @Before
    fun resetDeviceState() {
        device.pressHome()
        seeder.clearAllData()
    }

    @Test
    fun irregularLongContractionsStayInMonitorMode() {
        seedAndLaunch(ContractionScenario.monitorIrregular())

        openContractionScreen()

        assertTrue(waitForText(targetContext.getString(R.string.contraction_stage_monitor)))
        assertTrue(waitForText(targetContext.getString(R.string.recommendation_monitor)))
    }

    @Test
    fun regularModerateContractionsShowPrepareRecommendation() {
        seedAndLaunch(ContractionScenario.prepareRegular())

        openContractionScreen()

        assertTrue(waitForText(targetContext.getString(R.string.contraction_stage_prepare)))
        assertTrue(waitForText(targetContext.getString(R.string.recommendation_prepare)))
    }

    @Test
    fun regularStrongContractionsShowGoToHospitalRecommendation() {
        seedAndLaunch(ContractionScenario.goRegular())

        openContractionScreen()

        assertTrue(waitForText(targetContext.getString(R.string.contraction_stage_go)))
        assertTrue(waitForText(targetContext.getString(R.string.recommendation_go_hospital)))
    }

    private fun seedAndLaunch(scenario: ContractionScenario) {
        seeder.seedContractionScenario(scenario)
        launchApp()
        assertTrue(waitForText(targetContext.getString(R.string.dashboard_title)))
    }

    private fun launchApp() {
        val launchIntent = targetContext.packageManager.getLaunchIntentForPackage(targetPackage)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        requireNotNull(launchIntent) { "Launch intent for $targetPackage was not found" }
        targetContext.startActivity(launchIntent)
    }

    private fun openContractionScreen() {
        clickText(targetContext.getString(R.string.nav_events))
        assertTrue(waitForText(targetContext.getString(R.string.events_title)))
        clickText(targetContext.getString(R.string.action_contraction_counter))
        assertTrue(waitForText(targetContext.getString(R.string.contraction_title)))
    }

    private fun waitForText(text: String, timeout: Long = timeoutMs): Boolean {
        return device.wait(Until.hasObject(By.text(text)), timeout)
    }

    private fun clickText(text: String) {
        val object2 = device.wait(Until.findObject(By.text(text)), timeoutMs)
        requireNotNull(object2) { "Could not find text '$text'" }
        object2.click()
        device.waitForIdle()
    }
}
