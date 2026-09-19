package org.paeddeadspace.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.SemanticsProperties
import org.junit.Rule
import org.junit.Assert.assertTrue
import org.junit.Test
import org.paeddeadspace.android.core.CalculationRequest
import org.paeddeadspace.android.core.CoreGateway
import org.paeddeadspace.android.core.CoreResponse
import org.paeddeadspace.android.state.PaedDeadSpaceViewModel


class ScenarioFlowTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun calculateIsBlockedUntilAgeUnitAndModelAreExplicitlySelected() {
        setApp(CoreResponse.InputError("unused"))
        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithText("Calculate").performScrollTo().performClick()

        rule.onNodeWithText("Select an age unit").performScrollTo().assertExists()
        rule.onNodeWithText("Select a patient dead-space model").performScrollTo().assertExists()
    }

    @Test
    fun newScenarioClearsInMemoryValues() {
        setApp(CoreResponse.InputError("Test response"))
        rule.onNodeWithText("Continue").performClick()
        enterValidPearsallScenario()
        rule.onNodeWithText("Calculate").performScrollTo().performClick()
        rule.onNodeWithText("Input error").assertExists()

        rule.onNodeWithText("New scenario").performScrollTo().performClick()
        rule.onNodeWithTag("weight").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText,
                AnnotatedString(""),
            ),
        )
    }

    @Test
    fun noticeContinueRemainsReachableAtLargeFontOnSmallScreen() {
        var continued = false
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                MaterialTheme {
                    Box(Modifier.height(360.dp)) {
                        NoticeScreen { continued = true }
                    }
                }
            }
        }

        rule.onNodeWithText("Continue").performScrollTo().performClick()
        rule.runOnIdle { assertTrue(continued) }
    }

    private fun setApp(response: CoreResponse) {
        val viewModel = PaedDeadSpaceViewModel(FakeCoreGateway(response))
        rule.setContent {
            MaterialTheme {
                PaedDeadSpaceApp(viewModel)
            }
        }
    }

    private fun enterValidPearsallScenario() {
        rule.onNodeWithTag("weight").performTextInput("10")
        rule.onNodeWithTag("age").performTextInput("2")
        rule.onNodeWithText("Years").performClick()
        rule.onNodeWithTag("tidal_volume").performTextInput("8")
        rule.onNodeWithTag("respiratory_rate").performTextInput("20")
        rule.onNodeWithText("Pearsall benchmark (VD/VT 0.30)").performScrollTo().performClick()
        rule.onNodeWithTag("apparatus_name").performTextInput("Test apparatus")
        rule.onNodeWithText("User estimate / uncertain").performScrollTo().performClick()
    }
}

private class FakeCoreGateway(private val response: CoreResponse) : CoreGateway {
    override suspend fun calculate(request: CalculationRequest): CoreResponse = response
}
