package com.hoyn.common.network

/**
 * 与业务项目中常见的 BaseResponse 命名保持一致。
 *
 * 该类型与 [ApiResponse] 完全等价，推荐在 Service 接口中优先使用 BaseResponse 命名。
 */
typealias BaseResponse<T> = ApiResponse<T>
