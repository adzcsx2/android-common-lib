package com.hoyn.common.base.event

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

/**
 * LiveEvent 基类。
 *
 * 封装应用内 LiveEvent 的通用操作，支持生命周期自动解绑、手动解绑、粘性消息和多线程安全派发。
 */
abstract class BaseLiveEvent<T : Any> {
    /** 主线程 Handler，用于跨线程事件分发 */
    private val mainHandler = Handler(Looper.getMainLooper())
    /** 观察者注册表操作锁 */
    private val lock = Any()
    /** 观察者注册表，保持插入顺序 */
    private val observerStates = LinkedHashMap<Observer<T>, ObserverState<T>>()

    /** 最近一次分发的事件包装，用于粘性事件补发 */
    @Volatile
    private var stickyEvent: EventEnvelope<T>? = null

    /** 下一个事件的版本号，单调递增，用于去重 */
    @Volatile
    private var nextVersion: Long = 0L

    /**
     * 立即在主线程分发事件，并等待分发流程完成。
     *
     * @param event 要分发的事件
     */
    protected fun post(event: T) {
        runOnMainThreadAndWait {
            dispatch(event)
        }
    }

    /**
     * 在指定延迟后分发事件。
     *
     * @param event 要分发的事件
     * @param delay 延迟时间（毫秒）
     */
    protected fun postDelay(event: T, delay: Long) {
        mainHandler.postDelayed({ dispatch(event) }, delay)
    }

    /**
     * 当发送方仍处于激活状态时，在指定延迟后分发事件。
     *
     * @param sender 发送方的 LifecycleOwner
     * @param event 要分发的事件
     * @param delay 延迟时间（毫秒）
     */
    protected fun postDelay(sender: LifecycleOwner, event: T, delay: Long) {
        mainHandler.postDelayed({
            if (sender.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                dispatch(event)
            }
        }, delay)
    }

    /**
     * 按调用顺序在主线程分发事件。
     *
     * @param event 要分发的事件
     */
    protected fun postOrderly(event: T) {
        runOnMainThreadAndWait { dispatch(event) }
    }

