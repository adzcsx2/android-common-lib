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
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.WindowManager
import com.hoyn.common.utils.device.DeviceHelper
import com.hoyn.common.utils.device.DisplayUtils

/**
 * 刘海屏适配工具类
 */
object NotchHelper {
    private const val TAG = "NotchHelper"
    private const val NOTCH_IN_SCREEN_VOIO = 0x00000020
    private const val MIUI_NOTCH = "ro.miui.notch"
    private var sHasNotch: Boolean? = null
    private var sRotation0SafeInset: Rect? = null
    private var sRotation90SafeInset: Rect? = null
    private var sRotation180SafeInset: Rect? = null
    private var sRotation270SafeInset: Rect? = null
    private var sNotchSizeInHawei: IntArray? = null
    private var sHuaweiIsNotchSetToShow: Boolean? = null

    fun hasNotchInVivo(context: Context): Boolean {
        var ret = false
        try {
            val cl = context.classLoader
            val ftFeature = cl.loadClass("android.util.FtFeature")
            val methods = ftFeature.declaredMethods
            for (i in methods.indices) {
                val method = methods[i]
                if (method.name.equals("isFeatureSupport", ignoreCase = true)) {
                    ret = method.invoke(ftFeature, NOTCH_IN_SCREEN_VOIO) as Boolean
                    break
                }
            }
        } catch (e: ClassNotFoundException) {
            Log.i(TAG, "hasNotchInVivo ClassNotFoundException")
        } catch (e: Exception) {
            Log.e(TAG, "hasNotchInVivo Exception")
        }
        return ret
    }

