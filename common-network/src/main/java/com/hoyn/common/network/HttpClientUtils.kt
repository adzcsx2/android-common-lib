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
 *
 * 用于配置和创建 Retrofit 客户端
 * 提供统一的网络请求初始化和配置
 */
object HttpClientUtils {

    /**
     * Retrofit 实例
     */
    private var retrofit: Retrofit? = null

    /**
     * Retrofit 配置
     */
    private var config: InitRetrofitConfig? = null

    /**
     * 初始化 Gson Builder
     *
     * 配置 Gson 的序列化选项，包括：
     * - 序列化 null 值
     * - 启用复杂 Map Key 序列化
     * - Double 类型优化（整数不显示小数点）
     *
     * @return 配置好的 GsonBuilder
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
     *
     * 根据配置创建 OkHttpClient 和 Retrofit 实例
     *
     * @param config Retrofit 配置对象
     */
    fun initConfig(config: InitRetrofitConfig) {
        this.config = config
        config.context?.let {
            val okHttpClient = initOkHttp(config)
            retrofit = initRetrofit(config, okHttpClient)
        }
    }

    /**
     * 初始化 OkHttpClient
     *
     * 配置超时时间、连接池、拦截器、SSL 等
     *
     * @param config Retrofit 配置对象
     * @return 配置好的 OkHttpClient 实例
     */
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

    /**
     * 初始化 Retrofit
     *
     * 使用配置的 baseUrl、OkHttpClient 和 GsonConverterFactory
     *
     * @param config Retrofit 配置对象
     * @param okHttpClient OkHttpClient 实例
     * @return 配置好的 Retrofit 实例
     */
    private fun initRetrofit(config: InitRetrofitConfig, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(initGsonBuilder().create()))
            .build()
    }

    /**
     * 获取 Retrofit 实例
     *
     * @return Retrofit 实例，如果未初始化则返回 null
     */
    fun getRetrofit(): Retrofit? = retrofit

    /**
     * 获取 API 服务实例
     *
     * @param retrofitClass API 接口的 Class 对象
     * @return API 服务实例
     * @throws IllegalStateException 如果 Retrofit 未初始化
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getApi(retrofitClass: Class<T>): T {
        return retrofit?.create(retrofitClass)
            ?: throw IllegalStateException("Retrofit not initialized! Call HttpClientUtils.initConfig() first.")
    }

    /**
     * Retrofit 配置类
     *
     * 用于配置 Retrofit 的各项参数
     */
    class InitRetrofitConfig {
        /**
         * Context 实例
         */
        var context: Context? = null

        /**
         * 基础 URL
         */
        var baseUrl: String = "https://www.example.com/"

        /**
         * 超时时间（秒）
         */
        var timeOut: Long = 30L

        /**
         * 是否为正式发布版本
         * 正式版会禁用抓包和日志
         */
        var isRelease: Boolean = true

        /**
         * 自定义拦截器列表
         */
        var interceptors: ArrayList<Interceptor> = ArrayList()
    }
}
