package com.hoyn.common.lib.ui.network

import com.hoyn.common.base.BaseViewModel
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.inject

/**
 * 网络请求示例 ViewModel
 *
 * 遵循 MVVM 架构，负责：
 * - 管理 UI 状态
 * - 调用 Repository 获取数据
 * - 处理业务逻辑
 *
 * 注意：KoinComponent 已从 BaseViewModel 继承，无需单独声明
 */
class NetworkDemoViewModel : BaseViewModel<PostRepository>() {

    override val repository: PostRepository by inject()

    // UI 状态流
    private val _uiState = MutableStateFlow<UIState<List<Post>>>(UIState.Loading)
    val uiState: StateFlow<UIState<List<Post>>> = _uiState.asStateFlow()

    // 是否来自缓存
    private val _isFromCache = MutableStateFlow(false)
    val isFromCache: StateFlow<Boolean> = _isFromCache.asStateFlow()

    /**
     * 加载帖子列表
     *
     * 使用网络优先策略：
     * 1. 优先从网络获取
     * 2. 网络失败时从本地缓存加载
     */
    fun loadPosts() {
        launchIO {
            _uiState.value = UIState.Loading
            _isFromCache.value = false

            repository.getPosts().fold(
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
        launchIO {
            repository.clearCache()
        }
    }
}
