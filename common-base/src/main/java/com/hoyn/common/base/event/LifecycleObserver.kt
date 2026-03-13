package com.hoyn.common.base.event

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 生命周期观察者 DSL
 *
 * 使用 DefaultLifecycleObserver 替代废弃的 @OnLifecycleEvent 注解
 *
 * 使用示例：
 * ```kotlin
 * subscribeLifecycle(lifecycle) {
 *     onCreate = { ... }
 *     onResume = { ... }
 *     onDestroy = { ... }
 * }
 * ```
 */
class LifecycleObserver : DefaultLifecycleObserver {

    var onCreate: (() -> Unit)? = null
    var onStart: (() -> Unit)? = null
    var onResume: (() -> Unit)? = null
    var onPause: (() -> Unit)? = null
    var onStop: (() -> Unit)? = null
    var onDestroy: (() -> Unit)? = null

    override fun onCreate(owner: LifecycleOwner) {
        onCreate?.invoke()
    }

    override fun onStart(owner: LifecycleOwner) {
        onStart?.invoke()
    }

    override fun onResume(owner: LifecycleOwner) {
        onResume?.invoke()
    }

    override fun onPause(owner: LifecycleOwner) {
        onPause?.invoke()
    }

    override fun onStop(owner: LifecycleOwner) {
        onStop?.invoke()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        onDestroy?.invoke()
    }
}

/**
 * 订阅生命周期事件的 DSL 函数
 */
fun subscribeLifecycle(
    lifecycleOwner: LifecycleOwner,
    init: LifecycleObserver.() -> Unit
) {
    val observer = LifecycleObserver().apply(init)
    lifecycleOwner.lifecycle.addObserver(observer)
}
