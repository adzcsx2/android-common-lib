package com.hoyn.common.lib

import android.app.Application
import com.hoyn.common.lib.di.initKoin
import com.hoyn.common.lib.logging.AppRuntimeLogCapture
import com.hoyn.common.utils.LanguageHelper
import com.hoyn.common.utils.Logger
import com.hoyn.common.utils.MMKVUtils
import com.hoyn.common.utils.ThemeManager

/**
 * MainApplication
 *
 * 应用程序入口类
 * 负责初始化各种工具和组件
 */
class MainApplication : Application() {

    companion object {
        /**
         * Application 实例
         */
        lateinit var instance: MainApplication
            private set
    }

    /**
     * Application 创建时的回调
     *
     * 初始化日志、MMKV、主题等组件
     */
    override fun onCreate() {
        super.onCreate()
        instance = this
        // 初始化 Koin
        initKoin(this)
        val runtimeLogFile = AppRuntimeLogCapture.start(this)
        // 初始化日志
        Logger.init(BuildConfig.DEBUG, getString(R.string.app_name))
        Logger.i("Runtime log file=${runtimeLogFile?.absolutePath ?: "unavailable"}")
        // 初始化 MMKV
        MMKVUtils.init(this)
        // 应用主题设置
        ThemeManager.applyTheme()
    }
}
