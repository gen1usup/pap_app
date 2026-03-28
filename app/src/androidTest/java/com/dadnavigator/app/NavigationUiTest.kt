package com.dadnavigator.app

import android.graphics.Rect
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.testsupport.TestAppStateSeeder
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for top-level navigation flow.
 */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val timeoutMs = 15_000L

    @Before
    fun seedPreparingStage() {
        TestAppStateSeeder(composeRule.activity).seedStage(AppStage.PREPARING)
        composeRule.waitForIdle()
    }

    @Test
    fun opensEventsTabFromBottomNavigation() {
        val context = composeRule.activity
        val eventsTab = context.getString(com.dadnavigator.app.R.string.nav_events)
        val titleText = context.getString(com.dadnavigator.app.R.string.events_title)

        clickBottomNav(eventsTab)

        assertTrue(device.wait(Until.hasObject(By.text(titleText)), timeoutMs))
    }

    private fun clickBottomNav(text: String) {
        clickText(text, minTop = (device.displayHeight * 0.82f).toInt())
    }

    private fun clickText(
        text: String,
        maxTop: Int? = null,
        minTop: Int? = null
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val candidate = device.findObjects(By.text(text))
                .filter { it.visibleBounds.isUsable(maxTop, minTop) }
                .maxByOrNull { it.visibleBounds.top }
            if (candidate != null) {
                candidate.click()
                return
            }
            device.waitForIdle()
        }
        throw AssertionError("Could not find clickable text '$text'")
    }

    private fun Rect.isUsable(maxTop: Int?, minTop: Int?): Boolean {
        if (width() <= 0 || height() <= 0) return false
        if (maxTop != null && top > maxTop) return false
        if (minTop != null && top < minTop) return false
        return true
    }
}
