package com.hoyn.common.network

import com.hoyn.common.core.IBaseResponse

/**
 * 通用 API 响应封装
 * 实现 IBaseResponse 接口以支持统一的响应处理
 */
data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) : IBaseResponse<T> {

    override fun code(): Int = code

    override fun msg(): String = message

    override fun data(): T? = data

    override fun isSuccess(): Boolean = code == 0

    companion object {
        fun <T> success(data: T?): ApiResponse<T> {
            return ApiResponse(code = 0, message = "success", data = data)
        }

        fun <T> error(code: Int, message: String): ApiResponse<T> {
            return ApiResponse(code = code, message = message, data = null)
        }
    }
}
