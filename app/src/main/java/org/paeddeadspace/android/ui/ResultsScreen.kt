package org.paeddeadspace.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.paeddeadspace.android.R
import org.paeddeadspace.android.core.CalculationResult
import org.paeddeadspace.android.core.CoreReleaseMetadata
import org.paeddeadspace.android.core.CoreResponse
import org.paeddeadspace.android.core.PartialVolumes
import java.util.Locale


@Composable
fun ResultsScreen(
    response: CoreResponse,
    metadata: CoreReleaseMetadata?,
    onNewScenario: () -> Unit,
) {
    var detailsExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.results), style = MaterialTheme.typography.headlineMedium)
        when (response) {
            is CoreResponse.Success -> SuccessContent(response.result)
            is CoreResponse.InputError -> {
                Text(stringResource(R.string.input_error), style = MaterialTheme.typography.titleLarge)
                Text(response.message)
            }
            is CoreResponse.ModelBreakdown -> BoundaryContent(response)
        }

        TextButton(onClick = { detailsExpanded = !detailsExpanded }) {
            Text(stringResource(R.string.details_and_provenance))
        }
        if (detailsExpanded) DetailsScreen(metadata)

        Button(onClick = onNewScenario, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.new_scenario))
        }
    }
}

@Composable
private fun SuccessContent(result: CalculationResult) {
    if (result.warnings.isNotEmpty()) {
        Text(stringResource(R.string.warnings), style = MaterialTheme.typography.titleLarge)
        result.warnings.forEach { warning ->
            Text(warning, color = MaterialTheme.colorScheme.error)
        }
    }
    ResultLine(R.string.result_vt, result.vtMl, "mL")
    ResultLine(R.string.result_patient_vd, result.patientVdMl, "mL")
    ResultLine(R.string.result_apparatus_vd, result.apparatusVdMl, "mL")
    ResultLine(R.string.result_total_vd, result.totalVdMl, "mL")
    ResultLine(R.string.result_alveolar_vt, result.alveolarVtMl, "mL")
    ResultLine(R.string.result_alveolar_ve, result.alveolarVeMlMin, "mL/min")
    ResultLine(R.string.result_relative_co2, result.relativeCo2Burden, "ratio")
    ResultLine(R.string.result_rr_required, result.rrRequiredBpm, "breaths/min")
}

@Composable
private fun BoundaryContent(response: CoreResponse.ModelBreakdown) {
    Text(
        stringResource(R.string.mathematical_model_boundary),
        style = MaterialTheme.typography.titleLarge,
    )
    response.warnings.forEach { warning ->
        Text(warning, color = MaterialTheme.colorScheme.error)
    }
    Text(response.message)
    response.partial?.let { PartialContent(it) }
}

@Composable
private fun PartialContent(partial: PartialVolumes) {
    partial.vtMl?.let { ResultLine(R.string.result_vt, it, "mL") }
    partial.patientVdMl?.let { ResultLine(R.string.result_patient_vd, it, "mL") }
    partial.apparatusVdMl?.let { ResultLine(R.string.result_apparatus_vd, it, "mL") }
    partial.totalVdMl?.let { ResultLine(R.string.result_total_vd, it, "mL") }
}

@Composable
private fun ResultLine(label: Int, value: Double, unit: String) {
    Text(
        text = stringResource(
            R.string.result_value,
            stringResource(label),
            formatNumber(value),
            unit,
        ),
    )
}

private fun formatNumber(value: Double): String = String.format(Locale.US, "%.2f", value)
