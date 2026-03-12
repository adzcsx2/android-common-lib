package com.hoyn.common.image

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

/**
 * 图片加载工具类
 *
 * 基于 Glide 封装的图片加载接口
 */
object ImageLoader {

    fun load(
        context: Context,
        url: String?,
        imageView: ImageView,
        options: RequestOptions.() -> Unit = {}
    ) {
        Glide.with(context)
            .load(url)
            .apply(RequestOptions().apply(options))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    fun load(
        context: Context,
        resourceId: Int,
        imageView: ImageView,
        options: RequestOptions.() -> Unit = {}
    ) {
        Glide.with(context)
            .load(resourceId)
            .apply(RequestOptions().apply(options))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    fun clear(context: Context) {
        Glide.get(context).clearMemory()
    }

    fun clearDiskCache(context: Context) {
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }
}
