package com.hoyn.common.base

import android.app.Activity
import java.lang.ref.WeakReference
import java.util.Stack

/**
 * Activity 栈管理器
 *
 * 使用 WeakReference 存储 Activity 引用，防止内存泄露
 * 提供统一的 Activity 生命周期管理接口
 * 支持 AppCompatActivity 和 ComponentActivity（Compose）
 */
object ActivityStackManager {

    /**
     * Activity 栈，使用 WeakReference 防止内存泄露
     */
    private val activityStack = Stack<WeakReference<Activity>>()

    /**
     * 将 Activity 推入栈中
     */
    fun push(activity: Activity) {
        activityStack.add(WeakReference(activity))
    }

    /**
     * 从栈中移除指定的 Activity
     */
    fun pop(activity: Activity) {
        // 使用 iterator 安全遍历并移除
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val act = ref.get()
            if (act == null || act == activity) {
                iterator.remove()
            }
        }
    }

    /**
     * 获取栈顶的 Activity（当前正在显示的）
     */
    fun current(): Activity? {
        if (activityStack.isEmpty()) return null

        // 清理已回收的引用并获取最后一个有效引用
        val iterator = activityStack.iterator()
        var lastValid: Activity? = null

        while (iterator.hasNext()) {
            val ref = iterator.next()
            val act = ref.get()
            if (act == null) {
                iterator.remove()
            } else {
                lastValid = act
            }
        }

        return lastValid
    }

    /**
     * 获取栈中指定类型的 Activity
     */
    fun <T : Activity> getActivity(clazz: Class<T>): T? {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val act = ref.get()
            if (act == null) {
                iterator.remove()
            } else if (clazz.isInstance(act)) {
                @Suppress("UNCHECKED_CAST")
                return act as T
            }
        }
        return null
    }

    /**
     * 判断指定类型的 Activity 是否在栈中
     */
    fun isActivityInStack(clazz: Class<*>): Boolean {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val act = ref.get()
            if (act == null) {
                iterator.remove()
            } else if (clazz.isInstance(act)) {
                return true
            }
        }
        return false
    }

    /**
     * 结束栈中所有的 Activity
     */
    fun finishAll() {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val act = ref.get()
            if (act == null) {
                iterator.remove()
            } else {
                act.finish()
                iterator.remove()
            }
        }
    }

    /**
     * 结束除了当前 Activity 之外的所有 Activity
     */
    fun finishAllExceptCurrent() {
        val current = current() ?: return
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val act = ref.get()
            if (act == null) {
                iterator.remove()
            } else if (act != current) {
                act.finish()
                iterator.remove()
            }
        }
    }

    /**
     * 结束指定类型的 Activity
     */
    fun finishActivity(clazz: Class<*>) {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val act = ref.get()
            if (act == null) {
                iterator.remove()
            } else if (clazz.isInstance(act)) {
                act.finish()
                iterator.remove()
            }
        }
    }

    /**
     * 获取栈中 Activity 的数量
     */
    fun getActivityCount(): Int {
        // 清理已回收的引用
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            if (ref.get() == null) {
                iterator.remove()
            }
        }
        return activityStack.size
    }

    /**
     * 退出应用程序（结束所有 Activity）
     */
    fun appExit() {
        finishAll()
        // 杀掉进程（可选）
        // android.os.Process.killProcess(android.os.Process.myPid())
        // System.exit(0)
    }
}
