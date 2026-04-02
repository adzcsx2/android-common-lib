package com.hoyn.common.base.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 * 全局 LiveEvent。
 */
object GlobalLiveEvent : BaseLiveEvent<Message>() {

    /** 全局错误事件通道，独立于普通消息事件 */
    private val errorLiveEvent = ErrorLiveEvent()

    /**
     * 发送全局消息事件。
     *
     * @param message 要发送的消息事件
     */
    fun sendMessage(message: Message) = post(message)

    /**
     * 根据消息码和文案组装后发送全局消息事件。
     *
     * @param code 消息码
     * @param msg 消息文案
     */
    fun sendMessage(code: Int, msg: String) = post(Message(code, msg))

    /**
     * 延迟发送全局消息事件。
     *
     * @param message 要发送的消息事件
     * @param delay 延迟时间（毫秒）
     */
    fun sendMessageDelay(message: Message, delay: Long) = postDelay(message, delay)

    /**
     * 根据消息码和文案组装后延迟发送全局消息事件。
     *
     * @param code 消息码
     * @param msg 消息文案
     * @param delay 延迟时间（毫秒）
     */
    fun sendMessageDelay(code: Int, msg: String, delay: Long) =
        postDelay(Message(code, msg), delay)

    /**
     * 仅当发送方仍处于激活状态时延迟发送全局消息事件。
     *
     * @param sender 发送方的 LifecycleOwner
     * @param message 要发送的消息事件
     * @param delay 延迟时间（毫秒）
     */
    fun sendMessageDelay(sender: LifecycleOwner, message: Message, delay: Long) =
        postDelay(sender, message, delay)

    /**
     * 注册全局消息观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 消息事件回调
     * @return 注册的 Observer 实例
     */
    fun observeMessage(
        lifecycleOwner: LifecycleOwner,
        callback: (msg: Message) -> Unit
    ) = observe(lifecycleOwner, callback)

