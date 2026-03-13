package com.hoyn.common.ui.toast

import androidx.annotation.IntDef

/**
 * Toast 显示时长定义
 */
const val DURATION_SHORT = 2000
const val DURATION_LONG = 3500

@Retention(AnnotationRetention.SOURCE)
@IntDef(DURATION_SHORT, DURATION_LONG)
annotation class ToastDuration
