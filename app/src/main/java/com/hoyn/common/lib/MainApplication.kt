package com.hoyn.common.lib

import android.app.Application
import com.hoyn.common.utils.LanguageHelper
import com.hoyn.common.utils.Logger
import com.hoyn.common.utils.MMKVUtils
import com.hoyn.common.utils.ThemeManager

class MainApplication : Application() {

    companion object {
        lateinit var instance: MainApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // 初始化日志
        Logger.init(BuildConfig.DEBUG, getString(R.string.app_name))
        // 初始化 MMKV
        MMKVUtils.init(this)
        // 应用主题设置
        ThemeManager.applyTheme()
    }
}
