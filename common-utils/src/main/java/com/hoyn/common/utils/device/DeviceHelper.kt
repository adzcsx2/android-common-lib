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
import android.app.AppOpsManager
import android.content.Context
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Method
import java.util.*
import java.util.regex.Pattern

/**
 * 设备信息检测工具类
 * 用于判断设备品牌、系统版本等信息
 *
 * @author yzy
 * @date 2016-08-11
 */
object DeviceHelper {
    private const val TAG = "DeviceHelper"
    private const val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"
    private const val KEY_FLYME_VERSION_NAME = "ro.build.display.id"
    private const val FLYME = "flyme"
    private const val ZTEC2016 = "zte c2016"
    private const val ZUKZ1 = "zuk z1"
    private const val ESSENTIAL = "essential"
    private val MEIZUBOARD = arrayOf("m9", "M9", "mx", "MX")
    private var sMiuiVersionName: String? = null
    private var sFlymeVersionName: String? = null
    private var sIsTabletChecked = false
    private var sIsTabletValue = false

    @SuppressLint("ConstantLocale")
    private val BRAND = Build.BRAND.lowercase(Locale.getDefault())

    /**
     * 判断是否为平板设备的内部实现
     *
     * 通过检查屏幕布局配置中的尺寸位来判断
     *
     * @param context Context 实例
     * @return true 表示是平板设备，false 表示不是
     */
    private fun _isTablet(context: Context): Boolean {
        return context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
                Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /**
     * 判断是否为平板设备
     *
     * 使用缓存机制提高性能
     *
     * @param context Context 实例
     * @return true 表示是平板设备，false 表示不是
     */
    fun isTablet(context: Context): Boolean {
        if (sIsTabletChecked) {
            return sIsTabletValue
        }
        sIsTabletValue = _isTablet(context)
        sIsTabletChecked = true
        return sIsTabletValue
    }

    /**
     * 判断是否是 Flyme 系统
     *
     * @return true 表示是魅族 Flyme 系统，false 表示不是
     */
    val isFlyme: Boolean = !TextUtils.isEmpty(sFlymeVersionName) && sFlymeVersionName!!.contains(FLYME)

    /**
     * 判断是否是 MIUI 系统
     *
     * @return true 表示是小米 MIUI 系统，false 表示不是
     */
    val isMIUI: Boolean = !TextUtils.isEmpty(sMiuiVersionName)

    /**
     * 判断是否是 MIUI V5 版本
     *
     * @return true 表示是 MIUI V5，false 表示不是
     */
    val isMIUIV5: Boolean = "v5" == sMiuiVersionName

    /**
     * 判断是否是 MIUI V6 版本
     *
     * @return true 表示是 MIUI V6，false 表示不是
     */
    val isMIUIV6: Boolean = "v6" == sMiuiVersionName

    /**
     * 判断是否是 MIUI V7 版本
     *
     * @return true 表示是 MIUI V7，false 表示不是
     */
    val isMIUIV7: Boolean = "v7" == sMiuiVersionName

    /**
     * 判断是否是 MIUI V8 版本
     *
     * @return true 表示是 MIUI V8，false 表示不是
     */
    val isMIUIV8: Boolean = "v8" == sMiuiVersionName

    /**
     * 判断是否是 MIUI V9 版本
     *
     * @return true 表示是 MIUI V9，false 表示不是
     */
    val isMIUIV9: Boolean = "v9" == sMiuiVersionName

    /**
     * 判断是否是低于 V8 版本的 Flyme 系统
     *
     * @return true 表示是 Flyme 且版本低于 8，false 表示不是
     */
    val isFlymeLowerThan8: Boolean
        get() {
            var isLower = false
            if (sFlymeVersionName != null && sFlymeVersionName != "") {
                val pattern = Pattern.compile("(\\d+\\.){2}\\d")
                val matcher = pattern.matcher(sFlymeVersionName)
                if (matcher.find()) {
                    val versionString = matcher.group()
                    if (versionString != "") {
                        val version = versionString.split(".").toTypedArray()
                        if (version.isNotEmpty()) {
                            if (version[0].toInt() < 8) {
                                isLower = true
                            }
                        }
                    }
                }
            }
            return isMeizu && isLower
        }

    /**
     * 判断是否是高于 5.2.4 版本的 Flyme 系统
     *
     * @return true 表示是 Flyme 且版本高于 5.2.4，false 表示不是
     */
    val isFlymeVersionHigher5_2_4: Boolean
        get() {
            var isHigher = true
            if (sFlymeVersionName != null && sFlymeVersionName != "") {
                val pattern = Pattern.compile("(\\d+\\.){2}\\d")
                val matcher = pattern.matcher(sFlymeVersionName)
                if (matcher.find()) {
                    val versionString = matcher.group()
                    if (versionString != "") {
                        val version = versionString.split("\\.").toTypedArray()
                        if (version.size == 3) {
                            val majorVersion = version[0].toInt()
                            if (majorVersion < 5) {
                                isHigher = false
                            } else if (majorVersion == 5) {
                                val minorVersion = version[1].toInt()
                                if (minorVersion < 2) {
                                    isHigher = false
                                } else if (minorVersion == 2) {
                                    val patchVersion = version[2].toInt()
                                    if (patchVersion < 4) {
                                        isHigher = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return isMeizu && isHigher
        }

    /**
     * 判断是否为魅族设备
     *
     * @return true 表示是魅族设备，false 表示不是
     */
    val isMeizu: Boolean = isPhone(MEIZUBOARD) || isFlyme

    /**
     * 判断是否为小米设备
     *
     * 参考文档: https://dev.mi.com/doc/?p=254
     *
     * @return true 表示是小米设备，false 表示不是
     */
    @SuppressLint("ConstantLocale")
    val isXiaomi: Boolean = Build.MANUFACTURER.lowercase(Locale.getDefault()) == "xiaomi"

    /**
     * 判断是否为 vivo 设备
     *
     * @return true 表示是 vivo 设备，false 表示不是
     */
    val isVivo: Boolean = BRAND.contains("vivo") || BRAND.contains("bbk")

    /**
     * 判断是否为 OPPO 设备
     *
     * @return true 表示是 OPPO 设备，false 表示不是
     */
    val isOppo: Boolean = BRAND.contains("oppo")

    /**
     * 判断是否为华为/荣耀设备
     *
     * @return true 表示是华为或荣耀设备，false 表示不是
     */
    val isHuawei: Boolean = BRAND.contains("huawei") || BRAND.contains("honor")

    /**
     * 判断是否为 Essential Phone
     *
     * @return true 表示是 Essential Phone，false 表示不是
     */
    val isEssentialPhone: Boolean = BRAND.contains("essential")

    /**
     * 判断是否为 ZUK Z1 设备
     *
     * ZUK Z1 虽然系统为 Android 6.0，但不支持状态栏图标颜色改变
     * 因此经常需要对它们进行额外判断
     *
     * @return true 表示是 ZUK Z1，false 表示不是
     */
    fun isZUKZ1(): Boolean {
        val board = Build.MODEL
        return board != null && board.lowercase(Locale.getDefault()).contains(ZUKZ1)
    }

    /**
     * 判断是否为 ZTE C2016 设备
     *
     * ZTE C2016 虽然系统为 Android 6.0，但不支持状态栏图标颜色改变
     *
     * @return true 表示是 ZTE C2016，false 表示不是
     */
    val isZTKC2016: Boolean
        get() {
            val board = Build.MODEL
            return board != null && board.lowercase(Locale.getDefault()).contains(ZTEC2016)
        }

    /**
     * 判断设备主板是否在指定列表中
     *
     * @param boards 主板名称数组
     * @return true 表示主板名称在列表中，false 表示不在
     */
    private fun isPhone(boards: Array<String>): Boolean {
        val board = Build.BOARD ?: return false
        for (board1 in boards) {
            if (board == board1) {
                return true
            }
        }
        return false
    }

    /**
     * 判断是否有悬浮窗权限
     *
     * 目前主要用于魅族与小米的检测
     * 对于 API 19 及以上使用 AppOpsManager 检查
     * 对于 API 19 以下通过检查应用标志位判断
     *
     * @param context Context 实例
     * @return true 表示有悬浮窗权限，false 表示没有
     */
    fun isFloatWindowOpAllowed(context: Context): Boolean {
        val version = Build.VERSION.SDK_INT
        return if (version >= 19) {
            checkOp(context, 24) // 24 是AppOpsManager.OP_SYSTEM_ALERT_WINDOW 的值，该值无法直接访问
        } else {
            try {
                context.applicationInfo.flags and 1 shl 27 == 1 shl 27
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * 检查 AppOps 操作是否允许
     *
     * 通过反射调用 AppOpsManager.checkOp 方法
     *
     * @param context Context 实例
     * @param op 操作代码，24 表示悬浮窗操作
     * @return true 表示操作被允许，false 表示不允许
     */
    @TargetApi(19)
    private fun checkOp(context: Context, op: Int): Boolean {
        val version = Build.VERSION.SDK_INT
        if (version >= Build.VERSION_CODES.KITKAT) {
            val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                val method = manager.javaClass.getDeclaredMethod(
                    "checkOp",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                val property = method.invoke(
                    manager, op,
                    Binder.getCallingUid(), context.packageName
                ) as Int
                return AppOpsManager.MODE_ALLOWED == property
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    /**
     * 获取系统属性的小写名称
     *
     * 从 Properties 或通过反射获取系统属性
     *
     * @param p Properties 对象
     * @param get 反射方法对象
     * @param key 属性键名
     * @return 属性值的小写字符串，获取失败返回 null
     */
    private fun getLowerCaseName(
        p: Properties,
        get: Method,
        key: String
    ): String? {
        var name = p.getProperty(key)
        if (name == null) {
            try {
                name = get.invoke(null, key) as String
            } catch (ignored: Exception) {
            }
        }
        if (name != null) name = name.lowercase(Locale.getDefault())
        return name
    }

    init {
        val properties = Properties()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // android 8.0，读取 /system/uild.prop 会报 permission denied
            var fileInputStream: FileInputStream? = null
            try {
                fileInputStream = FileInputStream(
                    File(
                        Environment.getRootDirectory(),
                        "build.prop"
                    )
                )
                properties.load(fileInputStream)
            } catch (e: Exception) {
                Log.e(TAG, "read file error", e)
            } finally {
                try {
                    fileInputStream?.close()
                } catch (e: Exception) {
                    Log.e(TAG, "close stream error", e)
                }
            }
        }
        var clzSystemProperties: Class<*>? = null
        try {
            @SuppressLint("PrivateApi")
            clzSystemProperties = Class.forName("android.os.SystemProperties")
            val getMethod: Method = clzSystemProperties.getDeclaredMethod("get", String::class.java)
            // miui
            sMiuiVersionName = getLowerCaseName(properties, getMethod, KEY_MIUI_VERSION_NAME)
            // flyme
            sFlymeVersionName = getLowerCaseName(properties, getMethod, KEY_FLYME_VERSION_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "read SystemProperties error", e)
        }
    }
}
