package com.hoyn.common.ui.toast.inner

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.hoyn.common.ui.R
import com.hoyn.common.ui.toast.DURATION_LONG
import com.hoyn.common.ui.toast.DURATION_SHORT
import com.hoyn.common.ui.toast.ToastDuration

/**
 * 系统 Toast 封装
 * 注意：内部使用 applicationContext 避免内存泄漏
 */
class SystemToast(context: Context) : IToast, Cloneable {

    /** 使用 applicationContext 避免内存泄漏 */
    private val mContext: Context = context.applicationContext
    /** 系统 Toast 实例 */
    private var mToast: Toast? = null
    /** Toast 优先级 */
    private var priority: Int = 0
    /** Toast 显示的内容视图 */
    private var contentView: View? = null
    /** Toast 显示/消失的动画资源 ID */
    private var animation = android.R.style.Animation_Toast
    /** Toast 在屏幕上的显示位置，默认底部居中 */
    private var gravity = Gravity.BOTTOM or Gravity.CENTER
    /** Toast X 轴偏移量（像素） */
    private var xOffset: Int = 0
    /** Toast Y 轴偏移量（像素） */
    private var yOffset: Int = 0
    /** Toast 显示时长（毫秒） */
    @ToastDuration
    private var duration = DURATION_SHORT

    init {
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams")
        this.contentView = layoutInflater.inflate(R.layout.layout_toast, null)
    }

    /**
     * 显示 Toast
     *
     * 将 Toast 添加到系统 Toast 显示队列
     */
    override fun show() {
        SystemTN.instance().add(this)
    }

    /**
     * 显示长时长 Toast
     */
    fun showLong() {
        this.setDuration(DURATION_LONG).show()
    }

    /**
     * 取消 Toast
     *
     * 取消系统 Toast 显示
     */
    override fun cancel() {
        SystemTN.instance().cancelAll()
    }

    /**
     * 内部显示方法
     *
     * 使用系统 Toast 显示内容
     */
    fun showInternal() {
        mToast = Toast(mContext)
        mToast?.view = contentView
        mToast?.duration = if (duration == DURATION_SHORT) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
        mToast?.setGravity(gravity, xOffset, yOffset)
        mToast?.show()
    }

    /**
     * 内部取消方法。
     *
     * 仅取消当前系统 Toast 实例，避免再次回调 SystemTN 产生递归。
     */
    fun cancelInternal() {
        mToast?.cancel()
        mToast = null
    }

    /**
     * 设置 Toast 的视图
     *
     * @param mView 要设置的视图
     * @return 当前 SystemToast 实例，支持链式调用
     */
    override fun setView(mView: View): SystemToast {
        this.contentView = mView
        return this
    }

    /**
     * 获取 Toast 的视图
     *
     * @return Toast 的视图对象
     */
    override fun getView(): View {
        return this.contentView!!
    }

    /**
     * 设置 Toast 显示时长
     *
     * @param duration 显示时长
     * @return 当前 SystemToast 实例，支持链式调用
     */
    override fun setDuration(@ToastDuration duration: Int): SystemToast {
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
     * @return 当前 SystemToast 实例，支持链式调用
     */
    override fun setAnimation(animation: Int): SystemToast {
        this.animation = animation
        return this
    }

    /**
     * 设置 Toast 显示位置（带偏移）
     *
     * @param gravity 显示位置，使用 Gravity 常量
     * @param xOffset X 轴偏移量
     * @param yOffset Y 轴偏移量
     * @return 当前 SystemToast 实例，支持链式调用
     */
    override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int): SystemToast {
        this.gravity = gravity
        this.xOffset = xOffset
        this.yOffset = yOffset
        return this
    }

    /**
     * 设置 Toast 显示位置（默认偏移）
     *
     * @param gravity 显示位置，使用 Gravity 常量
     * @return 当前 SystemToast 实例，支持链式调用
     */
    override fun setGravity(gravity: Int): SystemToast {
        return setGravity(gravity, 0, 0)
    }

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
    fun getPriority(): Int = priority

    /**
     * 设置 Toast 优先级
     *
     * @param mPriority 优先级值
     * @return 当前 SystemToast 实例，支持链式调用
     */
    /**
     * 设置 Toast 优先级
     *
     * @param mPriority 优先级值
     * @return 当前 SystemToast 实例，支持链式调用
     */
    override fun setPriority(mPriority: Int): SystemToast {
        this.priority = mPriority
        return this
    }

    /**
     * 克隆当前 Toast
     *
     * @return 克隆后的 SystemToast 实例
     */
    public override fun clone(): SystemToast {
        var mToast: SystemToast? = null
        try {
            mToast = super.clone() as SystemToast
            // mContext 是 val，通过构造函数已设置，无需重新赋值
            mToast.contentView = this.contentView
            mToast.duration = this.duration
            mToast.animation = this.animation
            mToast.gravity = this.gravity
            mToast.xOffset = this.xOffset
            mToast.yOffset = this.yOffset
            mToast.priority = this.priority
        } catch (mE: CloneNotSupportedException) {
            mE.printStackTrace()
        }
        return mToast!!
    }

    companion object {
        /**
         * 取消所有系统 Toast
         */
        fun cancelAll() {
            SystemTN.instance().cancelAll()
        }

        /**
         * 添加并立即替换当前显示的系统 Toast。
         */
        fun addAndReplaceCurrent(toast: SystemToast) {
            SystemTN.instance().addAndReplaceCurrent(toast)
        }
    }
}
