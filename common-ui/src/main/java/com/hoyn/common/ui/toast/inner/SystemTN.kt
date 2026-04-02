package com.hoyn.common.ui.toast.inner

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.*

/**
 * 系统 Toast 管理类
 */
/**
 * 系统 Toast 管理类
 *
 * 负责管理系统 Toast 的显示队列，确保同一时刻只有一个系统 Toast 显示
 * 支持新 Toast 替换当前 Toast 的原子操作
 */
class SystemTN private constructor() {

    /** 系统 Toast 显示队列 */
    private val mQueue: LinkedList<SystemToast> = LinkedList()
    /** 主线程 Handler，用于延迟调度和线程切换 */
    private val mHandler = Handler(Looper.getMainLooper())
    /** 当前正在显示的系统 Toast */
    private var mCurrentToast: SystemToast? = null
    /** 取消操作锁，防止并发取消导致的递归问题 */
    private var isCancelling = false

    companion object {
        @Volatile
        private var instance: SystemTN? = null

        /**
         * 获取 SystemTN 单例实例
         *
         * 使用双重检查锁定确保线程安全
         *
         * @return SystemTN 实例
         */
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
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post { add(toast) }
            return
        }
        mQueue.add(toast)
        if (mCurrentToast == null) {
            scheduleNext()
        }
    }

    /**
     * 添加新 Toast 并替换当前显示的 Toast。
     *
     * 为保证“最新提示立即显示”，会丢弃等待队列中的旧消息。
     */
    fun addAndReplaceCurrent(toast: SystemToast) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post { addAndReplaceCurrent(toast) }
            return
        }

        // 仅保留最新消息，避免高频点击后旧消息继续排队
        mQueue.clear()
        mQueue.add(toast)

        if (mCurrentToast == null) {
            scheduleNext()
            return
        }

        cancelCurrentToast()
        mHandler.removeCallbacksAndMessages(null)
        scheduleNext()
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
     * 取消当前正在显示的系统 Toast
     */
    private fun cancelCurrentToast() {
        mCurrentToast?.cancelInternal()
        mCurrentToast = null
    }

    /**
     * 取消所有 Toast
     *
     * 清空队列并取消当前显示的 Toast
     */
    fun cancelAll() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post { cancelAll() }
            return
        }

        if (isCancelling) return
        isCancelling = true
        mHandler.removeCallbacksAndMessages(null)
        try {
            cancelCurrentToast()
            mQueue.clear()
        } finally {
            isCancelling = false
        }
    }
}
