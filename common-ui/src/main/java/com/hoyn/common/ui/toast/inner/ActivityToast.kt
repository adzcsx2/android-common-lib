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

    private var mParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null
    private var isAdded: Boolean = false

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

    override fun getWMManager(): WindowManager? {
        if (mWindowManager == null) {
            mWindowManager = mActivity.windowManager
        }
        return mWindowManager
    }

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

    override fun cancel() {
        if (!isAdded) return
        try {
            getWMManager()?.removeViewImmediate(getView())
            isAdded = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActivity(): Activity = mActivity
}
