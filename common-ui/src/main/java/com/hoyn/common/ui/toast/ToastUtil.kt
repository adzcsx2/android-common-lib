package com.hoyn.common.ui.toast

import android.view.Gravity
import android.widget.TextView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RomUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.Utils
import com.hoyn.common.ui.R
import com.hoyn.common.ui.toast.inner.ActivityToast
import com.hoyn.common.ui.toast.inner.BaseToast
import com.hoyn.common.ui.toast.inner.IToast
import com.hoyn.common.ui.toast.inner.SystemToast

/**
 * Toast 工具类 - 简化版
 *
 * 特点：
 * 1. 使用 Application Context，避免内存泄漏
 * 2. 新 Toast 会立即取消前一个 Toast 并显示
 * 3. 无需传入 Context 参数
 *
 * @author hoyn
 */
object ToastUtil {

    /**
     * 显示短 Toast（底部）
     */
    fun show(message: String?) {
        showInternal(message, DURATION_SHORT, Gravity.BOTTOM or Gravity.CENTER, 0, SizeUtils.dp2px(25f))
    }

    /**
     * 显示长 Toast（底部）
     */
    fun showLong(message: String?) {
        showInternal(message, DURATION_LONG, Gravity.BOTTOM or Gravity.CENTER, 0, SizeUtils.dp2px(25f))
    }

    /**
     * 显示短 Toast（中间）
     */
    fun showCenter(message: String?) {
        showInternal(message, DURATION_SHORT, Gravity.CENTER, 0, 0)
    }

    /**
     * 显示长 Toast（中间）
     */
    fun showCenterLong(message: String?) {
        showInternal(message, DURATION_LONG, Gravity.CENTER, 0, 0)
    }

    /**
     * 自定义时长显示（底部）
     * @param duration DURATION_SHORT 或 DURATION_LONG
     */
    fun show(message: String?, @ToastDuration duration: Int) {
        showInternal(message, duration, Gravity.BOTTOM or Gravity.CENTER, 0, SizeUtils.dp2px(25f))
    }

    /**
     * 取消当前 Toast
     */
    fun cancel() {
        BaseToast.cancelAll()
        SystemToast.cancelAll()
    }

    /**
     * 内部实现
     */
    private fun showInternal(
        message: String?,
        @ToastDuration duration: Int,
        gravity: Int,
        xOffset: Int,
        yOffset: Int
    ) {
        if (message.isNullOrBlank()) return

        val context = Utils.getApp()
        val toast = make(context) ?: return

        // 设置内容
        val textView = toast.getView().findViewById<TextView>(R.id.tv_toast_content)
        textView.text = message

        // 设置时长和位置
        toast.setDuration(duration)
        toast.setGravity(gravity, xOffset, yOffset)

        // 使用原子操作：先入队再取消当前，避免竞态条件 (KI-001)
        if (toast is BaseToast) {
            BaseToast.addAndReplaceCurrent(toast)
        } else if (toast is SystemToast) {
            SystemToast.addAndReplaceCurrent(toast)
        } else {
            // 理论上不会进入此分支，兜底保持“新提示优先”语义
            cancel()
            toast.show()
        }
    }

    /**
     * 创建 Toast 实例
     * 使用 Application Context 避免内存泄漏
     */
    private fun make(context: android.content.Context): IToast? {
        return try {
            // 检查通知权限
            if (androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                // 有通知权限，使用系统 Toast（传入 applicationContext）
                SystemToast(context.applicationContext)
            } else {
                // 没有通知权限
                when {
                    RomUtils.isHuawei() || RomUtils.isMeizu() -> {
                        // 华为/魅族使用自定义 Toast
                        val activity = ActivityUtils.getTopActivity()
                        if (activity != null) {
                            ActivityToast(activity)
                        } else {
                            BaseToast(context.applicationContext)
                        }
                    }
                    RomUtils.isXiaomi() -> {
                        // 小米使用系统 Toast
                        SystemToast(context.applicationContext)
                    }
                    else -> {
                        // 其他机型使用系统 Toast
                        SystemToast(context.applicationContext)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
