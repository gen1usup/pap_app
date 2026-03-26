package com.dadnavigator.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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

    @Test
    fun opensContractionScreenFromDashboard() {
        val context = composeRule.activity
        val actionText = context.getString(com.dadnavigator.app.R.string.action_contraction_counter)
        val titleText = context.getString(com.dadnavigator.app.R.string.contraction_title)

        composeRule.onNodeWithText(actionText).performClick()
        composeRule.onNodeWithText(titleText).assertIsDisplayed()
    }
}
