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
     * 为非 BaseActivity 的页面提供显式注册入口。
     */
    @Synchronized
    fun registerActivity(activity: Activity) {
        cleanupStaleReferences()
        if (activityStack.any { it.get() === activity }) {
            return
        }
        activityStack.add(WeakReference(activity))
    }

    /**
     * 为非 BaseActivity 的页面提供显式反注册入口。
     */
    @Synchronized
    fun unregisterActivity(activity: Activity) {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val act = ref.get()
            if (act == null || act === activity) {
                iterator.remove()
            }
        }
    }

    /**
     * 将 Activity 推入栈中
     */
    @Synchronized
    fun push(activity: Activity) {
        registerActivity(activity)
    }

    /**
     * 从栈中移除指定的 Activity
     */
    @Synchronized
    fun pop(activity: Activity) {
        unregisterActivity(activity)
    }

    /**
     * 获取栈顶的 Activity（当前正在显示的）
     */
    @Synchronized
    fun current(): Activity? {
        return getActivityStackSnapshot().lastOrNull()
    }

    /**
     * 获取栈中指定类型的 Activity
     */
    @Synchronized
    fun <T : Activity> getActivity(clazz: Class<T>): T? {
        cleanupStaleReferences()
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
    @Synchronized
    fun isActivityInStack(clazz: Class<*>): Boolean {
        cleanupStaleReferences()
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
    @Synchronized
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
    @Synchronized
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
    @Synchronized
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
    @Synchronized
    fun getActivityCount(): Int {
        cleanupStaleReferences()
        return activityStack.size
    }

    /**
     * 获取当前 Activity 栈快照，返回顺序与入栈顺序一致。
     */
    @Synchronized
    fun getActivityStackSnapshot(): List<Activity> {
        cleanupStaleReferences()
        return activityStack.mapNotNull { it.get() }
    }

    /**
     * 退出应用程序（结束所有 Activity）
     */
    @Synchronized
    fun appExit() {
        finishAll()
        // 杀掉进程（可选）
        // android.os.Process.killProcess(android.os.Process.myPid())
        // System.exit(0)
    }

    private fun cleanupStaleReferences() {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().get() == null) {
                iterator.remove()
            }
        }
    }
}
