package com.hoyn.common.lib.ui.fragment_demo

import com.hoyn.common.base.BaseViewModel
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class FragmentDemoUiState(
    val sessionId: String = UUID.randomUUID().toString(),
    val count: Int = 0,
    val message: String = "SavedStateHandle survives configuration changes"
)

class FragmentDemoViewModel(
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Nothing?>() {

    companion object {
        const val KEY_SESSION_ID = "fragment_demo_session_id"
        const val KEY_COUNT = "fragment_demo_count"
    }

    override val repository: Nothing? = null

    private val initialState = FragmentDemoUiState(
        sessionId = savedStateHandle.get<String>(KEY_SESSION_ID)
            ?: UUID.randomUUID().toString().also { generatedId ->
                savedStateHandle[KEY_SESSION_ID] = generatedId
            },
        count = savedStateHandle[KEY_COUNT] ?: 0
    )

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<FragmentDemoUiState> = _uiState.asStateFlow()

    fun incrementCounter() {
        val currentState = _uiState.value
        val updatedState = currentState.copy(count = currentState.count + 1)
        savedStateHandle[KEY_COUNT] = updatedState.count
        _uiState.value = updatedState
    }

    fun resetCounter() {
        val currentState = _uiState.value
        val updatedState = currentState.copy(count = 0)
        savedStateHandle[KEY_COUNT] = 0
        _uiState.value = updatedState
    }
}