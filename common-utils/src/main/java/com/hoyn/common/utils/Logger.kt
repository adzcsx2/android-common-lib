package com.hoyn.common.utils

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
            .setGlobalTag(tag)               // 全局Tag
            .stackDeep = 3                          // 调用栈深度
    }

    fun v(message: String, tag: String = defaultTag) {
        if (isDebug) LogUtils.v(tag, message)
    }

    fun d(message: String, tag: String = defaultTag) {
        if (isDebug) LogUtils.d(tag, message)
    }

    fun i(message: String, tag: String = defaultTag) {
        if (isDebug) LogUtils.i(tag, message)
    }

    fun w(message: String, tag: String = defaultTag) {
        if (isDebug) LogUtils.w(tag, message)
    }

    fun e(message: String, tag: String = defaultTag) {
        LogUtils.e(tag, message)
    }

    fun e(message: String, throwable: Throwable, tag: String = defaultTag) {
        LogUtils.e(tag, message, throwable)
    }

    fun json(obj: Any, tag: String = defaultTag) {
        if (isDebug) LogUtils.json(defaultTag, obj)

    }
}
