package com.hoyn.common.base.ext

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.hoyn.common.base.BaseViewModel
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.base.event.Message
import com.hoyn.common.core.ThrowableBean

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
    viewModel.defUI.toastEvent.observeWithLifecycle(owner, onToast)
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
    viewModel.defUI.showDialog.observeWithLifecycle(owner, onShow)
    viewModel.defUI.dismissDialog.observeWithLifecycle(owner) {
        onDismiss()
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
    viewModel.defUI.errorEvent.observeWithLifecycle(owner, onError)
}

/**
 * 观察消息事件
 */
fun <R> Fragment.observeMessage(
    owner: LifecycleOwner,
    viewModel: BaseViewModel<R>,
    onMessage: (Message) -> Unit
) {
    viewModel.defUI.msgEvent.observeWithLifecycle(owner, onMessage)
}

/**
 * 观察全局消息事件
 */
fun Fragment.observeGlobalMessage(
    owner: LifecycleOwner,
    onMessage: (Message) -> Unit
) {
    GlobalLiveEvent.observeMessage(owner, onMessage)
}

/**
 * 观察全局错误事件
 */
fun Fragment.observeGlobalError(
    owner: LifecycleOwner,
    onError: (Throwable) -> Unit
) {
    GlobalLiveEvent.observeError(owner, onError)
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

// ============== Activity 扩展方法 ==============

/**
 * 观察 Toast 事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeToast(
    viewModel: BaseViewModel<R>,
    onToast: (String) -> Unit
) {
    viewModel.defUI.toastEvent.observeWithLifecycle(this, onToast)
}

/**
 * 观察加载框显示事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeShowDialog(
    viewModel: BaseViewModel<R>,
    onShow: (String?) -> Unit,
    onDismiss: () -> Unit = {}
) {
    viewModel.defUI.showDialog.observeWithLifecycle(this, onShow)
    viewModel.defUI.dismissDialog.observeWithLifecycle(this) {
        onDismiss()
    }
}

/**
 * 观察错误事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeError(
    viewModel: BaseViewModel<R>,
    onError: (ThrowableBean) -> Unit
) {
    viewModel.defUI.errorEvent.observeWithLifecycle(this, onError)
}

/**
 * 观察消息事件
 */
fun <R> androidx.appcompat.app.AppCompatActivity.observeMessage(
    viewModel: BaseViewModel<R>,
    onMessage: (Message) -> Unit
) {
    viewModel.defUI.msgEvent.observeWithLifecycle(this, onMessage)
}

/**
 * 观察全局消息事件
 */
fun androidx.appcompat.app.AppCompatActivity.observeGlobalMessage(
    onMessage: (Message) -> Unit
) {
    GlobalLiveEvent.observeMessage(this, onMessage)
}

/**
 * 观察全局错误事件
 */
fun androidx.appcompat.app.AppCompatActivity.observeGlobalError(
    onError: (Throwable) -> Unit
) {
    GlobalLiveEvent.observeError(this, onError)
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
