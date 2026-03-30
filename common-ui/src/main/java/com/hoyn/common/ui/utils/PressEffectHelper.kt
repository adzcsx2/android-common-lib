package com.hoyn.common.ui.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.SizeUtils

/**
 * 按压效果工具类
 *
 * 提供按下时改变透明度或背景色的效果，支持长按检测
 *
 * @author yzy
 * @date 2019/7/2
 */
class PressEffectHelper private constructor() {

    companion object {
        private const val LONG_PRESS_TIME = 600L

        /**
         * 按下改变透明度的效果
         *
         * 为 View 设置触摸监听器，按下时降低透明度，抬起时恢复
         * 支持长按事件触发
         *
         * @param view 要设置效果的 View
         * @param pressAlpha 按下时的透明度，默认为 0.8f
         */
        fun alphaEffect(view: View, pressAlpha: Float = 0.8f) {
            val location = IntArray(2)
            var isDown = false
            var lastTouchTime = 0L
            var isLongClick = false
            var longPressRunnable: Runnable? = null

            fun startLongClick() {
                cancelLongClick(view, longPressRunnable)
                isLongClick = false
                longPressRunnable = Runnable {
                    isLongClick = true
                    view.performLongClick()
                }
                view.postDelayed(longPressRunnable, LONG_PRESS_TIME)
            }

            fun cancelLongClick() {
                cancelLongClick(view, longPressRunnable)
                longPressRunnable = null
                isLongClick = false
            }

            view.setOnTouchListener { v, event ->
                if (System.currentTimeMillis() - lastTouchTime < 200) {
                    return@setOnTouchListener true
                }
                view.getLocationOnScreen(location)
                val rawX = event.rawX
                val rawY = event.rawY
                val width = view.width
                val height = view.height
                val contains = rawX > location[0] && rawX < location[0] + width && rawY > location[1] && rawY < location[1] + height

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isDown = true
                        v.alpha = 1f
                        v.postDelayed({ v.alpha = if (isDown) pressAlpha else 1.0f }, 150)
                        startLongClick()
                        return@setOnTouchListener true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!contains) {
                            isDown = false
                            v.alpha = 1f
                            return@setOnTouchListener true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        isDown = false
                        if (contains) {
                            if (v.alpha == 1f) {
                                v.alpha = pressAlpha
                                v.postDelayed({
                                    v.alpha = 1f
                                    v.performClick()
                                }, 50)
                            } else {
                                v.alpha = 1f
                                if (!isLongClick) {
                                    v.performClick()
                                }
                            }
                            lastTouchTime = System.currentTimeMillis()
                            cancelLongClick()
                            return@setOnTouchListener true
                        }
                        isDown = false
                        v.alpha = 1f
                        cancelLongClick()
                        return@setOnTouchListener true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        isDown = false
                        v.alpha = 1f
                        cancelLongClick()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        /**
         * 按下改变背景色的效果
         *
         * 为 View 设置触摸监听器，按下时改变背景色，抬起时恢复
         * 支持圆角背景和长按事件触发
         *
         * @param view 要设置效果的 View
         * @param bgColor 按下时的背景色，默认为浅灰色
         * @param topLeftRadiusDp 左上角圆角半径（dp）
         * @param topRightRadiusDp 右上角圆角半径（dp）
         * @param bottomRightRadiusDp 右下角圆角半径（dp）
         * @param bottomLeftRadiusDp 左下角圆角半径（dp）
         */
        fun bgColorEffect(
            view: View,
            bgColor: Int = Color.parseColor("#f7f7f7"),
            topLeftRadiusDp: Float = 0f,
            topRightRadiusDp: Float = 0f,
            bottomRightRadiusDp: Float = 0f,
            bottomLeftRadiusDp: Float = 0f
        ) {
            val location = IntArray(2)
            var isDown = false
            var lastTouchTime = 0L
            var isLongClick = false
            var longPressRunnable: Runnable? = null
            val originalBgColor = view.background
            val pressDrawable: Drawable = if (topLeftRadiusDp == 0f && topRightRadiusDp == 0f && bottomLeftRadiusDp == 0f && bottomRightRadiusDp == 0f) {
                ColorDrawable(bgColor)
            } else {
                GradientDrawable().apply {
                    setColor(bgColor)
                    cornerRadii = floatArrayOf(
                        if (topLeftRadiusDp != 0.0f) SizeUtils.dp2px(topLeftRadiusDp).toFloat() else 0.0f,
                        if (topLeftRadiusDp != 0.0f) SizeUtils.dp2px(topLeftRadiusDp).toFloat() else 0.0f,
                        if (topRightRadiusDp != 0.0f) SizeUtils.dp2px(topRightRadiusDp).toFloat() else 0.0f,
                        if (topRightRadiusDp != 0.0f) SizeUtils.dp2px(topRightRadiusDp).toFloat() else 0.0f,
                        if (bottomRightRadiusDp != 0.0f) SizeUtils.dp2px(bottomRightRadiusDp).toFloat() else 0.0f,
                        if (bottomRightRadiusDp != 0.0f) SizeUtils.dp2px(bottomRightRadiusDp).toFloat() else 0.0f,
                        if (bottomLeftRadiusDp != 0.0f) SizeUtils.dp2px(bottomLeftRadiusDp).toFloat() else 0.0f,
                        if (bottomLeftRadiusDp != 0.0f) SizeUtils.dp2px(bottomLeftRadiusDp).toFloat() else 0.0f
                    )
                }
            }

            fun startLongClick() {
                cancelLongClick(view, longPressRunnable)
                isLongClick = false
                longPressRunnable = Runnable {
                    isLongClick = true
                    view.performLongClick()
                }
                view.postDelayed(longPressRunnable, LONG_PRESS_TIME)
            }

            fun cancelLongClick() {
                cancelLongClick(view, longPressRunnable)
                longPressRunnable = null
                isLongClick = false
            }

            view.setOnTouchListener { v, event ->
                if (System.currentTimeMillis() - lastTouchTime < 200) {
                    return@setOnTouchListener true
                }
                view.getLocationOnScreen(location)
                val rawX = event.rawX
                val rawY = event.rawY
                val width = view.width
                val height = view.height
                val contains = rawX > location[0] && rawX < location[0] + width && rawY > location[1] && rawY < location[1] + height

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isDown = true
                        v.background = originalBgColor
                        v.postDelayed({ v.background = if (isDown) pressDrawable else originalBgColor }, 150)
                        startLongClick()
                        return@setOnTouchListener true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!contains) {
                            isDown = false
                            v.background = originalBgColor
                            return@setOnTouchListener true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        isDown = false
                        if (contains) {
                            if (v.background == originalBgColor) {
                                v.background = pressDrawable
                                v.postDelayed({
                                    v.background = originalBgColor
                                    v.performClick()
                                }, 50)
                            } else {
                                v.background = originalBgColor
                                if (!isLongClick) {
                                    v.performClick()
                                }
                            }
                            lastTouchTime = System.currentTimeMillis()
                            cancelLongClick()
                            return@setOnTouchListener true
                        }
                        isDown = false
                        v.background = originalBgColor
                        cancelLongClick()
                        return@setOnTouchListener true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        isDown = false
                        v.background = originalBgColor
                        cancelLongClick()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        private fun cancelLongClick(view: View, longPressRunnable: Runnable?) {
            if (longPressRunnable != null) {
                view.removeCallbacks(longPressRunnable)
            }
        }
    }
}
