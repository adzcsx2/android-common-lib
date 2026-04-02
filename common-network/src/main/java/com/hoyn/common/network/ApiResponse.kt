package com.hoyn.common.network

import com.google.gson.annotations.SerializedName
import com.hoyn.common.core.IBaseResponse

/**
 * 通用 API 响应封装
 *
 * 实现 IBaseResponse 接口以支持统一的响应处理
 * 封装了标准 API 响应的字段：code、message、data
 *
 * @param T 响应数据类型
 * @property code 响应状态码，0 表示成功
 * @property message 响应消息
 * @property data 响应数据，可能为 null
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName(value = "message", alternate = ["msg"])
    val message: String = "",
    @SerializedName("data")
    val data: T? = null
) : IBaseResponse<T> {

    /**
     * 获取响应状态码
     */
    override fun code(): Int = code

    /**
     * 获取响应消息
     */
    override fun msg(): String = message

    /**
     * 获取响应数据
     */
    override fun data(): T? = data

    /**
     * 判断响应是否成功
     *
     * @return true 表示成功（code 为 0），false 表示失败
     */
    override fun isSuccess(): Boolean = code == 0

    companion object {
        /**
         * 创建成功响应
         *
         * @param data 响应数据
         * @return 成功的 ApiResponse 实例
         */
        fun <T> success(data: T?): ApiResponse<T> {
            return ApiResponse(code = 0, message = "success", data = data)
        }

        /**
         * 创建错误响应
         *
         * @param code 错误码
         * @param message 错误消息
         * @return 错误的 ApiResponse 实例
         */
        fun <T> error(code: Int, message: String): ApiResponse<T> {
            return ApiResponse(code = code, message = message, data = null)
        }
    }
}
