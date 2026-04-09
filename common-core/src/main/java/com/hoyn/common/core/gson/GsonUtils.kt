package com.hoyn.common.core.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.hjq.gson.factory.GsonFactory
import com.hjq.gson.factory.ParseExceptionCallback
import java.lang.reflect.Type

/**
 * Gson 工具类，提供全局单例 Gson 实例和常用的序列化/反序列化方法。
 *
 * 基于 GsonFactory 封装，内置以下配置：
 * - 序列化 null 值
 * - 复杂 Map Key 序列化
 * - Double 类型智能格式化（整数值去掉小数点）
 * - 可选的解析异常回调
 *
 * 使用前需在 Application.onCreate() 中调用 [init]，也可不调用直接使用（延迟初始化）。
 */
object GsonUtils {

    /** 同步锁，保证线程安全 */
    private val lock = Any()

    /** 全局 Gson 单例，双重检查锁定初始化 */
    @Volatile
    private var gson: Gson? = null

    /** 解析异常回调处理器 */
    @Volatile
    private var parseExceptionHandler: GsonParseExceptionHandler? = null

    /**
     * 初始化 GsonUtils，创建并缓存全局 Gson 实例
     * @param handler 可选的解析异常回调，为 null 时不注册异常监听
     */
    fun init(handler: GsonParseExceptionHandler? = null) {
        synchronized(lock) {
            parseExceptionHandler = handler
            val configuredGson = createConfiguredGson()
            gson = configuredGson
            GsonFactory.setSingletonGson(configuredGson)
        }
    }

    /**
     * 获取全局 Gson 单例，双重检查锁定保证线程安全
     * @return 全局 Gson 实例
     */
    fun getGson(): Gson {
        gson?.let { return it }
        synchronized(lock) {
            gson?.let { return it }
            val configuredGson = createConfiguredGson()
            gson = configuredGson
            GsonFactory.setSingletonGson(configuredGson)
            return configuredGson
        }
    }

    /**
     * 创建新的 GsonBuilder，已预配置序列化选项和异常回调
     * @return 配置好的 GsonBuilder 实例
     */
    fun newGsonBuilder(): GsonBuilder = synchronized(lock) {
        GsonFactory.setParseExceptionCallback(createParseExceptionCallback(parseExceptionHandler))
        GsonFactory.newGsonBuilder()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(Double::class.java, DOUBLE_JSON_SERIALIZER)
    }

    /** 将对象序列化为 JSON 字符串 */
    fun toJson(src: Any?): String {
        return getGson().toJson(src)
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     * @param json JSON 字符串
     * @param clazz 目标类型
     * @return 反序列化结果，解析失败时返回 null
     */
    fun <T> fromJson(json: String, clazz: Class<T>): T? {
        return runCatching { getGson().fromJson(json, clazz) }.getOrNull()
    }

    /**
     * 将 JSON 字符串反序列化为指定 Type 的对象，用于泛型场景
     * @param json JSON 字符串
     * @param type 目标类型（可通过 object : TypeToken<T>() {}.type 获取）
     * @return 反序列化结果，解析失败时返回 null
     */
    fun <T> fromJson(json: String, type: Type): T? {
        return runCatching { getGson().fromJson<T>(json, type) }.getOrNull()
    }

    /**
     * 将 JSON 字符串反序列化为指定泛型类型的对象（inline reified 便捷方法）
     * @param json JSON 字符串
     * @return 反序列化结果，解析失败时返回 null
     */
    inline fun <reified T> fromJson(json: String): T? {
        return fromJson(json, object : TypeToken<T>() {}.type)
    }

    /** 创建已配置的 Gson 实例 */
    private fun createConfiguredGson(): Gson {
        return newGsonBuilder().create()
    }

    /**
     * 将 [GsonParseExceptionHandler] 适配为 GsonFactory 的 [ParseExceptionCallback]
     * @param handler 业务层异常处理器
     * @return 适配后的回调，handler 为 null 时返回 null
     */
    private fun createParseExceptionCallback(handler: GsonParseExceptionHandler?): ParseExceptionCallback? {
        if (handler == null) {
            return null
        }
        return object : ParseExceptionCallback {
            override fun onParseObjectException(
                typeToken: TypeToken<*>,
                fieldName: String?,
                jsonToken: com.google.gson.stream.JsonToken?
            ) {
                handler.handleGsonParseException(
                    GsonParseExceptionEvent(
                        kind = GsonParseExceptionKind.OBJECT,
                        typeToken = typeToken,
                        fieldName = fieldName,
                        jsonToken = jsonToken
                    )
                )
            }

            override fun onParseListItemException(
                typeToken: TypeToken<*>,
                fieldName: String?,
                listItemJsonToken: com.google.gson.stream.JsonToken?
            ) {
                handler.handleGsonParseException(
                    GsonParseExceptionEvent(
                        kind = GsonParseExceptionKind.LIST_ITEM,
                        typeToken = typeToken,
                        fieldName = fieldName,
                        jsonToken = listItemJsonToken
                    )
                )
            }

            override fun onParseMapItemException(
                typeToken: TypeToken<*>,
                fieldName: String?,
                mapItemKey: String?,
                mapItemJsonToken: com.google.gson.stream.JsonToken?
            ) {
                handler.handleGsonParseException(
                    GsonParseExceptionEvent(
                        kind = GsonParseExceptionKind.MAP_ITEM,
                        typeToken = typeToken,
                        fieldName = fieldName,
                        jsonToken = mapItemJsonToken,
                        mapItemKey = mapItemKey
                    )
                )
            }
        }
    }

    /** Double 序列化器：整数值（如 1.0）序列化为 Long（输出 "1" 而非 "1.0"） */
    private val DOUBLE_JSON_SERIALIZER = JsonSerializer<Double> { src, _, _ ->
        if (src != null && src == src.toLong().toDouble()) {
            JsonPrimitive(src.toLong())
        } else {
            JsonPrimitive(src)
        }
    }
}
