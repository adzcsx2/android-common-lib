package com.hoyn.common.lib.ui.fragment_demo

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class FragmentDemoViewModelSavedStateTest {

    private fun SavedStateHandle.snapshot(): Map<String, Any?> {
        return keys().associateWith { key -> get<Any?>(key) }
    }

    @Test
    fun restoresCounterAndSessionFromSavedStateHandle() {
        val firstHandle = SavedStateHandle()
        val firstViewModel = FragmentDemoViewModel(firstHandle)

        firstViewModel.incrementCounter()
        firstViewModel.incrementCounter()

        val persistedState = firstHandle.snapshot()
        val recreatedViewModel = FragmentDemoViewModel(SavedStateHandle(persistedState))

        assertEquals(firstViewModel.uiState.value.sessionId, recreatedViewModel.uiState.value.sessionId)
        assertEquals(2, recreatedViewModel.uiState.value.count)
    }

    @Test
    fun resetCounterUpdatesSavedState() {
        val handle = SavedStateHandle()
        val viewModel = FragmentDemoViewModel(handle)

        viewModel.incrementCounter()
        viewModel.resetCounter()

        val recreatedViewModel = FragmentDemoViewModel(SavedStateHandle(handle.snapshot()))
        assertEquals(0, recreatedViewModel.uiState.value.count)
    }
}