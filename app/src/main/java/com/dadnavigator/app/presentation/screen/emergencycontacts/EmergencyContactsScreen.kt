package com.dadnavigator.app.presentation.screen.emergencycontacts

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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.Route
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

    val addableTypes = buildAddableContactTypes(state.contacts)

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
                contentPadding = PaddingValues(horizontal = DadTheme.spacing.md, vertical = DadTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)
            ) {
                items(state.contacts, key = { it.id }) { contact ->
                    EmergencyContactCard(
                        contact = contact,
                        onTitleChanged = { viewModel.updateTitle(contact.id, it) },
                        onPhoneChanged = { viewModel.updatePhone(contact.id, it) },
                        onAddressChanged = { viewModel.updateAddress(contact.id, it) },
                        onDelete = { viewModel.deleteContact(contact.id) }
                    )
                }
                item {
                    InfoCard(
                        title = stringResource(id = R.string.emergency_contacts_add_more),
                        description = stringResource(id = R.string.emergency_contacts_add_description),
                        icon = Icons.Outlined.PersonAddAlt1
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)) {
                            addableTypes.forEach { type ->
                                SecondaryButton(
                                    text = stringResource(
                                        id = R.string.emergency_contacts_add_contact,
                                        stringResource(id = contactTypeLabel(type))
                                    ),
                                    onClick = { viewModel.addContact(type) }
                                )
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
    onPhoneChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

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
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = contact.title,
                onValueChange = onTitleChanged,
                enabled = !contact.isDefault,
                label = { Text(text = stringResource(id = R.string.emergency_contact_name)) }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = contact.phone,
                onValueChange = onPhoneChanged,
                enabled = !contact.isDefault,
                label = { Text(text = stringResource(id = R.string.emergency_contact_phone)) }
            )
            if (contact.type == EmergencyContactType.HOSPITAL) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = contact.address,
                    onValueChange = onAddressChanged,
                    label = { Text(text = stringResource(id = R.string.emergency_contact_address)) }
                )
            }
            SecondaryButton(
                text = stringResource(id = R.string.emergency_contact_call),
                onClick = { openDialer(context, contact.phone) },
                icon = Icons.Outlined.Call
            )
            if (contact.type == EmergencyContactType.HOSPITAL && contact.address.isNotBlank()) {
                SecondaryButton(
                    text = stringResource(id = R.string.emergency_contact_route),
                    onClick = { openRoute(context, contact.address) },
                    icon = Icons.Outlined.Route
                )
                SecondaryButton(
                    text = stringResource(id = R.string.emergency_contact_copy_address),
                    onClick = { clipboard.setText(AnnotatedString(contact.address)) },
                    icon = Icons.Outlined.ContentCopy
                )
            }
            if (!contact.isDefault) {
                SecondaryButton(
                    text = stringResource(id = R.string.action_delete),
                    onClick = onDelete,
                    icon = Icons.Outlined.DeleteOutline
                )
            }
        }
    }
}

private fun buildAddableContactTypes(contacts: List<EmergencyContact>): List<EmergencyContactType> {
    val result = mutableListOf<EmergencyContactType>()
    val singleTypes = listOf(
        EmergencyContactType.WIFE,
        EmergencyContactType.DOCTOR,
        EmergencyContactType.HOSPITAL,
        EmergencyContactType.TAXI
    )
    singleTypes.filterNot { type -> contacts.any { it.type == type } }.forEach(result::add)
    result += EmergencyContactType.RELATIVE
    result += EmergencyContactType.CUSTOM
    return result
}

private fun contactTypeLabel(type: EmergencyContactType): Int = when (type) {
    EmergencyContactType.EMERGENCY -> R.string.contact_type_ambulance
    EmergencyContactType.HOSPITAL -> R.string.contact_type_maternity
    EmergencyContactType.DOCTOR -> R.string.contact_type_doctor
    EmergencyContactType.WIFE -> R.string.contact_type_partner
    EmergencyContactType.TAXI -> R.string.contact_type_taxi
    EmergencyContactType.RELATIVE -> R.string.contact_type_relative
    EmergencyContactType.CUSTOM -> R.string.contact_type_custom
}
