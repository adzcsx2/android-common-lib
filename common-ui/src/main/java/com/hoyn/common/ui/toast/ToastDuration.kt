package com.hoyn.common.ui.toast

import androidx.annotation.IntDef

/**
 * Toast 显示时长定义
 */
/** 短时长 Toast，持续 2000 毫秒 */
const val DURATION_SHORT = 2000
/** 长时长 Toast，持续 3500 毫秒 */
const val DURATION_LONG = 3500

/**
 * Toast 时长注解
 *
 * 用于标记 Toast 时长参数，限定值为 [DURATION_SHORT] 或 [DURATION_LONG]
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(DURATION_SHORT, DURATION_LONG)
annotation class ToastDuration
