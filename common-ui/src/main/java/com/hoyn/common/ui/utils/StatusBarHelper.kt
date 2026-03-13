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
import androidx.core.view.ViewCompat
import com.hoyn.common.utils.device.DeviceHelper
import com.hoyn.common.utils.device.DisplayUtils
import java.lang.reflect.Field

/**
 * 状态栏工具类
 * 支持沉浸式状态栏、状态栏字体颜色设置
 */
object StatusBarHelper {
    private const val STATUS_BAR_TYPE_DEFAULT = 0
    private const val STATUS_BAR_TYPE_MI = 1
    private const val STATUS_BAR_TYPE_FL = 2
    private const val STATUS_BAR_TYPE_ANDROID6 = 3
    private const val STATUS_BAR_DEFAULT_HEIGHT_DP = 25

    private var sVirtualDensity = -1f
    private var sVirtualDensityDpi = -1f
    private var mStatusBarHeight = -1

    @StatusBarType
    private var mStatusBarType = STATUS_BAR_TYPE_DEFAULT
    private var sTransparentValue: Int? = null

    fun translucent(activity: Activity) {
        translucent(activity.window)
    }

    fun translucent(window: Window) {
        translucent(window, 0x40000000)
    }

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

    @TargetApi(19)
    fun translucent(window: Window, @ColorInt colorOn5x: Int) {
        if (!supportTranslucent()) return

        if (NotchHelper.isNotchOfficialSupport) {
            handleDisplayCutoutMode(window)
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

    @TargetApi(28)
    private fun realHandleDisplayCutoutMode(window: Window, decorView: View) {
        if (decorView.rootWindowInsets?.displayCutout != null) {
            val params = window.attributes
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params
        }
    }

    /**
     * 设置状态栏黑色字体图标
     */
    fun setStatusBarLightMode(activity: Activity?): Boolean {
        if (activity == null) return false
        if (DeviceHelper.isZTKC2016) return false

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

    private fun setStatusBarLightMode(activity: Activity, @StatusBarType type: Int): Boolean {
        return when (type) {
            STATUS_BAR_TYPE_MI -> setMISetStatusBarLightMode(activity.window, true)
            STATUS_BAR_TYPE_FL -> setFlSetStatusBarLightMode(activity.window, true)
            STATUS_BAR_TYPE_ANDROID6 -> setAndroid6SetStatusBarLightMode(activity.window, true)
            else -> false
        }
    }

    /**
     * 设置状态栏白色字体图标
     */
    fun setStatusBarDarkMode(activity: Activity?): Boolean {
        if (activity == null) return false
        if (mStatusBarType == STATUS_BAR_TYPE_DEFAULT) return true

        return when (mStatusBarType) {
            STATUS_BAR_TYPE_MI -> setMISetStatusBarLightMode(activity.window, false)
            STATUS_BAR_TYPE_FL -> setFlSetStatusBarLightMode(activity.window, false)
            STATUS_BAR_TYPE_ANDROID6 -> setAndroid6SetStatusBarLightMode(activity.window, false)
            else -> true
        }
    }

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

    private fun retainSystemUiFlag(window: Window, out: Int, type: Int): Int {
        var result = out
        val now = window.decorView.systemUiVisibility
        if (now and type == type) {
            result = result or type
        }
        return result
    }

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

    private val isMIUICustomStatusBarLightModeImpl: Boolean =
        DeviceHelper.isMIUIV5 || DeviceHelper.isMIUIV6 || DeviceHelper.isMIUIV7 || DeviceHelper.isMIUIV8

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

    fun isFullScreen(activity: Activity): Boolean {
        return try {
            val attrs = activity.window.attributes
            attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

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

    private fun supportTransplantStatusBar6(): Boolean = !(DeviceHelper.isZUKZ1() || DeviceHelper.isZTKC2016)

    /**
     * 获取状态栏的高度
     */
    fun getStatusBarHeight(context: Context): Int {
        if (mStatusBarHeight == -1) {
            initStatusBarHeight(context)
        }
        return mStatusBarHeight
    }

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

    fun setVirtualDensity(density: Float) {
        sVirtualDensity = density
    }

    fun setVirtualDensityDpi(densityDpi: Float) {
        sVirtualDensityDpi = densityDpi
    }

    @IntDef(STATUS_BAR_TYPE_DEFAULT, STATUS_BAR_TYPE_MI, STATUS_BAR_TYPE_FL, STATUS_BAR_TYPE_ANDROID6)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    private annotation class StatusBarType
}
