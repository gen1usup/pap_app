package com.dadnavigator.app.domain.model

/**
 * Manual high-level scenario chosen by the user.
 *
 * The app never switches stages automatically based only on due date.
 */
enum class AppStage {
    PREPARING,
    LABOR,
    AFTER_BIRTH
}
