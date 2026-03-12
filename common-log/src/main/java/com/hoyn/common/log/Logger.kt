package com.hoyn.common.log

import android.util.Log

/**
 * 日志工具类
 *
 * 提供统一的日志输出接口，支持日志级别控制和格式化输出。
 */
object Logger {

    private var isDebug = true
    private var defaultTag = "CommonLib"

    fun init(debug: Boolean, tag: String = "CommonLib") {
        isDebug = debug
        defaultTag = tag
    }

    fun v(message: String, tag: String = defaultTag) {
        if (isDebug) Log.v(tag, message)
    }

    fun d(message: String, tag: String = defaultTag) {
        if (isDebug) Log.d(tag, message)
    }

    fun i(message: String, tag: String = defaultTag) {
        if (isDebug) Log.i(tag, message)
    }

    fun w(message: String, tag: String = defaultTag) {
        if (isDebug) Log.w(tag, message)
    }

    fun e(message: String, tag: String = defaultTag) {
        Log.e(tag, message)
    }

    fun e(message: String, throwable: Throwable, tag: String = defaultTag) {
        Log.e(tag, message, throwable)
    }

    fun json(json: String, tag: String = defaultTag) {
        if (isDebug) {
            Log.d(tag, "JSON: $json")
        }
    }
}
