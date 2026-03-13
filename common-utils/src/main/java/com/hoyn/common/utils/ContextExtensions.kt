package com.hoyn.common.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * Context 扩展函数
 */

// ==================== 资源获取 ====================

fun Context.getResColor(resId: Int): Int = ContextCompat.getColor(this, resId)

fun Context.getResDrawable(resId: Int): Drawable? = ContextCompat.getDrawable(this, resId)

// ==================== Toast ====================

fun Context.showToast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageRes, duration).show()
}

// ==================== 布局加载 ====================

fun Context.inflate(
    layoutResource: Int,
    parent: ViewGroup? = null,
    attachToRoot: Boolean = false
): View {
    return LayoutInflater.from(this).inflate(layoutResource, parent, attachToRoot)
}

// ==================== 单位转换 ====================

fun Context.dp2px(dip: Int): Int {
    val scale = resources.displayMetrics.density
    return (dip * scale + 0.5f).toInt()
}

fun Context.dp2px(dip: Float): Int {
    val scale = resources.displayMetrics.density
    return (dip * scale + 0.5f).toInt()
}

fun Context.px2dp(px: Int): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

fun Context.sp2px(sp: Int): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (sp * scale + 0.5f).toInt()
}

fun Context.px2sp(px: Int): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (px / scale + 0.5f).toInt()
}

// ==================== Activity 扩展 ====================

/** 上下文(方便使用) */
val Activity.mContext: Context
    get() = this

/** Activity本身(弹窗,权限请求等需要用到Activity) */
val Activity.mActivity: Activity
    get() = this

/** 屏幕宽度 */
val Activity.mScreenWidth: Int
    get() = resources.displayMetrics.widthPixels

/** 屏幕高度(包含状态栏高度但不包含底部虚拟按键高度) */
val Activity.mScreenHeight: Int
    get() = resources.displayMetrics.heightPixels

/** ContentView */
val Activity.mContentView: FrameLayout
    get() = this.findViewById(android.R.id.content)

// ==================== 键盘相关 ====================

/**
 * 监听键盘高度
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
 * 常亮
 */
fun Activity.extKeepScreenOn() {
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

/**
 * 获取屏幕尺寸
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
