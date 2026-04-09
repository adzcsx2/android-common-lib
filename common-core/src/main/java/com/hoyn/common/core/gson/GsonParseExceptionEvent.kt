package com.hoyn.common.core.gson

import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken

/**
 * Gson 解析异常类型枚举
 * - [OBJECT]: 对象字段解析异常
 * - [LIST_ITEM]: 列表元素解析异常
 * - [MAP_ITEM]: Map 元素解析异常
 */
enum class GsonParseExceptionKind {
    OBJECT,
    LIST_ITEM,
    MAP_ITEM
}

/**
 * Gson 解析异常事件，封装解析失败时的上下文信息
 * @param kind 异常类型
 * @param typeToken 解析目标的类型令牌
 * @param fieldName 发生异常的字段名，可能为 null
 * @param jsonToken 解析时遇到的实际 JsonToken 类型
 * @param mapItemKey Map 异常时的键，仅 [GsonParseExceptionKind.MAP_ITEM] 时有值
 */
data class GsonParseExceptionEvent(
    val kind: GsonParseExceptionKind,
    val typeToken: TypeToken<*>,
    val fieldName: String?,
    val jsonToken: JsonToken?,
    val mapItemKey: String? = null
)
