package org.paeddeadspace.android.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class ScenarioValidatorTest {
    @Test
    fun blankDraftRequiresExplicitAgeUnitAndModel() {
        val result = ScenarioValidator.toRequest(ScenarioDraft.blank())

        assertEquals(DraftValidationError.MissingAgeUnit, result.error)
        assertTrue(DraftValidationError.MissingPatientModel in result.errors)
    }

    @Test
    fun userDefinedModeRequiresPatientOnlyVdVt() {
        val result = ScenarioValidator.toRequest(
            ScenarioDraft.valid().copy(
                model = PatientModel.USER_DEFINED,
                patientVdVt = "",
            ),
        )

        assertEquals(DraftValidationError.MissingPatientVdVt, result.error)
    }

    @Test
    fun validDraftCreatesTheTypedRequest() {
        val result = ScenarioValidator.toRequest(ScenarioDraft.valid())

        assertTrue(result.errors.isEmpty())
        assertEquals(10.0, result.request?.weightKg ?: Double.NaN, 0.0)
        assertEquals(AgeUnit.YEARS, result.request?.ageUnit)
        assertEquals(PatientModel.PEARSALL_BENCHMARK, result.request?.model)
    }

    @Test
    fun zeroApparatusDeadSpaceIsAccepted() {
        val result = ScenarioValidator.toRequest(
            ScenarioDraft.valid().copy(apparatusDeadSpaceMl = "0"),
        )

        assertTrue(result.errors.isEmpty())
        assertEquals(0.0, result.request?.apparatusDeadSpaceMl ?: Double.NaN, 0.0)
    }

    @Test
    fun patientVdVtOutsideCoreRangeIsRejected() {
        val result = ScenarioValidator.toRequest(
            ScenarioDraft.valid().copy(
                model = PatientModel.USER_DEFINED,
                patientVdVt = "1.01",
            ),
        )

        assertEquals(DraftValidationError.InvalidPatientVdVt, result.error)
    }

    @Test
    fun commaDecimalProducedByLocalizedKeyboardIsAccepted() {
        val result = ScenarioValidator.toRequest(
            ScenarioDraft.valid().copy(
                weightKg = "10,5",
                ageValue = "2,5",
                tidalVolumeMlKg = "7,5",
                respiratoryRateBpm = "19,5",
                apparatusDeadSpaceMl = "12,5",
            ),
        )

        assertTrue(result.errors.isEmpty())
        assertEquals(10.5, result.request?.weightKg ?: Double.NaN, 0.0)
        assertEquals(7.5, result.request?.tidalVolumeMlKg ?: Double.NaN, 0.0)
        assertEquals(12.5, result.request?.apparatusDeadSpaceMl ?: Double.NaN, 0.0)
    }
}
