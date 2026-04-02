package com.hoyn.common.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * 语言助手
 *
 * 用于管理应用语言设置和切换
 * 封装在 common-utils 模块中，供所有模块使用
 */
object LanguageHelper {

    private const val KEY_LANGUAGE = "app_language"

    /**
     * 支持的语言
     */
    enum class AppLanguage(val code: String, val displayName: String) {
        SYSTEM("system", "Follow System"),
        ENGLISH("en", "English"),
        CHINESE_SIMPLIFIED("zh", "简体中文"),
        CHINESE_TRADITIONAL("zh-TW", "繁體中文");

        companion object {
            /**
             * 根据语言代码查找对应的 AppLanguage 枚举值
             *
             * @param code 语言代码字符串
             * @return 对应的 AppLanguage 枚举值，未找到则返回 SYSTEM
             */
            fun fromCode(code: String): AppLanguage {
                return entries.find { it.code == code } ?: SYSTEM
            }
        }
    }

    /**
     * 获取当前保存的语言设置
     */
    fun getSavedLanguage(): AppLanguage {
        val savedCode = MMKVUtils.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.code)
        return AppLanguage.fromCode(savedCode)
    }

    /**
     * 保存语言设置
     */
    fun saveLanguage(language: AppLanguage) {
        MMKVUtils.put(KEY_LANGUAGE, language.code)
    }

    /**
     * 应用语言设置到 Context
     *
     * @param context 需要应用语言的 Context
     * @return 配置后的 Context
     */
    fun applyLanguage(context: Context): Context {
        val language = getSavedLanguage()

        if (language == AppLanguage.SYSTEM) {
            return context
        }

        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.CHINESE_SIMPLIFIED -> Locale.SIMPLIFIED_CHINESE
            AppLanguage.CHINESE_TRADITIONAL -> Locale.TRADITIONAL_CHINESE
            else -> return context
        }

        return updateResources(context, locale)
    }

    /**
     * 设置语言
     *
     * @param language 目标语言
     */
    fun setLanguage(language: AppLanguage) {
        saveLanguage(language)
    }

    /**
     * 更新资源配置
     *
     * 根据目标 Locale 更新 Context 的配置信息，兼容 Android N 及以上版本
     *
     * @param context 原始 Context
     * @param locale 目标 Locale
     * @return 更新配置后的 Context
     */
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    /**
     * 获取当前语言显示名称
     */
    fun getCurrentLanguageDisplayName(): String {
        return getSavedLanguage().displayName
    }
}
