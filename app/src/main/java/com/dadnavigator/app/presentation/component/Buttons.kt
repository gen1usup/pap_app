package com.dadnavigator.app.presentation.component

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.dadnavigator.app.core.ui.DadTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    val buttonModifier = modifier.defaultMinSize(minHeight = DadTheme.spacing.touchTarget)
    Button(
        modifier = if (fullWidth) buttonModifier.fillMaxWidth() else buttonModifier,
        onClick = onClick,
        enabled = enabled,
        shape = DadTheme.shapes.pill,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    val buttonModifier = modifier.defaultMinSize(minHeight = DadTheme.spacing.touchTarget)
    FilledTonalButton(
        modifier = if (fullWidth) buttonModifier.fillMaxWidth() else buttonModifier,
        onClick = onClick,
        enabled = enabled,
        shape = DadTheme.shapes.pill,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    val buttonModifier = modifier.defaultMinSize(minHeight = DadTheme.spacing.touchTarget)
    Button(
        modifier = if (fullWidth) buttonModifier.fillMaxWidth() else buttonModifier,
        onClick = onClick,
        shape = DadTheme.shapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.error,
            contentColor = colors.onError
        ),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
