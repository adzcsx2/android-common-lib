package com.hoyn.common.utils

import com.blankj.utilcode.util.LogUtils

/**
 * 日志工具类
 *
 * 提供统一的日志输出接口，支持日志级别控制和格式化输出。
 * 基于 LogUtils 封装，提供更简洁的 API
 */
object Logger {

    /**
     * 初始化日志工具
     *
     * 应在 Application 的 onCreate 中调用此方法进行初始化
     *
     * @param debug 是否为调试模式，true 表示输出日志，false 表示关闭日志输出
     * @param tag 全局日志标签，用于标识日志来源，默认为 "CommonLib"
     */
    fun init(debug: Boolean, tag: String = "CommonLib") {
        LogUtils.getConfig()
            .setLogSwitch(debug)             // Debug才开启日志
            .setConsoleSwitch(true)          // 确保控制台输出开启
            .setConsoleFilter(LogUtils.V)    // 设置最低过滤级别为 VERBOSE (KI-002)
            .setGlobalTag(tag)               // 全局Tag
            .stackDeep = 3                   // 调用栈深度
    }


    /**
     * 输出 Debug 级别日志
     *
     * 用于输出调试信息，仅在调试模式下输出
     *
     * @param message 日志消息内容
     * @param tag 日志标签，默认使用全局标签
     */
    fun d(message: String, tag: String? = null) {
        LogUtils.d(tag, message)
    }

    /**
     * 输出 Info 级别日志
     *
     * 用于输出一般信息，仅在调试模式下输出
     *
     * @param message 日志消息内容
     * @param tag 日志标签，默认使用全局标签
     */
    fun i(message: String, tag: String? = null) {
        LogUtils.i(tag, message)
    }

    /**
     * 输出 Warning 级别日志
     *
     * 用于输出警告信息，仅在调试模式下输出
     *
     * @param message 日志消息内容
     * @param tag 日志标签，默认使用全局标签
     */
    fun w(message: String, tag: String? = null) {
        LogUtils.w(tag, message)
    }

    /**
     * 输出 Error 级别日志
     * 按json格式输出
     * 默认Type是Error
     * 用于输出错误信息，无论是否调试模式都会输出
     *
     * @param message 日志消息内容
     * @param tag 日志标签，默认使用全局标签
     */
    fun e(message: String, tag: String? = null, type: Int = LogUtils.E) {
        LogUtils.json(type, tag, message)
    }

    /**
     * 输出 Error 级别日志（带异常堆栈）
     *
     * 用于输出错误信息和异常堆栈，无论是否调试模式都会输出
     *
     * @param message 日志消息内容
     * @param throwable 异常对象，用于打印堆栈信息
     * @param tag 日志标签，默认使用全局标签
     */
    fun e(message: String, throwable: Throwable, tag: String? = null) {
        LogUtils.e(tag, message, throwable)
    }

    /**
     * 输出 JSON 格式日志
     * V 级
     * 将对象格式化为 JSON 字符串输出，便于查看数据结构
     *
     * @param obj 要格式化的对象
     * @param tag 日志标签，默认使用全局标签
     */
    fun json(obj: Any, tag: String? = null) {
        LogUtils.json(tag, obj)
    }


}
