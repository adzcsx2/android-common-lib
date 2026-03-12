package com.hoyn.common.network

import com.hoyn.common.log.Logger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
            if (NetworkConfig.isDebug) {
                addInterceptor(
                    HttpLoggingInterceptor { message ->
                        Logger.d(message, "OkHttp")
                    }.apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }

            // 添加自定义拦截器
            interceptors.forEach { addInterceptor(it) }
            networkInterceptors.forEach { addNetworkInterceptor(it) }

        }.build()
    }
}
