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

    /** Toast 显示的内容视图 */
    private var contentView: View
    /** Toast 显示/消失的动画资源 ID */
    private var animation = android.R.style.Animation_Toast
    /** Toast 在屏幕上的显示位置，默认底部居中 */
    private var gravity = Gravity.BOTTOM or Gravity.CENTER
    /** Toast X 轴偏移量（像素） */
    private var xOffset: Int = 0
    /** Toast Y 轴偏移量（像素） */
    private var yOffset: Int = 0
    /** Toast 宽度，默认自适应内容 */
    private var width = WindowManager.LayoutParams.WRAP_CONTENT
    /** Toast 高度，默认自适应内容 */
    private var height = WindowManager.LayoutParams.WRAP_CONTENT
    /** Toast 优先级，数值越高优先级越高 */
    private var priority: Int = 0
    /** Toast 创建时间戳 */
    private var timestamp: Long = 0
    /** Toast 显示时长（毫秒） */
    @ToastDuration
    private var duration = DURATION_SHORT
    /** Toast 是否正在显示 */
    private var isShowing: Boolean = false

    init {
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams")
        this.contentView = layoutInflater.inflate(R.layout.layout_toast, null)
    }

    /**
     * 获取 WindowManager 布局参数
     *
     * @return WindowManager.LayoutParams 对象
     */
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

    /**
     * 获取 WindowManager 实例
     *
     * @return WindowManager 实例
     */
    open fun getWMManager(): WindowManager? {
        return mContext.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    /**
     * 显示 Toast
     *
     * 将 Toast 添加到显示队列
     */
    override fun show() {
        YTN.instance().add(this)
    }

    /**
     * 取消 Toast
     *
     * 清空 Toast 显示队列
     */
    override fun cancel() {
        YTN.instance().cancelAll()
    }

    /**
     * 获取 Context
     *
     * @return Context 实例
     */
    fun getContext(): Context? = this.mContext

    /**
     * 设置 Toast 的视图
     *
     * @param mView 要设置的视图
     * @return 当前 BaseToast 实例，支持链式调用
     */
    override fun setView(mView: View): BaseToast {
        this.contentView = mView
        return this
    }

    /**
     * 获取 Toast 的视图
     *
     * @return Toast 的视图对象
     */
    override fun getView(): View = this.contentView

    /**
     * 设置 Toast 显示时长
     *
     * @param duration 显示时长
     * @return 当前 BaseToast 实例，支持链式调用
     */
    override fun setDuration(@ToastDuration duration: Int): BaseToast {
        this.duration = duration
        return this
    }

    /**
     * 获取 Toast 显示时长
     *
     * @return 显示时长（毫秒）
     */
    fun getDuration(): Int = this.duration

    /**
     * 设置 Toast 显示动画
     *
     * @param animation 动画资源 ID
     * @return 当前 BaseToast 实例，支持链式调用
     */
    override fun setAnimation(animation: Int): BaseToast {
        this.animation = animation
        return this
    }

    /**
     * 设置 Toast 显示位置（带偏移）
     *
     * @param gravity 显示位置，使用 Gravity 常量
     * @param xOffset X 轴偏移量
     * @param yOffset Y 轴偏移量
     * @return 当前 BaseToast 实例，支持链式调用
     */
    override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int): BaseToast {
        this.gravity = gravity
        this.xOffset = xOffset
        this.yOffset = yOffset
        return this
    }

    /**
     * 设置 Toast 显示位置（默认偏移）
     *
     * @param gravity 显示位置，使用 Gravity 常量
     * @return 当前 BaseToast 实例，支持链式调用
     */
    override fun setGravity(gravity: Int): BaseToast = setGravity(gravity, 0, 0)

    /**
     * 获取 Toast 显示位置
     *
     * @return 显示位置，使用 Gravity 常量
     */
    fun getGravity(): Int = this.gravity

    /**
     * 获取 X 轴偏移量
     *
     * @return X 轴偏移量（像素）
     */
    fun getXOffset(): Int = this.xOffset

    /**
     * 获取 Y 轴偏移量
     *
     * @return Y 轴偏移量（像素）
     */
    fun getYOffset(): Int = this.yOffset

    /**
     * 获取 Toast 优先级
     *
     * @return 优先级值
     */
    fun getPriority(): Int = this.priority

    /**
     * 设置 Toast 优先级
     *
     * @param mPriority 优先级值
     * @return 当前 BaseToast 实例，支持链式调用
     */
    override fun setPriority(mPriority: Int): BaseToast {
        this.priority = mPriority
        return this
    }

    /**
     * 获取 Toast 时间戳
     *
     * @return 时间戳
     */
    fun getTimestamp(): Long = this.timestamp

    /**
     * 设置 Toast 时间戳
     *
     * @param mTimestamp 时间戳
     * @return 当前 BaseToast 实例，支持链式调用
     */
    fun setTimestamp(mTimestamp: Long): BaseToast {
        this.timestamp = mTimestamp
        return this
    }

    /**
     * 判断 Toast 是否正在显示
     *
     * @return true 表示正在显示，false 表示未显示
     */
    fun isShowing(): Boolean = this.isShowing && contentView.isShown

    /**
     * 设置 Toast 显示状态
     *
     * @param isShowing true 表示正在显示，false 表示未显示
     */
    fun setShowing(isShowing: Boolean) {
        this.isShowing = isShowing
    }

    /**
     * 克隆当前 Toast
     *
     * @return 克隆后的 BaseToast 实例
     */
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
        /** BadTokenException 异常计数器 */
        var Count4BadTokenException: Long = 0

        /**
         * 取消所有 Toast
         */
        fun cancelAll() {
            YTN.instance().cancelAll()
        }

        /**
         * 取消当前 Toast 并显示下一个
         */
        fun cancelCurrentAndShowNext() {
            YTN.instance().cancelCurrentAndShowNext()
        }

        /**
         * 添加 Toast 并替换当前显示的 Toast
         *
         * 原子操作，避免竞态条件
         *
         * @param toast 要显示的 Toast
         */
        fun addAndReplaceCurrent(toast: BaseToast) {
            YTN.instance().addAndReplaceCurrent(toast)
        }

        /**
         * 取消指定 Activity 的所有 Toast
         *
         * @param mActivity 要取消 Toast 的 Activity
         */
        fun cancelActivityToast(mActivity: Activity) {
            YTN.instance().cancelActivityToast(mActivity)
        }
    }
}
