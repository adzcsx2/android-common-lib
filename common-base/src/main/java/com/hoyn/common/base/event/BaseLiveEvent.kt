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
abstract class BaseLiveEvent<T : Any>(
    private val tag: String,
    private val eventClass: Class<T>
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lock = Any()
    private val observerStates = LinkedHashMap<Observer<T>, ObserverState<T>>()

    @Volatile
    private var stickyEvent: EventEnvelope<T>? = null

    @Volatile
    private var nextVersion: Long = 0L

    protected fun post(event: T) {
        runOnMainThreadAndWait {
            dispatch(event)
        }
    }

    protected fun postDelay(event: T, delay: Long) {
        mainHandler.postDelayed({ dispatch(event) }, delay)
    }

    protected fun postDelay(sender: LifecycleOwner, event: T, delay: Long) {
        mainHandler.postDelayed({
            if (sender.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                dispatch(event)
            }
        }, delay)
    }

    protected fun postOrderly(event: T) {
        runOnMainThreadAndWait { dispatch(event) }
    }

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

    protected fun createObserver(callback: (event: T) -> Unit): Observer<T> {
        return Observer { event -> callback.invoke(event) }
    }

    protected fun observeForever(observer: Observer<T>) {
        registerObserver(
            observer = observer,
            lifecycleOwner = null,
            sticky = false,
            minActiveState = Lifecycle.State.CREATED
        )
    }

    protected fun removeObserver(observer: Observer<T>) {
        runOnMainThreadAndWait {
            removeObserverInternal(observer)
        }
    }

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

    protected fun observeStickyForever(observer: Observer<T>) {
        registerObserver(
            observer = observer,
            lifecycleOwner = null,
            sticky = true,
            minActiveState = Lifecycle.State.CREATED
        )
    }

    protected fun postAcrossProcess(event: T) {
        post(event)
    }

    protected fun postAcrossApp(event: T) {
        post(event)
    }

    protected fun broadcast(event: T, foreground: Boolean = false, onlyInApp: Boolean = false) {
        post(event)
    }

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

    private fun dispatchStickyIfNeeded(observer: Observer<T>) {
        val state = synchronized(lock) {
            observerStates[observer]
        } ?: return

        val envelope = stickyEvent ?: return
        dispatchToObserver(state, envelope)
    }

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

    private fun removeObserverInternal(observer: Observer<T>) {
        val removed = synchronized(lock) {
            observerStates.remove(observer)
        } ?: return

        removed.lifecycleOwner?.lifecycle?.removeObserver(removed.lifecycleObserver ?: return)
    }

    protected fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

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

    private data class EventEnvelope<T>(
        val value: T,
        val version: Long
    )

    private data class ObserverState<T>(
        val observer: Observer<T>,
        val lifecycleOwner: LifecycleOwner?,
        val sticky: Boolean,
        val minActiveState: Lifecycle.State,
        var lastDeliveredVersion: Long,
        var lifecycleObserver: LifecycleEventObserver? = null
    )
}
