package com.hoyn.common.image

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions

/**
 * ImageView 扩展函数
 */

fun ImageView.loadImage(
    url: String?,
    placeholder: Int = 0,
    error: Int = 0
) {
    ImageLoader.load(context, url, this) {
        if (placeholder != 0) placeholder(placeholder)
        if (error != 0) error(error)
    }
}

fun ImageView.loadImage(
    resourceId: Int,
    placeholder: Int = 0,
    error: Int = 0
) {
    ImageLoader.load(context, resourceId, this) {
        if (placeholder != 0) placeholder(placeholder)
        if (error != 0) error(error)
    }
}

fun ImageView.loadCircleImage(
    url: String?,
    placeholder: Int = 0,
    error: Int = 0
) {
    ImageLoader.load(context, url, this) {
        if (placeholder != 0) placeholder(placeholder)
        if (error != 0) error(error)
        circleCrop()
    }
}

fun ImageView.loadRoundedImage(
    url: String?,
    radius: Int,
    placeholder: Int = 0,
    error: Int = 0
) {
    ImageLoader.load(context, url, this) {
        if (placeholder != 0) placeholder(placeholder)
        if (error != 0) error(error)
        // 需要添加 RoundedCorners 变换
    }
}
