package com.hoyn.common.ui.toast

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RomUtils
import com.blankj.utilcode.util.SizeUtils
import com.hoyn.common.ui.R
import com.hoyn.common.ui.toast.inner.ActivityToast
import com.hoyn.common.ui.toast.inner.BaseToast
import com.hoyn.common.ui.toast.inner.IToast
import com.hoyn.common.ui.toast.inner.SystemToast

/**
 * Toast 工具类
 * 防止各种异常情况及机型适配
 *
 * @author yzy
 * @date 2019/7/15
 */
object ToastUtils {
    // 上一条 toast 信息
    private var lastMsg: String? = null
    // 上一条 toast show 的时间戳
    private var lastTime: Long = 0L
    // 上一条 toast 需要展示的时长
    private var lastDuration: Int = 0

    /**
     * 显示 Toast（底部显示）
     */
    fun show(mContext: Context?, msg: String?, duration: Int = DURATION_SHORT) {
        showBottomToast(mContext, msg, duration)
    }

    /**
     * 显示长 Toast
     */
    fun showLong(mContext: Context?, msg: String?) {
        showBottomToast(mContext, msg, DURATION_LONG)
    }

    fun make(mContext: Context?): IToast? {
        mContext?.let {
            if (NotificationManagerCompat.from(it).areNotificationsEnabled()) {
                // 有通知权限直接用系统的
                return SystemToast(it)
            } else {
                // 没有通知权限
                return when {
                    RomUtils.isHuawei() -> {
                        // 华为用自定义的
                        if (ActivityUtils.getTopActivity() != null) {
                            ActivityToast(ActivityUtils.getTopActivity())
                        } else {
                            BaseToast(it)
                        }
                    }
                    RomUtils.isXiaomi() -> {
                        // 小米还是用系统的没问题
                        SystemToast(it)
                    }
                    RomUtils.isMeizu() -> {
                        // 魅族是个坑
                        if (ActivityUtils.getTopActivity() != null) {
                            ActivityToast(ActivityUtils.getTopActivity())
                        } else {
                            BaseToast(it)
                        }
                    }
                    else -> {
                        // 其他机型用系统的
                        SystemToast(it)
                    }
                }
            }
        }
        return null
    }

    /**
     * 底部显示一个 Toast
     */
    fun showBottomToast(mContext: Context?, msg: String?, duration: Int = DURATION_SHORT) {
        if (mContext == null || msg.isNullOrBlank()) return

        val showDuration = if (duration == Toast.LENGTH_SHORT) DURATION_SHORT else DURATION_LONG
        if (lastMsg == msg && lastDuration != 0 && lastTime != 0L && lastDuration > (System.currentTimeMillis() - lastTime)) {
            // 同样的提示且正在显示中
            return
        }

        val toast = make(mContext) ?: return
        val textView = toast.getView().findViewById<TextView>(R.id.tv_toast_content)
        textView.text = msg
        lastMsg = msg
        lastTime = System.currentTimeMillis()
        lastDuration = showDuration
        toast.setDuration(showDuration)
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER, 0, SizeUtils.dp2px(25f)).show()
    }

    /**
     * 中间显示一个 Toast
     */
    fun showCenterToast(mContext: Context?, msg: String?, duration: Int = DURATION_SHORT) {
        if (mContext == null || msg.isNullOrBlank()) return

        val showDuration = if (duration == Toast.LENGTH_SHORT) DURATION_SHORT else DURATION_LONG
        if (lastMsg == msg && lastDuration != 0 && lastTime != 0L && lastDuration > (System.currentTimeMillis() - lastTime)) {
            // 同样的提示且正在显示中
            return
        }

        val toast = make(mContext) ?: return
        val textView = toast.getView().findViewById<TextView>(R.id.tv_toast_content)
        textView.text = msg
        lastMsg = msg
        lastTime = System.currentTimeMillis()
        lastDuration = showDuration
        toast.setDuration(showDuration)
        toast.setGravity(Gravity.CENTER, 0, 0).show()
    }

    /**
     * 终止并清除所有弹窗
     */
    fun cancel() {
        BaseToast.cancelAll()
        SystemToast.cancelAll()
    }

    /**
     * 清除与 Activity 关联的 ActivityToast，避免窗口泄漏
     */
    fun cancelActivityToast(mActivity: Activity) {
        BaseToast.cancelActivityToast(mActivity)
    }
}
