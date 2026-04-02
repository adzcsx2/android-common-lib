/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hoyn.common.ui.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hoyn.common.utils.device.DeviceHelper
import com.hoyn.common.utils.device.DisplayUtils
import java.lang.reflect.Field

/**
 * 状态栏工具类
 *
 * 支持沉浸式状态栏、状态栏字体颜色设置
 * 兼容 MIUI、Flyme 和 Android 6.0+ 原生方案
 */
object StatusBarHelper {
    /** 默认状态栏类型 */
    private const val STATUS_BAR_TYPE_DEFAULT = 0
    /** MIUI 状态栏类型 */
    private const val STATUS_BAR_TYPE_MI = 1
    /** Flyme 状态栏类型 */
    private const val STATUS_BAR_TYPE_FL = 2
    /** Android 6.0+ 原生状态栏类型 */
    private const val STATUS_BAR_TYPE_ANDROID6 = 3
    /** 状态栏默认高度（dp） */
    private const val STATUS_BAR_DEFAULT_HEIGHT_DP = 25

    /** 自定义虚拟屏幕密度，用于计算状态栏高度 */
    private var sVirtualDensity = -1f
    /** 自定义虚拟屏幕 DPI，用于计算状态栏高度 */
    private var sVirtualDensityDpi = -1f
    /** 状态栏高度缓存（px） */
    private var mStatusBarHeight = -1

    /** 当前状态栏类型 */
    @StatusBarType
    private var mStatusBarType = STATUS_BAR_TYPE_DEFAULT
    /** 系统 Toast 透明 API 标志值缓存 */
    private var sTransparentValue: Int? = null

    /**
     * 设置 Activity 为半透明状态栏模式
     *
     * 使用默认的半透明颜色
     *
     * @param activity 要设置的 Activity
     */
    fun translucent(activity: Activity) {
        translucent(activity.window)
    }

    /**
     * 设置 Window 为半透明状态栏模式
     *
     * 使用默认的半透明颜色
     *
     * @param window 要设置的 Window
     */
    fun translucent(window: Window) {
        translucent(window, 0x40000000)
    }

    /**
     * 判断设备是否支持半透明状态栏
     *
     * Essential Phone 在 API 26 之前不支持
     *
     * @return true 表示支持，false 表示不支持
     */
    private fun supportTranslucent(): Boolean {
        return !(DeviceHelper.isEssentialPhone && Build.VERSION.SDK_INT < 26)
    }

    /**
     * 沉浸式状态栏。
     * 支持 4.4 以上版本的 MIUI 和 Flyme，以及 5.0 以上版本的其他 Android。
     */
    fun translucent(activity: Activity, @ColorInt colorOn5x: Int) {
        translucent(activity.window, colorOn5x)
    }

