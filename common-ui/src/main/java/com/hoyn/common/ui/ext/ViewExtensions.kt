package com.hoyn.common.ui.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.annotation.DimenRes
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.hoyn.common.ui.R
import com.hoyn.common.ui.utils.PressEffectHelper

/**
 * View 扩展函数
 */

// ==================== 基础可见性 ====================

fun View.visible() {
    isVisible = true
}

fun View.invisible() {
    isInvisible = true
}

fun View.gone() {
    isGone = true
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

// ==================== 带动画的可见性 ====================

/**
 * 显示的动画
 * @param mDuration 动画时间
 */
fun View.visibleWithAnimation(mDuration: Long = 300) {
    startAnimation(AlphaAnimation(0f, 1f).apply { duration = mDuration })
    visible()
}

/**
 * 消失的动画
 * @param mDuration 动画时间
 */
fun View.goneWithAnimation(mDuration: Long = 200) {
    startAnimation(AlphaAnimation(1f, 0f).apply {
        duration = mDuration
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) { gone() }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    })
}

// ==================== 启用/禁用 ====================

fun View.enable() {
    if (!isEnabled) isEnabled = true
}

fun View.disable() {
    if (isEnabled) isEnabled = false
}

// ==================== 点击事件 ====================

fun View.onClick(action: (View) -> Unit) {
    setOnClickListener(action)
}

fun View.onLongClick(action: (View) -> Boolean) {
    setOnLongClickListener(action)
}

/**
 * 防抖动点击事件
 * @param delay 防抖延迟时间，默认600ms
 */
@SuppressLint("CheckResult")
inline fun View.click(crossinline function: (view: View) -> Unit, delay: Int = 600) {
    this.setOnClickListener {
        stableFun({ function.invoke(it) }, delay)
    }
}

/**
 * 防抖动的方法
 * @param stableFunction 防抖动点击事件
 * @param delay 抖动点击延迟判定
 */
@SuppressLint("CheckResult")
inline fun View.stableFun(crossinline stableFunction: () -> Unit, delay: Int) {
    val tag = this.getTag(R.id.id_tag_click)
    if (tag == null || System.currentTimeMillis() - tag.toString().toLong() > delay) {
        this.setTag(R.id.id_tag_click, System.currentTimeMillis())
        stableFunction.invoke()
    }
}

/**
 * 连点事件处理，可用于连点事件处理，例如连点5下，中途取消点击计数器归零
 * @param effectiveClickFunction 有效点击事件
 * @param invalidClickFunction 无效时事件处理
 * @param delay 连点延迟判定
 */
@SuppressLint("CheckResult")
inline fun View.continuousClick(
    crossinline effectiveClickFunction: (view: View) -> Unit,
    crossinline invalidClickFunction: (view: View) -> Unit,
    delay: Int
) {
    click({
        val tag = this.getTag(R.id.id_tag_click2)
        if (tag == null || System.currentTimeMillis() - tag.toString().toLong() < delay) {
            effectiveClickFunction.invoke(it)
        } else {
            invalidClickFunction.invoke(it)
        }
        this.setTag(R.id.id_tag_click2, System.currentTimeMillis())
    }, 0)
}

/**
 * 点击事件，点击完成之后锁住，释放之后才能再次点击
 */
@SuppressLint("CheckResult")
inline fun View.holdClick(crossinline function: (view: View) -> Unit) {
    click(function, 999999)
}

/**
 * 释放点击事件
 */
fun View.releaseClick() {
    this.setTag(R.id.id_tag_click, 0)
}

/**
 * 如果回调不为空则设置点击事件,回调函数中需要返回数据
 */
fun <T> View.setClickNotNull(t: T, onClick: ((t: T) -> Unit)?) {
    if (onClick == null) {
        setOnClickListener(null)
    } else {
        click({ onClick.invoke(t) })
    }
}

/**
 * 如果回调不为空则设置点击事件
 */
fun View.setClickNotNull(onClick: (() -> Unit)?) {
    if (onClick == null) {
        setOnClickListener(null)
    } else {
        click({ onClick.invoke() })
    }
}

// ==================== 按压效果 ====================

/**
 * 设置按下效果为改变背景色
 */
