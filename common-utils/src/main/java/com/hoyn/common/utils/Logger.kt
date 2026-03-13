package com.hoyn.common.utils

import android.util.Log
import com.blankj.utilcode.util.LogUtils

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
        LogUtils.getConfig()
            .setLogSwitch(isDebug)  // Debug才开启日志
            .setGlobalTag("BaseLib")               // 全局Tag
            .stackDeep = 3                          // 调用栈深度
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
