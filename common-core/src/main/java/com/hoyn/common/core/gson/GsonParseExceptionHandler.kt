package com.hoyn.common.core.gson

/**
 * Gson 解析异常回调接口（SAM）。
 *
 * 当 Gson 在反序列化过程中遇到类型不匹配等解析异常时，
 * 通过此接口通知上层处理（如日志记录、数据上报等）。
 */
fun interface GsonParseExceptionHandler {
    /**
     * 处理 Gson 解析异常
     * @param event 解析异常事件，包含异常类型、字段名等上下文信息
     */
    fun handleGsonParseException(event: GsonParseExceptionEvent)
}
