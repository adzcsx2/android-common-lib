package com.hoyn.common.base.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
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
) : BaseLiveEvent<SingleLiveEvent.EventPayload<T>>() {

    /** 对外观察者与内部适配观察者的绑定关系映射 */
    private val observerBindings = LinkedHashMap<Observer<T>, ObserverBinding<T>>()
    /** 绑定关系操作锁 */
    private val lock = Any()

    /**
     * 发送事件。
     *
     * @param value 事件携带的值
     */
    fun emit(value: T) {
        post(EventPayload(value))
    }

    /**
     * 发送无参事件。
     *
     * @throws IllegalStateException 未提供 [emptyValueProvider] 时抛出
     */
    fun emit() {
        val provider = emptyValueProvider
            ?: throw IllegalStateException("SingleLiveEvent requires emptyValueProvider for parameterless emit()")
        emit(provider())
    }

    /**
     * 发送空事件（用于不需要携带数据的通知）。
     *
     * @throws IllegalStateException 未提供 [emptyValueProvider] 时抛出
     */
    fun call() {
        emit()
    }

    /**
     * 订阅事件，在 LifecycleOwner 销毁时自动解绑。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 事件回调
     * @return 注册的 Observer 实例
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
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 事件回调
     * @return 注册的 Observer 实例
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
     * 永久订阅，需要手动调用 [removeObserver] 解绑。
     *
     * @param callback 事件回调
     * @return 注册的 Observer 实例
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

    /**
     * 根据值回调创建对外暴露的观察者实例。
     *
     * @param callback 事件回调
     * @return 创建的 Observer 实例
     */
    @JvmName("createValueObserver")
    fun createObserver(callback: (T) -> Unit): Observer<T> {
        return Observer { value -> callback(value) }
    }

    /**
     * 根据对外观察者移除内部适配观察者，并清理绑定关系。
     *
     * @param observer 要移除的 Observer 实例
     */
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

    /**
     * 保存对外观察者与内部适配观察者之间的绑定关系。
     *
     * @param observer 对外暴露的观察者
     * @param adapter 内部适配观察者
     * @param lifecycleOwner 生命周期宿主，null 表示不受生命周期管理
     */
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

    /**
     * 在宿主销毁时清除对外观察者的绑定缓存。
     *
     * @param observer 要清除绑定的观察者
     */
    private fun clearBinding(observer: Observer<T>) {
        synchronized(lock) {
            observerBindings.remove(observer)
        }
    }

    /**
     * 移除为绑定关系附加的生命周期清理监听。
     *
     * @param binding 观察者绑定记录
     */
    private fun removeCleanupObserver(binding: ObserverBinding<T>) {
        val lifecycleOwner = binding.lifecycleOwner ?: return
        val cleanupObserver = binding.cleanupObserver ?: return
        lifecycleOwner.lifecycle.removeObserver(cleanupObserver)
    }

    /**
     * 一次性事件的值包装体，用于复用 BaseLiveEvent 的泛型分发能力。
     *
     * @param T 事件值类型
     * @property value 实际的事件值
     */
    data class EventPayload<T>(
        val value: T
    )

    /**
     * 对外观察者与内部适配观察者之间的绑定记录。
     *
     * @param T 事件值类型
     * @property adapter 内部适配观察者
     * @property lifecycleOwner 生命周期宿主，null 表示不受生命周期管理
     * @property cleanupObserver 附加的生命周期清理监听
     */
    private data class ObserverBinding<T>(
        val adapter: Observer<EventPayload<T>>,
        val lifecycleOwner: LifecycleOwner?,
        val cleanupObserver: LifecycleEventObserver?
    )
}
