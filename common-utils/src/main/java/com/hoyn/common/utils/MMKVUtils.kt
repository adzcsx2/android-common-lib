package com.hoyn.common.utils

import android.content.Context
import com.google.gson.Gson
import com.tencent.mmkv.MMKV

/**
 * MMKV 工具类
 * 基于腾讯 MMKV 的高性能键值存储工具
 *
 * 使用前需要在 Application 中调用 MMKVUtils.init(context)
 *
 * @author hoyn
 */
object MMKVUtils {

    private var mmkv: MMKV? = null
    private val gson = Gson()

    /**
     * 初始化 MMKV
     * 需要在 Application 中调用
     */
    fun init(context: Context) {
        MMKV.initialize(context)
        mmkv = MMKV.defaultMMKV()
    }

    /**
     * 获取 MMKV 实例
     */
    fun getInstance(): MMKV {
        return mmkv ?: throw IllegalStateException("MMKVUtils not initialized! Call MMKVUtils.init(context) in your Application.")
    }

    // ==================== String ====================

    /**
     * 保存 String
     */
    fun put(key: String, value: String?) {
        getInstance().encode(key, value)
    }

    /**
     * 获取 String
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return getInstance().decodeString(key, defaultValue) ?: defaultValue
    }

    // ==================== Int ====================

    /**
     * 保存 Int
     */
    fun put(key: String, value: Int) {
        getInstance().encode(key, value)
    }

    /**
     * 获取 Int
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getInstance().decodeInt(key, defaultValue)
    }

    // ==================== Long ====================

    /**
     * 保存 Long
     */
    fun put(key: String, value: Long) {
        getInstance().encode(key, value)
    }

    /**
     * 获取 Long
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getInstance().decodeLong(key, defaultValue)
    }

    // ==================== Float ====================

    /**
     * 保存 Float
     */
    fun put(key: String, value: Float) {
        getInstance().encode(key, value)
    }

    /**
     * 获取 Float
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return getInstance().decodeFloat(key, defaultValue)
    }

    // ==================== Double ====================

    /**
     * 保存 Double
     */
    fun put(key: String, value: Double) {
        getInstance().encode(key, value)
    }

    /**
     * 获取 Double
     */
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return getInstance().decodeDouble(key, defaultValue)
    }

    // ==================== Boolean ====================

    /**
     * 保存 Boolean
     */
    fun put(key: String, value: Boolean) {
        getInstance().encode(key, value)
    }

    /**
     * 获取 Boolean
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getInstance().decodeBool(key, defaultValue)
    }

    // ==================== ByteArray ====================

    /**
     * 保存 ByteArray
     */
    fun put(key: String, value: ByteArray) {
        getInstance().encode(key, value)
    }

    /**
     * 获取 ByteArray
     */
    fun getBytes(key: String): ByteArray? {
        return getInstance().decodeBytes(key)
    }

    // ==================== Set ====================

    /**
     * 保存 StringSet
     */
    fun put(key: String, value: Set<String>?) {
        getInstance().encode(key, value)
    }

    /**
     * 获取 StringSet
     */
    fun getStringSet(key: String): Set<String>? {
        return getInstance().decodeStringSet(key, null)
    }

    // ==================== 通用操作 ====================

    /**
     * 移除指定 key
     */
    fun remove(key: String) {
        getInstance().removeValueForKey(key)
    }

    /**
     * 移除多个 key
     */
    fun remove(vararg keys: String) {
        keys.forEach { getInstance().removeValueForKey(it) }
    }

    /**
     * 清除所有数据
     */
    fun clearAll() {
        getInstance().clearAll()
    }

    /**
     * 检查 key 是否存在
     */
    fun contains(key: String): Boolean {
        return getInstance().containsKey(key)
    }

    /**
     * 获取所有 key
     */
    fun getAllKeys(): Array<String>? {
        return getInstance().allKeys()
    }

    /**
     * 获取存储的数据总数
     */
    fun count(): Long {
        return getInstance().count()
    }

    // ==================== 扩展方法：对象存储 ====================

    /**
     * 保存对象（通过 JSON 序列化）
     */
    fun <T> putObject(key: String, obj: T?) {
        if (obj == null) {
            remove(key)
        } else {
            val json = gson.toJson(obj)
            put(key, json)
        }
    }

    /**
     * 获取对象（通过 JSON 反序列化）
     */
    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val json = getString(key)
        if (json.isEmpty()) return null
        return try {
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== 便捷方法 ====================

    /**
     * 保存可空字符串（空字符串会被移除）
     */
    fun putNullableString(key: String, value: String?) {
        if (value.isNullOrEmpty()) {
            remove(key)
        } else {
            put(key, value)
        }
    }

    /**
     * 增加Int值
     */
    fun increment(key: String, delta: Int = 1): Int {
        val newValue = getInt(key) + delta
        put(key, newValue)
        return newValue
    }

    /**
     * 减少Int值
     */
    fun decrement(key: String, delta: Int = 1): Int {
        return increment(key, -delta)
    }
}