    /**
     * 注册仅在 STARTED 及以上状态接收的全局消息观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 消息事件回调
     * @return 注册的 Observer 实例
     */
    fun observeMessageWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (msg: Message) -> Unit
    ) = observeWithLifecycle(lifecycleOwner, callback)

    /**
     * 注册可立即收到最近一次消息的粘性观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 消息事件回调
     * @return 注册的 Observer 实例
     */
    fun observeStickyMessage(
        lifecycleOwner: LifecycleOwner,
        callback: (msg: Message) -> Unit
    ) = observeSticky(lifecycleOwner, callback)

    /**
     * 注册仅在 STARTED 及以上状态接收的粘性消息观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 消息事件回调
     * @return 注册的 Observer 实例
     */
    fun observeStickyMessageWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (msg: Message) -> Unit
    ) = observeStickyWithLifecycle(lifecycleOwner, callback)

    /**
     * 创建全局消息观察者实例。
     *
     * @param callback 消息事件回调
     * @return 创建的 Observer 实例
     */
    fun createMessageObserver(callback: (msg: Message) -> Unit): Observer<Message> =
        createObserver(callback)

    /**
     * 注册永久生效的全局消息观察者，需要手动调用 [removeMessageObserver] 解绑。
     *
     * @param observer 要注册的 Observer 实例
     */
    fun observeMessageForever(observer: Observer<Message>) = observeForever(observer)

    /**
     * 通过回调创建并注册永久生效的全局消息观察者。
     *
     * @param callback 消息事件回调
     * @return 注册的 Observer 实例
     */
    fun subscribeMessage(callback: (msg: Message) -> Unit): Observer<Message> {
        val observer = createMessageObserver(callback)
        observeMessageForever(observer)
        return observer
    }

    /**
     * 注册永久生效的粘性消息观察者，需要手动调用 [removeMessageObserver] 解绑。
     *
     * @param observer 要注册的 Observer 实例
     */
    fun observeStickyMessageForever(observer: Observer<Message>) = observeStickyForever(observer)

    /**
     * 通过回调创建并注册永久生效的粘性消息观察者。
     *
     * @param callback 消息事件回调
     * @return 注册的 Observer 实例
     */
    fun subscribeStickyMessage(callback: (msg: Message) -> Unit): Observer<Message> {
        val observer = createMessageObserver(callback)
        observeStickyMessageForever(observer)
        return observer
    }

    /**
     * 手动移除全局消息观察者。
     *
     * @param observer 要移除的 Observer 实例
     */
    fun removeMessageObserver(observer: Observer<Message>) = removeObserver(observer)

    /**
     * 发送全局错误事件。
     *
     * @param throwable 要发送的错误对象
     */
    fun sendError(throwable: Throwable) = errorLiveEvent.send(throwable)

    /**
     * 注册全局错误观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 错误事件回调
     * @return 注册的 Observer 实例
     */
    fun observeError(
        lifecycleOwner: LifecycleOwner,
        callback: (throwable: Throwable) -> Unit
    ) = errorLiveEvent.observeError(lifecycleOwner, callback)

    /**
     * 注册仅在 STARTED 及以上状态接收的全局错误观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 错误事件回调
     * @return 注册的 Observer 实例
     */
    fun observeErrorWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (throwable: Throwable) -> Unit
    ) = errorLiveEvent.observeErrorWithLifecycle(lifecycleOwner, callback)

    /**
     * 注册可立即收到最近一次错误的粘性观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 错误事件回调
     * @return 注册的 Observer 实例
     */
    fun observeStickyError(
        lifecycleOwner: LifecycleOwner,
        callback: (throwable: Throwable) -> Unit
    ) = errorLiveEvent.observeStickyError(lifecycleOwner, callback)

    /**
     * 创建全局错误观察者实例。
     *
     * @param callback 错误事件回调
     * @return 创建的 Observer 实例
     */
    fun createErrorObserver(callback: (throwable: Throwable) -> Unit): Observer<Throwable> =
        errorLiveEvent.createErrorObserver(callback)

    /**
     * 注册永久生效的全局错误观察者，需要手动调用 [removeErrorObserver] 解绑。
     *
     * @param observer 要注册的 Observer 实例
     */
    fun observeErrorForever(observer: Observer<Throwable>) =
        errorLiveEvent.observeErrorForever(observer)

    /**
     * 通过回调创建并注册永久生效的全局错误观察者。
     *
     * @param callback 错误事件回调
     * @return 注册的 Observer 实例
     */
    fun subscribeError(callback: (throwable: Throwable) -> Unit): Observer<Throwable> {
        val observer = createErrorObserver(callback)
        observeErrorForever(observer)
        return observer
    }

    /**
     * 注册永久生效的粘性错误观察者，需要手动调用 [removeErrorObserver] 解绑。
     *
     * @param observer 要注册的 Observer 实例
     */
    fun observeStickyErrorForever(observer: Observer<Throwable>) =
        errorLiveEvent.observeStickyErrorForever(observer)

    /**
     * 通过回调创建并注册永久生效的粘性错误观察者。
     *
     * @param callback 错误事件回调
     * @return 注册的 Observer 实例
     */
    fun subscribeStickyError(callback: (throwable: Throwable) -> Unit): Observer<Throwable> {
        val observer = createErrorObserver(callback)
        observeStickyErrorForever(observer)
        return observer
    }

    /**
     * 手动移除全局错误观察者。
     *
     * @param observer 要移除的 Observer 实例
     */
    fun removeErrorObserver(observer: Observer<Throwable>) =
        errorLiveEvent.removeErrorObserver(observer)

    /**
     * 全局错误事件通道，独立于普通消息事件，提供错误相关的订阅/发布能力。
     */
    private class ErrorLiveEvent : BaseLiveEvent<Throwable>() {
        /**
         * 发送错误事件。
         *
         * @param throwable 要发送的错误对象
         */
        fun send(throwable: Throwable) = super.post(throwable)

        /**
         * 注册错误观察者。
         *
         * @param lifecycleOwner 观察者的生命周期宿主
         * @param callback 错误事件回调
         * @return 注册的 Observer 实例
         */
        fun observeError(
            lifecycleOwner: LifecycleOwner,
            callback: (throwable: Throwable) -> Unit
        ) = observe(lifecycleOwner, callback)

        /**
         * 注册仅在 STARTED 及以上状态接收的错误观察者。
         *
         * @param lifecycleOwner 观察者的生命周期宿主
         * @param callback 错误事件回调
         * @return 注册的 Observer 实例
         */
        fun observeErrorWithLifecycle(
            lifecycleOwner: LifecycleOwner,
            callback: (throwable: Throwable) -> Unit
        ) = observeWithLifecycle(lifecycleOwner, callback)

        /**
         * 注册可立即收到最近一次错误的粘性观察者。
         *
         * @param lifecycleOwner 观察者的生命周期宿主
         * @param callback 错误事件回调
         * @return 注册的 Observer 实例
         */
        fun observeStickyError(
            lifecycleOwner: LifecycleOwner,
            callback: (throwable: Throwable) -> Unit
        ) = observeSticky(lifecycleOwner, callback)

        /**
         * 创建错误观察者实例。
         *
         * @param callback 错误事件回调
         * @return 创建的 Observer 实例
         */
        fun createErrorObserver(callback: (throwable: Throwable) -> Unit): Observer<Throwable> =
            createObserver(callback)

        /**
         * 注册永久生效的错误观察者，需要手动解绑。
         *
         * @param observer 要注册的 Observer 实例
         */
        fun observeErrorForever(observer: Observer<Throwable>) = observeForever(observer)

        /**
         * 注册永久生效的粘性错误观察者，需要手动解绑。
         *
         * @param observer 要注册的 Observer 实例
         */
        fun observeStickyErrorForever(observer: Observer<Throwable>) =
            observeStickyForever(observer)

        /**
         * 手动移除错误观察者。
         *
         * @param observer 要移除的 Observer 实例
         */
        fun removeErrorObserver(observer: Observer<Throwable>) = removeObserver(observer)
    }
}
