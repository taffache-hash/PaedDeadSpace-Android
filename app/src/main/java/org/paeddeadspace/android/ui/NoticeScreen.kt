package org.paeddeadspace.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.paeddeadspace.android.R


@Composable
fun NoticeScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.educational_research_use),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(stringResource(R.string.professional_nonclinical_notice))
        Text(stringResource(R.string.no_identifiers_notice))
        Text(stringResource(R.string.privacy_summary))
        Button(onClick = onContinue) {
            Text(stringResource(R.string.continue_action))
        }
    }
}
