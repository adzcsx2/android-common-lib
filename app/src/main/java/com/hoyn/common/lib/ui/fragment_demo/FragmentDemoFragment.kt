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

/**
 * Fragment Demo 页面
 *
 * 演示 Fragment 与 ViewModel 配合使用，以及 SavedStateHandle 的状态保持能力
 */
class FragmentDemoFragment : BaseFragment<FragmentFragmentDemoBinding, FragmentDemoViewModel>() {

    companion object {
        /**
         * 创建 FragmentDemoFragment 实例
         *
         * @param initialSessionId 初始会话 ID（可选），为 null 时由 ViewModel 自动生成
         * @param initialCount 初始计数值，默认为 0
         * @return FragmentDemoFragment 实例
         */
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

    /** 记录最新的 UI 状态快照，用于测试验证 */
    private var latestUiState: FragmentDemoUiState? = null

    /**
     * 初始化视图
     *
     * 绑定按钮事件，订阅 ViewModel 的 UI 状态流
     *
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
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

    /**
     * 渲染 UI 状态到视图
     *
     * @param state Fragment Demo 的 UI 状态
     */
    private fun render(state: FragmentDemoUiState) {
        binding.tvSessionValue.text = state.sessionId
        binding.tvCounterValue.text = state.count.toString()
        binding.tvMessage.text = state.message
    }

    /**
     * 测试辅助方法：连续递增计数器指定次数
     *
     * @param times 递增次数
     */
    fun incrementForTest(times: Int) {
        repeat(times) {
            viewModel.incrementCounter()
        }
    }

    /**
     * 测试辅助方法：获取 ViewModel 当前状态
     *
     * @return ViewModel 的 UI 状态
     */
    fun viewModelStateForTest(): FragmentDemoUiState = viewModel.uiState.value

    /**
     * 测试辅助方法：获取 Fragment 层记录的最新状态快照
     *
     * @return 最新的 UI 状态，可能为 null（尚未收集过状态时）
     */
    fun snapshotForTest(): FragmentDemoUiState? = latestUiState
}