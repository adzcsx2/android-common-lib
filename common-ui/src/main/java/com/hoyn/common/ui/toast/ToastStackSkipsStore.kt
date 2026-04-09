package com.hoyn.common.ui.toast

import com.hjq.toast.ToastParams
import java.util.WeakHashMap


/**
 * Toast 堆栈跳过层数临时存储。
 *
 * 使用 WeakHashMap 将 stackSkips 值与 ToastParams 实例关联，
 * 在 show 端写入、在拦截器端消费，实现跨调用链传递跳过层数。
 * WeakHashMap 保证 ToastParams 被回收时自动清理对应条目。
 */
internal object ToastStackSkipsStore {

    /** 同步锁 */
    private val lock = Any()

    /** ToastParams -> stackSkips 的弱引用映射 */
    private val stackSkipsMap = WeakHashMap<ToastParams, Int>()

    /**
     * 将 stackSkips 绑定到指定的 ToastParams 实例
     * @param params 目标 ToastParams
     * @param stackSkips 跳过层数，为 0 时移除绑定
     */
    internal fun bind(params: ToastParams, stackSkips: Int) {
        synchronized(lock) {
            if (stackSkips == 0) {
                stackSkipsMap.remove(params)
            } else {
                stackSkipsMap[params] = stackSkips
            }
        }
    }

    /**
     * 取出并移除指定 ToastParams 对应的 stackSkips 值
     * @param params 目标 ToastParams
     * @return 跳过层数，未绑定时返回 0
     */
    internal fun take(params: ToastParams): Int {
        return synchronized(lock) {
            stackSkipsMap.remove(params) ?: 0
        }
    }
}
