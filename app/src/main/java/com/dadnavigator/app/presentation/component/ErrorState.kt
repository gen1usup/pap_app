package com.dadnavigator.app.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ErrorState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    EmptyState(
        title = title,
        description = description,
        icon = Icons.Outlined.ErrorOutline,
        action = action,
        modifier = modifier
    )
}
