package com.hoyn.common.lib.ui.fragment_demo

import com.hoyn.common.base.BaseViewModel
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Fragment Demo 的 UI 状态数据类
 *
 * @property sessionId 会话唯一标识，首次创建时自动生成
 * @property count 计数器当前值
 * @property message 状态提示消息
 */
data class FragmentDemoUiState(
    val sessionId: String = UUID.randomUUID().toString(),
    val count: Int = 0,
    val message: String = "SavedStateHandle survives configuration changes"
)

/**
 * Fragment Demo 的 ViewModel
 *
 * 演示 SavedStateHandle 在配置变更（如旋转屏幕）时保持状态的能力
 *
 * @param savedStateHandle 用于在配置变更时保存和恢复状态
 */
class FragmentDemoViewModel(
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Nothing?>() {

    companion object {
        /** SavedStateHandle 中保存会话 ID 的键名 */
        const val KEY_SESSION_ID = "fragment_demo_session_id"
        /** SavedStateHandle 中保存计数值的键名 */
        const val KEY_COUNT = "fragment_demo_count"
    }

    override val repository: Nothing? = null

    /** 初始状态，优先从 SavedStateHandle 恢复 */
    private val initialState = FragmentDemoUiState(
        sessionId = savedStateHandle.get<String>(KEY_SESSION_ID)
            ?: UUID.randomUUID().toString().also { generatedId ->
                savedStateHandle[KEY_SESSION_ID] = generatedId
            },
        count = savedStateHandle[KEY_COUNT] ?: 0
    )

    private val _uiState = MutableStateFlow(initialState)
    /** UI 状态流（公开只读） */
    val uiState: StateFlow<FragmentDemoUiState> = _uiState.asStateFlow()

    /**
     * 递增计数器
     *
     * 计数器值加 1，并同步保存到 SavedStateHandle
     */
    fun incrementCounter() {
        val currentState = _uiState.value
        val updatedState = currentState.copy(count = currentState.count + 1)
        savedStateHandle[KEY_COUNT] = updatedState.count
        _uiState.value = updatedState
    }

    /**
     * 重置计数器
     *
     * 计数器归零，并同步保存到 SavedStateHandle
     */
    fun resetCounter() {
        val currentState = _uiState.value
        val updatedState = currentState.copy(count = 0)
        savedStateHandle[KEY_COUNT] = 0
        _uiState.value = updatedState
    }
}