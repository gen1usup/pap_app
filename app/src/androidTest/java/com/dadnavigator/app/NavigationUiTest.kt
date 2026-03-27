package com.dadnavigator.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.testsupport.TestAppStateSeeder
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

    @Before
    fun seedPreparingStage() {
        TestAppStateSeeder(composeRule.activity).seedStage(AppStage.PREPARING)
        composeRule.waitForIdle()
    }

    @Test
    fun opensContractionScreenFromEventsTab() {
        val context = composeRule.activity
        val eventsTab = context.getString(com.dadnavigator.app.R.string.nav_events)
        val actionText = context.getString(com.dadnavigator.app.R.string.action_contraction_counter)
        val titleText = context.getString(com.dadnavigator.app.R.string.contraction_title)

        composeRule.onNodeWithText(eventsTab).performClick()
        composeRule.onNodeWithText(actionText).performClick()
        composeRule.onNodeWithText(titleText).assertIsDisplayed()
    }
}
