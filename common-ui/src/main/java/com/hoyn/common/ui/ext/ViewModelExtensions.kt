package com.hoyn.common.ui.ext

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hoyn.common.core.Message
import com.hoyn.common.core.ThrowableBean
import com.hoyn.common.ui.base.BaseViewModel
import com.hoyn.common.ui.event.GlobalEventBus
import kotlinx.coroutines.launch

/**
 * ViewModel 扩展方法
 * 提供观察 BaseViewModel 中 defUI 事件的便捷方法
 */

/**
 * 观察 Toast 事件
 */
fun <R> Fragment.observeToast(
    owner: LifecycleOwner,
    viewModel: BaseViewModel<R>,
    onToast: (String) -> Unit
) {
    owner.lifecycleScope.launch {
        viewModel.defUI.toastEvent.asSharedFlow()
            .flowWithLifecycle(owner.lifecycle)
            .collect { message ->
                onToast(message)
            }
    }
}

/**
 * 观察加载框显示事件
 */
fun <R> Fragment.observeShowDialog(
    owner: LifecycleOwner,
    viewModel: BaseViewModel<R>,
    onShow: (String?) -> Unit,
    onDismiss: () -> Unit = {}
) {
    owner.lifecycleScope.launch {
        viewModel.defUI.showDialog.asSharedFlow()
            .flowWithLifecycle(owner.lifecycle)
            .collect { message ->
                onShow(message)
            }
    }
    owner.lifecycleScope.launch {
        viewModel.defUI.dismissDialog.asSharedFlow()
            .flowWithLifecycle(owner.lifecycle)
            .collect {
                onDismiss()
            }
    }
}

/**
 * 观察错误事件
 */
fun <R> Fragment.observeError(
    owner: LifecycleOwner,
    viewModel: BaseViewModel<R>,
    onError: (ThrowableBean) -> Unit
) {
    owner.lifecycleScope.launch {
        viewModel.defUI.errorEvent.asSharedFlow()
            .flowWithLifecycle(owner.lifecycle)
            .collect { error ->
                onError(error)
            }
    }
}

/**
 * 观察消息事件
 */
fun <R> Fragment.observeMessage(
    owner: LifecycleOwner,
    viewModel: BaseViewModel<R>,
    onMessage: (Message) -> Unit
) {
    owner.lifecycleScope.launch {
        viewModel.defUI.msgEvent.asSharedFlow()
            .flowWithLifecycle(owner.lifecycle)
            .collect { message ->
                onMessage(message)
            }
    }
}

/**
 * 观察全局消息事件
 */
fun Fragment.observeGlobalMessage(
    owner: LifecycleOwner,
    onMessage: (Message) -> Unit
) {
    owner.lifecycleScope.launch {
        GlobalEventBus.observeMessage()
            .flowWithLifecycle(owner.lifecycle)
            .collect { message ->
                onMessage(message)
            }
    }
}

/**
 * 观察全局错误事件
 */
fun Fragment.observeGlobalError(
    owner: LifecycleOwner,
    onError: (Throwable) -> Unit
) {
    owner.lifecycleScope.launch {
        GlobalEventBus.observeError()
            .flowWithLifecycle(owner.lifecycle)
            .collect { error ->
                onError(error)
            }
    }
}

/**
 * 观察 BaseViewModel 的所有 UI 事件
 */
fun <R> Fragment.observeAllUIEvents(
    owner: LifecycleOwner,
    viewModel: BaseViewModel<R>,
    onToast: (String) -> Unit = {},
    onShowDialog: (String?) -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onError: (ThrowableBean) -> Unit = {},
    onMessage: (Message) -> Unit = {}
) {
    observeToast(owner, viewModel, onToast)
    observeShowDialog(owner, viewModel, onShowDialog, onDismissDialog)
    observeError(owner, viewModel, onError)
    observeMessage(owner, viewModel, onMessage)
}

/**
 * Activity 扩展方法
 */

/**
 * 观察 Toast 事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeToast(
    viewModel: BaseViewModel<R>,
    onToast: (String) -> Unit
) {
    lifecycleScope.launch {
        viewModel.defUI.toastEvent.asSharedFlow()
            .flowWithLifecycle(lifecycle)
            .collect { message ->
                onToast(message)
            }
    }
}

/**
 * 观察加载框显示事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeShowDialog(
    viewModel: BaseViewModel<R>,
    onShow: (String?) -> Unit,
    onDismiss: () -> Unit = {}
) {
    lifecycleScope.launch {
        viewModel.defUI.showDialog.asSharedFlow()
            .flowWithLifecycle(lifecycle)
            .collect { message ->
                onShow(message)
            }
    }
    lifecycleScope.launch {
        viewModel.defUI.dismissDialog.asSharedFlow()
            .flowWithLifecycle(lifecycle)
            .collect {
                onDismiss()
            }
    }
}

/**
 * 观察错误事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeError(
    viewModel: BaseViewModel<R>,
    onError: (ThrowableBean) -> Unit
) {
    lifecycleScope.launch {
        viewModel.defUI.errorEvent.asSharedFlow()
            .flowWithLifecycle(lifecycle)
            .collect { error ->
                onError(error)
            }
    }
}

/**
 * 观察消息事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeMessage(
    viewModel: BaseViewModel<R>,
    onMessage: (Message) -> Unit
) {
    lifecycleScope.launch {
        viewModel.defUI.msgEvent.asSharedFlow()
            .flowWithLifecycle(lifecycle)
            .collect { message ->
                onMessage(message)
            }
    }
}

/**
 * 观察全局消息事件
 */
fun androidx.appcompat.app.AppCompatActivity.observeGlobalMessage(
    onMessage: (Message) -> Unit
) {
    lifecycleScope.launch {
        GlobalEventBus.observeMessage()
            .flowWithLifecycle(lifecycle)
            .collect { message ->
                onMessage(message)
            }
    }
}

/**
 * 观察全局错误事件
 */
fun androidx.appcompat.app.AppCompatActivity.observeGlobalError(
    onError: (Throwable) -> Unit
) {
    lifecycleScope.launch {
        GlobalEventBus.observeError()
            .flowWithLifecycle(lifecycle)
            .collect { error ->
                onError(error)
            }
    }
}

/**
 * 观察 BaseViewModel 的所有 UI 事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeAllUIEvents(
    viewModel: BaseViewModel<R>,
    onToast: (String) -> Unit = {},
    onShowDialog: (String?) -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onError: (ThrowableBean) -> Unit = {},
    onMessage: (Message) -> Unit = {}
) {
    observeToast(viewModel, onToast)
    observeShowDialog(viewModel, onShowDialog, onDismissDialog)
    observeError(viewModel, onError)
    observeMessage(viewModel, onMessage)
}
