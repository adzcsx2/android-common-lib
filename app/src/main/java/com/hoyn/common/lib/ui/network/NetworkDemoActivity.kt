package com.hoyn.common.lib.ui.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.MultiAdapterItem
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.R
import com.hoyn.common.lib.data.model.Comment
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.databinding.ActivityNetworkDemoBinding
import com.hoyn.common.ui.ext.click
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

    /** 帖子列表适配器 */
    private val postAdapter = PostAdapter()
    /** 评论多类型列表适配器 */
    private val multiPostAdapter = MultiPostAdapter()

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
        viewModel.loadComments()
    }

    /**
     * 设置视图和点击事件
     */
    private fun setupViews() {
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postAdapter
        postAdapter.isStateViewEnable = true
        postAdapter.setStateViewLayout(this, R.layout.layout_state_loading)

        binding.rvMultiPosts.layoutManager = LinearLayoutManager(this)
        binding.rvMultiPosts.adapter = multiPostAdapter
        multiPostAdapter.isStateViewEnable = true
        multiPostAdapter.setStateViewLayout(this, R.layout.layout_state_loading)

        binding.btnRefresh.click {
            viewModel.loadPosts()
            viewModel.loadComments()
        }

        binding.btnBack.click {
            finish()
        }
    }

    /**
     * 观察数据变化
     */
    private fun observeData() {
        // 观察帖子状态
        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { state -> renderPostsState(state) }
        }

        // 观察评论状态
        lifecycleScope.launch {
            viewModel.commentsState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { state -> renderCommentsState(state) }
        }

        // 观察缓存状态
        lifecycleScope.launch {
            viewModel.isFromCache
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { fromCache ->
                    if (fromCache) {
                        toast.show(R.string.using_cached_data)
                    }
                }
        }
    }

    /**
     * 渲染帖子列表的 UI 状态
     *
     * 根据状态切换加载中、空数据、错误和成功四种界面
     *
     * @param state 帖子列表的 UI 状态
     */
    private fun renderPostsState(state: UIState<List<Post>>) {
        when (state) {
            is UIState.Success -> {
                postAdapter.setStateViewLayout(this, R.layout.layout_state_empty)
                postAdapter.submitList(state.data)
            }

            is UIState.Empty -> {
                postAdapter.setStateViewLayout(this, R.layout.layout_state_empty)
                postAdapter.submitList(emptyList())
            }

            is UIState.Error -> {
                postAdapter.stateView = createErrorStateView(binding.rvPosts, state.message)
                postAdapter.submitList(emptyList())
            }

            is UIState.Loading -> {
                postAdapter.setStateViewLayout(this, R.layout.layout_state_loading)
                postAdapter.submitList(emptyList())
            }
        }
    }

    /**
     * 渲染评论列表的 UI 状态
     *
     * 根据状态切换加载中、空数据、错误和成功四种界面
     *
     * @param state 评论列表的 UI 状态
     */
    private fun renderCommentsState(state: UIState<List<Comment>>) {
        when (state) {
            is UIState.Success -> {
                multiPostAdapter.setStateViewLayout(this, R.layout.layout_state_empty)
                multiPostAdapter.submitList(buildMultiItems(state.data))
            }

            is UIState.Empty -> {
                multiPostAdapter.setStateViewLayout(this, R.layout.layout_state_empty)
                multiPostAdapter.submitList(emptyList())
            }

            is UIState.Error -> {
                multiPostAdapter.stateView =
                    createErrorStateView(binding.rvMultiPosts, state.message)
                multiPostAdapter.submitList(emptyList())
            }

            is UIState.Loading -> {
                multiPostAdapter.setStateViewLayout(this, R.layout.layout_state_loading)
                multiPostAdapter.submitList(emptyList())
            }
        }
    }

    /**
     * 创建错误状态视图
     *
     * 加载错误布局并设置错误消息文本
     *
     * @param parent 父布局
     * @param message 错误消息文本
     * @return 填充后的错误状态视图
     */
    private fun createErrorStateView(parent: ViewGroup, message: String) =
        LayoutInflater.from(this).inflate(R.layout.layout_state_error, parent, false).apply {
            findViewById<TextView>(R.id.tvStateMessage)?.text = message
        }

    /**
     * 构建多类型评论列表数据
     *
     * 每 3 条评论中第 1 条使用精选样式（VIEW_TYPE_FEATURED），其余使用紧凑样式（VIEW_TYPE_COMPACT）
     *
     * @param comments 评论列表
     * @return 多类型适配器项列表
     */
    private fun buildMultiItems(comments: List<Comment>): List<MultiAdapterItem<Comment>> {
        return comments.mapIndexed { index, comment ->
            val viewType = if (index % 3 == 0) {
                MultiPostAdapter.VIEW_TYPE_FEATURED
            } else {
                MultiPostAdapter.VIEW_TYPE_COMPACT
            }
            MultiAdapterItem(data = listOf(comment), viewType = viewType)
        }
    }
}
