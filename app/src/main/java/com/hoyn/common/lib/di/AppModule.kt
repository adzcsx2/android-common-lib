package com.hoyn.common.lib.di

import com.hoyn.common.lib.MainApplication
import org.koin.android.ext.koin.androidContext

/**
 * 初始化 Koin 依赖注入框架
 *
 * @param application Application 实例
 */
fun initKoin(application: MainApplication) {
    org.koin.core.context.startKoin {
        androidContext(application)
        modules() // No modules needed for camera-only app
    }
}
