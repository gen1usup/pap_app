package com.dadnavigator.app.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dadnavigator.app.core.ui.DadTheme

enum class StatusTone {
    Calm,
    Success,
    Warning,
    Critical
}

@Composable
fun StatusCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Calm,
    icon: ImageVector? = null,
    headline: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    val colors = statusColorsForTone(tone)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DadTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.container)
    ) {
        Column(
            modifier = Modifier.padding(DadTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.content
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.xxs)) {
                    if (headline != null) {
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.content
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.content
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.content.copy(alpha = 0.88f)
            )
            if (content != null) {
                content()
            }
        }
    }
}

@Composable
private fun statusColorsForTone(tone: StatusTone): ToneColors {
    val scheme = MaterialTheme.colorScheme
    val status = DadTheme.status
    return when (tone) {
        StatusTone.Calm -> ToneColors(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer
        )
        StatusTone.Success -> ToneColors(
            container = status.successContainer,
            content = status.onSuccessContainer
        )
        StatusTone.Warning -> ToneColors(
            container = status.warningContainer,
            content = status.onWarningContainer
        )
        StatusTone.Critical -> ToneColors(
            container = status.criticalContainer,
            content = status.onCriticalContainer
        )
    }
}

private data class ToneColors(
    val container: Color,
    val content: Color
)
