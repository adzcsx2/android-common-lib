package com.hoyn.common.base.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicLong
import kotlin.jvm.JvmName

/**
 * 一次性事件流
 *
 * 使用 BaseLiveEvent 实现一次性事件分发。
 *
 * 默认不支持粘性事件，新订阅者不会收到订阅前发送的事件。
 * 对于无参事件，可通过 emptyValueProvider 提供默认值。
 */
class SingleLiveEvent<T>(
    private val emptyValueProvider: (() -> T)? = null
) : BaseLiveEvent<SingleLiveEvent.EventPayload<T>>(
    tag = nextTag(),
    eventClass = payloadClass()
) {

    private val observerBindings = LinkedHashMap<Observer<T>, ObserverBinding<T>>()
    private val lock = Any()

    companion object {
        private val nextId = AtomicLong(0L)

        @Suppress("UNCHECKED_CAST")
        private fun <T> payloadClass(): Class<EventPayload<T>> {
            return EventPayload::class.java as Class<EventPayload<T>>
        }

        private fun nextTag(): String = "single_live_event_${nextId.incrementAndGet()}"
    }

    /**
     * 发送事件
     */
    fun emit(value: T) {
        post(EventPayload(value))
    }

    /**
     * 发送无参事件。
     */
    fun emit() {
        val provider = emptyValueProvider
            ?: throw IllegalStateException("SingleLiveEvent requires emptyValueProvider for parameterless emit()")
        emit(provider())
    }

    /**
     * 发送空事件（用于不需要携带数据的通知）
     */
    fun call() {
        emit()
    }

    /**
     * 订阅事件，在 LifecycleOwner 销毁时自动解绑。
     */
    @JvmName("observeValue")
    fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (T) -> Unit
    ): Observer<T> {
        val observer = createObserver(callback)
        val adapter = super.observe(lifecycleOwner) { payload ->
            observer.onChanged(payload.value)
        }
        storeBinding(observer, adapter, lifecycleOwner)
        return observer
    }

    /**
     * 订阅事件，仅在 STARTED 及以上状态接收。
     */
    @JvmName("observeValueWithLifecycle")
    fun observeWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (T) -> Unit
    ): Observer<T> {
        val observer = createObserver(callback)
        val adapter = super.observeWithLifecycle(lifecycleOwner) { payload ->
            observer.onChanged(payload.value)
        }
        storeBinding(observer, adapter, lifecycleOwner)
        return observer
    }

    /**
     * 永久订阅，需要手动解绑。
     */
    fun observeForever(callback: (T) -> Unit): Observer<T> {
        val observer = createObserver(callback)
        val adapter = Observer<EventPayload<T>> { payload ->
            observer.onChanged(payload.value)
        }
        super.observeForever(adapter)
        storeBinding(observer, adapter, null)
        return observer
    }

    @JvmName("createValueObserver")
    fun createObserver(callback: (T) -> Unit): Observer<T> {
        return Observer { value -> callback(value) }
    }

    @JvmName("removeValueObserver")
    fun removeObserver(observer: Observer<T>) {
        runOnMainThreadAndWait {
            val binding = synchronized(lock) {
                observerBindings.remove(observer)
            } ?: return@runOnMainThreadAndWait
            removeCleanupObserver(binding)
            super.removeObserver(binding.adapter)
        }
    }

    private fun storeBinding(
        observer: Observer<T>,
        adapter: Observer<EventPayload<T>>,
        lifecycleOwner: LifecycleOwner?
    ) {
        runOnMainThreadAndWait {
            val previous = synchronized(lock) {
                observerBindings.remove(observer)
            }
            if (previous != null) {
                removeCleanupObserver(previous)
                super.removeObserver(previous.adapter)
            }

            val cleanupObserver = lifecycleOwner?.let { owner ->
                LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        clearBinding(observer)
                    }
                }.also { owner.lifecycle.addObserver(it) }
            }

            synchronized(lock) {
                observerBindings[observer] = ObserverBinding(
                    adapter = adapter,
                    lifecycleOwner = lifecycleOwner,
                    cleanupObserver = cleanupObserver
                )
            }
        }
    }

    private fun clearBinding(observer: Observer<T>) {
        synchronized(lock) {
            observerBindings.remove(observer)
        }
    }

    private fun removeCleanupObserver(binding: ObserverBinding<T>) {
        val lifecycleOwner = binding.lifecycleOwner ?: return
        val cleanupObserver = binding.cleanupObserver ?: return
        lifecycleOwner.lifecycle.removeObserver(cleanupObserver)
    }

    data class EventPayload<T>(
        val value: T
    )

    private data class ObserverBinding<T>(
        val adapter: Observer<EventPayload<T>>,
        val lifecycleOwner: LifecycleOwner?,
        val cleanupObserver: LifecycleEventObserver?
    )
}
