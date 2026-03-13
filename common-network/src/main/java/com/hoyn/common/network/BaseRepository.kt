package com.hoyn.common.network

/**
 * BaseRepository
 *
 * 数据层基类，提供 Retrofit API 访问入口
 * 子类应继承此类并提供具体的 API 接口类型
 *
 * 使用示例：
 * ```kotlin
 * class UserRepository : BaseRepository<UserApi>() {
 *     override val api: UserApi by lazy { createApi("https://api.example.com") }
 * }
 * ```
 */
abstract class BaseRepository<T> {

    /**
     * API 接口实例
     * 子类需要重写此属性以提供具体的 API 实现
     */
    protected abstract val api: T

    /**
     * 创建 API 服务
     *
     * @param baseUrl 基础 URL
     * @param serviceClass API 接口的 Class 对象
     * @return API 服务实例
     */
    protected inline fun <S> createApi(baseUrl: String, serviceClass: Class<S>): S {
        return RetrofitFactory.createService(baseUrl, serviceClass)
    }

    /**
     * 创建 API 服务（使用自定义 OkHttpClient）
     *
     * @param baseUrl 基础 URL
     * @param serviceClass API 接口的 Class 对象
     * @param okHttpClient 自定义 OkHttpClient
     * @return API 服务实例
     */
    protected inline fun <S> createApi(
        baseUrl: String,
        serviceClass: Class<S>,
        okHttpClient: okhttp3.OkHttpClient
    ): S {
        return RetrofitFactory.createService(baseUrl, serviceClass, okHttpClient)
    }
}
