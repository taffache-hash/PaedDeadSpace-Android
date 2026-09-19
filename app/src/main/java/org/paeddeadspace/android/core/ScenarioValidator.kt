package org.paeddeadspace.android.core


object ScenarioValidator {
    fun toRequest(draft: ScenarioDraft): DraftValidationResult {
        val errors = mutableListOf<DraftValidationError>()

        if (draft.ageUnit == null) errors += DraftValidationError.MissingAgeUnit
        if (draft.model == null) errors += DraftValidationError.MissingPatientModel

        val patientVdVt = when {
            draft.model != PatientModel.USER_DEFINED -> null
            draft.patientVdVt.isBlank() -> {
                errors += DraftValidationError.MissingPatientVdVt
                null
            }
            else -> draft.patientVdVt.finiteDoubleOrNull().also { value ->
                if (value == null || value !in 0.0..1.0) {
                    errors += DraftValidationError.InvalidPatientVdVt
                }
            }
        }

        val weight = requiredPositive(
            draft.weightKg,
            DraftValidationError.MissingWeight,
            DraftValidationError.InvalidWeight,
            errors,
        )
        val age = requiredNonnegative(
            draft.ageValue,
            DraftValidationError.MissingAge,
            DraftValidationError.InvalidAge,
            errors,
        )
        val tidalVolume = requiredPositive(
            draft.tidalVolumeMlKg,
            DraftValidationError.MissingTidalVolume,
            DraftValidationError.InvalidTidalVolume,
            errors,
        )
        val respiratoryRate = requiredPositive(
            draft.respiratoryRateBpm,
            DraftValidationError.MissingRespiratoryRate,
            DraftValidationError.InvalidRespiratoryRate,
            errors,
        )
        val apparatusDeadSpace = requiredNonnegative(
            draft.apparatusDeadSpaceMl,
            DraftValidationError.MissingApparatusDeadSpace,
            DraftValidationError.InvalidApparatusDeadSpace,
            errors,
        )

        if (draft.apparatusName.isBlank()) {
            errors += DraftValidationError.MissingApparatusName
        }
        if (draft.apparatusQualification == null) {
            errors += DraftValidationError.MissingApparatusQualification
        }

        if (errors.isNotEmpty()) {
            return DraftValidationResult(request = null, errors = errors.distinct())
        }

        return DraftValidationResult(
            request = CalculationRequest(
                weightKg = requireNotNull(weight),
                ageValue = requireNotNull(age),
                ageUnit = requireNotNull(draft.ageUnit),
                tidalVolumeMlKg = requireNotNull(tidalVolume),
                respiratoryRateBpm = requireNotNull(respiratoryRate),
                model = requireNotNull(draft.model),
                patientVdVt = patientVdVt,
                apparatusDeadSpaceMl = requireNotNull(apparatusDeadSpace),
                apparatusName = draft.apparatusName.trim(),
                apparatusQualification = requireNotNull(draft.apparatusQualification),
            ),
            errors = emptyList(),
        )
    }

    private fun requiredPositive(
        text: String,
        missing: DraftValidationError,
        invalid: DraftValidationError,
        errors: MutableList<DraftValidationError>,
    ): Double? = requiredNumber(text, missing, invalid, errors) { it > 0.0 }

    private fun requiredNonnegative(
        text: String,
        missing: DraftValidationError,
        invalid: DraftValidationError,
        errors: MutableList<DraftValidationError>,
    ): Double? = requiredNumber(text, missing, invalid, errors) { it >= 0.0 }

    private fun requiredNumber(
        text: String,
        missing: DraftValidationError,
        invalid: DraftValidationError,
        errors: MutableList<DraftValidationError>,
        predicate: (Double) -> Boolean,
    ): Double? {
        if (text.isBlank()) {
            errors += missing
            return null
        }
        val value = text.finiteDoubleOrNull()
        if (value == null || !predicate(value)) {
            errors += invalid
            return null
        }
        return value
    }

    private fun String.finiteDoubleOrNull(): Double? =
        toDoubleOrNull()?.takeIf(Double::isFinite)
}
