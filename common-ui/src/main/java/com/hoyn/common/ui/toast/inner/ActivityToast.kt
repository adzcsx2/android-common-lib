package com.hoyn.common.ui.toast.inner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.view.*
import android.widget.FrameLayout
import com.hoyn.common.ui.R
import com.hoyn.common.ui.toast.DURATION_SHORT
import com.hoyn.common.ui.toast.ToastDuration

/**
 * 基于 Activity 的 Toast 实现
 * 用于没有通知权限时的降级方案
 */
class ActivityToast(private val mActivity: Activity) : BaseToast(mActivity) {

    /** WindowManager 布局参数，懒加载 */
    private var mParams: WindowManager.LayoutParams? = null
    /** Activity 的 WindowManager 实例，懒加载 */
    private var mWindowManager: WindowManager? = null
    /** Toast 视图是否已添加到 WindowManager */
    private var isAdded: Boolean = false

    /**
     * 获取 WindowManager 布局参数
     *
     * @return WindowManager.LayoutParams 对象
     */
    override fun getWMParams(): WindowManager.LayoutParams {
        if (mParams == null) {
            mParams = WindowManager.LayoutParams()
            mParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
            mParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
            mParams?.format = PixelFormat.TRANSLUCENT
            mParams?.windowAnimations = android.R.style.Animation_Toast
            mParams?.gravity = getGravity()
            mParams?.x = getXOffset()
            mParams?.y = getYOffset()
        }
        return mParams!!
    }

    /**
     * 获取 WindowManager 实例
     *
     * @return Activity 的 WindowManager 实例
     */
    override fun getWMManager(): WindowManager? {
        if (mWindowManager == null) {
            mWindowManager = mActivity.windowManager
        }
        return mWindowManager
    }

    /**
     * 显示 Toast
     *
     * 将 Toast 视图添加到 Activity 的 WindowManager 中
     */
    override fun show() {
        if (isAdded) return
        try {
            getWMManager()?.addView(getView(), getWMParams())
            isAdded = true
            YTN.instance().addActivityToast(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 取消 Toast
     *
     * 从 Activity 的 WindowManager 中移除 Toast 视图
     */
    override fun cancel() {
        if (!isAdded) return
        try {
            getWMManager()?.removeViewImmediate(getView())
            isAdded = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取关联的 Activity
     *
     * @return Activity 实例
     */
    fun getActivity(): Activity = mActivity
}
