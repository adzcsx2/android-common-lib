package com.hoyn.common.base.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hoyn.common.core.Message

private const val TAG_MESSAGE = "tag_global_message"
private const val TAG_ERROR = "tag_global_error"

/**
 * 全局 LiveEvent。
 */
object GlobalLiveEvent : BaseLiveEvent<Message>(TAG_MESSAGE, Message::class.java) {

    private val errorLiveEvent = ErrorLiveEvent()

    fun sendMessage(message: Message) = post(message)

    fun sendMessage(code: Int, msg: String) = post(Message(code, msg))

    fun sendMessageDelay(message: Message, delay: Long) = postDelay(message, delay)

    fun sendMessageDelay(code: Int, msg: String, delay: Long) =
        postDelay(Message(code, msg), delay)

    fun sendMessageDelay(sender: LifecycleOwner, message: Message, delay: Long) =
        postDelay(sender, message, delay)

    fun observeMessage(
        lifecycleOwner: LifecycleOwner,
        callback: (msg: Message) -> Unit
    ) = observe(lifecycleOwner, callback)

    fun observeMessageWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (msg: Message) -> Unit
    ) = observeWithLifecycle(lifecycleOwner, callback)

    fun observeStickyMessage(
        lifecycleOwner: LifecycleOwner,
        callback: (msg: Message) -> Unit
    ) = observeSticky(lifecycleOwner, callback)

    fun observeStickyMessageWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (msg: Message) -> Unit
    ) = observeStickyWithLifecycle(lifecycleOwner, callback)

    fun createMessageObserver(callback: (msg: Message) -> Unit): Observer<Message> =
        createObserver(callback)

    fun observeMessageForever(observer: Observer<Message>) = observeForever(observer)

    fun subscribeMessage(callback: (msg: Message) -> Unit): Observer<Message> {
        val observer = createMessageObserver(callback)
        observeMessageForever(observer)
        return observer
    }

    fun observeStickyMessageForever(observer: Observer<Message>) = observeStickyForever(observer)

    fun subscribeStickyMessage(callback: (msg: Message) -> Unit): Observer<Message> {
        val observer = createMessageObserver(callback)
        observeStickyMessageForever(observer)
        return observer
    }

    fun removeMessageObserver(observer: Observer<Message>) = removeObserver(observer)

    fun sendError(throwable: Throwable) = errorLiveEvent.send(throwable)

    fun observeError(
        lifecycleOwner: LifecycleOwner,
        callback: (throwable: Throwable) -> Unit
    ) = errorLiveEvent.observeError(lifecycleOwner, callback)

    fun observeErrorWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (throwable: Throwable) -> Unit
    ) = errorLiveEvent.observeErrorWithLifecycle(lifecycleOwner, callback)

    fun observeStickyError(
        lifecycleOwner: LifecycleOwner,
        callback: (throwable: Throwable) -> Unit
    ) = errorLiveEvent.observeStickyError(lifecycleOwner, callback)

    fun createErrorObserver(callback: (throwable: Throwable) -> Unit): Observer<Throwable> =
        errorLiveEvent.createErrorObserver(callback)

    fun observeErrorForever(observer: Observer<Throwable>) = errorLiveEvent.observeErrorForever(observer)

    fun subscribeError(callback: (throwable: Throwable) -> Unit): Observer<Throwable> {
        val observer = createErrorObserver(callback)
        observeErrorForever(observer)
        return observer
    }

    fun observeStickyErrorForever(observer: Observer<Throwable>) = errorLiveEvent.observeStickyErrorForever(observer)

    fun subscribeStickyError(callback: (throwable: Throwable) -> Unit): Observer<Throwable> {
        val observer = createErrorObserver(callback)
        observeStickyErrorForever(observer)
        return observer
    }

    fun removeErrorObserver(observer: Observer<Throwable>) = errorLiveEvent.removeErrorObserver(observer)

    private class ErrorLiveEvent : BaseLiveEvent<Throwable>(TAG_ERROR, Throwable::class.java) {
        fun send(throwable: Throwable) = super.post(throwable)

        fun observeError(
            lifecycleOwner: LifecycleOwner,
            callback: (throwable: Throwable) -> Unit
        ) = observe(lifecycleOwner, callback)

        fun observeErrorWithLifecycle(
            lifecycleOwner: LifecycleOwner,
            callback: (throwable: Throwable) -> Unit
        ) = observeWithLifecycle(lifecycleOwner, callback)

        fun observeStickyError(
            lifecycleOwner: LifecycleOwner,
            callback: (throwable: Throwable) -> Unit
        ) = observeSticky(lifecycleOwner, callback)

        fun createErrorObserver(callback: (throwable: Throwable) -> Unit): Observer<Throwable> =
            createObserver(callback)

        fun observeErrorForever(observer: Observer<Throwable>) = observeForever(observer)

        fun observeStickyErrorForever(observer: Observer<Throwable>) = observeStickyForever(observer)

        fun removeErrorObserver(observer: Observer<Throwable>) = removeObserver(observer)
    }
}
