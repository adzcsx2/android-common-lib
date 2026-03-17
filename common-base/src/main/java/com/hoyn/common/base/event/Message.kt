package com.hoyn.common.base.event

/**
 * 通用消息事件载体
 *
 * 用于在应用内传递消息事件，支持携带多个参数和任意对象
 */
data class Message(
    val code: Int = 0,
    val msg: String = "",
    val arg1: Int = 0,
    val arg2: Int = 0,
    val obj: Any? = null
) {
    companion object {
        /**
         * 创建简单消息
         */
        fun simple(msg: String): Message = Message(msg = msg)

        /**
         * 创建带代码的消息
         */
        fun withCode(code: Int, msg: String): Message = Message(code = code, msg = msg)
    }
}
