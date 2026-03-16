package com.hoyn.common.lib.ui.compose

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.repository.PostRepository
import com.hoyn.common.ui.toast.ToastUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Compose Demo ViewModel
 *
 * 遵循 MVVM 架构，负责：
 * - 管理 UI 状态
 * - 调用 Repository 获取数据
 * - 处理业务逻辑
 */
class ComposeDemoViewModel(
    application: Application,
    private val repository: PostRepository = PostRepository.getInstance(application),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    // UI 状态流
    private val _uiState = MutableStateFlow<UIState<List<Post>>>(UIState.Loading)
    val uiState: StateFlow<UIState<List<Post>>> = _uiState.asStateFlow()

    // 是否来自缓存
    private val _isFromCache = MutableStateFlow(false)
    val isFromCache: StateFlow<Boolean> = _isFromCache.asStateFlow()

    init {
        // 初始化时不自动加载数据， 由外部调用 loadPosts() 触发
    }

    /**
     * 加载帖子列表
     *
     * 使用网络优先策略
     */
    fun loadPosts() {
        _uiState.value = UIState.Loading
        _isFromCache.value = false

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getPosts()

            result.fold(
                onSuccess = { resultData ->
                    _isFromCache.value = resultData.isFromCache
                    _uiState.value = if (resultData.posts.isEmpty()) UIState.Empty else UIState.Success(resultData.posts)
                },
                onFailure = { error ->
                    _uiState.value = UIState.Error(-1, error.message ?: "Unknown error")
                }
            )
        }
    }

    /**
     * 显示测试 Toast
     */
    fun showTestToast(context: Context) {
        ToastUtils.show(context, "来自 Compose 的 Toast!")
    }
}
