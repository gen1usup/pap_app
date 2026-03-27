package com.dadnavigator.app.presentation.screen.emergencycontacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.core.util.openDialer
import com.dadnavigator.app.core.util.openRoute
import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.model.EmergencyContactType
import com.dadnavigator.app.presentation.component.DangerButton
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
                contentPadding = PaddingValues(
                    horizontal = DadTheme.spacing.md,
                    vertical = DadTheme.spacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)
            ) {
                items(state.contacts, key = { it.id }) { contact ->
                    ContactCard(
                        contact = contact,
                        isDirty = state.dirtyContactIds.contains(contact.id),
                        canSave = viewModel.canSave(contact),
                        onTitleChanged = { viewModel.updateTitle(contact.id, it) },
                        onPhoneChanged = { viewModel.updatePhone(contact.id, it) },
                        onAddressChanged = { viewModel.updateAddress(contact.id, it) },
                        onSave = { viewModel.saveContact(contact.id) },
                        onDelete = { viewModel.deleteContact(contact.id) }
                    )
                }
                item {
                    InfoCard(
                        title = stringResource(id = R.string.emergency_contacts_add_more),
                        description = stringResource(id = R.string.emergency_contacts_add_description),
                        icon = Icons.Outlined.Add
                    ) {
                        PrimaryButton(
                            text = stringResource(id = R.string.emergency_contacts_add_action),
                            onClick = viewModel::addContact,
                            icon = Icons.Outlined.Add
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    isDirty: Boolean,
    canSave: Boolean,
    onTitleChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val colors = MaterialTheme.colorScheme
    val isEmergency = contact.type == EmergencyContactType.EMERGENCY
    val isHospital = contact.type == EmergencyContactType.HOSPITAL

    Card(
        shape = DadTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = if (isEmergency) {
                colors.errorContainer
            } else {
                colors.surfaceContainerLow
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(DadTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contactHeader(contact),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isEmergency) colors.onErrorContainer else colors.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.xs)) {
                    if (isHospital && contact.address.isNotBlank()) {
                        IconButton(
                            onClick = { openRoute(context, contact.address) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Route,
                                contentDescription = stringResource(id = R.string.emergency_contact_route)
                            )
                        }
                        IconButton(
                            onClick = { clipboard.setText(AnnotatedString(contact.address)) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = stringResource(id = R.string.emergency_contact_copy_address)
                            )
                        }
                    }
                    if (!contact.isDefault) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(id = R.string.action_delete)
                            )
                        }
                    }
                }
            }

            when {
                isEmergency -> {
                    Text(
                        text = contact.phone,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onErrorContainer
                    )
                }

                isHospital -> {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = contact.phone,
                        onValueChange = onPhoneChanged,
                        label = { Text(text = stringResource(id = R.string.emergency_contact_phone)) }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = contact.address,
                        onValueChange = onAddressChanged,
                        label = { Text(text = stringResource(id = R.string.emergency_contact_address)) }
                    )
                }

                else -> {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = contact.title,
                        onValueChange = onTitleChanged,
                        label = { Text(text = stringResource(id = R.string.emergency_contact_name)) }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = contact.phone,
                        onValueChange = onPhoneChanged,
                        label = { Text(text = stringResource(id = R.string.emergency_contact_phone)) }
                    )
                }
            }

            if (isDirty && !isEmergency) {
                PrimaryButton(
                    text = stringResource(id = R.string.action_save),
                    onClick = onSave,
                    enabled = canSave
                )
            }

            if (contact.phone.isNotBlank()) {
                if (isEmergency) {
                    DangerButton(
                        text = stringResource(id = R.string.emergency_contact_call),
                        onClick = { openDialer(context, contact.phone) },
                        icon = Icons.Outlined.Call
                    )
                } else {
                    SecondaryButton(
                        text = stringResource(id = R.string.emergency_contact_call),
                        onClick = { openDialer(context, contact.phone) },
                        icon = Icons.Outlined.Call
                    )
                }
            }
        }
    }
}

@Composable
private fun contactHeader(contact: EmergencyContact): String {
    return when (contact.type) {
        EmergencyContactType.EMERGENCY -> stringResource(id = R.string.contact_type_ambulance_full)
        EmergencyContactType.HOSPITAL -> stringResource(id = R.string.contact_type_maternity)
        else -> contact.title.ifBlank { stringResource(id = R.string.emergency_contact_new_title) }
    }
}
