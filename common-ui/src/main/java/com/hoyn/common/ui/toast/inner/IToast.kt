package com.hoyn.common.ui.toast.inner

import android.view.View
import com.hoyn.common.ui.toast.ToastDuration

/**
 * Toast 接口
 */
interface IToast {

    fun show()

    fun cancel()

    fun setView(mView: View): IToast

    fun getView(): View

    fun setDuration(@ToastDuration duration: Int): IToast

    fun setGravity(gravity: Int): IToast

    fun setGravity(gravity: Int, xOffset: Int, yOffset: Int): IToast

    fun setAnimation(animation: Int): IToast

    fun setPriority(mPriority: Int): IToast
}
