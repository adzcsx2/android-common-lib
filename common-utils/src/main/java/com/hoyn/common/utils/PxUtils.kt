package com.hoyn.common.utils

import android.content.res.Resources
import android.util.TypedValue

/**
 * 像素单位转换工具
 *
 * 提供基于系统 Resources 的 dp/sp 与 px 之间的转换扩展属性
 */
object PxUtils {

    /**
     * Float 类型的 dp 转 px 扩展属性
     *
     * 将 dp 值转换为 px 浮点值，使用系统显示密度进行转换
     *
     * @return 转换后的 px 浮点值
     */
    val Float.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )

    /**
     * Int 类型的 dp 转 px 扩展属性
     *
     * 将 dp 值转换为 px 整数值，使用系统显示密度进行转换
     *
     * @return 转换后的 px 整数值
     */
    val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    /**
     * Float 类型的 sp 转 px 扩展属性
     *
     * 将 sp 值转换为 px 浮点值，使用系统缩放密度进行转换
     * 推荐用于字体大小的转换
     *
     * @return 转换后的 px 浮点值
     */
    val Float.sp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            Resources.getSystem().displayMetrics
        )

    /**
     * Int 类型的 sp 转 px 扩展属性
     *
     * 将 sp 值转换为 px 整数值，使用系统缩放密度进行转换
     * 推荐用于字体大小的转换
     *
     * @return 转换后的 px 整数值
     */
    val Int.sp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

}