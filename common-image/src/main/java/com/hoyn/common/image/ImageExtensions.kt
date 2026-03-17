package com.hoyn.common.image

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.request.RequestOptions
import com.hoyn.common.utils.PxUtils.dp

/**
 * ImageView 常用图片加载扩展。
 */

/**
 * 加载网络图片。
 *
 * [placeholder] 和 [error] 为可选占位图与错误图资源。
 */
fun ImageView.loadImage(
    url: String?,
    @DrawableRes
    placeholder: Int = 0,
    @DrawableRes
    error: Int = 0
) {
    GlideUtils.load(this, url) {
        applyCommonDrawableOptions(placeholder, error)
    }
}

/**
 * 加载本地资源图片。
 *
 * [placeholder] 和 [error] 为可选占位图与错误图资源。
 */
fun ImageView.loadImage(
    resourceId: Int,
    @DrawableRes
    placeholder: Int = 0,
    @DrawableRes
    error: Int = 0
) {
    GlideUtils.load(this, resourceId) {
        applyCommonDrawableOptions(placeholder, error)
    }
}

/**
 * 加载圆形网络图片。
 *
 * [placeholder] 和 [error] 为可选占位图与错误图资源。
 */
fun ImageView.loadCircleImage(
    url: String?,
    @DrawableRes
    placeholder: Int = 0,
    @DrawableRes
    error: Int = 0
) {
    GlideUtils.loadCircle(this, url) {
        applyCommonDrawableOptions(placeholder, error)
    }
}

/**
 * 加载圆形本地资源图片。
 *
 * [placeholder] 和 [error] 为可选占位图与错误图资源。
 */
fun ImageView.loadCircleImage(
    resourceId: Int,
    @DrawableRes
    placeholder: Int = 0,
    @DrawableRes
    error: Int = 0
) {
    GlideUtils.loadCircle(this, resourceId) {
        applyCommonDrawableOptions(placeholder, error)
    }
}

/**
 * 加载圆角网络图片。
 *
 * [radiusPx] 为像素值，通常直接传入 `10.dp`。
 */
fun ImageView.loadRoundedImage(
    url: String?,
    radiusPx: Int = 10.dp,
    @DrawableRes
    placeholder: Int = 0,
    @DrawableRes
    error: Int = 0
) {
    GlideUtils.loadRounded(this, url, radiusPx) {
        applyCommonDrawableOptions(placeholder, error)
    }
}

/**
 * 加载圆角本地资源图片。
 *
 * [radiusPx] 为像素值，通常直接传入 `10.dp`。
 */
fun ImageView.loadRoundedImage(
    resourceId: Int,
    radiusPx: Int = 10.dp,
    @DrawableRes
    placeholder: Int = 0,
    @DrawableRes
    error: Int = 0
) {
    GlideUtils.loadRounded(this, resourceId, radiusPx) {
        applyCommonDrawableOptions(placeholder, error)
    }
}

private fun RequestOptions.applyCommonDrawableOptions(
    @DrawableRes placeholder: Int,
    @DrawableRes error: Int
) {
    if (placeholder != 0) {
        placeholder(placeholder)
    }
    if (error != 0) {
        error(error)
    }
}
