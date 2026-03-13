package com.hoyn.common.core

/**
 * 通用 API 响应接口
 *
 * 所有 API 响应数据类应实现此接口，以便统一处理响应结果
 */
interface IBaseResponse<T> {
    /**
     * 响应状态码
     */
    fun code(): Int

    /**
     * 响应消息
     */
    fun msg(): String

    /**
     * 响应数据
     */
    fun data(): T?

    /**
     * 是否成功
     * 默认判断 code 为 0 表示成功，子类可重写
     */
    fun isSuccess(): Boolean = code() == 0
}
