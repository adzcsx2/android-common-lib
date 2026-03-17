package com.hoyn.common.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit 工厂类
 *
 * 提供 Retrofit 实例的创建和缓存
 * 使用缓存机制避免重复创建相同 baseUrl 的 Retrofit 实例
 */
object RetrofitFactory {

    /**
     * Retrofit 实例缓存
     * Key 为 baseUrl，Value 为对应的 Retrofit 实例
     */
    private val retrofits = mutableMapOf<String, Retrofit>()

    /**
     * 创建或获取 Retrofit 实例
     *
     * 使用缓存机制，相同 baseUrl 只创建一次
     *
     * @param baseUrl 基础 URL
     * @param okHttpClient OkHttpClient 实例，默认使用工厂创建的实例
     * @return Retrofit 实例
     */
    fun create(
        baseUrl: String,
        okHttpClient: OkHttpClient = OkHttpClientFactory.create()
    ): Retrofit {
        return retrofits.getOrPut(baseUrl) {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    /**
     * 创建 API 服务实例（非泛型）
     *
     * @param baseUrl 基础 URL
     * @param serviceClass API 接口的 Class 对象
     * @param okHttpClient OkHttpClient 实例，默认使用工厂创建的实例
     * @return API 服务实例
     */
    fun <T> createService(
        baseUrl: String,
        serviceClass: Class<T>,
        okHttpClient: OkHttpClient = OkHttpClientFactory.create()
    ): T {
        return create(baseUrl, okHttpClient).create(serviceClass)
    }

    /**
     * 创建 API 服务实例（泛型 inline）
     *
     * 使用 reified 类型参数简化调用
     *
     * @param baseUrl 基础 URL
     * @param okHttpClient OkHttpClient 实例，默认使用工厂创建的实例
     * @return API 服务实例
     */
    inline fun <reified T> createService(
        baseUrl: String,
        okHttpClient: OkHttpClient = OkHttpClientFactory.create()
    ): T {
        return createService(baseUrl, T::class.java, okHttpClient)
    }
}
