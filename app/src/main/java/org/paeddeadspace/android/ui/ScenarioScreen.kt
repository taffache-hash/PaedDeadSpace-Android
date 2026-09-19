package org.paeddeadspace.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.paeddeadspace.android.R
import org.paeddeadspace.android.core.AgeUnit
import org.paeddeadspace.android.core.ApparatusQualification
import org.paeddeadspace.android.core.DraftValidationError
import org.paeddeadspace.android.core.PatientModel
import org.paeddeadspace.android.core.ScenarioDraft


@Composable
fun ScenarioScreen(
    draft: ScenarioDraft,
    errors: List<DraftValidationError>,
    onDraftChange: ((ScenarioDraft) -> ScenarioDraft) -> Unit,
    onCalculate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.new_scenario), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.scenario_no_identifiers))

        NumberField(
            value = draft.weightKg,
            label = stringResource(R.string.weight_kg),
            tag = "weight",
            onValueChange = { value -> onDraftChange { it.copy(weightKg = value) } },
        )
        NumberField(
            value = draft.ageValue,
            label = stringResource(R.string.age_value),
            tag = "age",
            onValueChange = { value -> onDraftChange { it.copy(ageValue = value) } },
        )

        ChoiceHeading(stringResource(R.string.age_unit))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AgeChoice(AgeUnit.DAYS, R.string.days, draft, onDraftChange)
            AgeChoice(AgeUnit.MONTHS, R.string.months, draft, onDraftChange)
            AgeChoice(AgeUnit.YEARS, R.string.years, draft, onDraftChange)
        }
        ErrorText(errors, DraftValidationError.MissingAgeUnit, R.string.select_age_unit)

        NumberField(
            value = draft.tidalVolumeMlKg,
            label = stringResource(R.string.tidal_volume_ml_kg),
            tag = "tidal_volume",
            onValueChange = { value -> onDraftChange { it.copy(tidalVolumeMlKg = value) } },
        )
        NumberField(
            value = draft.respiratoryRateBpm,
            label = stringResource(R.string.respiratory_rate_bpm),
            tag = "respiratory_rate",
            onValueChange = { value -> onDraftChange { it.copy(respiratoryRateBpm = value) } },
        )

        ChoiceHeading(stringResource(R.string.patient_dead_space_model))
        ModelChoice(
            PatientModel.USER_DEFINED,
            R.string.model_user_defined,
            draft,
            onDraftChange,
        )
        ModelChoice(
            PatientModel.NUMA_FLETCHER_REFERENCE,
            R.string.model_numa_fletcher,
            draft,
            onDraftChange,
        )
        ModelChoice(
            PatientModel.PEARSALL_BENCHMARK,
            R.string.model_pearsall,
            draft,
            onDraftChange,
        )
        ErrorText(
            errors,
            DraftValidationError.MissingPatientModel,
            R.string.select_patient_model,
        )

        if (draft.model == PatientModel.USER_DEFINED) {
            NumberField(
                value = draft.patientVdVt,
                label = stringResource(R.string.patient_vd_vt),
                tag = "patient_vd_vt",
                onValueChange = { value -> onDraftChange { it.copy(patientVdVt = value) } },
            )
        }

        Text(stringResource(R.string.apparatus_section), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.apparatus_separate_notice))
        NumberField(
            value = draft.apparatusDeadSpaceMl,
            label = stringResource(R.string.apparatus_dead_space_ml),
            tag = "apparatus_dead_space",
            onValueChange = { value -> onDraftChange { it.copy(apparatusDeadSpaceMl = value) } },
        )
        OutlinedTextField(
            value = draft.apparatusName,
            onValueChange = { value -> onDraftChange { it.copy(apparatusName = value) } },
            modifier = Modifier.fillMaxWidth().testTag("apparatus_name"),
            label = { Text(stringResource(R.string.apparatus_name)) },
            singleLine = true,
        )

        ChoiceHeading(stringResource(R.string.apparatus_qualification))
        QualificationChoice(
            ApparatusQualification.FUNCTIONAL_MEASURED,
            R.string.qualification_functional,
            draft,
            onDraftChange,
        )
        QualificationChoice(
            ApparatusQualification.INTERNAL_OR_GEOMETRIC,
            R.string.qualification_internal,
            draft,
            onDraftChange,
        )
        QualificationChoice(
            ApparatusQualification.USER_ESTIMATE,
            R.string.qualification_estimate,
            draft,
            onDraftChange,
        )

        errors.filterNot {
            it == DraftValidationError.MissingAgeUnit ||
                it == DraftValidationError.MissingPatientModel
        }.forEach { error ->
            Text(
                text = validationMessage(error),
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.calculate))
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    label: String,
    tag: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().testTag(tag),
        label = { Text(label) },
        singleLine = true,
    )
}

@Composable
private fun ChoiceHeading(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun AgeChoice(
    value: AgeUnit,
    label: Int,
    draft: ScenarioDraft,
    onDraftChange: ((ScenarioDraft) -> ScenarioDraft) -> Unit,
) {
    FilterChip(
        selected = draft.ageUnit == value,
        onClick = { onDraftChange { it.copy(ageUnit = value) } },
        label = { Text(stringResource(label)) },
    )
}

@Composable
private fun ModelChoice(
    value: PatientModel,
    label: Int,
    draft: ScenarioDraft,
    onDraftChange: ((ScenarioDraft) -> ScenarioDraft) -> Unit,
) {
    FilterChip(
        selected = draft.model == value,
        onClick = { onDraftChange { it.copy(model = value) } },
        label = { Text(stringResource(label)) },
    )
}

@Composable
private fun QualificationChoice(
    value: ApparatusQualification,
    label: Int,
    draft: ScenarioDraft,
    onDraftChange: ((ScenarioDraft) -> ScenarioDraft) -> Unit,
) {
    FilterChip(
        selected = draft.apparatusQualification == value,
        onClick = { onDraftChange { it.copy(apparatusQualification = value) } },
        label = { Text(stringResource(label)) },
    )
}

@Composable
private fun ErrorText(
    errors: List<DraftValidationError>,
    error: DraftValidationError,
    message: Int,
) {
    if (error in errors) {
        Text(stringResource(message), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun validationMessage(error: DraftValidationError): String = stringResource(
    when (error) {
        DraftValidationError.MissingPatientVdVt -> R.string.missing_patient_vd_vt
        DraftValidationError.InvalidPatientVdVt -> R.string.invalid_patient_vd_vt
        DraftValidationError.MissingWeight -> R.string.missing_weight
        DraftValidationError.InvalidWeight -> R.string.invalid_weight
        DraftValidationError.MissingAge -> R.string.missing_age
        DraftValidationError.InvalidAge -> R.string.invalid_age
        DraftValidationError.MissingTidalVolume -> R.string.missing_tidal_volume
        DraftValidationError.InvalidTidalVolume -> R.string.invalid_tidal_volume
        DraftValidationError.MissingRespiratoryRate -> R.string.missing_respiratory_rate
        DraftValidationError.InvalidRespiratoryRate -> R.string.invalid_respiratory_rate
        DraftValidationError.MissingApparatusDeadSpace -> R.string.missing_apparatus_dead_space
        DraftValidationError.InvalidApparatusDeadSpace -> R.string.invalid_apparatus_dead_space
        DraftValidationError.MissingApparatusName -> R.string.missing_apparatus_name
        DraftValidationError.MissingApparatusQualification -> R.string.missing_apparatus_qualification
        DraftValidationError.MissingAgeUnit -> R.string.select_age_unit
        DraftValidationError.MissingPatientModel -> R.string.select_patient_model
    },
)
