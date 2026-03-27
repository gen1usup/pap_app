package com.dadnavigator.app.domain.model

/**
 * Manual high-level scenario chosen by the user.
 *
 * The app never switches stages automatically based only on due date.
 * Stored values remain string-based, so this enum also keeps compatibility
 * with older persisted names from previous app versions.
 */
enum class AppStage {
    PREPARING,
    CONTRACTIONS,
    AT_HOSPITAL,
    AT_HOME;

    companion object {
        fun fromStorage(rawValue: String?): AppStage = when (rawValue) {
            null,
            PREPARING.name -> PREPARING

            CONTRACTIONS.name,
            "LABOR" -> CONTRACTIONS

            AT_HOSPITAL.name -> AT_HOSPITAL

            AT_HOME.name,
            "AFTER_BIRTH" -> AT_HOME

            else -> PREPARING
        }
    }
}
