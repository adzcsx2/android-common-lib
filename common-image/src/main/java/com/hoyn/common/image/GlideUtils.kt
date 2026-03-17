package com.hoyn.common.image

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import kotlin.concurrent.thread

/**
 * Glide 图片加载工具。
 *
 * 统一封装常用图片加载能力，并将请求生命周期绑定到目标 [ImageView]，
 * 以避免仅依赖 [Context] 带来的生命周期不匹配问题。
 */
object GlideUtils {

    /** 加载网络图片。 */
    fun load(
        imageView: ImageView,
        url: String?,
        options: RequestOptions.() -> Unit = {}
    ) {
        loadInternal(imageView, url, options)
    }

    /** 加载本地资源图片。 */
    fun load(
        imageView: ImageView,
        resourceId: Int,
        options: RequestOptions.() -> Unit = {}
    ) {
        loadInternal(imageView, resourceId, options)
    }

    /** 加载圆形网络图片。 */
    fun loadCircle(
        imageView: ImageView,
        url: String?,
        options: RequestOptions.() -> Unit = {}
    ) {
        loadInternal(imageView, url) {
            options()
            circleCrop()
        }
    }

    /** 加载圆形本地资源图片。 */
    fun loadCircle(
        imageView: ImageView,
        resourceId: Int,
        options: RequestOptions.() -> Unit = {}
    ) {
        loadInternal(imageView, resourceId) {
            options()
            circleCrop()
        }
    }

    /**
     * 加载圆角网络图片。
     *
     * [radiusPx] 为像素值，调用方通常传入 `10.dp` 这类由 `PxUtils` 转换后的值。
     */
    fun loadRounded(
        imageView: ImageView,
        url: String?,
        radiusPx: Int,
        options: RequestOptions.() -> Unit = {}
    ) {
        loadInternal(imageView, url) {
            options()
            transform(CenterCrop(), RoundedCorners(radiusPx))
        }
    }

    /**
     * 加载圆角本地资源图片。
     *
     * [radiusPx] 为像素值，调用方通常传入 `10.dp` 这类由 `PxUtils` 转换后的值。
     */
    fun loadRounded(
        imageView: ImageView,
        resourceId: Int,
        radiusPx: Int,
        options: RequestOptions.() -> Unit = {}
    ) {
        loadInternal(imageView, resourceId) {
            options()
            transform(CenterCrop(), RoundedCorners(radiusPx))
        }
    }

    /** 清除指定 [ImageView] 上的 Glide 请求。 */
    fun clear(imageView: ImageView) {
        Glide.with(imageView).clear(imageView)
    }

    /**
     * 清除内存缓存。
     *
     * 该方法需要在主线程调用。
     */
    fun clearMemory(context: Context) {
        Glide.get(context.applicationContext).clearMemory()
    }

    /**
     * 清除磁盘缓存。
     *
     * Glide 要求该操作在后台线程执行，因此这里使用独立线程处理。
     */
    fun clearDiskCache(context: Context) {
        thread(start = true, name = "glide-disk-cache-clear") {
            Glide.get(context.applicationContext).clearDiskCache()
        }
    }

    private fun loadInternal(
        imageView: ImageView,
        model: Any?,
        options: RequestOptions.() -> Unit = {}
    ) {
        Glide.with(imageView)
            .load(model)
            .apply(RequestOptions().apply(options))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
}