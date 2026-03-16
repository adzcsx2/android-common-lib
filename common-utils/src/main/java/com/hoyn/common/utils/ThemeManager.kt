package com.hoyn.common.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * 主题管理器
 *
 * 用于管理应用的黑暗模式设置
 * 封装在 common-utils 模块中，供所有模块使用
 */
object ThemeManager {

    private const val KEY_THEME = "app_theme"

    /**
     * 主题模式
     */
    enum class ThemeMode(val value: Int, val displayName: String) {
        /**
         * 跟随系统
         */
        SYSTEM(0, "Follow System"),
        /**
         * 亮色模式
         */
        LIGHT(1, "Light"),
        /**
         * 暗色模式
         */
        DARK(2, "Dark");

        companion object {
            fun fromValue(value: Int): ThemeMode {
                return entries.find { it.value == value } ?: SYSTEM
            }
        }
    }

    /**
     * 获取当前保存的主题模式
     */
    fun getSavedThemeMode(): ThemeMode {
        val savedValue = MMKVUtils.getInt(KEY_THEME, ThemeMode.SYSTEM.value)
        return ThemeMode.fromValue(savedValue)
    }

    /**
     * 保存主题模式设置
     */
    fun saveThemeMode(mode: ThemeMode) {
        MMKVUtils.put(KEY_THEME, mode.value)
    }

    /**
     * 应用主题模式
     *
     * 应在 Application 初始化时调用
     */
    fun applyTheme(mode: ThemeMode = getSavedThemeMode()) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    /**
     * 设置主题模式
     *
     * @param mode 目标主题模式
     */
    fun setThemeMode(mode: ThemeMode) {
        saveThemeMode(mode)
        applyTheme(mode)
    }

    /**
     * 切换主题模式
     *
     * @return 切换后的主题模式
     */
    fun toggleTheme(): ThemeMode {
        val currentMode = getSavedThemeMode()
        val newMode = when (currentMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(newMode)
        return newMode
    }

    /**
     * 判断当前是否为暗色模式
     *
     * @param context Context
     * @return 是否为暗色模式
     */
    fun isDarkMode(context: Context): Boolean {
        val currentMode = getSavedThemeMode()
        return when (currentMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    /**
     * 获取当前主题模式显示名称
     */
    fun getCurrentThemeDisplayName(): String {
        return getSavedThemeMode().displayName
    }
}
