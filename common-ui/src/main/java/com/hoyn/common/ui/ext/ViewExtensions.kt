package com.hoyn.common.ui.ext

import android.view.View

/**
 * View 扩展函数
 */

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.onClick(action: (View) -> Unit) {
    setOnClickListener(action)
}

fun View.onLongClick(action: (View) -> Boolean) {
    setOnLongClickListener(action)
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}