    /**
     * 注册普通观察者，宿主销毁时自动解绑。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 事件回调
     * @return 注册的 Observer 实例
     */
    protected fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (event: T) -> Unit
    ): Observer<T> {
        val observer = createObserver(callback)
        registerObserver(
            observer = observer,
            lifecycleOwner = lifecycleOwner,
            sticky = false,
            minActiveState = Lifecycle.State.CREATED
        )
        return observer
    }

    /**
     * 注册仅在 STARTED 及以上状态接收事件的观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 事件回调
     * @return 注册的 Observer 实例
     */
    protected fun observeWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (event: T) -> Unit
    ): Observer<T> {
        val observer = createObserver(callback)
        registerObserver(
            observer = observer,
            lifecycleOwner = lifecycleOwner,
            sticky = false,
            minActiveState = Lifecycle.State.STARTED
        )
        return observer
    }

    /**
     * 根据回调创建 Lifecycle Observer 包装。
     *
     * @param callback 事件回调
     * @return 包装后的 Observer 实例
     */
    protected fun createObserver(callback: (event: T) -> Unit): Observer<T> {
        return Observer { event -> callback.invoke(event) }
    }

    /**
     * 注册不受生命周期管理的普通观察者，需要手动调用 [removeObserver] 解绑。
     *
     * @param observer 要注册的 Observer 实例
     */
    protected fun observeForever(observer: Observer<T>) {
        registerObserver(
            observer = observer,
            lifecycleOwner = null,
            sticky = false,
            minActiveState = Lifecycle.State.CREATED
        )
    }

    /**
     * 手动移除已注册的观察者。
     *
     * @param observer 要移除的 Observer 实例
     */
    protected fun removeObserver(observer: Observer<T>) {
        runOnMainThreadAndWait {
            removeObserverInternal(observer)
        }
    }

    /**
     * 注册粘性观察者，必要时会立即收到最近一次事件。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 事件回调
     * @return 注册的 Observer 实例
     */
    protected fun observeSticky(
        lifecycleOwner: LifecycleOwner,
        callback: (event: T) -> Unit
    ): Observer<T> {
        val observer = createObserver(callback)
        registerObserver(
            observer = observer,
            lifecycleOwner = lifecycleOwner,
            sticky = true,
            minActiveState = Lifecycle.State.CREATED
        )
        return observer
    }

    /**
     * 注册仅在 STARTED 及以上状态接收粘性事件的观察者。
     *
     * @param lifecycleOwner 观察者的生命周期宿主
     * @param callback 事件回调
     * @return 注册的 Observer 实例
     */
    protected fun observeStickyWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        callback: (event: T) -> Unit
    ): Observer<T> {
        val observer = createObserver(callback)
        registerObserver(
            observer = observer,
            lifecycleOwner = lifecycleOwner,
            sticky = true,
            minActiveState = Lifecycle.State.STARTED
        )
        return observer
    }

    /**
     * 注册不受生命周期管理的粘性观察者，需要手动调用 [removeObserver] 解绑。
     *
     * @param observer 要注册的 Observer 实例
     */
    protected fun observeStickyForever(observer: Observer<T>) {
        registerObserver(
            observer = observer,
            lifecycleOwner = null,
            sticky = true,
            minActiveState = Lifecycle.State.CREATED
        )
    }

    /**
     * 统一完成观察者注册，并根据配置补发粘性事件。
     *
     * @param observer 要注册的观察者
     * @param lifecycleOwner 生命周期宿主，null 表示不受生命周期管理
     * @param sticky 是否为粘性观察者
     * @param minActiveState 观察者接收事件的最小活跃状态
     */
    private fun registerObserver(
        observer: Observer<T>,
        lifecycleOwner: LifecycleOwner?,
        sticky: Boolean,
        minActiveState: Lifecycle.State
    ) {
        runOnMainThreadAndWait {
            if (lifecycleOwner?.lifecycle?.currentState == Lifecycle.State.DESTROYED) {
                return@runOnMainThreadAndWait
            }

            removeObserverInternal(observer)

            val state = ObserverState(
                observer = observer,
                lifecycleOwner = lifecycleOwner,
                sticky = sticky,
                minActiveState = minActiveState,
                lastDeliveredVersion = -1L
            )

            if (lifecycleOwner != null) {
                val lifecycleObserver = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        removeObserver(observer)
                    } else if (sticky) {
                        dispatchStickyIfNeeded(observer)
                    }
                }
                state.lifecycleObserver = lifecycleObserver
                lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            }

            synchronized(lock) {
                observerStates[observer] = state
            }

            if (sticky) {
                dispatchStickyIfNeeded(observer)
            }
        }
    }

    /**
     * 生成带版本号的事件包装并分发给当前所有观察者。
     */
    private fun dispatch(event: T) {
        val envelope = synchronized(lock) {
            EventEnvelope(value = event, version = nextVersion++).also { stickyEvent = it }
        }

        val states = synchronized(lock) {
            observerStates.values.toList()
        }

        states.forEach { state ->
            dispatchToObserver(state, envelope)
        }
    }

    /**
     * 当存在最近一次事件时，向指定观察者补发粘性事件。
     */
    private fun dispatchStickyIfNeeded(observer: Observer<T>) {
        val state = synchronized(lock) {
            observerStates[observer]
        } ?: return

        val envelope = stickyEvent ?: return
        dispatchToObserver(state, envelope)
    }

    /**
     * 在通过版本和生命周期校验后，将事件投递给具体观察者。
     */
    private fun dispatchToObserver(state: ObserverState<T>, envelope: EventEnvelope<T>) {
        if (!shouldDispatch(state, envelope.version)) {
            return
        }

        try {
            state.observer.onChanged(envelope.value)
        } catch (_: ClassCastException) {
        } catch (_: Exception) {
        }
    }

    /**
     * 判断当前事件版本是否应投递给指定观察者，并更新投递进度。
     */
    private fun shouldDispatch(state: ObserverState<T>, version: Long): Boolean {
        if (version <= state.lastDeliveredVersion) {
            return false
        }

        val lifecycleOwner = state.lifecycleOwner
        if (lifecycleOwner != null && !lifecycleOwner.lifecycle.currentState.isAtLeast(state.minActiveState)) {
            return false
        }

        synchronized(lock) {
            val current = observerStates[state.observer] ?: return false
            if (version <= current.lastDeliveredVersion) {
                return false
            }
            current.lastDeliveredVersion = version
        }
        state.lastDeliveredVersion = version
        return true
    }

    /**
     * 移除观察者并清理其生命周期监听。
     */
    private fun removeObserverInternal(observer: Observer<T>) {
        val removed = synchronized(lock) {
            observerStates.remove(observer)
        } ?: return

        removed.lifecycleOwner?.lifecycle?.removeObserver(removed.lifecycleObserver ?: return)
    }

    /**
     * 将任务切换到主线程异步执行。
     *
     * @param action 要执行的任务
     */
    protected fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    /**
     * 将任务切换到主线程执行，并等待执行完成后再返回。
     * 如果任务在主线程抛出异常，会在调用线程重新抛出。
     *
     * @param action 要执行的任务
     * @throws IllegalStateException 主线程执行失败或线程被中断时抛出
     */
    protected fun runOnMainThreadAndWait(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
            return
        }

        val latch = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>(null)

        mainHandler.post {
            try {
                action()
            } catch (throwable: Throwable) {
                failure.set(throwable)
            } finally {
                latch.countDown()
            }
        }

        try {
            latch.await()
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while waiting for main-thread live event work", interrupted)
        }

        val throwable = failure.get() ?: return
        throw when (throwable) {
            is RuntimeException -> throwable
            is Error -> throwable
            else -> IllegalStateException("Main-thread live event work failed", throwable)
        }
    }

    /**
     * 事件包装体，记录实际值和单调递增版本号。
     *
     * @param T 事件类型
     * @property value 实际的事件值
     * @property version 事件版本号，用于判断是否需要投递
     */
    private data class EventEnvelope<T>(
        val value: T,
        val version: Long
    )

    /**
     * 观察者注册状态，包含生命周期和投递进度信息。
     *
     * @param T 事件类型
     * @property observer 注册的观察者
     * @property lifecycleOwner 生命周期宿主，null 表示不受生命周期管理
     * @property sticky 是否为粘性观察者
     * @property minActiveState 观察者接收事件的最小活跃状态
     * @property lastDeliveredVersion 最后一次投递的事件版本号
     * @property lifecycleObserver 附加的生命周期监听，用于自动解绑
     */
    private data class ObserverState<T>(
        val observer: Observer<T>,
        val lifecycleOwner: LifecycleOwner?,
        val sticky: Boolean,
        val minActiveState: Lifecycle.State,
        var lastDeliveredVersion: Long,
        var lifecycleObserver: LifecycleEventObserver? = null
    )
}
