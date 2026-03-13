package com.hoyn.common.compose.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoyn.common.core.UIState
import kotlinx.coroutines.flow.StateFlow

/**
 * 观察 UIState 的扩展函数
 */

/**
 * 将 StateFlow<UIState<T>> 转换为 Compose State（带生命周期感知）
 */
@Composable
fun <T> StateFlow<UIState<T>>.collectAsUIState(): UIState<T> {
    return this.collectAsStateWithLifecycle(initialValue = UIState.Loading).value
}

/**
 * 检查 UIState 是否为加载中
 */
val <T> UIState<T>.isLoading: Boolean
    get() = this is UIState.Loading

/**
 * 检查 UIState 是否为成功
 */
val <T> UIState<T>.isSuccess: Boolean
    get() = this is UIState.Success

/**
 * 检查 UIState 是否为错误
 */
val <T> UIState<T>.isError: Boolean
    get() = this is UIState.Error

/**
 * 检查 UIState 是否为空
 */
val <T> UIState<T>.isEmpty: Boolean
    get() = this is UIState.Empty

/**
 * 获取成功状态的数据，如果不是成功状态则返回 null
 */
fun <T> UIState<T>.getDataOrNull(): T? {
    return (this as? UIState.Success)?.data
}

/**
 * 获取错误状态的消息，如果不是错误状态则返回 null
 */
fun <T> UIState<T>.getErrorOrNull(): String? {
    return (this as? UIState.Error)?.message
}
