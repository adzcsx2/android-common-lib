package com.hoyn.common.network

/**
 * 通用 API 响应封装
 */
data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) {
    val isSuccess: Boolean
        get() = code == 0

    companion object {
        fun <T> success(data: T?): ApiResponse<T> {
            return ApiResponse(code = 0, message = "success", data = data)
        }

        fun <T> error(code: Int, message: String): ApiResponse<T> {
            return ApiResponse(code = code, message = message, data = null)
        }
    }
}
