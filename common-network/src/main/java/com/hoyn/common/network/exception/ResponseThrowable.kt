package com.hoyn.common.network.exception

/**
 * 响应异常类
 *
 * 封装网络请求的错误信息，包括错误码和错误消息
 *
 * @property code 错误码
 * @property errMsg 错误消息
 */
class ResponseThrowable : Exception {
    /**
     * 错误码
     */
    var code: Int

    /**
     * 错误消息
     */
    var errMsg: String

    /**
     * 使用错误枚举创建异常
     *
     * @param error 错误枚举
     * @param e 原始异常，可为 null
     */
    constructor(error: ERROR, e: Throwable? = null) : super(e) {
        code = error.ordinal
        errMsg = error.getValue()
    }

    /**
     * 使用自定义错误码和消息创建异常
     *
     * @param code 错误码
     * @param msg 错误消息
     * @param e 原始异常，可为 null
     */
    constructor(code: Int, msg: String, e: Throwable? = null) : super(e) {
        this.code = code
        this.errMsg = msg
    }
}
