package com.hoyn.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.base.event.Message
import com.hoyn.common.base.event.SingleLiveEvent
import com.hoyn.common.core.IBaseResponse
import com.hoyn.common.core.ThrowableBean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * BaseViewModel
 *
 * 提供 ViewModel 的通用能力：
 * - 协程作用域管理
 * - 网络请求封装
 * - 异常处理（由子类实现）
 * - UI 事件分发
 *
 * 注意：不使用反射注入 Repository，子类需要手动创建 Repository 实例
 *
 * @param R Repository 类型参数，子类可以指定具体的 Repository 类型
 */
abstract class BaseViewModel<R> : ViewModel() {

    /**
     * UI 变化事件
     */
    val defUI: UIChange by lazy { UIChange() }

    /**
     * Repository 实例
     * 子类需要重写此属性以提供具体的 Repository 实现
     * 建议使用 lazy 延迟初始化
     */
    protected abstract val repository: R

    /**
     * 异常处理器
     * 子类可以重写此方法以提供自定义的异常处理逻辑
     * 默认实现返回简单的错误信息
     */
    protected open fun handleException(throwable: Throwable): ThrowableBean {
        return ThrowableBean(errMsg = throwable.message ?: "Unknown error")
    }

    /**
     * 在 viewModelScope 中启动协程
     */
    fun launchUI(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch {
        block()
    }

    /**
     * 在 IO 线程中启动协程
     */
    fun launchIO(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        block()
    }

    /**
     * 创建 Flow
     */
    fun <T> launchFlow(block: suspend () -> T): Flow<T> = flow {
        emit(block())
    }.flowOn(Dispatchers.IO)

    /**
     * 同步执行多个网络请求
     *
     * @param block 请求体
     * @param error 失败回调
     * @param showDialog 是否显示加载框
     * @param toastError 是否显示错误 Toast
     */
    fun launchSync(
        block: suspend CoroutineScope.() -> Unit,
        error: (Throwable) -> Unit = {},
        showDialog: Boolean = true,
        toastError: Boolean = true
    ) {
        if (showDialog) defUI.showDialog.emit()
        launchUI {
            handleException(
                block = { withContext(Dispatchers.IO) { block() } },
                error = { handleError(it, error, toastError) }
            )
        }
    }

    /**
     * 执行网络请求并处理结果
     *
     * @param block 请求体
     * @param success 成功回调
     * @param error 失败回调
     * @param showDialog 是否显示加载框
     * @param toastError 是否显示错误 Toast
     */
    fun <T> launchOnlyResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: (T) -> Unit,
        error: (Throwable) -> Unit = {},
        showDialog: Boolean = true,
        toastError: Boolean = true
    ) {
        if (showDialog) defUI.showDialog.emit()
        launchUI {
            handleException(
                block = { withContext(Dispatchers.IO) { block() } },
                success = { response ->
                    executeResponse(response, success)
                },
                error = { handleError(it, error, toastError) }
            )
        }
    }

    /**
     * 执行网络请求，不自动处理 Toast
     */
    fun <T> launchOnlyResultMessage(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: (T) -> Unit,
        error: (Throwable) -> Unit = {},
        showDialog: Boolean = true
    ) {
        if (showDialog) defUI.showDialog.emit()
        launchUI {
            handleException(
                block = { withContext(Dispatchers.IO) { block() } },
                success = { response ->
                    executeResponse(response, success)
                },
                error = { error.invoke(it) }
            )
        }
    }

    /**
     * 执行响应结果处理
     */
    private suspend fun <T> executeResponse(
        response: IBaseResponse<T>,
        success: suspend (T) -> Unit
    ) {
        if (response.isSuccess()) {
            val data = response.data()
            if (data == null) {
                @Suppress("UNCHECKED_CAST")
                try {
                    success(Any() as T)
                } catch (e: Exception) {
                    throw RuntimeException("DATA_ERROR: Response data is null")
                }
            } else {
                success(data)
            }
        } else {
            throw RuntimeException("${response.code()}: ${response.msg()}")
        }
    }

    /**
     * 同步执行响应结果处理（非协程）
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> IBaseResponse<T>.executeResponseSync(): T {
        return if (isSuccess()) {
            val data = data()
            if (data == null) {
                try {
                    Any() as T
                } catch (e: Exception) {
                    throw RuntimeException("DATA_ERROR: Response data is null")
                }
            } else {
                data
            }
        } else {
            throw RuntimeException("${code()}: ${msg()}")
        }
    }

    /**
     * 处理异常
     */
    private suspend fun CoroutineScope.handleException(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend (Throwable) -> Unit
    ) {
        try {
            block()
        } catch (e: CancellationException) {
            // 协程取消，不做处理
        } catch (e: Throwable) {
            error(e)
        }
    }

    /**
     * 处理异常（带成功回调）
     */
    private suspend fun <T> CoroutineScope.handleException(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: suspend (IBaseResponse<T>) -> Unit,
        error: suspend (Throwable) -> Unit
    ) {
        try {
            success(block())
        } catch (e: CancellationException) {
            // 协程取消，不做处理
        } catch (e: Throwable) {
            error(e)
        }
    }

    /**
     * 处理错误
     */
    private suspend fun handleError(
        throwable: Throwable,
        errorCallback: (Throwable) -> Unit,
        toastError: Boolean
    ) {
        delay(100) // 延迟以确保 UI 可以响应
        errorCallback(throwable)

        // 处理异常
        val errorBean = handleException(throwable)

        // 发送到全局事件总线
        GlobalLiveEvent.sendMessage(Message(code = errorBean.code, msg = errorBean.errMsg))

        if (toastError) {
            defUI.toastEvent.emit(errorBean.errMsg)
        }
        defUI.errorEvent.emit(errorBean)
    }

    /**
     * 显示 Toast
     */
    fun showToast(message: String) {
        defUI.toastEvent.emit(message)
    }

    /**
     * 显示加载框
     */
    fun showDialog(message: String? = null) {
        if (message != null) {
            defUI.showDialog.emit(message)
        } else {
            defUI.showDialog.emit()
        }
    }

    /**
     * 隐藏加载框
     */
    fun dismissDialog() {
        defUI.dismissDialog.emit()
    }

    /**
     * 发送消息事件
     */
    fun sendMessage(message: Message) {
        defUI.msgEvent.emit(message)
        GlobalLiveEvent.sendMessage(message)
    }

    /**
     * UI 事件类
     */
    inner class UIChange {
        val showDialog by lazy { SingleLiveEvent<String?>(emptyValueProvider = { null }) }
        val dismissDialog by lazy { SingleLiveEvent<Unit>(emptyValueProvider = { Unit }) }
        val errorEvent by lazy { SingleLiveEvent<ThrowableBean>() }
        val toastEvent by lazy { SingleLiveEvent<String>() }
        val msgEvent by lazy { SingleLiveEvent<Message>() }
    }
}
