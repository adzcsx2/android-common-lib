package com.hoyn.common.lib.ui.network

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityNetworkDemoBinding
import com.hoyn.common.ui.ext.click
import com.hoyn.common.ui.toast.ToastUtil
import com.hoyn.common.utils.Logger
import kotlinx.coroutines.launch

/**
 * 网络请求示例页面
 *
 * 展示完整架构链路：
 * - Activity -> ViewModel -> Repository -> Api/Database -> UIState
 * - 网络优先策略，离线缓存支持
 * - 语言设置由 BaseActivity 统一处理
 *
 * TAG 由 BaseActivity 自动提供
 * 启动方式：context.startActivity<NetworkDemoActivity>()
 */
class NetworkDemoActivity : BaseActivity<ActivityNetworkDemoBinding, NetworkDemoViewModel>() {

    private val adapter = PostAdapter()

    /**
     * 初始化视图
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
        observeData()
    }

    /**
     * 初始化数据
     */
    override fun initData() {
        Logger.d("initData: 开始加载数据")
        viewModel.loadPosts()
    }

    /**
     * 设置视图和点击事件
     */
    private fun setupViews() {
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter

        binding.btnRefresh.click {
            viewModel.loadPosts()
        }

        binding.btnBack.click {
            finish()
        }
    }

    /**
     * 观察数据变化
     */
    private fun observeData() {
        // 观察 UI 状态
        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { state -> renderState(state) }
        }

        // 观察缓存状态
        lifecycleScope.launch {
            viewModel.isFromCache
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { fromCache ->
                    if (fromCache) {
                        ToastUtil.show(getString(R.string.using_cached_data))
                    }
                }
        }
    }

    /**
     * 渲染 UI 状态
     *
     * @param state UI 状态
     */
    private fun renderState(state: UIState<List<com.hoyn.common.lib.data.model.Post>>) {
        when (state) {
            is UIState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvPosts.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.visibility = View.GONE
            }

            is UIState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.rvPosts.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.visibility = View.GONE
                adapter.submitList(state.data)
            }

            is UIState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.rvPosts.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = state.message
            }

            is UIState.Empty -> {
                binding.progressBar.visibility = View.GONE
                binding.rvPosts.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvError.visibility = View.GONE
            }
        }
    }
}
