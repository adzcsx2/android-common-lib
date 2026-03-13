package com.hoyn.common.network.exception

/**
 * 错误码枚举
 */
enum class ERROR(private val code: String) {
    /**
     * 未知错误
     */
    UNKNOWN("000000"),

    /**
     * 解析错误
     */
    PARSE_ERROR("000001"),

    /**
     * 网络错误
     */
    NETWORK_ERROR("000002"),

    /**
     * HTTP错误
     */
    HTTP_ERROR("000003"),

    /**
     * SSL证书错误
     */
    SSL_ERROR("000004"),

    /**
     * 连接超时
     */
    TIMEOUT_ERROR("000005"),

    /**
     * 服务器内部错误
     */
    SERVER_ERROR("000006"),

    /**
     * 空数据
     */
    EMPTY_DATA("000007"),

    /**
     * Token过期
     */
    TOKEN_EXPIRED("000008"),

    /**
     * 未授权
     */
    UNAUTHORIZED("000009");

    fun getKey(): String = code
    fun getValue(): String = name
}
