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

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.hoyn.common.ui.toast.DURATION_SHORT
import java.lang.ref.WeakReference
import java.util.*

/**
 * Toast 管理类
 * 负责管理自定义 Toast 的显示队列
 */
class YTN private constructor() {

    private val mQueue: LinkedList<BaseToast> = LinkedList()
    private val mHandler = Handler(Looper.getMainLooper())
    private var mCurrentToast: BaseToast? = null
    private val mActivityToasts: MutableSet<WeakReference<ActivityToast>> = Collections.newSetFromMap(WeakHashMap())

    companion object {
        @Volatile
        private var instance: YTN? = null

        fun instance(): YTN {
            return instance ?: synchronized(this) {
                instance ?: YTN().also { instance = it }
            }
        }
    }

    fun add(toast: BaseToast) {
        mQueue.add(toast)
        if (mCurrentToast == null) {
            scheduleNext()
        }
    }

    private fun scheduleNext() {
        if (mQueue.isEmpty()) {
            mCurrentToast = null
            return
        }

        mCurrentToast = mQueue.poll()
        mCurrentToast?.let { toast ->
            try {
                showToast(toast)
                val duration = toast.getDuration()
                mHandler.postDelayed({ scheduleNext() }, duration.toLong())
            } catch (e: Exception) {
                e.printStackTrace()
                scheduleNext()
            }
        }
    }

    private fun showToast(toast: BaseToast) {
        try {
            val context = toast.getContext() ?: return
            val wm = toast.getWMManager() ?: return
            val params = toast.getWMParams()

            if (toast.isShowing()) {
                wm.updateViewLayout(toast.getView(), params)
            } else {
                wm.addView(toast.getView(), params)
                toast.setShowing(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 发生异常时尝试使用系统 Toast
            try {
                val context = toast.getContext() ?: return
                val systemToast = Toast(context)
                systemToast.view = toast.getView()
                systemToast.duration = if (toast.getDuration() == DURATION_SHORT) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                systemToast.setGravity(toast.getGravity(), toast.getXOffset(), toast.getYOffset())
                systemToast.show()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun cancelAll() {
        mHandler.removeCallbacksAndMessages(null)
        mCurrentToast?.let {
            try {
                it.getWMManager()?.removeViewImmediate(it.getView())
                it.setShowing(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mQueue.clear()
        mCurrentToast = null
    }

    fun addActivityToast(toast: ActivityToast) {
        mActivityToasts.add(WeakReference(toast))
    }

    fun cancelActivityToast(mActivity: Activity) {
        val iterator = mActivityToasts.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val toast = ref.get()
            if (toast == null || toast.getActivity() == mActivity) {
                toast?.cancel()
                iterator.remove()
            }
        }
    }
}
