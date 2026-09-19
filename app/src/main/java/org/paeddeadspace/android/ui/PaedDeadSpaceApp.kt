package org.paeddeadspace.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import org.paeddeadspace.android.core.ChaquopyCoreGateway
import org.paeddeadspace.android.core.CoreReleaseMetadata
import org.paeddeadspace.android.state.PaedDeadSpaceUiState
import org.paeddeadspace.android.state.PaedDeadSpaceViewModel


@Composable
fun PaedDeadSpaceApp() {
    val context = LocalContext.current
    val factory = remember(context) {
        PaedDeadSpaceViewModel.Factory(ChaquopyCoreGateway(context))
    }
    val viewModel: PaedDeadSpaceViewModel = viewModel(factory = factory)
    PaedDeadSpaceApp(viewModel)
}

@Composable
fun PaedDeadSpaceApp(viewModel: PaedDeadSpaceViewModel) {
    val context = LocalContext.current
    val metadata = remember(context) {
        runCatching {
            context.assets.open("core_release.json").bufferedReader().use { reader ->
                CoreReleaseMetadata.parse(reader.readText())
            }
        }.getOrNull()
    }
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        PaedDeadSpaceUiState.Notice -> NoticeScreen(viewModel::acceptNotice)
        is PaedDeadSpaceUiState.Scenario -> ScenarioScreen(
            draft = current.draft,
            errors = current.fieldErrors,
            onDraftChange = viewModel::updateDraft,
            onCalculate = viewModel::calculate,
        )
        PaedDeadSpaceUiState.Calculating -> Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        is PaedDeadSpaceUiState.Results -> ResultsScreen(
            response = current.response,
            metadata = metadata,
            onNewScenario = viewModel::newScenario,
        )
    }
}
