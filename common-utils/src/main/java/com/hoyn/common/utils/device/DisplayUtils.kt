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
     *
     * 返回指定 Context 的显示度量信息
     *
     * @param context Context 实例
     * @return DisplayMetrics 对象
     */
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        return context.resources.displayMetrics
    }

    /**
     * 把以 dp 为单位的值，转化为以 px 为单位的值
     *
     * 使用系统屏幕密度进行转换
     *
     * @param dpValue 以 dp 为单位的值
     * @return px 值
     */
    fun dpToPx(dpValue: Int): Int {
        return (dpValue * DENSITY + 0.5f).toInt()
    }

    /**
     * 把以 px 为单位的值，转化为以 dp 为单位的值
     *
     * 使用系统屏幕密度进行转换
     *
     * @param pxValue 以 px 为单位的值
     * @return dp 值
     */
    fun pxToDp(pxValue: Float): Int {
        return (pxValue / DENSITY + 0.5f).toInt()
    }

    /**
     * 获取屏幕密度
     *
     * @param context Context 实例
     * @return 屏幕密度值
     */
    fun getDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * 获取字体密度
     *
     * @param context Context 实例
     * @return 字体缩放密度值
     */
    fun getFontDensity(context: Context): Float {
        return context.resources.displayMetrics.scaledDensity
    }

    /**
     * 获取屏幕宽度
     *
     * @param context Context 实例
     * @return 屏幕宽度（px）
     */
    fun getScreenWidth(context: Context): Int {
        return getDisplayMetrics(context).widthPixels
    }

    /**
     * 获取屏幕高度
     *
     * 对于小米设备，如果启用了手势导航，会加上导航栏高度
     *
     * @param context Context 实例
     * @return 屏幕高度（px）
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
     *
     * 包含窗口装饰（状态栏、导航栏）的完整屏幕尺寸
     *
     * @param context Context 实例
     * @return 包含宽度和高度的 IntArray，index 0 为宽度，index 1 为高度
     */
    fun getRealScreenSize(context: Context): IntArray {
        return doGetRealScreenSize(context)
    }

    /**
     * 获取屏幕真实宽高的内部实现
     *
     * 兼容不同 Android 版本的 API
     *
     * @param context Context 实例
     * @return 包含宽度和高度的 IntArray
     */
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

    /**
     * 判断设备是否存在虚拟导航菜单
     *
     * 通过检查设备是否有物理返回键和菜单键来判断
     *
     * @param context Context 实例
     * @return true 表示存在虚拟导航菜单，false 表示不存在
     */
    fun isNavMenuExist(context: Context): Boolean {
        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        return !hasMenuKey && !hasBackKey
    }

    /**
     * 单位转换: dp -> px
     *
     * @param context Context 实例
     * @param dp dp 值
     * @return 转换后的 px 值
     */
    fun dp2px(context: Context, dp: Int): Int {
        return (getDensity(context) * dp + 0.5).toInt()
    }

    /**
     * 单位转换: sp -> px
     *
     * @param context Context 实例
     * @param sp sp 值
     * @return 转换后的 px 值
     */
    fun sp2px(context: Context, sp: Int): Int {
        return (getFontDensity(context) * sp + 0.5).toInt()
    }

    /**
     * 单位转换: px -> dp
     *
     * @param context Context 实例
     * @param px px 值
     * @return 转换后的 dp 值
     */
    fun px2dp(context: Context, px: Int): Int {
        return (px / getDensity(context) + 0.5).toInt()
    }

    /**
     * 单位转换: px -> sp
     *
     * @param context Context 实例
     * @param px px 值
     * @return 转换后的 sp 值
     */
    fun px2sp(context: Context, px: Int): Int {
        return (px / getFontDensity(context) + 0.5).toInt()
    }

    /**
     * 判断是否有状态栏
     *
     * 通过检查窗口的 FLAG_FULLSCREEN 标志来判断
     *
     * @param context Context 实例，如果是 Activity 会进行更精确的判断
     * @return true 表示有状态栏，false 表示全屏无状态栏
     */
    fun hasStatusBar(context: Context?): Boolean {
        if (context is Activity) {
            val attrs = context.window.attributes
            return attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != WindowManager.LayoutParams.FLAG_FULLSCREEN
        }
        return true
    }

    /**
     * 获取 ActionBar 高度
     *
     * 通过解析主题属性获取 ActionBar 的高度
     *
     * @param context Context 实例
     * @return ActionBar 高度（px），如果未设置则返回 0
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
     *
     * 对小米设备有特殊处理，通过反射获取系统资源
     *
     * @param context Context 实例
     * @return 状态栏高度（px）
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
     * 获取虚拟导航菜单的高度
     *
     * 如果不存在虚拟导航菜单则返回 0
     *
     * @param context Context 实例
     * @return 导航菜单高度（px），不存在则返回 0
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

    /**
     * 从资源获取导航栏高度
     *
     * @param context Context 实例
     * @return 导航栏高度（px），如果获取失败则返回 -1
     */
    private fun getResourceNavHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else -1
    }

    /**
     * 判断设备是否有摄像头
     *
     * 检查前置摄像头或后置摄像头的存在
     *
     * @param context Context 实例
     * @return true 表示有摄像头，false 表示没有
     */
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
     * 判断是否有硬件菜单键
     *
     * @param context Context 实例
     * @return true 表示有硬件菜单键，false 表示没有
     */
    fun hasHardwareMenuKey(context: Context): Boolean {
        return ViewConfiguration.get(context).hasPermanentMenuKey()
    }

    /**
     * 判断设备是否有网络连接
     *
     * @param context Context 实例
     * @return true 表示有网络连接，false 表示没有
     */
    @SuppressLint("MissingPermission")
    fun hasInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    /**
     * 判断 SD 卡是否就绪
     *
     * @return true 表示 SD 卡已挂载可读写，false 表示不可用
     */
    val isSdcardReady: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    /**
     * 获取当前国家的语言代码
     *
     * 返回格式如 "zh-CN"、"en-US"
     *
     * @param context Context 实例
     * @return 语言-国家代码
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
     * 判断是否为中文（中国）环境
     *
     * @param context Context 实例
     * @return true 表示当前语言为简体中文（中国），false 表示不是
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
     * 设置 Activity 为全屏模式
     *
     * 添加全屏和布局无限制标志
     *
     * @param activity 要设置为全屏的 Activity
     */
    fun setFullScreen(activity: Activity) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /**
     * 取消 Activity 的全屏模式
     *
     * 清除全屏和布局无限制标志
     *
     * @param activity 要取消全屏的 Activity
     */
    fun cancelFullScreen(activity: Activity) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    /**
     * 判断 Activity 是否为全屏模式
     *
     * @param activity 要检查的 Activity
     * @return true 表示为全屏模式，false 表示不是
     */
    fun isFullScreen(activity: Activity): Boolean {
        val params = activity.window.attributes
        return params.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == WindowManager.LayoutParams.FLAG_FULLSCREEN
    }

    /**
     * 判断设备是否支持 Z 轴高度（阴影效果）
     *
     * @return true 表示 API 21 及以上支持阴影效果，false 表示不支持
     */
    val isElevationSupported: Boolean
        get() = Build.VERSION.SDK_INT >= 21

    /**
     * 判断设备是否有导航栏
     *
     * 对 vivo 设备进行特殊处理，检查是否启用了手势导航
     *
     * @param context Context 实例
     * @return true 表示有导航栏，false 表示没有
     */
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
     * 判断设备硬件是否存在 NavigationBar
     *
     * 通过反射调用 WindowManagerGlobal 的方法来判断
     *
     * @return true 表示存在，false 表示不存在
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
     * 获取 vivo 手机设置中的导航手势状态
     *
     * 判断当前系统是使用导航键还是手势导航操作
     *
     * @param context 应用 Context
     * @return false 表示使用的是虚拟导航键（NavigationBar），true 表示使用的是手势，默认是 false
     */
    fun vivoNavigationGestureEnabled(context: Context): Boolean {
        val `val` = Settings.Secure.getInt(context.contentResolver, VIVO_NAVIGATION_GESTURE, 0)
        return `val` != 0
    }

    /**
     * 获取小米手机的全面屏手势状态
     *
     * @param context Context 实例
     * @return true 表示启用了全面屏手势，false 表示使用虚拟导航键
     */
    fun xiaomiNavigationGestureEnabled(context: Context): Boolean {
        val `val` = Settings.Global.getInt(context.contentResolver, XIAOMI_FULLSCREEN_GESTURE, 0)
        return `val` != 0
    }

    /**
     * 判断华为手机的刘海屏是否设置为显示
     *
     * @param context Context 实例
     * @return true 表示设置为显示刘海区域，false 表示隐藏
     */
    fun huaweiIsNotchSetToShowInSetting(context: Context): Boolean {
        // 0: 默认
        // 1: 隐藏显示区域
        val result = Settings.Secure.getInt(context.contentResolver, HUAWAI_DISPLAY_NOTCH_STATUS, 0)
        return result == 0
    }

    /**
     * 判断小米手机的刘海屏是否设置为显示
     *
     * @param context Context 实例
     * @return true 表示设置为显示刘海区域，false 表示隐藏
     */
    @TargetApi(17)
    fun xiaomiIsNotchSetToShowInSetting(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, XIAOMI_DISPLAY_NOTCH_STATUS, 0) == 0
    }
}
