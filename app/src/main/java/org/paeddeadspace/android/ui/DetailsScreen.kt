package org.paeddeadspace.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.paeddeadspace.android.R
import org.paeddeadspace.android.core.CoreReleaseMetadata


@Composable
fun DetailsScreen(metadata: CoreReleaseMetadata?) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider()
        Text(stringResource(R.string.model_status))
        Text(stringResource(R.string.details_nonclinical_scope))
        Text(stringResource(R.string.privacy_summary))
        if (metadata != null) {
            Text(stringResource(R.string.core_version_value, metadata.packageVersion))
            Text(stringResource(R.string.github_value, metadata.repository))
            Text(stringResource(R.string.release_doi_value, metadata.releaseDoi))
            Text(stringResource(R.string.concept_doi_value, metadata.conceptDoi))
        }
    }
}
