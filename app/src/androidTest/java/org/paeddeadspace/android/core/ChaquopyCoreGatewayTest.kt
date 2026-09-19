package org.paeddeadspace.android.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ChaquopyCoreGatewayTest {
    private lateinit var gateway: CoreGateway

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        gateway = ChaquopyCoreGateway(context)
    }

    @Test
    fun pearsallGoldenCaseMatchesFrozenCoreOutput() = runBlocking {
        val response = gateway.calculate(pearsallRequest())
        assertTrue(response is CoreResponse.Success)
        val result = (response as CoreResponse.Success).result
        assertEquals(80.0, result.vtMl, 0.0001)
        assertEquals(54.0, result.totalVdMl, 0.0001)
        assertEquals(43.0769230769, result.rrRequiredBpm, 0.0001)
        assertEquals("1.0.0", result.coreVersion)
    }

    @Test
    fun numaFletcherWarningIsPropagated() = runBlocking {
        val response = gateway.calculate(
            pearsallRequest().copy(
                ageValue = 0.0,
                ageUnit = AgeUnit.DAYS,
                model = PatientModel.NUMA_FLETCHER_REFERENCE,
                patientVdVt = null,
                apparatusDeadSpaceMl = 0.0,
            ),
        )
        assertTrue(response is CoreResponse.Success)
        val warnings = (response as CoreResponse.Success).result.warnings
        assertEquals(1, warnings.size)
        assertTrue(warnings.single().contains("Numa intrathoracic component"))
    }

    @Test
    fun zeroApparatusRemainsAValidCoreCase() = runBlocking {
        val response = gateway.calculate(
            pearsallRequest().copy(
                model = PatientModel.USER_DEFINED,
                patientVdVt = 0.3,
                apparatusDeadSpaceMl = 0.0,
            ),
        )
        assertTrue(response is CoreResponse.Success)
        val result = (response as CoreResponse.Success).result
        assertEquals(0.0, result.apparatusVdMl, 0.0)
        assertEquals(1.0, result.relativeCo2Burden, 0.0001)
    }

    @Test
    fun invalidWeightMapsToInputError() = runBlocking {
        val response = gateway.calculate(pearsallRequest().copy(weightKg = -1.0))
        assertTrue(response is CoreResponse.InputError)
        assertTrue((response as CoreResponse.InputError).message.contains("weight_kg"))
    }

    @Test
    fun vdAtOrAboveVtMapsToBoundaryWithPartialVolumes() = runBlocking {
        val response = gateway.calculate(
            pearsallRequest().copy(
                model = PatientModel.USER_DEFINED,
                patientVdVt = 0.3,
                apparatusDeadSpaceMl = 60.0,
            ),
        )
        assertTrue(response is CoreResponse.ModelBreakdown)
        val boundary = response as CoreResponse.ModelBreakdown
        assertEquals(80.0, boundary.partial?.vtMl ?: Double.NaN, 0.0001)
        assertEquals(84.0, boundary.partial?.totalVdMl ?: Double.NaN, 0.0001)
    }

    private fun pearsallRequest() = CalculationRequest(
        weightKg = 10.0,
        ageValue = 2.0,
        ageUnit = AgeUnit.YEARS,
        tidalVolumeMlKg = 8.0,
        respiratoryRateBpm = 20.0,
        model = PatientModel.PEARSALL_BENCHMARK,
        patientVdVt = null,
        apparatusDeadSpaceMl = 30.0,
        apparatusName = "User-entered apparatus",
        apparatusQualification = ApparatusQualification.USER_ESTIMATE,
    )
}
