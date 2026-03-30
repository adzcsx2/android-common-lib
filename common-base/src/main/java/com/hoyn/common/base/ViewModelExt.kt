package com.hoyn.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel 扩展函数
 *
 * 提供 viewModelScope 的便捷扩展方法
 */

/**
 * 在 IO 线程中启动协程的扩展函数
 *
 * 用法:
 * ```
 * class MyViewModel : ViewModel() {
 *     fun loadData() {
 *         launchIO {
 *             // 在 IO 线程执行
 *         }
 *     }
 * }
 * ```
 *
 * 等同于: viewModelScope.launch(Dispatchers.IO) { ... }
 *
 * @param block 协程体
 */
fun ViewModel.launchIO(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(Dispatchers.IO, block = block)
