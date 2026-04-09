package com.hoyn.common.lib.ui.compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Compose Demo ViewModel
 *
 * 遵循 MVVM 架构，负责：
 * - 管理 UI 状态
 * - 调用 Repository 获取数据
 * - 处理业务逻辑
 *
 * 注意：由于继承 AndroidViewModel 而非 BaseViewModel，需要单独声明 KoinComponent
 *
 * @param application Application 实例
 * @param ioDispatcher IO 调度器
 * @param autoLoadOnInit 是否在初始化时自动加载
 */
class ComposeDemoViewModel(
    application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val autoLoadOnInit: Boolean = true
) : AndroidViewModel(application), KoinComponent {

    /** 帖子数据仓库 */
    private val repository: PostRepository by inject()

    // UI 状态流
    private val _uiState = MutableStateFlow<UIState<List<Post>>>(UIState.Loading)
    /** 帖子列表的 UI 状态流（公开只读） */
    val uiState: StateFlow<UIState<List<Post>>> = _uiState.asStateFlow()

    // 是否来自缓存
    private val _isFromCache = MutableStateFlow(false)
    /** 是否来自本地缓存的状态流（公开只读） */
    val isFromCache: StateFlow<Boolean> = _isFromCache.asStateFlow()

    init {
        if (autoLoadOnInit) {
            loadPosts()
        }
    }

    /**
     * 加载帖子列表
     *
     * 使用网络优先策略
     * - 优先从网络获取数据
     * - 网络失败时从本地缓存加载
     * - 更新 UI 状态和缓存标识
     */
    fun loadPosts() {
        _uiState.value = UIState.Loading
        _isFromCache.value = false

        viewModelScope.launch(ioDispatcher) {
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
     *
     * 用于演示从 Compose 调用 Toast
     */
    fun showTestToast() {
        ToastUtils.show("来自 Compose 的 Toast!")
    }
}
