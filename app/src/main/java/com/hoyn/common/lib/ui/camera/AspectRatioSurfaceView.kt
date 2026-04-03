package com.hoyn.common.lib.ui.camera

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class AspectRatioSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {

    private var aspectRatioWidth = 9
    private var aspectRatioHeight = 16

    fun setAspectRatio(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            return
        }
        aspectRatioWidth = width
        aspectRatioHeight = height
        requestLayout()
    }

    fun setFixedBufferSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            return
        }
        holder.setFixedSize(width, height)
    }

    fun addCallback(callback: SurfaceHolder.Callback) {
        holder.addCallback(callback)
    }

    fun removeCallback(callback: SurfaceHolder.Callback) {
        holder.removeCallback(callback)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availableHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (availableWidth == 0 || availableHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val targetWidth: Int
        val targetHeight: Int
        if (availableWidth * aspectRatioHeight <= availableHeight * aspectRatioWidth) {
            targetWidth = availableWidth
            targetHeight = availableWidth * aspectRatioHeight / aspectRatioWidth
        } else {
            targetHeight = availableHeight
            targetWidth = availableHeight * aspectRatioWidth / aspectRatioHeight
        }
        setMeasuredDimension(targetWidth, targetHeight)
    }
}