package com.dadnavigator.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme

@Composable
fun TimelineItem(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    description: String? = null,
    expanded: Boolean = true,
    onToggleExpanded: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = DadTheme.shapes.pill
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onToggleExpanded != null) {
                        Modifier.clickable(onClick = onToggleExpanded)
                    } else {
                        Modifier
                    }
                ),
            shape = DadTheme.shapes.card,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(DadTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.xxs)
                    ) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (!description.isNullOrBlank() && expanded) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else if (!description.isNullOrBlank() && onToggleExpanded != null) {
                    Text(
                        text = stringResource(id = R.string.timeline_item_expand_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
