package com.hoyn.common.lib.ui.network

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.hoyn.common.base.BaseViewModel
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.repository.PostRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 网络请求示例 ViewModel
 *
 * 遵循 MVVM 架构，负责：
 * - 管理 UI 状态
 * - 调用 Repository 获取数据
 * - 处理业务逻辑
 */
class NetworkDemoViewModel(
    application: Application,
    repository: PostRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel<PostRepository>() {

    constructor(application: Application) : this(
        application = application,
        repository = null,
        ioDispatcher = Dispatchers.IO
    )

    override val repository: PostRepository = repository ?: PostRepository.getInstance(application)

    // UI 状态流
    private val _uiState = MutableStateFlow<UIState<List<Post>>>(UIState.Loading)
    val uiState: StateFlow<UIState<List<Post>>> = _uiState.asStateFlow()

    // 是否来自缓存
    private val _isFromCache = MutableStateFlow(false)
    val isFromCache: StateFlow<Boolean> = _isFromCache.asStateFlow()

    init {
        // 刃不在构造函数中自动加载，由外部调用 loadPosts() 触发
    }

    /**
     * 加载帖子列表
     *
     * 使用网络优先策略：
     * 1. 优先从网络获取
     * 2. 网络失败时从本地缓存加载
     */
    fun loadPosts() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = UIState.Loading
            _isFromCache.value = false

            val result = repository.getPosts()

            result.fold(
                onSuccess = { resultData ->
                    _isFromCache.value = resultData.isFromCache

                    if (resultData.posts.isEmpty()) {
                        _uiState.value = UIState.Empty
                    } else {
                        _uiState.value = UIState.Success(resultData.posts)
                    }
                },
                onFailure = { error ->
                    _uiState.value = UIState.Error(-1, error.message ?: "Unknown error")
                }
            )
        }
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
        }
    }
}