    fun hasNotchInHuawei(context: Context): Boolean {
        var hasNotch = false
        try {
            val cl = context.classLoader
            val HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil")
            val get = HwNotchSizeUtil.getMethod("hasNotchInScreen")
            hasNotch = get.invoke(HwNotchSizeUtil) as Boolean
        } catch (e: ClassNotFoundException) {
            Log.i(TAG, "hasNotchInHuawei ClassNotFoundException")
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "hasNotchInHuawei NoSuchMethodException")
        } catch (e: Exception) {
            Log.e(TAG, "hasNotchInHuawei Exception")
        }
        return hasNotch
    }

    fun hasNotchInOppo(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("com.oppo.feature.screen.heteromorphism")
    }

    @SuppressLint("PrivateApi")
    fun hasNotchInXiaomi(): Boolean {
        try {
            val spClass = Class.forName("android.os.SystemProperties")
            val getMethod = spClass.getDeclaredMethod("getInt", String::class.java, Int::class.javaPrimitiveType)
            getMethod.isAccessible = true
            val hasNotch = getMethod.invoke(null, MIUI_NOTCH, 0) as Int
            return hasNotch == 1
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun hasNotch(view: View): Boolean {
        if (sHasNotch == null) {
            if (isNotchOfficialSupport) {
                if (!attachHasOfficialNotch(view)) {
                    return false
                }
            } else {
                sHasNotch = has3rdNotch(view.context)
            }
        }
        return sHasNotch!!
    }

    fun hasNotch(activity: Activity): Boolean {
        if (sHasNotch == null) {
            if (isNotchOfficialSupport) {
                val window = activity.window ?: return false
                val decorView = window.decorView
                if (!attachHasOfficialNotch(decorView)) {
                    return false
                }
            } else {
                sHasNotch = has3rdNotch(activity)
            }
        }
        return sHasNotch!!
    }

    @TargetApi(28)
    private fun attachHasOfficialNotch(view: View): Boolean {
        val windowInsets = view.rootWindowInsets
        return if (windowInsets != null) {
            val displayCutout = windowInsets.displayCutout
            sHasNotch = displayCutout != null
            true
        } else {
            false
        }
    }

    fun has3rdNotch(context: Context): Boolean {
        if (DeviceHelper.isHuawei) {
            return hasNotchInHuawei(context)
        } else if (DeviceHelper.isVivo) {
            return hasNotchInVivo(context)
        } else if (DeviceHelper.isOppo) {
            return hasNotchInOppo(context)
        } else if (DeviceHelper.isXiaomi) {
            return hasNotchInXiaomi()
        }
        return false
    }

    fun getSafeInsetTop(activity: Activity): Int {
        return if (!hasNotch(activity)) 0 else getSafeInsetRect(activity)?.top ?: 0
    }

    fun getSafeInsetBottom(activity: Activity): Int {
        return if (!hasNotch(activity)) 0 else getSafeInsetRect(activity)?.bottom ?: 0
    }

    fun getSafeInsetLeft(activity: Activity): Int {
        return if (!hasNotch(activity)) 0 else getSafeInsetRect(activity)?.left ?: 0
    }

    fun getSafeInsetRight(activity: Activity): Int {
        return if (!hasNotch(activity)) 0 else getSafeInsetRect(activity)?.right ?: 0
    }

    fun getSafeInsetTop(view: View): Int {
        return if (!hasNotch(view)) 0 else getSafeInsetRect(view)?.top ?: 0
    }

    fun getSafeInsetBottom(view: View): Int {
        return if (!hasNotch(view)) 0 else getSafeInsetRect(view)?.bottom ?: 0
    }

    fun getSafeInsetLeft(view: View): Int {
        return if (!hasNotch(view)) 0 else getSafeInsetRect(view)?.left ?: 0
    }

    fun getSafeInsetRight(view: View): Int {
        return if (!hasNotch(view)) 0 else getSafeInsetRect(view)?.right ?: 0
    }

    private fun clearAllRectInfo() {
        sRotation0SafeInset = null
        sRotation90SafeInset = null
        sRotation180SafeInset = null
        sRotation270SafeInset = null
    }

    private fun clearPortraitRectInfo() {
        sRotation0SafeInset = null
        sRotation180SafeInset = null
    }

    private fun clearLandscapeRectInfo() {
        sRotation90SafeInset = null
        sRotation270SafeInset = null
    }

    private fun getSafeInsetRect(activity: Activity): Rect? {
        if (isNotchOfficialSupport) {
            val rect = Rect()
            val decorView = activity.window.decorView
            getOfficialSafeInsetRect(decorView, rect)
            return rect
        }
        return get3rdSafeInsetRect(activity)
    }

    private fun getSafeInsetRect(view: View): Rect? {
        if (isNotchOfficialSupport) {
            val rect = Rect()
            getOfficialSafeInsetRect(view, rect)
            return rect
        }
        return get3rdSafeInsetRect(view.context)
    }

    @TargetApi(28)
    private fun getOfficialSafeInsetRect(view: View?, out: Rect) {
        if (view == null) return
        val rootWindowInsets = view.rootWindowInsets ?: return
        val displayCutout = rootWindowInsets.displayCutout
        if (displayCutout != null) {
            out[displayCutout.safeInsetLeft, displayCutout.safeInsetTop, displayCutout.safeInsetRight] = displayCutout.safeInsetBottom
        }
    }

    private fun get3rdSafeInsetRect(context: Context): Rect? {
        if (DeviceHelper.isHuawei) {
            val isHuaweiNotchSetToShow = DisplayUtils.huaweiIsNotchSetToShowInSetting(context)
            if (sHuaweiIsNotchSetToShow != null && sHuaweiIsNotchSetToShow != isHuaweiNotchSetToShow) {
                clearLandscapeRectInfo()
            }
            sHuaweiIsNotchSetToShow = isHuaweiNotchSetToShow
        }
        val screenRotation = getScreenRotation(context)
        return when (screenRotation) {
            Surface.ROTATION_90 -> {
                if (sRotation90SafeInset == null) sRotation90SafeInset = getRectInfoRotation90(context)
                sRotation90SafeInset
            }
            Surface.ROTATION_180 -> {
                if (sRotation180SafeInset == null) sRotation180SafeInset = getRectInfoRotation180(context)
                sRotation180SafeInset
            }
            Surface.ROTATION_270 -> {
                if (sRotation270SafeInset == null) sRotation270SafeInset = getRectInfoRotation270(context)
                sRotation270SafeInset
            }
            else -> {
                if (sRotation0SafeInset == null) sRotation0SafeInset = getRectInfoRotation0(context)
                sRotation0SafeInset
            }
        }
    }

    private fun getRectInfoRotation0(context: Context): Rect {
        val rect = Rect()
        when {
            DeviceHelper.isVivo -> rect.top = getNotchHeightInVivo(context)
            DeviceHelper.isOppo -> rect.top = StatusBarHelper.getStatusBarHeight(context)
            DeviceHelper.isHuawei -> {
                val notchSize = getNotchSizeInHuawei(context)
                rect.top = notchSize?.get(1) ?: 0
            }
            DeviceHelper.isXiaomi -> rect.top = getNotchHeightInXiaomi(context)
        }
        rect.bottom = 0
        return rect
    }

    private fun getRectInfoRotation90(context: Context): Rect {
        val rect = Rect()
        when {
            DeviceHelper.isVivo -> rect.left = getNotchHeightInVivo(context)
            DeviceHelper.isOppo -> rect.left = StatusBarHelper.getStatusBarHeight(context)
            DeviceHelper.isHuawei -> {
                rect.left = if (sHuaweiIsNotchSetToShow == true) getNotchSizeInHuawei(context)?.get(1) ?: 0 else 0
            }
            DeviceHelper.isXiaomi -> rect.left = getNotchHeightInXiaomi(context)
        }
        rect.right = 0
        return rect
    }

    private fun getRectInfoRotation180(context: Context): Rect {
        val rect = Rect()
        when {
            DeviceHelper.isVivo -> rect.bottom = getNotchHeightInVivo(context)
            DeviceHelper.isOppo -> rect.bottom = StatusBarHelper.getStatusBarHeight(context)
            DeviceHelper.isHuawei -> {
                val notchSize = getNotchSizeInHuawei(context)
                rect.bottom = notchSize?.get(1) ?: 0
            }
            DeviceHelper.isXiaomi -> rect.bottom = getNotchHeightInXiaomi(context)
        }
        rect.top = 0
        return rect
    }

    private fun getRectInfoRotation270(context: Context): Rect {
        val rect = Rect()
        when {
            DeviceHelper.isVivo -> rect.right = getNotchHeightInVivo(context)
            DeviceHelper.isOppo -> rect.right = StatusBarHelper.getStatusBarHeight(context)
            DeviceHelper.isHuawei -> {
                rect.right = if (sHuaweiIsNotchSetToShow == true) getNotchSizeInHuawei(context)?.get(1) ?: 0 else 0
            }
            DeviceHelper.isXiaomi -> rect.right = getNotchHeightInXiaomi(context)
        }
        rect.left = 0
        return rect
    }

    fun getNotchSizeInHuawei(context: Context): IntArray? {
        if (sNotchSizeInHawei == null) {
            sNotchSizeInHawei = intArrayOf(0, 0)
            try {
                val cl = context.classLoader
                val HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil")
                val get = HwNotchSizeUtil.getMethod("getNotchSize")
                sNotchSizeInHawei = get.invoke(HwNotchSizeUtil) as IntArray
            } catch (e: Exception) {
                Log.e(TAG, "getNotchSizeInHuawei Exception")
            }
        }
        return sNotchSizeInHawei
    }

    fun getNotchWidthInXiaomi(context: Context): Int {
        val resourceId = context.resources.getIdentifier("notch_width", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else -1
    }

    fun getNotchHeightInXiaomi(context: Context): Int {
        val resourceId = context.resources.getIdentifier("notch_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else DisplayUtils.getStatusBarHeight(context)
    }

    fun getNotchWidthInVivo(context: Context): Int = DisplayUtils.dp2px(context, 100)

    fun getNotchHeightInVivo(context: Context): Int = DisplayUtils.dp2px(context, 27)

    private fun getScreenRotation(context: Context): Int {
        val w = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = w.defaultDisplay ?: return Surface.ROTATION_0
        return display.rotation
    }

    val isNotchOfficialSupport: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    /**
     * fitSystemWindows 对小米、vivo挖孔屏横屏挖孔区域无效
     */
    fun needFixLandscapeNotchAreaFitSystemWindow(view: View): Boolean {
        return (DeviceHelper.isXiaomi || DeviceHelper.isVivo) && hasNotch(view)
    }
}
