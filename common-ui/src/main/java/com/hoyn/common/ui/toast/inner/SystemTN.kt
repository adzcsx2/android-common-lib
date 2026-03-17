package com.hoyn.common.ui.toast.inner

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.hoyn.common.ui.toast.DURATION_LONG
import com.hoyn.common.ui.toast.DURATION_SHORT
import java.util.*

/**
 * 系统 Toast 管理类
 */
class SystemTN private constructor() {

    private val mQueue: LinkedList<SystemToast> = LinkedList()
    private val mHandler = Handler(Looper.getMainLooper())
    private var mCurrentToast: SystemToast? = null

    companion object {
        @Volatile
        private var instance: SystemTN? = null

        fun instance(): SystemTN {
            return instance ?: synchronized(this) {
                instance ?: SystemTN().also { instance = it }
            }
        }
    }

    /**
     * 添加 Toast 到队列
     *
     * @param toast 要显示的 SystemToast 实例
     */
    fun add(toast: SystemToast) {
        mQueue.add(toast)
        if (mCurrentToast == null) {
            scheduleNext()
        }
    }

    /**
     * 调度下一个 Toast 显示
     *
     * 从队列中取出下一个 Toast 并显示，设置延迟后继续调度
     */
    private fun scheduleNext() {
        if (mQueue.isEmpty()) {
            mCurrentToast = null
            return
        }

        mCurrentToast = mQueue.poll()
        mCurrentToast?.let { toast ->
            try {
                toast.showInternal()
                val duration = toast.getDuration()
                mHandler.postDelayed({ scheduleNext() }, duration.toLong())
            } catch (e: Exception) {
                e.printStackTrace()
                scheduleNext()
            }
        }
    }

    /**
     * 取消所有 Toast
     *
     * 清空队列并取消当前显示的 Toast
     */
    fun cancelAll() {
        mHandler.removeCallbacksAndMessages(null)
        mCurrentToast?.cancel()
        mQueue.clear()
        mCurrentToast = null
    }
}
