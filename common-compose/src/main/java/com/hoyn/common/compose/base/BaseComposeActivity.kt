package com.hoyn.common.compose.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable

/**
 * Base Compose Activity
 *
 * 提供纯 Compose Activity 的基类
 */
abstract class BaseComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }

    /**
     * Compose 内容
     */
    @Composable
    protected abstract fun Content()
}
