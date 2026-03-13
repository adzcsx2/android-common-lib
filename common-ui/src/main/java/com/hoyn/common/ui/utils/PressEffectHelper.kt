package com.hoyn.common.ui.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.SizeUtils
import kotlinx.coroutines.*

/**
 * 按压效果工具类
 *
 * @author yzy
 * @date 2019/7/2
 */
class PressEffectHelper private constructor() {

    companion object {
        private val location = IntArray(2)
        private var isDown: Boolean = false
        private var time: Long = 0
        private const val LONG_PRESS_TIME = 600L
        private var longPressDisposable: CoroutineScope? = null
        private var isLongClick = false

        /**
         * 按下改变透明度的效果
         */
        fun alphaEffect(view: View, pressAlpha: Float = 0.8f) {
            view.setOnTouchListener { v, event ->
                if (System.currentTimeMillis() - time < 200) {
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
                        longClick(view)
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
                            time = System.currentTimeMillis()
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
         */
        fun bgColorEffect(
            view: View,
            bgColor: Int = Color.parseColor("#f7f7f7"),
            topLeftRadiusDp: Float = 0f,
            topRightRadiusDp: Float = 0f,
            bottomRightRadiusDp: Float = 0f,
            bottomLeftRadiusDp: Float = 0f
        ) {
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

            view.setOnTouchListener { v, event ->
                if (System.currentTimeMillis() - time < 200) {
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
                        longClick(view)
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
                            time = System.currentTimeMillis()
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

        private fun longClick(view: View) {
            cancelLongClick()
            longPressDisposable = MainScope()
            longPressDisposable?.launch {
                delay(LONG_PRESS_TIME)
                isLongClick = true
                view.performLongClick()
            }
        }

        private fun cancelLongClick() {
            longPressDisposable?.cancel()
            longPressDisposable = null
            isLongClick = false
        }
    }
}
