package com.hoyn.common.base

import android.app.Dialog
import com.hoyn.common.utils.Logger
import java.lang.ref.WeakReference

/**
 * 管理宿主页面持有的普通 Dialog，统一做安全显示与生命周期清理。
 */
internal class DialogController(
    private val canShowDialog: () -> Boolean
) {

    /** 已管理的 Dialog 列表，使用 WeakReference 防止内存泄露 */
    private val dialogs = mutableListOf<WeakReference<Dialog>>()

    /**
     * 将 Dialog 纳入管理，自动清理已释放的引用。
     *
     * @param T Dialog 的具体类型
     * @param dialog 要管理的 Dialog 实例
     * @return 传入的 Dialog 实例，便于链式调用
     */
    fun <T : Dialog> manage(dialog: T): T {
        cleanupReleasedDialogs()
        if (dialogs.none { it.get() === dialog }) {
            dialogs.add(WeakReference(dialog))
        }
        return dialog
    }

    /**
     * 安全显示 Dialog，先检查宿主状态再执行显示。
     *
     * @param dialog 要显示的 Dialog 实例
     * @return true 表示显示成功或已经处于显示状态，false 表示宿主未就绪或显示失败
     */
    fun show(dialog: Dialog): Boolean {
        manage(dialog)
        if (!canShowDialog()) {
            Logger.w("Skip showing dialog because host is not ready: ${dialog.javaClass.simpleName}")
            return false
        }
        if (dialog.isShowing) {
            return true
        }
        return runCatching {
            dialog.show()
            true
        }.getOrElse {
            Logger.e("Failed to show dialog: ${dialog.javaClass.simpleName}", it)
            false
        }
    }

    /**
     * 安全关闭指定 Dialog，并从管理列表中移除。
     *
     * @param dialog 要关闭的 Dialog，为 null 时直接返回
     */
    fun dismiss(dialog: Dialog?) {
        val target = dialog ?: return
        runCatching {
            if (target.isShowing) {
                target.dismiss()
            }
        }.onFailure {
            Logger.e("Failed to dismiss dialog: ${target.javaClass.simpleName}", it)
        }
        remove(target)
    }

    /**
     * 关闭所有已管理的 Dialog，逆序关闭以保证后弹出的先关闭。
     */
    fun dismissAll() {
        val snapshot = dialogs.mapNotNull { it.get() }
        snapshot.asReversed().forEach { dialog ->
            runCatching {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }.onFailure {
                Logger.e("Failed to dismiss managed dialog: ${dialog.javaClass.simpleName}", it)
            }
        }
        dialogs.clear()
    }

    /**
     * 从管理列表中移除指定 Dialog，同时清理已被 GC 回收的引用。
     *
     * @param dialog 要移除的 Dialog 实例
     */
    private fun remove(dialog: Dialog) {
        val iterator = dialogs.iterator()
        while (iterator.hasNext()) {
            val current = iterator.next().get()
            if (current == null || current === dialog) {
                iterator.remove()
            }
        }
    }

    /**
     * 清理已被 GC 回收的 Dialog 引用，防止列表无限增长。
     */
    private fun cleanupReleasedDialogs() {
        val iterator = dialogs.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().get() == null) {
                iterator.remove()
            }
        }
    }
}