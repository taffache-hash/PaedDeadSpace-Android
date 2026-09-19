package org.paeddeadspace.android.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.paeddeadspace.android.core.CoreGateway
import org.paeddeadspace.android.core.CoreResponse
import org.paeddeadspace.android.core.DraftValidationError
import org.paeddeadspace.android.core.ScenarioDraft
import org.paeddeadspace.android.core.ScenarioValidator


sealed interface PaedDeadSpaceUiState {
    data object Notice : PaedDeadSpaceUiState
    data class Scenario(
        val draft: ScenarioDraft,
        val fieldErrors: List<DraftValidationError> = emptyList(),
    ) : PaedDeadSpaceUiState
    data object Calculating : PaedDeadSpaceUiState
    data class Results(val response: CoreResponse) : PaedDeadSpaceUiState
}

class PaedDeadSpaceViewModel(
    private val gateway: CoreGateway,
) : ViewModel() {
    private val mutableState = MutableStateFlow<PaedDeadSpaceUiState>(PaedDeadSpaceUiState.Notice)
    val state: StateFlow<PaedDeadSpaceUiState> = mutableState.asStateFlow()

    fun acceptNotice() {
        mutableState.value = PaedDeadSpaceUiState.Scenario(ScenarioDraft.blank())
    }

    fun updateDraft(transform: (ScenarioDraft) -> ScenarioDraft) {
        val current = mutableState.value as? PaedDeadSpaceUiState.Scenario ?: return
        mutableState.value = current.copy(
            draft = transform(current.draft),
            fieldErrors = emptyList(),
        )
    }

    fun calculate() {
        val current = mutableState.value as? PaedDeadSpaceUiState.Scenario ?: return
        val validation = ScenarioValidator.toRequest(current.draft)
        val request = validation.request
        if (request == null) {
            mutableState.value = current.copy(fieldErrors = validation.errors)
            return
        }

        mutableState.value = PaedDeadSpaceUiState.Calculating
        viewModelScope.launch {
            mutableState.value = PaedDeadSpaceUiState.Results(gateway.calculate(request))
        }
    }

    fun newScenario() {
        mutableState.value = PaedDeadSpaceUiState.Scenario(ScenarioDraft.blank())
    }

    class Factory(
        private val gateway: CoreGateway,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PaedDeadSpaceViewModel::class.java))
            return PaedDeadSpaceViewModel(gateway) as T
        }
    }
}
