package com.hoyn.common.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

/**
 * Context 扩展函数
 *
 * 提供 Context 相关的便捷扩展函数，包括资源获取、Toast 显示、单位转换等功能
 */

// ==================== 资源获取 ====================

/**
 * 获取颜色资源值
 *
 * @param resId 颜色资源 ID（如 R.color.colorPrimary）
 * @return 颜色的整数值
 */
fun Context.getResColor(resId: Int): Int = ContextCompat.getColor(this, resId)

/**
 * 获取 Drawable 资源
 *
 * @param resId Drawable 资源 ID（如 R.drawable.ic_launcher）
 * @return Drawable 对象，如果资源不存在则返回 null
 */
fun Context.getResDrawable(resId: Int): Drawable? = ContextCompat.getDrawable(this, resId)

// ==================== 布局加载 ====================

/**
 * 加载布局文件
 *
 * @param layoutResource 布局资源 ID（如 R.layout.activity_main）
 * @param parent 父容器 ViewGroup，可为 null
 * @param attachToRoot 是否将加载的视图附加到父容器，默认为 false
 * @return 加载后的 View 对象
 */
fun Context.inflate(
    layoutResource: Int,
    parent: ViewGroup? = null,
    attachToRoot: Boolean = false
): View {
    return LayoutInflater.from(this).inflate(layoutResource, parent, attachToRoot)
}

// ==================== 单位转换 ====================

/**
 * 将 dp 值转换为 px 值（Int）
 *
 * @param dip dp 值
 * @return 转换后的 px 值
 */
fun Context.dp2px(dip: Int): Int {
    val scale = resources.displayMetrics.density
    return (dip * scale + 0.5f).toInt()
}

/**
 * 将 dp 值转换为 px 值（Float）
 *
 * @param dip dp 值
 * @return 转换后的 px 值
 */
fun Context.dp2px(dip: Float): Int {
    val scale = resources.displayMetrics.density
    return (dip * scale + 0.5f).toInt()
}

/**
 * 将 px 值转换为 dp 值
 *
 * @param px px 值
 * @return 转换后的 dp 值
 */
fun Context.px2dp(px: Int): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

/**
 * 将 sp 值转换为 px 值
 *
 * @param sp sp 值（推荐用于字体大小）
 * @return 转换后的 px 值
 */
fun Context.sp2px(sp: Int): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (sp * scale + 0.5f).toInt()
}

/**
 * 将 px 值转换为 sp 值
 *
 * @param px px 值
 * @return 转换后的 sp 值
 */
fun Context.px2sp(px: Int): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (px / scale + 0.5f).toInt()
}

// ==================== Activity 扩展 ====================

/**
 * 获取 Context
 *
 * 提供对 Activity 的 Context 访问，方便使用
 */
val Activity.mContext: Context
    get() = this

/**
 * 获取 Activity 实例
 *
 * 用于弹窗、权限请求等需要 Activity 实例的场景
 */
val Activity.mActivity: Activity
    get() = this

/**
 * 获取屏幕宽度
 *
 * @return 屏幕宽度（px）
 */
val Activity.mScreenWidth: Int
    get() = resources.displayMetrics.widthPixels

/**
 * 获取屏幕高度
 *
 * @return 屏幕高度（px），包含状态栏高度但不包含底部虚拟按键高度
 */
val Activity.mScreenHeight: Int
    get() = resources.displayMetrics.heightPixels

/**
 * 获取 Activity 的 ContentView
 *
 * @return Activity 的根布局 FrameLayout
 */
val Activity.mContentView: FrameLayout
    get() = this.findViewById(android.R.id.content)

// ==================== 键盘相关 ====================

/**
 * 监听软键盘高度变化
 *
 * 用于监听软键盘的弹出和收起，获取状态栏、导航栏和键盘的高度
 *
 * @param keyCall 回调函数，参数依次为：状态栏高度、导航栏高度、键盘高度
 */
fun Activity.extKeyBoard(keyCall: (statusHeight: Int, navigationHeight: Int, keyBoardHeight: Int) -> Unit) {
    mContentView.post { mContentView.layoutParams.height = mContentView.height }
    this.window.decorView.setOnApplyWindowInsetsListener(object : View.OnApplyWindowInsetsListener {
        var preKeyOffset: Int = 0
        override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
            insets.let { ins ->
                val navHeight = ins.systemWindowInsetBottom
                val offset = if (navHeight < ins.stableInsetBottom) navHeight
                else navHeight - ins.stableInsetBottom
                if (offset != preKeyOffset || offset == 0) {
                    val decorHeight = mActivity.window.decorView.height
                    if (decorHeight > 0) {
                        mContentView.layoutParams.height = decorHeight - navHeight.coerceAtMost(ins.stableInsetBottom)
                    }
                    preKeyOffset = offset
                    keyCall.invoke(ins.stableInsetTop, ins.stableInsetBottom, offset)
                }
            }
            return mActivity.window.decorView.onApplyWindowInsets(insets)
        }
    })
}

/**
 * 保持屏幕常亮
 *
 * 设置 FLAG_KEEP_SCREEN_ON 标志，防止屏幕自动关闭
 * 适用于视频播放、长时间阅读等场景
 */
fun Activity.extKeepScreenOn() {
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

/**
 * 获取屏幕尺寸
 *
 * @param context Context 实例
 * @return 包含屏幕宽度和高度的 IntArray，index 0 为宽度，index 1 为高度
 */
fun getScreenSize(context: Context): IntArray {
    val size = IntArray(2)
    val w = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val d = w.defaultDisplay
    val metrics = DisplayMetrics()
    d.getMetrics(metrics)
    size[0] = metrics.widthPixels
    size[1] = metrics.heightPixels
    return size
}

/**
 * 修复输入法导致的内存泄漏
 *
 * InputMethodManager 可能会持有 Activity 的引用导致内存泄漏
 * 此方法通过反射清空 InputMethodManager 中的 View 引用
 *
 * @param destContext 需要修复泄漏的 Context，通常为 Activity
 */
fun fixInputMethodManagerLeak(destContext: Context?) {
    if (destContext == null) return
    val manager = destContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
    val viewArray = arrayOf("mCurRootView", "mServedView", "mNextServedView")
    var filed: java.lang.reflect.Field
    var filedObject: Any?
    for (view in viewArray) {
        try {
            filed = manager.javaClass.getDeclaredField(view)
            if (!filed.isAccessible) filed.isAccessible = true
            filedObject = filed.get(manager)
            if (filedObject != null && filedObject is View) {
                if (filedObject.context === destContext) {
                    filed.set(manager, null)
                } else {
                    break
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
