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
 *
 * 提供刘海屏检测、安全区域获取等功能，支持华为、vivo、OPPO、小米等品牌
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

    /**
     * 检测 vivo 设备是否有刘海屏
     *
     * 通过反射调用 FtFeature.isFeatureSupport 方法
     *
     * @param context Context 实例
     * @return true 表示有刘海屏，false 表示没有
     */
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

    /**
     * 检测华为设备是否有刘海屏
     *
     * 通过反射调用 HwNotchSizeUtil.hasNotchInScreen 方法
     *
     * @param context Context 实例
     * @return true 表示有刘海屏，false 表示没有
     */
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

    /**
     * 检测 OPPO 设备是否有刘海屏
     *
     * 通过检查系统特性来判断
     *
     * @param context Context 实例
     * @return true 表示有刘海屏，false 表示没有
     */
    fun hasNotchInOppo(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("com.oppo.feature.screen.heteromorphism")
    }

    /**
     * 检测小米设备是否有刘海屏
     *
     * 通过读取系统属性 ro.miui.notch 来判断
     *
     * @return true 表示有刘海屏，false 表示没有
     */
    @SuppressLint("PrivateApi")
    fun hasNotchInXiaomi(): Boolean {
        try {
            val spClass = Class.forName("android.os.SystemProperties")
            val getMethod = spClass.getDeclaredMethod(
                "getInt",
                String::class.java,
                Int::class.javaPrimitiveType
            )
            getMethod.isAccessible = true
            val hasNotch = getMethod.invoke(null, MIUI_NOTCH, 0) as Int
            return hasNotch == 1
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 检测 View 所在设备是否有刘海屏
     *
     * 使用缓存机制，只检测一次
     *
     * @param view View 实例
     * @return true 表示有刘海屏，false 表示没有
     */
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

    /**
     * 检测 Activity 所在设备是否有刘海屏
     *
     * 使用缓存机制，只检测一次
     *
     * @param activity Activity 实例
     * @return true 表示有刘海屏，false 表示没有
     */
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

    /**
     * 使用官方 API 检测是否有刘海屏
     *
     * 通过 DisplayCutout 判断，仅 API 28+ 支持
     *
     * @param view View 实例
     * @return true 表示检测成功，false 表示 View 未附加到窗口
     */
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

    /**
     * 检测第三方厂商的刘海屏
     *
     * 根据设备品牌调用相应的检测方法
     *
     * @param context Context 实例
     * @return true 表示有刘海屏，false 表示没有或不支持的厂商
     */
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

    /**
     * 获取 Activity 的安全区域顶部距离
     *
     * @param activity Activity 实例
     * @return 安全区域顶部距离（px），无刘海屏则返回 0
     */
    fun getSafeInsetTop(activity: Activity): Int {
        return if (!hasNotch(activity)) 0 else getSafeInsetRect(activity)?.top ?: 0
    }

    /**
     * 获取 Activity 的安全区域底部距离
     *
     * @param activity Activity 实例
     * @return 安全区域底部距离（px），无刘海屏则返回 0
     */
    fun getSafeInsetBottom(activity: Activity): Int {
        return if (!hasNotch(activity)) 0 else getSafeInsetRect(activity)?.bottom ?: 0
    }

    /**
     * 获取 Activity 的安全区域左侧距离
     *
     * @param activity Activity 实例
     * @return 安全区域左侧距离（px），无刘海屏则返回 0
     */
    fun getSafeInsetLeft(activity: Activity): Int {
        return if (!hasNotch(activity)) 0 else getSafeInsetRect(activity)?.left ?: 0
    }

    /**
     * 获取 Activity 的安全区域右侧距离
     *
     * @param activity Activity 实例
     * @return 安全区域右侧距离（px），无刘海屏则返回 0
     */
    fun getSafeInsetRight(activity: Activity): Int {
        return if (!hasNotch(activity)) 0 else getSafeInsetRect(activity)?.right ?: 0
    }

    /**
     * 获取 View 的安全区域顶部距离
     *
     * @param view View 实例
     * @return 安全区域顶部距离（px），无刘海屏则返回 0
     */
    fun getSafeInsetTop(view: View): Int {
        return if (!hasNotch(view)) 0 else getSafeInsetRect(view)?.top ?: 0
    }

    /**
     * 获取 View 的安全区域底部距离
     *
     * @param view View 实例
     * @return 安全区域底部距离（px），无刘海屏则返回 0
     */
    fun getSafeInsetBottom(view: View): Int {
        return if (!hasNotch(view)) 0 else getSafeInsetRect(view)?.bottom ?: 0
    }

    /**
     * 获取 View 的安全区域左侧距离
     *
     * @param view View 实例
     * @return 安全区域左侧距离（px），无刘海屏则返回 0
     */
    fun getSafeInsetLeft(view: View): Int {
        return if (!hasNotch(view)) 0 else getSafeInsetRect(view)?.left ?: 0
    }

    /**
     * 获取 View 的安全区域右侧距离
     *
     * @param view View 实例
     * @return 安全区域右侧距离（px），无刘海屏则返回 0
     */
    fun getSafeInsetRight(view: View): Int {
        return if (!hasNotch(view)) 0 else getSafeInsetRect(view)?.right ?: 0
    }

    /**
     * 清除所有方向的安全区域缓存
     */
    private fun clearAllRectInfo() {
        sRotation0SafeInset = null
        sRotation90SafeInset = null
        sRotation180SafeInset = null
        sRotation270SafeInset = null
    }

    /**
     * 清除竖屏方向的安全区域缓存
     */
    private fun clearPortraitRectInfo() {
        sRotation0SafeInset = null
        sRotation180SafeInset = null
    }

    /**
     * 清除横屏方向的安全区域缓存
     */
    private fun clearLandscapeRectInfo() {
        sRotation90SafeInset = null
        sRotation270SafeInset = null
    }

    /**
     * 获取 Activity 的安全区域 Rect
     *
     * 优先使用官方 API，否则使用第三方方案
     *
     * @param activity Activity 实例
     * @return 安全区域 Rect，可能为 null
     */
    private fun getSafeInsetRect(activity: Activity): Rect? {
        if (isNotchOfficialSupport) {
            val rect = Rect()
            val decorView = activity.window.decorView
            getOfficialSafeInsetRect(decorView, rect)
            return rect
        }
        return get3rdSafeInsetRect(activity)
    }

    /**
     * 获取 View 的安全区域 Rect
     *
     * 优先使用官方 API，否则使用第三方方案
     *
     * @param view View 实例
     * @return 安全区域 Rect，可能为 null
     */
    private fun getSafeInsetRect(view: View): Rect? {
        if (isNotchOfficialSupport) {
            val rect = Rect()
            getOfficialSafeInsetRect(view, rect)
            return rect
        }
        return get3rdSafeInsetRect(view.context)
    }

    /**
     * 使用官方 API 获取安全区域
     *
     * @param view View 实例
     * @param out 输出参数，安全区域数据将写入此 Rect
     */
    @TargetApi(28)
    private fun getOfficialSafeInsetRect(view: View?, out: Rect) {
        if (view == null) return
        val rootWindowInsets = view.rootWindowInsets ?: return
        val displayCutout = rootWindowInsets.displayCutout
        if (displayCutout != null) {
            out[displayCutout.safeInsetLeft, displayCutout.safeInsetTop, displayCutout.safeInsetRight] =
                displayCutout.safeInsetBottom
        }
    }

    /**
     * 获取第三方厂商的安全区域 Rect
     *
     * 根据屏幕旋转方向返回对应的安全区域
     *
     * @param context Context 实例
     * @return 安全区域 Rect，可能为 null
     */
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
                if (sRotation90SafeInset == null) sRotation90SafeInset =
                    getRectInfoRotation90(context)
                sRotation90SafeInset
            }

            Surface.ROTATION_180 -> {
                if (sRotation180SafeInset == null) sRotation180SafeInset =
                    getRectInfoRotation180(context)
                sRotation180SafeInset
            }

            Surface.ROTATION_270 -> {
                if (sRotation270SafeInset == null) sRotation270SafeInset =
                    getRectInfoRotation270(context)
                sRotation270SafeInset
            }

            else -> {
                if (sRotation0SafeInset == null) sRotation0SafeInset = getRectInfoRotation0(context)
                sRotation0SafeInset
            }
        }
    }

    /**
     * 获取 0 度旋转（竖屏）的安全区域
     *
     * @param context Context 实例
     * @return 安全区域 Rect
     */
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

    /**
     * 获取 90 度旋转（横屏，刘海在左侧）的安全区域
     *
     * @param context Context 实例
     * @return 安全区域 Rect
     */
    private fun getRectInfoRotation90(context: Context): Rect {
        val rect = Rect()
        when {
            DeviceHelper.isVivo -> rect.left = getNotchHeightInVivo(context)
            DeviceHelper.isOppo -> rect.left = StatusBarHelper.getStatusBarHeight(context)
            DeviceHelper.isHuawei -> {
                rect.left =
                    if (sHuaweiIsNotchSetToShow == true) getNotchSizeInHuawei(context)?.get(1)
                        ?: 0 else 0
            }

            DeviceHelper.isXiaomi -> rect.left = getNotchHeightInXiaomi(context)
        }
        rect.right = 0
        return rect
    }

    /**
     * 获取 180 度旋转（反向竖屏）的安全区域
     *
     * @param context Context 实例
     * @return 安全区域 Rect
     */
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

    /**
     * 获取 270 度旋转（横屏，刘海在右侧）的安全区域
     *
     * @param context Context 实例
     * @return 安全区域 Rect
     */
    private fun getRectInfoRotation270(context: Context): Rect {
        val rect = Rect()
        when {
            DeviceHelper.isVivo -> rect.right = getNotchHeightInVivo(context)
            DeviceHelper.isOppo -> rect.right = StatusBarHelper.getStatusBarHeight(context)
            DeviceHelper.isHuawei -> {
                rect.right =
                    if (sHuaweiIsNotchSetToShow == true) getNotchSizeInHuawei(context)?.get(1)
                        ?: 0 else 0
            }

            DeviceHelper.isXiaomi -> rect.right = getNotchHeightInXiaomi(context)
        }
        rect.left = 0
        return rect
    }

    /**
     * 获取华为设备的刘海尺寸
     *
     * @param context Context 实例
     * @return IntArray，index 0 为宽度，index 1 为高度，失败返回 null
     */
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

    /**
     * 获取小米设备的刘海宽度
     *
     * @param context Context 实例
     * @return 刘海宽度（px），获取失败返回 -1
     */
    fun getNotchWidthInXiaomi(context: Context): Int {
        val resourceId = context.resources.getIdentifier("notch_width", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else -1
    }

    /**
     * 获取小米设备的刘海高度
     *
     * @param context Context 实例
     * @return 刘海高度（px），获取失败返回状态栏高度
     */
    fun getNotchHeightInXiaomi(context: Context): Int {
        val resourceId = context.resources.getIdentifier("notch_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else DisplayUtils.getStatusBarHeight(
            context
        )
    }

    /**
     * 获取 vivo 设备的刘海宽度
     *
     * @param context Context 实例
     * @return 刘海宽度（px），固定为 100dp
     */
    fun getNotchWidthInVivo(context: Context): Int = DisplayUtils.dp2px(context, 100)

    /**
     * 获取 vivo 设备的刘海高度
     *
     * @param context Context 实例
     * @return 刘海高度（px），固定为 27dp
     */
    fun getNotchHeightInVivo(context: Context): Int = DisplayUtils.dp2px(context, 27)

    /**
     * 获取屏幕旋转角度
     *
     * @param context Context 实例
     * @return 屏幕旋转角度
     */
    private fun getScreenRotation(context: Context): Int {
        val w = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = w.defaultDisplay ?: return Surface.ROTATION_0
        return display.rotation
    }

    /**
     * 判断系统是否官方支持刘海屏 API
     *
     * @return true 表示 API 28+ 支持官方刘海屏 API，false 表示不支持
     */
    val isNotchOfficialSupport: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    /**
     * 判断是否需要修复横屏时的刘海区域 fitSystemWindows 问题
     *
     * fitSystemWindows 对小米、vivo 挖孔屏横屏挖孔区域无效
     *
     * @param view View 实例
     * @return true 表示需要修复，false 表示不需要
     */
    fun needFixLandscapeNotchAreaFitSystemWindow(view: View): Boolean {
        return (DeviceHelper.isXiaomi || DeviceHelper.isVivo) && hasNotch(view)
    }
}
