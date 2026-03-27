package com.dadnavigator.app.presentation.screen.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadNavigatorTheme
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.domain.model.EmergencyContactType
import com.dadnavigator.app.presentation.component.DangerButton
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.InfoSectionCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton

@Composable
fun SosScreen(
    onBack: () -> Unit,
    onOpenContacts: () -> Unit,
    viewModel: SosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val criticalScenarios = stringArrayResource(id = R.array.sos_critical_scenarios).toList()
    val actions = stringArrayResource(id = R.array.sos_actions).toList()
    val quickContacts = state.contacts.filter { it.phone.isNotBlank() }
    val maternityPhone = state.contacts
        .firstOrNull { it.type == EmergencyContactType.MATERNITY_HOSPITAL }
        ?.phone
        .orEmpty()
        .ifBlank { "103" }

    ScreenScaffold(
        title = stringResource(id = R.string.sos_title),
        subtitle = stringResource(id = R.string.sos_screen_subtitle),
        onBack = onBack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = DadTheme.spacing.md,
                vertical = DadTheme.spacing.sm
            ),
            verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)
        ) {
            item {
                Card(
                    shape = DadTheme.shapes.card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(DadTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)
                    ) {
                        Text(
                            text = stringResource(id = R.string.sos_immediate_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(id = R.string.sos_immediate),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            item {
                DangerButton(
                    text = stringResource(id = R.string.sos_call_112),
                    onClick = { dial(context, "112") },
                    icon = Icons.Outlined.Call
                )
            }

            item {
                SecondaryButton(
                    text = stringResource(id = R.string.sos_call_maternity),
                    onClick = { dial(context, maternityPhone) },
                    icon = Icons.Outlined.MedicalServices
                )
            }

            item {
                InfoCard(
                    title = stringResource(id = R.string.sos_contacts_title),
                    description = stringResource(id = R.string.sos_contacts_description),
                    icon = Icons.Outlined.Phone
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)) {
                        if (quickContacts.isEmpty()) {
                            SecondaryButton(
                                text = stringResource(id = R.string.sos_manage_contacts),
                                onClick = onOpenContacts,
                                icon = Icons.Outlined.Phone
                            )
                        } else {
                            quickContacts.take(4).forEach { contact ->
                                PrimaryButton(
                                    text = contact.title,
                                    onClick = { dial(context, contact.phone) },
                                    icon = Icons.Outlined.Call
                                )
                            }
                            SecondaryButton(
                                text = stringResource(id = R.string.sos_manage_contacts),
                                onClick = onOpenContacts
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(id = R.string.sos_signs_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(criticalScenarios) { scenario ->
                Card(
                    shape = DadTheme.shapes.card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Text(
                        text = scenario,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(DadTheme.spacing.lg)
                    )
                }
            }

            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.sos_actions_title),
                    lines = actions,
                    icon = Icons.Outlined.Emergency
                )
            }
        }
    }
}

private fun dial(context: android.content.Context, phone: String) {
    if (phone.isBlank()) return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SosPreview() {
    DadNavigatorTheme(dynamicColor = false) {
        SosScreen(
            onBack = {},
            onOpenContacts = {}
        )
    }
}
