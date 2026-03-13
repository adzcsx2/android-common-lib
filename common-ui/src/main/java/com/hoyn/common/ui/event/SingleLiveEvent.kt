package com.hoyn.common.ui.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 一次性事件流
 *
 * 替代传统的 SingleLiveEvent，使用 SharedFlow 实现
 * 特点：
 * - replay = 0：新订阅者不会收到之前发送的事件
 * - extraBufferCapacity = 1：允许在没有订阅者时缓存一个事件
 * - onBufferOverflow = DROP_OLDEST：缓冲区满时丢弃最旧的事件
 */
class SingleLiveEvent<T> {

    private val flow = MutableSharedFlow<T>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * 发送事件
     */
    fun emit(value: T) {
        flow.tryEmit(value)
    }

    /**
     * 发送事件（无参数版本，用于 Unit 或可空类型）
     */
    fun emit() {
        @Suppress("UNCHECKED_CAST")
        flow.tryEmit(null as T)
    }

    /**
     * 发送空事件（用于不需要携带数据的通知）
     */
    fun call() {
        @Suppress("UNCHECKED_CAST")
        flow.tryEmit(null as T)
    }

    /**
     * 获取只读的 SharedFlow
     */
    fun asSharedFlow(): SharedFlow<T> = flow.asSharedFlow()
}
