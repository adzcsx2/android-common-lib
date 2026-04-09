package com.hoyn.common.ui.toast

import android.view.Gravity

/**
 * Toast 配置类，用于 DSL 风格的全局和单次调用参数配置。
 *
 * 支持配置项：
 * - [gravity] 显示位置
 * - [xOffset] X 轴偏移量（px）
 * - [yOffset] Y 轴偏移量（px）
 * - [stackSkips] 堆栈跳过层数
 */
class ToastConfig private constructor(
    var gravity: Int,
    var xOffset: Int,
    var yOffset: Int,
    var stackSkips: Int
) {

    companion object {
        internal const val DEFAULT_TOAST_STACK_SKIPS: Int = 0

        /** 默认底部 Y 轴偏移量（dp） */
        const val DEFAULT_BOTTOM_Y_OFFSET_DP = 64F

        /** 默认 X 轴偏移量 */
        const val DEFAULT_X_OFFSET: Int = 0

        /** 默认 Y 轴偏移量（px），延迟计算以避免单元测试中 Resources 不可用 */
        val DEFAULT_Y_OFFSET_PX: Int by lazy {
            computeYOffsetPx()
        }

        internal var yOffsetPxComputer: () -> Int = {
            android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_BOTTOM_Y_OFFSET_DP,
                android.content.res.Resources.getSystem().displayMetrics
            ).toInt()
        }

        internal fun computeYOffsetPx(): Int = yOffsetPxComputer()

        /** 创建包含默认值的配置实例 */
        fun defaults(): ToastConfig = ToastConfig(
            gravity = Gravity.BOTTOM,
            xOffset = DEFAULT_X_OFFSET,
            yOffset = DEFAULT_Y_OFFSET_PX,
            stackSkips = DEFAULT_TOAST_STACK_SKIPS
        )
    }

    /** 创建独立副本 */
    fun copy(): ToastConfig = ToastConfig(
        gravity = gravity,
        xOffset = xOffset,
        yOffset = yOffset,
        stackSkips = stackSkips
    )
}
