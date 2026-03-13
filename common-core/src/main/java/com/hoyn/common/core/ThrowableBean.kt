package com.hoyn.common.core

/**
 * 异常信息载体
 *
 * 用于封装异常信息，便于在 UI 层展示错误详情
 */
data class ThrowableBean(
    val code: Int = 0,
    val errMsg: String = ""
) {
    companion object {
        /**
         * 从异常创建
         */
        fun from(throwable: Throwable): ThrowableBean {
            return ThrowableBean(errMsg = throwable.message ?: "Unknown error")
        }
    }
}
