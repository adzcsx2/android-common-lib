package com.hoyn.common.base

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * 以 DSL 方式为 Fragment 构建 arguments，同时保留 Bundle 持久化能力。
 *
 * 使用示例：
 * ```
 * MyFragment().withArguments {
 *     putString("key", "value")
 *     putInt("id", 123)
 * }
 * ```
 *
 * @param T Fragment 的具体类型
 * @param block 在 Bundle 作用域内的配置代码块
 * @return 配置好 arguments 的 Fragment 实例
 */
inline fun <T : Fragment> T.withArguments(block: Bundle.() -> Unit): T {
    arguments = (arguments ?: Bundle()).apply(block)
    return this
}