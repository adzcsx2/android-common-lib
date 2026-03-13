package com.hoyn.common.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * OkHttpClient 工厂类
 */
object OkHttpClientFactory {

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
