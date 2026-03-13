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
 */
class SystemToast(var mContext: Context) : IToast, Cloneable {

    private var mToast: Toast? = null
    private var priority: Int = 0
    private var contentView: View? = null
    private var animation = android.R.style.Animation_Toast
    private var gravity = Gravity.BOTTOM or Gravity.CENTER
    private var xOffset: Int = 0
    private var yOffset: Int = 0
    @ToastDuration
    private var duration = DURATION_SHORT

    init {
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams")
        this.contentView = layoutInflater.inflate(R.layout.layout_toast, null)
    }

    override fun show() {
        SystemTN.instance().add(this)
    }

    fun showLong() {
        this.setDuration(DURATION_LONG).show()
    }

    override fun cancel() {
        SystemTN.instance().cancelAll()
    }

    fun showInternal() {
        mToast = Toast(mContext)
        mToast?.view = contentView
        mToast?.duration = if (duration == DURATION_SHORT) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
        mToast?.setGravity(gravity, xOffset, yOffset)
        mToast?.show()
    }

    override fun setView(mView: View): SystemToast {
        this.contentView = mView
        return this
    }

    override fun getView(): View {
        return this.contentView!!
    }

    override fun setDuration(@ToastDuration duration: Int): SystemToast {
        this.duration = duration
        return this
    }

    fun getDuration(): Int = this.duration

    override fun setAnimation(animation: Int): SystemToast {
        this.animation = animation
        return this
    }

    override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int): SystemToast {
        this.gravity = gravity
        this.xOffset = xOffset
        this.yOffset = yOffset
        return this
    }

    override fun setGravity(gravity: Int): SystemToast {
        return setGravity(gravity, 0, 0)
    }

    fun getGravity(): Int = this.gravity

    fun getXOffset(): Int = this.xOffset

    fun getYOffset(): Int = this.yOffset

    fun getPriority(): Int = priority

    override fun setPriority(mPriority: Int): SystemToast {
        this.priority = mPriority
        return this
    }

    public override fun clone(): SystemToast {
        var mToast: SystemToast? = null
        try {
            mToast = super.clone() as SystemToast
            mToast.mContext = this.mContext
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
        fun cancelAll() {
            SystemTN.instance().cancelAll()
        }
    }
}
