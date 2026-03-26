package com.dadnavigator.app.presentation.screen.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dadnavigator.app.R
import com.dadnavigator.app.presentation.component.InfoSectionCard
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Emergency guidance screen.
 */
@Composable
fun SosScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val criticalScenarios = stringArrayResource(id = R.array.sos_critical_scenarios).toList()
    val actions = stringArrayResource(id = R.array.sos_actions).toList()

    ScreenScaffold(
        title = stringResource(id = R.string.sos_title),
        onBack = onBack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card {
                    Text(
                        text = stringResource(id = R.string.sos_immediate),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                        context.startActivity(intent)
                    }
                ) {
                    Text(text = stringResource(id = R.string.sos_call_112))
                }
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.sos_title),
                    lines = criticalScenarios
                )
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.action_sos),
                    lines = actions
                )
            }
        }
    }
}



