/*
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hoyn.common.ui.toast.inner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.Toast
import com.hoyn.common.ui.R
import com.hoyn.common.ui.toast.DURATION_LONG
import com.hoyn.common.ui.toast.DURATION_SHORT
import com.hoyn.common.ui.toast.ToastDuration
import java.lang.ref.WeakReference

/**
 * 自定义 Toast 基类
 */
open class BaseToast(var mContext: Context) : IToast, Cloneable {

    private var contentView: View
    private var animation = android.R.style.Animation_Toast
    private var gravity = Gravity.BOTTOM or Gravity.CENTER
    private var xOffset: Int = 0
    private var yOffset: Int = 0
    private var width = WindowManager.LayoutParams.WRAP_CONTENT
    private var height = WindowManager.LayoutParams.WRAP_CONTENT
    private var priority: Int = 0
    private var timestamp: Long = 0
    @ToastDuration
    private var duration = DURATION_SHORT
    private var isShowing: Boolean = false

    init {
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams")
        this.contentView = layoutInflater.inflate(R.layout.layout_toast, null)
    }

    open fun getWMParams(): WindowManager.LayoutParams {
        val lp = WindowManager.LayoutParams()
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.format = PixelFormat.TRANSLUCENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        lp.height = this.height
        lp.width = this.width
        lp.windowAnimations = this.animation
        lp.gravity = this.gravity
        lp.x = this.xOffset
        lp.y = this.yOffset
        return lp
    }

    open fun getWMManager(): WindowManager? {
        return mContext.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun show() {
        YTN.instance().add(this)
    }

    override fun cancel() {
        YTN.instance().cancelAll()
    }

    fun getContext(): Context? = this.mContext

    override fun setView(mView: View): BaseToast {
        this.contentView = mView
        return this
    }

    override fun getView(): View = this.contentView

    override fun setDuration(@ToastDuration duration: Int): BaseToast {
        this.duration = duration
        return this
    }

    fun getDuration(): Int = this.duration

    override fun setAnimation(animation: Int): BaseToast {
        this.animation = animation
        return this
    }

    override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int): BaseToast {
        this.gravity = gravity
        this.xOffset = xOffset
        this.yOffset = yOffset
        return this
    }

    override fun setGravity(gravity: Int): BaseToast = setGravity(gravity, 0, 0)

    fun getGravity(): Int = this.gravity

    fun getXOffset(): Int = this.xOffset

    fun getYOffset(): Int = this.yOffset

    fun getPriority(): Int = this.priority

    override fun setPriority(mPriority: Int): BaseToast {
        this.priority = mPriority
        return this
    }

    fun getTimestamp(): Long = this.timestamp

    fun setTimestamp(mTimestamp: Long): BaseToast {
        this.timestamp = mTimestamp
        return this
    }

    fun isShowing(): Boolean = this.isShowing && contentView.isShown

    fun setShowing(isShowing: Boolean) {
        this.isShowing = isShowing
    }

    public override fun clone(): BaseToast {
        var mToast: BaseToast? = null
        try {
            mToast = super.clone() as BaseToast
            mToast.mContext = this.mContext
            mToast.contentView = this.contentView
            mToast.duration = this.duration
            mToast.animation = this.animation
            mToast.gravity = this.gravity
            mToast.height = this.height
            mToast.width = this.width
            mToast.xOffset = this.xOffset
            mToast.yOffset = this.yOffset
            mToast.priority = this.priority
        } catch (mE: CloneNotSupportedException) {
            mE.printStackTrace()
        }
        return mToast!!
    }

    companion object {
        var Count4BadTokenException: Long = 0

        fun cancelAll() {
            YTN.instance().cancelAll()
        }

        fun cancelActivityToast(mActivity: Activity) {
            YTN.instance().cancelActivityToast(mActivity)
        }
    }
}
