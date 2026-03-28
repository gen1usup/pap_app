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
    LABOR,
    BABY_BORN;

    companion object {
        fun fromStorage(rawValue: String?): AppStage = when (rawValue) {
            null,
            PREPARING.name -> PREPARING

            LABOR.name,
            "CONTRACTIONS" -> LABOR

            BABY_BORN.name,
            "AT_HOSPITAL",
            "AT_HOME",
            "AFTER_BIRTH" -> BABY_BORN

            else -> PREPARING
        }
    }
}
