package com.dadnavigator.app.presentation.screen.help

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.InfoSectionCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    viewModel: HelpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val preparing = stringArrayResource(id = R.array.help_preparing).toList()
    val labor = stringArrayResource(id = R.array.help_labor).toList()
    val afterBirth = stringArrayResource(id = R.array.help_after_birth).toList()
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
        title = stringResource(id = R.string.help_title),
        subtitle = stringResource(id = R.string.help_subtitle),
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
                item {
                    InfoCard(
                        title = stringResource(id = R.string.help_maternity_route_title),
                        description = stringResource(id = R.string.help_maternity_route_description),
                        icon = Icons.Outlined.LocalHospital
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.maternityHospitalAddress,
                                onValueChange = viewModel::updateMaternityHospitalAddress,
                                label = { Text(text = stringResource(id = R.string.help_maternity_address_label)) }
                            )
                            PrimaryButton(
                                text = stringResource(id = R.string.help_save_maternity_address),
                                onClick = viewModel::saveMaternityHospitalAddress,
                                icon = Icons.Outlined.LocalHospital
                            )
                            if (state.maternityHospitalAddress.isNotBlank()) {
                                SecondaryButton(
                                    text = stringResource(id = R.string.help_open_route),
                                    onClick = {
                                        openRoute(
                                            context = context,
                                            address = state.maternityHospitalAddress
                                        )
                                    },
                                    icon = Icons.Outlined.Map
                                )
                                SecondaryButton(
                                    text = stringResource(id = R.string.help_clear_maternity_address),
                                    onClick = viewModel::clearMaternityHospitalAddress
                                )
                            }
                        }
                    }
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.app_stage_preparing),
                        lines = preparing,
                        icon = Icons.Outlined.Schedule
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.app_stage_labor),
                        lines = labor,
                        icon = Icons.Outlined.MonitorHeart
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.app_stage_after_birth),
                        lines = afterBirth,
                        icon = Icons.Outlined.Route
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.help_how_app_works),
                        lines = listOf(
                            stringResource(id = R.string.help_how_app_works_1),
                            stringResource(id = R.string.help_how_app_works_2),
                            stringResource(id = R.string.help_how_app_works_3)
                        ),
                        icon = Icons.Outlined.Info
                    )
                }
            }
        }
    }
}

private fun openRoute(context: android.content.Context, address: String) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=${Uri.encode(address)}")
    )

    runCatching {
        context.startActivity(intent)
    }.recoverCatching {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
        )
        context.startActivity(browserIntent)
    }.getOrElse { error ->
        if (error !is ActivityNotFoundException) {
            throw error
        }
    }
}
