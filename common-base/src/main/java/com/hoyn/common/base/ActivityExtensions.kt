package com.hoyn.common.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * Activity 启动扩展函数
 *
 * 提供简洁的 Activity 启动方式，使用泛型和 reified 获取实际类型
 * 同时支持 AppCompatActivity 和 ComponentActivity（Compose）
 */

/**
 * 启动指定的 Activity
 *
 * 使用方式：
 * ```
 * context.startActivity<MainActivity>()
 * ```
 */
inline fun <reified T : Activity> Context.startActivity() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

/**
 * 启动指定的 Activity，并可配置 Intent 参数
 *
 * 使用方式：
 * ```
 * context.startActivity<DetailActivity> {
 *     putExtra("id", 123)
 *     putExtra("name", "example")
 * }
 * ```
 */
inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit) {
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivity(intent)
}

/**
 * 在 Activity 中注册一个基于 Activity Result API 的页面启动器。
 *
 * 推荐把 launcher 声明为 Activity/Fragment 的属性，在生命周期创建阶段完成注册。
 * 这样结果回调会自动绑定到宿主生命周期，不需要手动维护 requestCode。
 *
 * 使用方式：
 * ```
 * private val loginLauncher = registerStartActivityForResult { result ->
 *     if (result.resultCode == Activity.RESULT_OK) {
 *         val token = result.data?.getStringExtra("token").orEmpty()
 *         handleLoginSuccess(token)
 *     }
 * }
 *
 * fun openLogin() {
 *     loginLauncher.launchActivity<LoginActivity>(this) {
 *         putExtra("force_login", true)
 *     }
 * }
 * ```
 */
fun ComponentActivity.registerStartActivityForResult(
    onResult: (ActivityResult) -> Unit
): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult(), onResult)
}

/**
 * 在 Fragment 中注册一个基于 Activity Result API 的页面启动器。
 *
 * 用法与 Activity 版本一致，区别只是 `launchActivity` 时通常传入 `requireContext()`。
 */
fun Fragment.registerStartActivityForResult(
    onResult: (ActivityResult) -> Unit
): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult(), onResult)
}

/**
 * 通过已注册的 launcher 启动目标 Activity。
 *
 * 这个方法只负责构造 Intent 并交给 launcher 发起跳转，结果处理统一放在
 * `registerStartActivityForResult` 的回调里。
 */
inline fun <reified T : Activity> ActivityResultLauncher<Intent>.launchActivity(
    context: Context,
    block: Intent.() -> Unit = {}
) {
    val intent = Intent(context, T::class.java).apply(block)
    launch(intent)
}
