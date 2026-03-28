package com.dadnavigator.app.presentation.navigation

import androidx.annotation.StringRes
import com.dadnavigator.app.R

/**
 * Navigation destinations of the app.
 */
sealed class AppDestination(
    val route: String,
    @StringRes val titleRes: Int
) {
    data object Dashboard : AppDestination("dashboard", R.string.dashboard_title)
    data object Events : AppDestination("events", R.string.events_title)
    data object Checklist : AppDestination("checklist", R.string.checklist_title)
    data object EmergencyContacts : AppDestination("emergency_contacts", R.string.emergency_contacts_title)
    data object Timeline : AppDestination("journal", R.string.journal_title)
    data object Contraction : AppDestination("contraction", R.string.contraction_title)
    data object WaterBreak : AppDestination("water_break", R.string.water_break_title)
    data object Baby : AppDestination("baby", R.string.baby_title)
    data object Decision : AppDestination("decision", R.string.decision_title)
    data object Help : AppDestination("help", R.string.help_title)
    data object About : AppDestination("about", R.string.about_title)
    data object Settings : AppDestination("settings", R.string.settings_title)
    data object StageDetails : AppDestination("stage/{stage}", R.string.drawer_stages_group) {
        const val stageArgument = "stage"

        fun routeFor(stage: com.dadnavigator.app.domain.model.AppStage): String = "stage/${stage.name}"
    }
}
