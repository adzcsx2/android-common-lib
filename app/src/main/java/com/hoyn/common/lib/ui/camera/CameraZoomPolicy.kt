package com.hoyn.common.lib.ui.camera

import kotlin.math.roundToInt

data class CameraZoomRange(
    val minRatio: Float,
    val maxRatio: Float
) {
    val isAdjustable: Boolean
        get() = maxRatio > minRatio
}

internal fun resolveZoomRange(fps: CameraViewModel.Fps, deviceMaxZoomRatio: Float): CameraZoomRange {
    val sanitizedMaxZoomRatio = deviceMaxZoomRatio.coerceAtLeast(1.0f)
    val maxRatio = when (fps) {
        CameraViewModel.Fps.FPS_480 -> 1.0f
        CameraViewModel.Fps.FPS_120,
        CameraViewModel.Fps.FPS_240 -> sanitizedMaxZoomRatio
    }
    return CameraZoomRange(
        minRatio = 1.0f,
        maxRatio = maxRatio.coerceAtLeast(1.0f)
    )
}

internal fun clampZoomRatio(ratio: Float, range: CameraZoomRange): Float {
    return ratio.coerceIn(range.minRatio, range.maxRatio)
}

internal fun storedValueToZoomRatio(value: Int): Float {
    return value.coerceAtLeast(100) / 100.0f
}

internal fun zoomRatioToStoredValue(ratio: Float): Int {
    return (ratio.coerceAtLeast(1.0f) * 100.0f).roundToInt()
}

internal fun progressToZoomRatio(progress: Int, range: CameraZoomRange, maxProgress: Int): Float {
    if (maxProgress <= 0 || !range.isAdjustable) {
        return range.minRatio
    }
    val normalizedProgress = progress.coerceIn(0, maxProgress)
    val fraction = normalizedProgress.toFloat() / maxProgress.toFloat()
    val ratio = range.minRatio + (range.maxRatio - range.minRatio) * fraction
    return clampZoomRatio(ratio, range)
}

internal fun zoomRatioToProgress(ratio: Float, range: CameraZoomRange, maxProgress: Int): Int {
    if (maxProgress <= 0 || !range.isAdjustable) {
        return 0
    }
    val clampedRatio = clampZoomRatio(ratio, range)
    val fraction = (clampedRatio - range.minRatio) / (range.maxRatio - range.minRatio)
    return (fraction * maxProgress.toFloat()).roundToInt().coerceIn(0, maxProgress)
}