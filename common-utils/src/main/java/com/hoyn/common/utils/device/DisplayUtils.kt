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
package com.hoyn.common.utils.device

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import java.util.*

/**
 * 屏幕显示工具类
 *
 * @author cginechen
 * @date 2016-03-17
 */
object DisplayUtils {
    /**
     * 屏幕密度,系统源码注释不推荐使用
     */
    val DENSITY = Resources.getSystem().displayMetrics.density
    private const val TAG = "DisplayUtils"

    /**
     * 是否有摄像头
     */
    private var sHasCamera: Boolean? = null

    /**
     * 获取 DisplayMetrics
     */
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        return context.resources.displayMetrics
    }

    /**
     * 把以 dp 为单位的值，转化为以 px 为单位的值
     *
     * @param dpValue 以 dp 为单位的值
     * @return px value
     */
    fun dpToPx(dpValue: Int): Int {
        return (dpValue * DENSITY + 0.5f).toInt()
    }

    /**
     * 把以 px 为单位的值，转化为以 dp 为单位的值
     *
     * @param pxValue 以 px 为单位的值
     * @return dp值
     */
    fun pxToDp(pxValue: Float): Int {
        return (pxValue / DENSITY + 0.5f).toInt()
    }

    fun getDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    fun getFontDensity(context: Context): Float {
        return context.resources.displayMetrics.scaledDensity
    }

    /**
     * 获取屏幕宽度
     */
    fun getScreenWidth(context: Context): Int {
        return getDisplayMetrics(context).widthPixels
    }

    /**
     * 获取屏幕高度
     */
    fun getScreenHeight(context: Context): Int {
        var screenHeight = getDisplayMetrics(context).heightPixels
        if (DeviceHelper.isXiaomi && xiaomiNavigationGestureEnabled(context)) {
            screenHeight += getResourceNavHeight(context)
        }
        return screenHeight
    }

    /**
     * 获取屏幕的真实宽高
     */
    fun getRealScreenSize(context: Context): IntArray {
        return doGetRealScreenSize(context)
    }

    private fun doGetRealScreenSize(context: Context): IntArray {
        val size = IntArray(2)
        var widthPixels: Int
        var heightPixels: Int
        val w = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val d = w.defaultDisplay
        val metrics = DisplayMetrics()
        d.getMetrics(metrics)
        // since SDK_INT = 1;
        widthPixels = metrics.widthPixels
        heightPixels = metrics.heightPixels
        try {
            // used when 17 > SDK_INT >= 14; includes window decorations (statusbar bar/menu bar)
            widthPixels = Display::class.java.getMethod("getRawWidth").invoke(d) as Int
            heightPixels = Display::class.java.getMethod("getRawHeight").invoke(d) as Int
        } catch (ignored: Exception) {
        }
        try {
            // used when SDK_INT >= 17; includes window decorations (statusbar bar/menu bar)
            val realSize = Point()
            d.getRealSize(realSize)
            Display::class.java.getMethod("getRealSize", Point::class.java).invoke(d, realSize)
            widthPixels = realSize.x
            heightPixels = realSize.y
        } catch (ignored: Exception) {
        }
        size[0] = widthPixels
        size[1] = heightPixels
        return size
    }

    fun isNavMenuExist(context: Context): Boolean {
        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        return !hasMenuKey && !hasBackKey
    }

    /**
     * 单位转换: dp -> px
     */
    fun dp2px(context: Context, dp: Int): Int {
        return (getDensity(context) * dp + 0.5).toInt()
    }

    /**
     * 单位转换: sp -> px
     */
    fun sp2px(context: Context, sp: Int): Int {
        return (getFontDensity(context) * sp + 0.5).toInt()
    }

    /**
     * 单位转换:px -> dp
     */
    fun px2dp(context: Context, px: Int): Int {
        return (px / getDensity(context) + 0.5).toInt()
    }

    /**
     * 单位转换:px -> sp
     */
    fun px2sp(context: Context, px: Int): Int {
        return (px / getFontDensity(context) + 0.5).toInt()
    }

    /**
     * 判断是否有状态栏
     */
    fun hasStatusBar(context: Context?): Boolean {
        if (context is Activity) {
            val attrs = context.window.attributes
            return attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != WindowManager.LayoutParams.FLAG_FULLSCREEN
        }
        return true
    }

    /**
     * 获取ActionBar高度
     */
    fun getActionBarHeight(context: Context): Int {
        var actionBarHeight = 0
        val tv = TypedValue()
        if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(
                tv.data, context.resources.displayMetrics
            )
        }
        return actionBarHeight
    }

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        if (DeviceHelper.isXiaomi) {
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                context.resources.getDimensionPixelSize(resourceId)
            } else 0
        }
        try {
            val c = Class.forName("com.android.internal.R\$dimen")
            val obj = c.newInstance()
            val field = c.getField("status_bar_height")
            val x = field[obj].toString().toInt()
            if (x > 0) {
                return context.resources.getDimensionPixelSize(x)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 获取虚拟菜单的高度,若无则返回0
     */
    fun getNavMenuHeight(context: Context): Int {
        if (!isNavMenuExist(context)) {
            return 0
        }
        val resourceNavHeight = getResourceNavHeight(context)
        return if (resourceNavHeight >= 0) {
            resourceNavHeight
        } else getRealScreenSize(context)[1] - getScreenHeight(context)
    }

    private fun getResourceNavHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else -1
    }

    fun hasCamera(context: Context): Boolean {
        if (sHasCamera == null) {
            val pckMgr = context.packageManager
            val flag = pckMgr.hasSystemFeature("android.hardware.camera.front")
            val flag1 = pckMgr.hasSystemFeature("android.hardware.camera")
            sHasCamera = flag || flag1
        }
        return sHasCamera!!
    }

    /**
     * 是否有硬件menu
     */
    fun hasHardwareMenuKey(context: Context): Boolean {
        return ViewConfiguration.get(context).hasPermanentMenuKey()
    }

    /**
     * 是否有网络功能
     */
    @SuppressLint("MissingPermission")
    fun hasInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    /**
     * 判断 SD Card 是否 ready
     */
    val isSdcardReady: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    /**
     * 获取当前国家的语言
     */
    fun getCurCountryLan(context: Context): String {
        val config = context.resources.configuration
        val sysLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            config.locale
        }
        return (sysLocale.language + "-" + sysLocale.country)
    }

    /**
     * 判断是否为中文环境
     */
    fun isZhCN(context: Context): Boolean {
        val config = context.resources.configuration
        val sysLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            config.locale
        }
        val lang = sysLocale.country
        return lang.equals("CN", ignoreCase = true)
    }

    /**
     * 设置全屏
     */
    fun setFullScreen(activity: Activity) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /**
     * 取消全屏
     */
    fun cancelFullScreen(activity: Activity) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    /**
     * 判断是否全屏
     */
    fun isFullScreen(activity: Activity): Boolean {
        val params = activity.window.attributes
        return params.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == WindowManager.LayoutParams.FLAG_FULLSCREEN
    }

    val isElevationSupported: Boolean
        get() = Build.VERSION.SDK_INT >= 21

    fun hasNavigationBar(context: Context): Boolean {
        val hasNav = deviceHasNavigationBar()
        if (!hasNav) {
            return false
        }
        return if (DeviceHelper.isVivo) {
            vivoNavigationGestureEnabled(context)
        } else true
    }

    /**
     * 判断设备是否存在NavigationBar
     *
     * @return true 存在, false 不存在
     */
    private fun deviceHasNavigationBar(): Boolean {
        var haveNav = false
        try {
            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val getWmServiceMethod = windowManagerGlobalClass.getDeclaredMethod("getWindowManagerService")
            getWmServiceMethod.isAccessible = true
            val iWindowManager = getWmServiceMethod.invoke(null)
            val iWindowManagerClass: Class<*> = iWindowManager.javaClass
            val hasNavBarMethod = iWindowManagerClass.getDeclaredMethod("hasNavigationBar")
            hasNavBarMethod.isAccessible = true
            haveNav = hasNavBarMethod.invoke(iWindowManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return haveNav
    }

    // ====================== Setting ===========================
    private const val VIVO_NAVIGATION_GESTURE = "navigation_gesture_on"
    private const val HUAWAI_DISPLAY_NOTCH_STATUS = "display_notch_status"
    private const val XIAOMI_DISPLAY_NOTCH_STATUS = "force_black"
    private const val XIAOMI_FULLSCREEN_GESTURE = "force_fsg_nav_bar"

    /**
     * 获取vivo手机设置中的"navigation_gesture_on"值，判断当前系统是使用导航键还是手势导航操作
     *
     * @param context app Context
     * @return false 表示使用的是虚拟导航键(NavigationBar)， true 表示使用的是手势， 默认是false
     */
    fun vivoNavigationGestureEnabled(context: Context): Boolean {
        val `val` = Settings.Secure.getInt(context.contentResolver, VIVO_NAVIGATION_GESTURE, 0)
        return `val` != 0
    }

    fun xiaomiNavigationGestureEnabled(context: Context): Boolean {
        val `val` = Settings.Global.getInt(context.contentResolver, XIAOMI_FULLSCREEN_GESTURE, 0)
        return `val` != 0
    }

    fun huaweiIsNotchSetToShowInSetting(context: Context): Boolean {
        // 0: 默认
        // 1: 隐藏显示区域
        val result = Settings.Secure.getInt(context.contentResolver, HUAWAI_DISPLAY_NOTCH_STATUS, 0)
        return result == 0
    }

    @TargetApi(17)
    fun xiaomiIsNotchSetToShowInSetting(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, XIAOMI_DISPLAY_NOTCH_STATUS, 0) == 0
    }
}
