package com.hoyn.common.ui.toast

import com.hjq.toast.ToastParams
import com.hjq.toast.config.IToastInterceptor

/**
 * Toast 参数映射器。
 *
 * 负责复制 ToastParams 并替换其中的 text 和 interceptor，
 * 避免直接修改原始 ToastParams 实例。
 */
internal object ToastParamsMapper {

    /**
     * 基于源参数创建新的 ToastParams，替换文本和拦截器
     * @param source 源 ToastParams
     * @param text 替换后的显示文本
     * @param interceptor 替换后的拦截器
     * @return 新的 ToastParams 实例
     */
    internal fun copyOf(
        source: ToastParams,
        text: CharSequence,
        interceptor: IToastInterceptor
    ): ToastParams {
        return ToastParams().apply {
            this.text = text
            this.duration = source.duration
            this.delayMillis = source.delayMillis
            this.priorityType = source.priorityType
            this.style = source.style
            this.strategy = source.strategy
            this.interceptor = interceptor
        }
    }
}
