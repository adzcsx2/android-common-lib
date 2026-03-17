package com.hoyn.common.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

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
 * 启动指定的 Activity 并等待结果
 *
 * 使用方式：
 * ```
 * context.startActivityForResult<LoginActivity>(requestCode = 100) {
 *     putExtra("force_login", true)
 * }
 * ```
 */
inline fun <reified T : Activity> Context.startActivityForResult(
    requestCode: Int,
    block: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.block()
    if (this is AppCompatActivity) {
        startActivityForResult(intent, requestCode)
    } else {
        startActivity(intent)
    }
}
