package com.hoyn.common.core

/**
 * UI 状态封装
 *
 * 使用 sealed class 表示页面可能的状态，便于在 ViewModel 中管理 UI 状态
 * 支持 Compose 预留扩展点
 */
sealed class UIState<out T> {

    /**
     * 加载中状态
     */
    data object Loading : UIState<Nothing>()

    /**
     * 成功状态
     * @param data 成功返回的数据
     */
    data class Success<T>(val data: T) : UIState<T>()

    /**
     * 错误状态
     * @param code 错误码
     * @param message 错误信息
     */
    data class Error(val code: Int, val message: String) : UIState<Nothing>()

    /**
     * 空数据状态
     */
    data object Empty : UIState<Nothing>()

    /**
     * 是否正在加载
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * 是否成功
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 是否错误
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 是否为空
     */
    val isEmpty: Boolean
        get() = this is Empty

    /**
     * 获取数据，如果不是 Success 则返回 null
     */
    fun getDataOrNull(): T? = (this as? Success)?.data

    /**
     * 获取错误信息，如果不是 Error 则返回 null
     */
    fun getErrorMsgOrNull(): String? = (this as? Error)?.message

    companion object {
        /**
         * 创建加载状态
         */
        fun loading(): UIState<Nothing> = Loading

        /**
         * 创建成功状态
         */
        fun <T> success(data: T): UIState<T> = Success(data)

        /**
         * 创建错误状态
         */
        fun error(code: Int, message: String): UIState<Nothing> = Error(code, message)

        /**
         * 创建空状态
         */
        fun empty(): UIState<Nothing> = Empty
    }
}
