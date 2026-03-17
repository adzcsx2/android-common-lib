package com.hoyn.common.compose.base

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.hoyn.common.base.ActivityStackManager
import com.hoyn.common.compose.theme.AppTheme
import com.hoyn.common.utils.LanguageHelper

/**
 * Base Compose Activity
 *
 * 提供纯 Compose Activity 的基类
 * 支持多语言设置、Activity 栈管理
 */
abstract class BaseComposeActivity : ComponentActivity() {

    /**
     * 自动生成的 TAG，使用类名
     * 子类可以直接使用，无需手动定义
     */
    val TAG: String get() = javaClass.simpleName

    override fun attachBaseContext(newBase: Context) {
        // 统一应用语言设置
        val context = LanguageHelper.applyLanguage(newBase)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityStackManager.push(this)
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content()
            }
        }
    }

    /**
     * Compose 内容
     */
    @Composable
    protected abstract fun Content()

    override fun onDestroy() {
        ActivityStackManager.pop(this)
        super.onDestroy()
    }
}
