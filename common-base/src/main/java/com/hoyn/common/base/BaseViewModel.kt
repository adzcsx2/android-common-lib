package com.hoyn.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.base.event.Message
import com.hoyn.common.base.event.SingleLiveEvent
import com.hoyn.common.core.IBaseResponse
import com.hoyn.common.core.ThrowableBean
import com.hoyn.common.core.UIState
import com.hoyn.common.utils.Logger
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
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
abstract class BaseViewModel<R> : ViewModel(), KoinComponent {

    protected class ResponseCodeException(val errorCode: Int, errorMessage: String) :
        RuntimeException(errorMessage)

    protected class EmptyResponseDataException(errorMessage: String) : RuntimeException(errorMessage)

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
        return when (throwable) {
            is ResponseCodeException -> ThrowableBean(
                code = throwable.errorCode,
                errMsg = throwable.message ?: "Unknown error"
            )

            else -> ThrowableBean(errMsg = throwable.message ?: "Unknown error")
        }
    }

    /**
     * 处理通用业务错误码。
     *
     * @return true 表示错误码已被处理（将不会再 toast），false 表示继续默认错误处理。
     */
    protected open fun onApiErrorCode(code: Int, message: String): Boolean = false

    /**
     * 将异常转换为 UIState.Error。
     *
     * 优先使用 BaseResponse 解析出的错误码/消息；如果没有，则回退 throwable 本身信息。
     * CancellationException 返回 null，由调用方直接忽略。
     */
    protected fun Throwable.toUiErrorStateOrNull(): UIState.Error? {
        if (this is CancellationException) {
            return null
        }

        val codeFromThrowable = when (this) {
            is ResponseCodeException -> this.errorCode
            else -> extractCodeByReflection(this)
        }

        return UIState.Error(
            code = codeFromThrowable ?: -1,
            message = this.message ?: "Unknown error"
        )
    }

    private fun extractCodeByReflection(throwable: Throwable): Int? {
        return runCatching {
            val field = throwable.javaClass.declaredFields.firstOrNull { it.name == "code" }
            if (field != null) {
                field.isAccessible = true
                when (val codeValue = field.get(throwable)) {
                    is Int -> codeValue
                    is Number -> codeValue.toInt()
                    else -> null
                }
            } else {
                null
            }
        }.getOrNull()
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
     * 执行网络请求并处理结果。
     *
     * error 回调直接给出服务端 code 和 message，无需在页面处理 Throwable 解析。
     * CancellationException 会被静默忽略，不会触发 error 回调。
     */
    fun <T> launchOnlyResult(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: (T) -> Unit,
        error: (code: Int, message: String) -> Unit = { _, _ -> },
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
                error = { throwable ->
                    handleError(
                        throwable,
                        { t -> t.toUiErrorStateOrNull()?.let { error(it.code, it.message) } },
                        toastError
                    )
                }
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
            if (data == null || (data is String && data.isEmpty())) {
                Logger.e("Response data is empty, code=${response.code()}, message=${response.msg()}")
                throw EmptyResponseDataException("Response data is null or empty")
            }
            success(data)
        } else {
            throw ResponseCodeException(response.code(), response.msg())
        }
    }

    /**
     * 同步执行响应结果处理（非协程）
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> IBaseResponse<T>.executeResponseSync(): T {
        return if (isSuccess()) {
            val data = data()
            if (data == null || (data is String && data.isEmpty())) {
                Logger.e("Response data is empty, code=${code()}, message=${msg()}")
                try {
                    Any() as T
                } catch (e: Exception) {
                    throw RuntimeException("DATA_ERROR: Response data is null or empty")
                }
            } else {
                data
            }
        } else {
            throw ResponseCodeException(code(), msg())
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

        val isEmptyDataError = throwable is EmptyResponseDataException
        val isResponseCodeError = throwable is ResponseCodeException
        val isHandledByCode = if (isResponseCodeError) {
            onApiErrorCode(errorBean.code, errorBean.errMsg)
        } else {
            false
        }

        // 发送到全局事件总线
        GlobalLiveEvent.sendMessage(Message(code = errorBean.code, msg = errorBean.errMsg))

        if (toastError && !isHandledByCode && !isEmptyDataError) {
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