    /**
     * 设置沉浸式状态栏
     *
     * 支持 4.4 以上版本的 MIUI 和 Flyme，以及 5.0 以上版本的其他 Android
     *
     * @param window 要设置的 Window
     * @param colorOn5x Android 5.0+ 使用的颜色值
     */
    @TargetApi(19)
    fun translucent(window: Window, @ColorInt colorOn5x: Int) {
        if (!supportTranslucent()) return

        if (NotchHelper.isNotchOfficialSupport) {
            handleDisplayCutoutMode(window)
        }

        // Android 15+ (API 35+) 使用 WindowCompat API
        if (Build.VERSION.SDK_INT >= 35) {
            translucentApi35(window)
            return
        }

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && supportTransplantStatusBar6()) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = colorOn5x
        }
    }

    /**
     * Android 15+ (API 35+) 沉浸式状态栏实现
     *
     * 使用 WindowCompat API 替代已废弃的 systemUiVisibility
     *
     * @param window 要设置的 Window
     */
    @TargetApi(35)
    private fun translucentApi35(window: Window) {
        val decorView = window.decorView

        // 确保在 DecorView 附加后设置（KI-005）
        if (ViewCompat.isAttachedToWindow(decorView)) {
            applyTranslucentApi35(window)
        } else {
            decorView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    applyTranslucentApi35(window)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }

    /**
     * 实际应用 Android 15+ 沉浸式状态栏设置
     *
     * @param window 要设置的 Window
     */
    @TargetApi(35)
    private fun applyTranslucentApi35(window: Window) {
        try {
            // 1. 让内容延伸到系统栏区域（关键！）
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // 2. 设置状态栏透明
            window.statusBarColor = Color.TRANSPARENT

            // 3. 设置导航栏透明（可选，建议一起设置）
            window.navigationBarColor = Color.TRANSPARENT

            // 透明状态栏默认使用浅色图标，避免继承上一次纯色状态栏的深色图标配置。
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        } catch (e: Exception) {
            e.printStackTrace()
            // 降级到传统方案
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * 设置纯色状态栏。
     *
     * @param exitEdgeToEdge true 时退出沉浸式/全屏布局，适合从 translucent() 切回普通状态栏
     */
    fun setStatusBarColor(activity: Activity, @ColorInt color: Int, exitEdgeToEdge: Boolean = false) {
        setStatusBarColor(activity.window, color, exitEdgeToEdge)
    }

    /**
     * 设置纯色状态栏，并根据背景颜色自动调整图标明暗。
     *
     * @param exitEdgeToEdge true 时退出沉浸式/全屏布局，false 时尽量保留现有布局标记
     */
    fun setStatusBarColor(window: Window, @ColorInt color: Int, exitEdgeToEdge: Boolean = false) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (exitEdgeToEdge) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
        window.statusBarColor = color

        val useDarkIcons = ColorUtils.calculateLuminance(color) >= 0.5
        var systemUi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && useDarkIcons) {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        systemUi = if (exitEdgeToEdge) {
            retainSystemUiFlag(window, systemUi, View.SYSTEM_UI_FLAG_HIDE_NAVIGATION).let {
                retainSystemUiFlag(window, it, View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }.let {
                retainSystemUiFlag(window, it, View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            }
        } else {
            changeStatusBarModeRetainFlag(window, systemUi)
        }

        window.decorView.systemUiVisibility = systemUi
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = useDarkIcons
    }

    /**
     * 处理刘海屏的显示模式
     *
     * 设置内容延伸到刘海区域
     *
     * @param window Window 实例
     */
    @TargetApi(28)
    private fun handleDisplayCutoutMode(window: Window) {
        val decorView = window.decorView
        if (ViewCompat.isAttachedToWindow(decorView)) {
            realHandleDisplayCutoutMode(window, decorView)
        } else {
            decorView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    realHandleDisplayCutoutMode(window, v)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }

    /**
     * 实际处理刘海屏的显示模式
     *
     * 如果存在 DisplayCutout，则设置布局延伸到刘海区域
     *
     * @param window Window 实例
     * @param decorView DecorView 实例
     */
    @TargetApi(28)
    private fun realHandleDisplayCutoutMode(window: Window, decorView: View) {
        if (decorView.rootWindowInsets?.displayCutout != null) {
            val params = window.attributes
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params
        }
    }

    /**
     * 设置状态栏为浅色模式（黑色字体图标）
     *
     * 自动检测设备类型并选择合适的实现方案：
     * - Android 15+ (API 35+): 使用 WindowInsetsControllerCompat
     * - MIUI V5-V8: 使用 MIUI 方案
     * - Flyme: 使用 Flyme 方案
     * - Android 6.0+: 使用原生方案
     *
     * @param activity Activity 实例
     * @return true 表示设置成功，false 表示失败
     */
    fun setStatusBarLightMode(activity: Activity?): Boolean {
        if (activity == null) return false
        if (DeviceHelper.isZTKC2016) return false

        // Android 15+ (API 35+) 使用新的 WindowInsetsController API
        if (Build.VERSION.SDK_INT >= 35) {
            return setStatusBarLightModeApi35(activity.window, true)
        }

        if (mStatusBarType != STATUS_BAR_TYPE_DEFAULT) {
            return setStatusBarLightMode(activity, mStatusBarType)
        }

        if (isMIUICustomStatusBarLightModeImpl && setMISetStatusBarLightMode(activity.window, true)) {
            mStatusBarType = STATUS_BAR_TYPE_MI
            return true
        } else if (setFlSetStatusBarLightMode(activity.window, true)) {
            mStatusBarType = STATUS_BAR_TYPE_FL
            return true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setAndroid6SetStatusBarLightMode(activity.window, true)
            mStatusBarType = STATUS_BAR_TYPE_ANDROID6
            return true
        }
        return false
    }

    /**
     * 根据指定的状态栏类型设置浅色模式
     *
     * @param activity Activity 实例
     * @param type 状态栏类型（MIUI/Flyme/Android6）
     * @return true 表示设置成功，false 表示失败
     */
    private fun setStatusBarLightMode(activity: Activity, @StatusBarType type: Int): Boolean {
        return when (type) {
            STATUS_BAR_TYPE_MI -> setMISetStatusBarLightMode(activity.window, true)
            STATUS_BAR_TYPE_FL -> setFlSetStatusBarLightMode(activity.window, true)
            STATUS_BAR_TYPE_ANDROID6 -> setAndroid6SetStatusBarLightMode(activity.window, true)
            else -> false
        }
    }

    /**
     * 设置状态栏为深色模式（白色字体图标）
     *
     * 根据之前检测的状态栏类型选择相应的方案
     *
     * @param activity Activity 实例
     * @return true 表示设置成功，false 表示失败
     */
    fun setStatusBarDarkMode(activity: Activity?): Boolean {
        if (activity == null) return false

        // Android 15+ (API 35+) 使用新的 WindowInsetsController API
        if (Build.VERSION.SDK_INT >= 35) {
            return setStatusBarLightModeApi35(activity.window, false)
        }

        if (mStatusBarType == STATUS_BAR_TYPE_DEFAULT) return true

        return when (mStatusBarType) {
            STATUS_BAR_TYPE_MI -> setMISetStatusBarLightMode(activity.window, false)
            STATUS_BAR_TYPE_FL -> setFlSetStatusBarLightMode(activity.window, false)
            STATUS_BAR_TYPE_ANDROID6 -> setAndroid6SetStatusBarLightMode(activity.window, false)
            else -> true
        }
    }

    /**
     * 改变状态栏模式时保留原有的 SystemUiFlag
     *
     * 防止设置状态栏模式时影响其他全屏等标志
     *
     * @param window Window 实例
     * @param out 新的 systemUiVisibility 值
     * @return 保留原有 flag 后的值
     */
    @TargetApi(23)
    private fun changeStatusBarModeRetainFlag(window: Window, out: Int): Int {
        var result = out
        result = retainSystemUiFlag(window, result, View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        result = retainSystemUiFlag(window, result, View.SYSTEM_UI_FLAG_FULLSCREEN)
        result = retainSystemUiFlag(window, result, View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        result = retainSystemUiFlag(window, result, View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        result = retainSystemUiFlag(window, result, View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        return result
    }

    /**
     * 保留指定的 SystemUiFlag
     *
     * @param window Window 实例
     * @param out 当前的 systemUiVisibility 值
     * @param type 要保留的 flag 类型
     * @return 保留了指定 flag 后的值
     */
    private fun retainSystemUiFlag(window: Window, out: Int, type: Int): Int {
        var result = out
        val now = window.decorView.systemUiVisibility
        if (now and type == type) {
            result = result or type
        }
        return result
    }

    /**
     * 使用 Android 6.0+ 原生方案设置状态栏模式
     *
     * @param window Window 实例
     * @param light true 表示浅色模式（黑色图标），false 表示深色模式（白色图标）
     * @return true 表示设置成功
     */
    @TargetApi(23)
    private fun setAndroid6SetStatusBarLightMode(window: Window, light: Boolean): Boolean {
        val decorView = window.decorView
        var systemUi = if (light) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        systemUi = changeStatusBarModeRetainFlag(window, systemUi)
        decorView.systemUiVisibility = systemUi
        if (DeviceHelper.isMIUIV9) {
            setMISetStatusBarLightMode(window, light)
        }
        return true
    }

    /**
     * 使用 Android 15+ (API 35+) WindowInsetsController 方案设置状态栏模式
     *
     * @param window Window 实例
     * @param light true 表示浅色模式（黑色图标），false 表示深色模式（白色图标）
     * @return true 表示设置成功
     */
    @TargetApi(35)
    private fun setStatusBarLightModeApi35(window: Window, light: Boolean): Boolean {
        return try {
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightStatusBars = light
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // 降级到 Android 6.0 方案
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setAndroid6SetStatusBarLightMode(window, light)
            } else {
                false
            }
        }
    }

    /**
     * 使用 MIUI 方案设置状态栏模式
     *
     * 通过反射调用 MiuiWindowManager.LayoutParams 的 EXTRA_FLAG_STATUS_BAR_DARK_MODE
     *
     * @param window Window 实例
     * @param light true 表示浅色模式（黑色图标），false 表示深色模式（白色图标）
     * @return true 表示设置成功，false 表示失败
     */
    @SuppressLint("PrivateApi")
    fun setMISetStatusBarLightMode(window: Window?, light: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                val darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = window.javaClass.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                if (light) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag)
                }
                result = true
            } catch (ignored: Exception) {}
        }
        return result
    }

    /** 判断当前 MIUI 版本是否使用自定义状态栏亮色模式实现（MIUI V5-V8） */
    private val isMIUICustomStatusBarLightModeImpl: Boolean =
        DeviceHelper.isMIUIV5 || DeviceHelper.isMIUIV6 || DeviceHelper.isMIUIV7 || DeviceHelper.isMIUIV8

    /**
     * 使用 Flyme 方案设置状态栏模式
     *
     * 通过反射设置 WindowManager.LayoutParams 的 MEIZU_FLAG_DARK_STATUS_BAR_ICON
     *
     * @param window Window 实例
     * @param light true 表示浅色模式（黑色图标），false 表示深色模式（白色图标）
     * @return true 表示设置成功，false 表示失败
     */
    private fun setFlSetStatusBarLightMode(window: Window?, light: Boolean): Boolean {
        var result = false
        if (window != null) {
            setAndroid6SetStatusBarLightMode(window, light)
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                value = if (light) value or bit else value and bit.inv()
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (ignored: Exception) {}
        }
        return result
    }

    /**
     * 判断 Activity 是否为全屏模式
     *
     * @param activity Activity 实例
     * @return true 表示全屏，false 表示非全屏
     */
    fun isFullScreen(activity: Activity): Boolean {
        return try {
            val attrs = activity.window.attributes
            attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取系统状态栏透明 API 的标志值
     *
     * 用于 Samsung TouchWiz 和 Sony 等定制系统的特殊 API
     *
     * @param context Context 实例
     * @return 透明标志值，不支持则返回 null
     */
    fun getStatusBarAPITransparentValue(context: Context): Int? {
        if (sTransparentValue != null) return sTransparentValue

        val systemSharedLibraryNames = context.packageManager.systemSharedLibraryNames
        var fieldName: String? = null
        systemSharedLibraryNames?.let {
            for (lib in it) {
                when (lib) {
                    "touchwiz" -> fieldName = "SYSTEM_UI_FLAG_TRANSPARENT_BACKGROUND"
                    else -> if (lib.startsWith("com.sonyericsson.navigationbar")) fieldName = "SYSTEM_UI_FLAG_TRANSPARENT"
                }
            }
        }
        fieldName?.let {
            try {
                val field = View::class.java.getField(it)
                if (field.type == Int::class.javaPrimitiveType) {
                    sTransparentValue = field.getInt(null)
                }
            } catch (ignored: Exception) {}
        }
        return sTransparentValue
    }

    /**
     * 判断是否支持移植 Android 6.0 状态栏方案
     *
     * ZUK Z1 和 ZTE C2016 不支持
     *
     * @return true 表示支持，false 表示不支持
     */
    private fun supportTransplantStatusBar6(): Boolean = !(DeviceHelper.isZUKZ1() || DeviceHelper.isZTKC2016)

    /**
     * 获取状态栏的高度
     *
     * 使用缓存机制，只计算一次
     *
     * @param context Context 实例
     * @return 状态栏高度（px）
     */
    fun getStatusBarHeight(context: Context): Int {
        if (mStatusBarHeight == -1) {
            initStatusBarHeight(context)
        }
        return mStatusBarHeight
    }

    /**
     * 初始化状态栏高度
     *
     * 通过反射获取系统资源，魅族设备有特殊处理
     *
     * @param context Context 实例
     */
    @SuppressLint("PrivateApi")
    private fun initStatusBarHeight(context: Context) {
        var field: Field? = null
        try {
            val clazz = Class.forName("com.android.internal.R\$dimen")
            val obj = clazz.newInstance()
            if (DeviceHelper.isMeizu) {
                try {
                    field = clazz.getField("status_bar_height_large")
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            if (field == null) {
                field = clazz.getField("status_bar_height")
            }
            if (field != null && obj != null) {
                val id = field[obj]?.toString()?.toInt() ?: 0
                mStatusBarHeight = context.resources.getDimensionPixelSize(id)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        if (DeviceHelper.isTablet(context) && mStatusBarHeight > DisplayUtils.dp2px(context, STATUS_BAR_DEFAULT_HEIGHT_DP)) {
            mStatusBarHeight = 0
        } else if (mStatusBarHeight <= 0) {
            mStatusBarHeight = if (sVirtualDensity == -1f) {
                DisplayUtils.dp2px(context, STATUS_BAR_DEFAULT_HEIGHT_DP)
            } else {
                (STATUS_BAR_DEFAULT_HEIGHT_DP * sVirtualDensity + 0.5f).toInt()
            }
        }
    }

    /**
     * 设置虚拟屏幕密度
     *
     * 用于自定义状态栏高度计算
     *
     * @param density 虚拟密度值
     */
    fun setVirtualDensity(density: Float) {
        sVirtualDensity = density
    }

    /**
     * 设置虚拟屏幕 DPI
     *
     * 用于自定义状态栏高度计算
     *
     * @param densityDpi 虚拟 DPI 值
     */
    fun setVirtualDensityDpi(densityDpi: Float) {
        sVirtualDensityDpi = densityDpi
    }

    /**
     * 状态栏类型注解
     *
     * 限定值为 [STATUS_BAR_TYPE_DEFAULT]、[STATUS_BAR_TYPE_MI]、
     * [STATUS_BAR_TYPE_FL] 或 [STATUS_BAR_TYPE_ANDROID6]
     */
    @IntDef(STATUS_BAR_TYPE_DEFAULT, STATUS_BAR_TYPE_MI, STATUS_BAR_TYPE_FL, STATUS_BAR_TYPE_ANDROID6)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    private annotation class StatusBarType
}
