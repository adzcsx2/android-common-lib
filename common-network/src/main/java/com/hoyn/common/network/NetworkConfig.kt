package com.hoyn.common.network

/**
 * 网络配置
 *
 * 用于配置网络请求的超时时间和调试模式
 * 应在应用启动时进行配置
 */
object NetworkConfig {
    /**
     * 连接超时时间（秒）
     */
    var connectTimeout: Long = 30

    /**
     * 读取超时时间（秒）
     */
    var readTimeout: Long = 30

    /**
     * 写入超时时间（秒）
     */
    var writeTimeout: Long = 30

    /**
     * 是否为调试模式
     * 调试模式下会输出详细的网络日志
     */
    var isDebug: Boolean = true
}
