package com.hoyn.common.ui.toast

import com.hjq.toast.ToastParams
import com.hjq.toast.config.IToastInterceptor

/**
 * Toast 拦截器组合器。
 *
 * 将调用位置日志拦截器（callSiteInterceptor）与全局拦截器（globalInterceptor）合并为一个拦截器链。
 * 两个拦截器依次执行，任一返回 true 即终止 Toast 显示。
 */
internal object ToastInterceptorComposer {

    /**
     * 组合两个拦截器。若全局拦截器为空或与调用位置拦截器相同，则直接返回调用位置拦截器
     * @param callSiteInterceptor 调用位置日志拦截器
     * @param globalInterceptor 全局拦截器，可为 null
     * @return 组合后的拦截器
     */
    internal fun compose(
        callSiteInterceptor: IToastInterceptor,
        globalInterceptor: IToastInterceptor?
    ): IToastInterceptor {
        if (globalInterceptor == null || globalInterceptor === callSiteInterceptor) {
            return callSiteInterceptor
        }
        return IToastInterceptor { params ->
            callSiteInterceptor.intercept(params) || globalInterceptor.intercept(params)
        }
    }
}
