package com.hoyn.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 协程扩展函数
 *
 * 提供便捷的协程调度器切换扩展函数，简化协程使用
 */

/**
 * 在 IO 调度器上启动协程
 *
 * 适用于执行数据库操作、文件读写、网络请求等 IO 密集型任务
 *
 * @param block 要在协程中执行的挂起代码块
 * @return 启动的 Job 对象，可用于取消协程
 */
fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit) =
    launch(Dispatchers.IO, block = block)

/**
 * 在主线程调度器上启动协程
 *
 * 适用于更新 UI、处理用户交互等需要在主线程执行的操作
 *
 * @param block 要在协程中执行的挂起代码块
 * @return 启动的 Job 对象，可用于取消协程
 */
fun CoroutineScope.launchMain(block: suspend CoroutineScope.() -> Unit) =
    launch(Dispatchers.Main, block = block)

/**
 * 切换到 IO 调度器执行代码块
 *
 * 用于将协程的执行环境切换到 IO 线程，执行完毕后自动返回原调度器
 *
 * @param block 要在 IO 调度器上执行的挂起代码块
 * @return 代码块的执行结果
 */
suspend fun <T> withIO(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO, block)

/**
 * 切换到主线程调度器执行代码块
 *
 * 用于将协程的执行环境切换到主线程，执行完毕后自动返回原调度器
 * 常用于在后台任务完成后更新 UI
 *
 * @param block 要在主线程调度器上执行的挂起代码块
 * @return 代码块的执行结果
 */
suspend fun <T> withMain(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Main, block)
