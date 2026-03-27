package com.dadnavigator.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose-level validation test for settings forms where UiAutomator text input is too flaky.
 */
@RunWith(AndroidJUnit4::class)
class SettingsValidationUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun invalidDueDateShowsInlineValidationMessage() {
        val context = composeRule.activity
        val menuLabel = context.getString(R.string.nav_menu)
        val settingsTitle = context.getString(R.string.settings_title)
        val saveLabel = context.getString(R.string.settings_save)
        val invalidDateMessage = context.getString(R.string.invalid_date)

        composeRule.onNodeWithContentDescription(menuLabel).performClick()
        composeRule.onNodeWithText(settingsTitle).performClick()

        composeRule.onAllNodes(hasSetTextAction())[1].performTextReplacement("31.02.2026")
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasText(saveLabel))
        composeRule.onNodeWithText(saveLabel).performClick()
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasText(invalidDateMessage))
        composeRule.onNodeWithText(invalidDateMessage).assertIsDisplayed()
    }
}
