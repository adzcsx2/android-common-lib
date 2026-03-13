package com.hoyn.common.ui.event

import com.hoyn.common.core.Message
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 全局事件总线
 *
 * 使用 SharedFlow 替代 LiveEventBus，提供轻量级的全局事件通信
 */
object GlobalEventBus {

    private val messageFlow = MutableSharedFlow<Message>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val errorFlow = MutableSharedFlow<Throwable>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * 发送消息事件
     */
    fun sendMessage(message: Message) {
        messageFlow.tryEmit(message)
    }

    /**
     * 发送消息事件（简化版）
     */
    fun sendMessage(code: Int, msg: String) {
        messageFlow.tryEmit(Message(code, msg))
    }

    /**
     * 发送错误事件
     */
    fun sendError(throwable: Throwable) {
        errorFlow.tryEmit(throwable)
    }

    /**
     * 观察消息事件
     */
    fun observeMessage(): SharedFlow<Message> = messageFlow.asSharedFlow()

    /**
     * 观察错误事件
     */
    fun observeError(): SharedFlow<Throwable> = errorFlow.asSharedFlow()
}
