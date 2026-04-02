package com.hoyn.common.base.event

/**
 * 通用消息事件载体
 *
 * 用于在应用内传递消息事件，支持携带多个参数和任意对象
 */
data class Message(
    /** 消息码，用于区分消息类型 */
    val code: Int = 0,
    /** 消息内容文本 */
    val msg: String = "",
    /** 额外整型参数 1 */
    val arg1: Int = 0,
    /** 额外整型参数 2 */
    val arg2: Int = 0,
    /** 额外携带的任意对象 */
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
