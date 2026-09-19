package org.paeddeadspace.android.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class AgeUnit { DAYS, MONTHS, YEARS }

@Serializable
enum class PatientModel {
    USER_DEFINED,
    PEARSALL_BENCHMARK,
    NUMA_FLETCHER_REFERENCE,
}

@Serializable
enum class ApparatusQualification {
    FUNCTIONAL_MEASURED,
    INTERNAL_OR_GEOMETRIC,
    USER_ESTIMATE,
}

@Serializable
data class CalculationRequest(
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("age_value") val ageValue: Double,
    @SerialName("age_unit") val ageUnit: AgeUnit,
    @SerialName("tidal_volume_ml_kg") val tidalVolumeMlKg: Double,
    @SerialName("respiratory_rate_bpm") val respiratoryRateBpm: Double,
    val model: PatientModel,
    @SerialName("patient_vd_vt") val patientVdVt: Double?,
    @SerialName("apparatus_dead_space_ml") val apparatusDeadSpaceMl: Double,
    @SerialName("apparatus_name") val apparatusName: String,
    @SerialName("apparatus_qualification")
    val apparatusQualification: ApparatusQualification,
)

@Serializable
data class SelectedModelMetadata(
    val key: String,
    val name: String,
    val kind: String,
)

@Serializable
data class CalculationResult(
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("age_value") val ageValue: Double,
    @SerialName("age_unit") val ageUnit: String,
    @SerialName("age_years") val ageYears: Double,
    @SerialName("tidal_volume_ml_kg") val tidalVolumeMlKg: Double,
    @SerialName("respiratory_rate_bpm") val respiratoryRateBpm: Double,
    @SerialName("vt_ml") val vtMl: Double,
    @SerialName("patient_vd_ml") val patientVdMl: Double,
    @SerialName("apparatus_vd_ml") val apparatusVdMl: Double,
    @SerialName("total_vd_ml") val totalVdMl: Double,
    @SerialName("total_vd_vt") val totalVdVt: Double,
    @SerialName("alveolar_vt_ml") val alveolarVtMl: Double,
    @SerialName("alveolar_ve_ml_min") val alveolarVeMlMin: Double,
    @SerialName("baseline_alveolar_ve_ml_min") val baselineAlveolarVeMlMin: Double,
    @SerialName("current_to_baseline_alveolar_ve_ratio")
    val currentToBaselineAlveolarVeRatio: Double,
    @SerialName("relative_co2_burden") val relativeCo2Burden: Double,
    @SerialName("rr_required_bpm") val rrRequiredBpm: Double,
    @SerialName("rr_multiplier") val rrMultiplier: Double,
    @SerialName("apparatus_dead_space_percent_vt")
    val apparatusDeadSpacePercentVt: Double,
    @SerialName("vt_to_apparatus_dead_space_ratio")
    val vtToApparatusDeadSpaceRatio: Double?,
    @SerialName("patient_airway_dead_space_ml")
    val patientAirwayDeadSpaceMl: Double?,
    @SerialName("patient_alveolar_tidal_volume_ml")
    val patientAlveolarTidalVolumeMl: Double?,
    @SerialName("patient_alveolar_dead_space_ml")
    val patientAlveolarDeadSpaceMl: Double?,
    val warnings: List<String>,
    val model: SelectedModelMetadata,
    @SerialName("core_version") val coreVersion: String,
)

@Serializable
data class PartialVolumes(
    @SerialName("vt_ml") val vtMl: Double? = null,
    @SerialName("patient_vd_ml") val patientVdMl: Double? = null,
    @SerialName("apparatus_vd_ml") val apparatusVdMl: Double? = null,
    @SerialName("total_vd_ml") val totalVdMl: Double? = null,
)

sealed interface CoreResponse {
    data class Success(val result: CalculationResult) : CoreResponse
    data class InputError(val message: String) : CoreResponse
    data class ModelBreakdown(
        val message: String,
        val partial: PartialVolumes?,
        val warnings: List<String> = emptyList(),
    ) : CoreResponse
}

data class ScenarioDraft(
    val weightKg: String,
    val ageValue: String,
    val ageUnit: AgeUnit?,
    val tidalVolumeMlKg: String,
    val respiratoryRateBpm: String,
    val model: PatientModel?,
    val patientVdVt: String,
    val apparatusDeadSpaceMl: String,
    val apparatusName: String,
    val apparatusQualification: ApparatusQualification?,
) {
    companion object {
        fun blank() = ScenarioDraft(
            weightKg = "",
            ageValue = "",
            ageUnit = null,
            tidalVolumeMlKg = "",
            respiratoryRateBpm = "",
            model = null,
            patientVdVt = "",
            apparatusDeadSpaceMl = "0",
            apparatusName = "",
            apparatusQualification = null,
        )

        fun valid() = ScenarioDraft(
            weightKg = "10",
            ageValue = "2",
            ageUnit = AgeUnit.YEARS,
            tidalVolumeMlKg = "8",
            respiratoryRateBpm = "20",
            model = PatientModel.PEARSALL_BENCHMARK,
            patientVdVt = "",
            apparatusDeadSpaceMl = "30",
            apparatusName = "User-entered apparatus",
            apparatusQualification = ApparatusQualification.USER_ESTIMATE,
        )
    }
}

enum class DraftValidationError {
    MissingAgeUnit,
    MissingPatientModel,
    MissingPatientVdVt,
    InvalidPatientVdVt,
    MissingWeight,
    InvalidWeight,
    MissingAge,
    InvalidAge,
    MissingTidalVolume,
    InvalidTidalVolume,
    MissingRespiratoryRate,
    InvalidRespiratoryRate,
    MissingApparatusDeadSpace,
    InvalidApparatusDeadSpace,
    MissingApparatusName,
    MissingApparatusQualification,
}

data class DraftValidationResult(
    val request: CalculationRequest?,
    val errors: List<DraftValidationError>,
) {
    val error: DraftValidationError? get() = errors.firstOrNull()
}
