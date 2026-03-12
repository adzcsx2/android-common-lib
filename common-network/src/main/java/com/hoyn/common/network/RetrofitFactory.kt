package com.hoyn.common.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit 工厂类
 */
object RetrofitFactory {

    private val retrofits = mutableMapOf<String, Retrofit>()

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

    fun <T> createService(
        baseUrl: String,
        serviceClass: Class<T>,
        okHttpClient: OkHttpClient = OkHttpClientFactory.create()
    ): T {
        return create(baseUrl, okHttpClient).create(serviceClass)
    }

    inline fun <reified T> createService(
        baseUrl: String,
        okHttpClient: OkHttpClient = OkHttpClientFactory.create()
    ): T {
        return createService(baseUrl, T::class.java, okHttpClient)
    }
}