fun View.pressEffectBgColor(
    bgColor: Int = Color.parseColor("#f7f7f7"),
    topLeftRadiusDp: Float = 0f,
    topRightRadiusDp: Float = 0f,
    bottomRightRadiusDp: Float = 0f,
    bottomLeftRadiusDp: Float = 0f
) {
    PressEffectHelper.bgColorEffect(this, bgColor, topLeftRadiusDp, topRightRadiusDp, bottomRightRadiusDp, bottomLeftRadiusDp)
}

/**
 * 设置按下效果为改变透明度
 */
fun View.pressEffectAlpha(pressAlpha: Float = 0.5f) {
    PressEffectHelper.alphaEffect(this, pressAlpha)
}

/**
 * 关闭按下效果
 */
fun View.pressEffectDisable() {
    this.setOnTouchListener(null)
}

// ==================== 双击检测 ====================

private var lastClickTime: Long = 0
private const val SPACE_TIME = 500

fun isDoubleClick(): Boolean {
    val currentTime = System.currentTimeMillis()
    val isDoubleClick = currentTime - lastClickTime <= SPACE_TIME
    if (!isDoubleClick) {
        lastClickTime = currentTime
    }
    return isDoubleClick
}

// ==================== 其他 ====================

/**
 * 从父控件移除
 */
fun View.removeParent() {
    val parentTemp = parent
    if (parentTemp is ViewManager) parentTemp.removeView(this)
}

/**
 * 设置Activity背景透明度
 * @param alpha 0.0F(黑暗) ~ 1.0F(透明)
 */
fun Context.setBackgroundAlpha(alpha: Float) {
    val act = this as? Activity ?: return
    val attributes = act.window.attributes
    attributes.alpha = alpha
    act.window.attributes = attributes
}

fun Context.getDimension(@DimenRes resId: Int): Float {
    return resources.getDimension(resId)
}

// ==================== 属性扩展 ====================

var View.backgroundColor: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setBackgroundColor(v)

var View.backgroundResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setBackgroundResource(v)

var android.widget.ImageView.imageResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setImageResource(v)

var android.widget.ImageView.imageURI: android.net.Uri?
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setImageURI(v)

var android.widget.ImageView.imageBitmap: android.graphics.Bitmap?
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setImageBitmap(v)

var android.widget.TextView.textColor: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setTextColor(v)

var android.widget.TextView.hintTextColor: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setHintTextColor(v)

var android.widget.TextView.linkTextColor: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setLinkTextColor(v)

var android.widget.TextView.lines: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setLines(v)

var android.widget.TextView.singleLine: Boolean
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setSingleLine(v)

var android.widget.TextView.hintResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setHint(v)

var android.widget.TextView.textResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setText(v)

var android.widget.RelativeLayout.horizontalGravity: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setHorizontalGravity(v)

var android.widget.RelativeLayout.verticalGravity: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setVerticalGravity(v)

var android.widget.LinearLayout.horizontalGravity: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setHorizontalGravity(v)

var android.widget.LinearLayout.verticalGravity: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setVerticalGravity(v)

var android.widget.AbsListView.selectorResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setSelector(v)

var android.widget.CheckedTextView.checkMarkDrawableResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setCheckMarkDrawable(v)

var android.widget.CompoundButton.buttonDrawableResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setButtonDrawable(v)

var android.widget.TabWidget.leftStripDrawableResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setLeftStripDrawable(v)

var android.widget.TabWidget.rightStripDrawableResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setRightStripDrawable(v)

var android.widget.Toolbar.logoResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setLogo(v)

var android.widget.Toolbar.logoDescriptionResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setLogoDescription(v)

var android.widget.Toolbar.navigationContentDescriptionResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setNavigationContentDescription(v)

var android.widget.Toolbar.navigationIconResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setNavigationIcon(v)

var android.widget.Toolbar.subtitleResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setSubtitle(v)

var android.widget.Toolbar.titleResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
    set(v) = setTitle(v)

fun noGetter(): Nothing = throw RuntimeException("Property does not have a getter")

const val NO_GETTER: String = "Property does not have a getter"
