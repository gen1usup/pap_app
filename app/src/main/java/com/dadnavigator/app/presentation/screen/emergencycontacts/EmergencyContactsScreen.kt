package com.dadnavigator.app.presentation.screen.emergencycontacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.model.EmergencyContactType
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton

@Composable
fun EmergencyContactsScreen(
    onBack: () -> Unit,
    viewModel: EmergencyContactsViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.errorRes?.let { stringResource(id = it) }
        ?: state.infoRes?.let { stringResource(id = it) }

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    val spacing = DadTheme.spacing
    val visibleContacts = state.contacts.filter { contact ->
        contact.type == EmergencyContactType.AMBULANCE ||
            contact.type in state.expandedTypes ||
            contact.phone.isNotBlank()
    }
    val hiddenContacts = state.contacts.filterNot { contact ->
        contact.type == EmergencyContactType.AMBULANCE ||
            contact.type in state.expandedTypes ||
            contact.phone.isNotBlank()
    }
    ScreenScaffold(
        title = stringResource(id = R.string.emergency_contacts_title),
        subtitle = stringResource(id = R.string.emergency_contacts_subtitle),
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        ScreenBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                items(visibleContacts, key = { it.type.name }) { contact ->
                    EmergencyContactCard(
                        contact = contact,
                        onTitleChanged = { viewModel.updateTitle(contact.type, it) },
                        onPhoneChanged = { viewModel.updatePhone(contact.type, it) }
                    )
                }
                if (hiddenContacts.isNotEmpty()) {
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.emergency_contacts_add_more),
                            description = stringResource(id = R.string.emergency_contacts_subtitle),
                            icon = Icons.Outlined.Call
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                hiddenContacts.forEach { contact ->
                                    SecondaryButton(
                                        text = stringResource(
                                            id = R.string.emergency_contacts_add_contact,
                                            stringResource(id = contactTypeLabel(contact.type))
                                        ),
                                        onClick = { viewModel.showContact(contact.type) }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    PrimaryButton(
                        text = stringResource(id = R.string.settings_save),
                        onClick = viewModel::save
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyContactCard(
    contact: EmergencyContact,
    onTitleChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = DadTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(DadTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)
        ) {
            Text(
                text = stringResource(id = contactTypeLabel(contact.type)),
                style = MaterialTheme.typography.titleMedium
            )
            if (contact.type != EmergencyContactType.AMBULANCE) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = contact.title,
                    onValueChange = onTitleChanged,
                    label = { Text(text = stringResource(id = R.string.emergency_contact_name)) }
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = contact.phone,
                onValueChange = onPhoneChanged,
                label = { Text(text = stringResource(id = R.string.emergency_contact_phone)) }
            )
            SecondaryButton(
                text = stringResource(id = R.string.emergency_contact_call),
                onClick = {
                    if (contact.phone.isNotBlank()) {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}")))
                    }
                },
                icon = Icons.Outlined.Call
            )
        }
    }
}

private fun contactTypeLabel(type: EmergencyContactType): Int = when (type) {
    EmergencyContactType.AMBULANCE -> R.string.contact_type_ambulance
    EmergencyContactType.MATERNITY_HOSPITAL -> R.string.contact_type_maternity
    EmergencyContactType.DOCTOR -> R.string.contact_type_doctor
    EmergencyContactType.MIDWIFE -> R.string.contact_type_midwife
    EmergencyContactType.TRUSTED_PERSON -> R.string.contact_type_trusted
    EmergencyContactType.PARTNER -> R.string.contact_type_partner
    EmergencyContactType.TAXI -> R.string.contact_type_taxi
}
