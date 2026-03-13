package com.hoyn.common.network

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.hoyn.common.network.ssl.SSLManager
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * HTTP Client 工具类
 * 用于配置和创建 Retrofit 客户端
 */
object HttpClientUtils {

    private var retrofit: Retrofit? = null
    private var config: InitRetrofitConfig? = null

    /**
     * 初始化 Gson Builder
     */
    fun initGsonBuilder(): GsonBuilder {
        return GsonBuilder().serializeNulls().enableComplexMapKeySerialization()
            .registerTypeAdapter(Double::class.java, JsonSerializer<Double> { src, _, _ ->
                if (src != null && src.equals(src.toLong().toDouble())) {
                    JsonPrimitive(src.toLong())
                } else {
                    JsonPrimitive(src)
                }
            })
    }

    /**
     * 初始化配置
     */
    fun initConfig(config: InitRetrofitConfig) {
        this.config = config
        config.context?.let {
            val okHttpClient = initOkHttp(config)
            retrofit = initRetrofit(config, okHttpClient)
        }
    }

    private fun initOkHttp(config: InitRetrofitConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(config.timeOut, TimeUnit.SECONDS)
            .writeTimeout(config.timeOut, TimeUnit.SECONDS)
            .readTimeout(config.timeOut, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(8, 15, TimeUnit.SECONDS))

        // 禁止正式版抓包
        if (config.isRelease) {
            builder.proxy(Proxy.NO_PROXY)
        }

        // 添加自定义拦截器
        config.interceptors.forEach {
            builder.addInterceptor(it)
        }

        // 添加日志拦截器
        if (!config.isRelease) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }

        // SSL 配置
        SSLManager.createSSLSocketFactory()?.let {
            builder.sslSocketFactory(it, SSLManager.TrustAllCerts())
            builder.hostnameVerifier(SSLManager.hostnameVerifier)
        }

        return builder.build()
    }

    private fun initRetrofit(config: InitRetrofitConfig, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(initGsonBuilder().create()))
            .build()
    }

    /**
     * 获取 Retrofit 实例
     */
    fun getRetrofit(): Retrofit? = retrofit

    /**
     * 获取 API 服务实例
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getApi(retrofitClass: Class<T>): T {
        return retrofit?.create(retrofitClass)
            ?: throw IllegalStateException("Retrofit not initialized! Call HttpClientUtils.initConfig() first.")
    }

    /**
     * Retrofit 配置类
     */
    class InitRetrofitConfig {
        var context: Context? = null
        var baseUrl: String = "https://www.example.com/"
        var timeOut: Long = 30L
        var isRelease: Boolean = true
        var interceptors: ArrayList<Interceptor> = ArrayList()
    }
}
