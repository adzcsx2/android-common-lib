package com.hoyn.common.lib.ui.fragment_demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hoyn.common.base.BaseFragment
import com.hoyn.common.lib.databinding.FragmentFragmentDemoBinding
import com.hoyn.common.ui.ext.click
import kotlinx.coroutines.launch

class FragmentDemoFragment : BaseFragment<FragmentFragmentDemoBinding, FragmentDemoViewModel>() {

    companion object {
        fun newInstance(
            initialSessionId: String? = null,
            initialCount: Int = 0
        ): FragmentDemoFragment {
            return FragmentDemoFragment().apply {
                arguments = Bundle().apply {
                    putInt(FragmentDemoViewModel.KEY_COUNT, initialCount)
                    if (initialSessionId != null) {
                        putString(FragmentDemoViewModel.KEY_SESSION_ID, initialSessionId)
                    }
                }
            }
        }
    }

    private var latestUiState: FragmentDemoUiState? = null

    override fun initView(view: View, savedInstanceState: Bundle?) {
        val initialState = viewModel.uiState.value
        latestUiState = initialState
        render(initialState)

        binding.btnIncrement.click {
            viewModel.incrementCounter()
        }

        binding.btnReset.click {
            viewModel.resetCounter()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    latestUiState = state
                    render(state)
                }
            }
        }
    }

    private fun render(state: FragmentDemoUiState) {
        binding.tvSessionValue.text = state.sessionId
        binding.tvCounterValue.text = state.count.toString()
        binding.tvMessage.text = state.message
    }

    fun incrementForTest(times: Int) {
        repeat(times) {
            viewModel.incrementCounter()
        }
    }

    fun viewModelStateForTest(): FragmentDemoUiState = viewModel.uiState.value

    fun snapshotForTest(): FragmentDemoUiState? = latestUiState
}