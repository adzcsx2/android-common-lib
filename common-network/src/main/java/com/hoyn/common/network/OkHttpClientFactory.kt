package com.hoyn.common.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * OkHttpClient 工厂类
 *
 * 用于创建配置好的 OkHttpClient 实例
 */
object OkHttpClientFactory {

    /**
     * 创建 OkHttpClient 实例
     *
     * 使用 NetworkConfig 配置超时时间
     * 自动添加日志拦截器和自定义拦截器
     *
     * @param interceptors 应用拦截器列表
     * @param networkInterceptors 网络拦截器列表
     * @return 配置好的 OkHttpClient 实例
     */
    fun create(
        interceptors: List<Interceptor> = emptyList(),
        networkInterceptors: List<Interceptor> = emptyList()
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(NetworkConfig.connectTimeout, TimeUnit.SECONDS)
            readTimeout(NetworkConfig.readTimeout, TimeUnit.SECONDS)
            writeTimeout(NetworkConfig.writeTimeout, TimeUnit.SECONDS)

            // 添加日志拦截器
            addInterceptor(
                LoggingInterceptor()
            )
            // 添加自定义拦截器
            interceptors.forEach { addInterceptor(it) }
            networkInterceptors.forEach { addNetworkInterceptor(it) }

        }.build()
    }
}
