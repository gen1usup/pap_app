package com.dadnavigator.app.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dadnavigator.app.R

@Composable
fun TimelineActionButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Outlined.NotificationsNone,
            contentDescription = stringResource(id = R.string.nav_journal)
        )
    }
}
