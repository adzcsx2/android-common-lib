package com.hoyn.common.lib.ui.camera

import android.util.Range

data class CameraFpsOption(
    val lower: Int,
    val upper: Int
) : Comparable<CameraFpsOption> {

    val isExact: Boolean
        get() = lower == upper

    override fun compareTo(other: CameraFpsOption): Int {
        return compareValuesBy(this, other, CameraFpsOption::upper, CameraFpsOption::lower)
    }

    companion object {
        fun fromRange(range: Range<Int>): CameraFpsOption {
            return CameraFpsOption(lower = range.lower, upper = range.upper)
        }
    }
}

internal fun normalizeHighSpeedFpsRanges(
    rawRanges: Iterable<Range<Int>>,
    minSupportedFps: Int,
    maxSupportedFps: Int
): List<CameraFpsOption> {
    return normalizeHighSpeedFpsOptions(
        rawOptions = rawRanges.map { range -> CameraFpsOption.fromRange(range) },
        minSupportedFps = minSupportedFps,
        maxSupportedFps = maxSupportedFps
    )
}

internal fun normalizeHighSpeedFpsOptions(
    rawOptions: Iterable<CameraFpsOption>,
    minSupportedFps: Int,
    maxSupportedFps: Int
): List<CameraFpsOption> {
    return rawOptions
        .filter { option ->
            option.lower in minSupportedFps..maxSupportedFps &&
                option.upper in minSupportedFps..maxSupportedFps &&
                option.lower <= option.upper
        }
        .distinct()
        .sorted()
}

private const val MIN_DISPLAY_FPS = 120

/**
 * 过滤并规范化 FPS 选项显示
 *
 * 规则：
 * 1. 优先显示精确帧率（lower == upper），如 [120, 120] 显示为 "120"
 * 2. 对于非精确帧率范围（如 [120, 480]），如果 upper >= 120，显示为 upper 值（如 "480"）
 * 3. 只显示 upper >= 120 的选项
 */
internal fun filterExactFpsOptions(
    options: List<CameraFpsOption>
): List<CameraFpsOption> {
    // 添加调试日志
    android.util.Log.d("CameraFpsOption",
        "filterExactFpsOptions: input count=${options.size}, " +
        "input fps=${options.joinToString { "${it.lower}-${it.upper}" }}"
    )

    val result = options
        .filter { it.upper >= MIN_DISPLAY_FPS }
        .map { option ->
            // 如果不是精确帧率，将其规范化为 upper 值的精确帧率
            if (option.isExact) {
                option
            } else {
                CameraFpsOption(lower = option.upper, upper = option.upper)
            }
        }
        .distinct()
        .sorted()

    android.util.Log.d("CameraFpsOption",
        "filterExactFpsOptions: output count=${result.size}, " +
        "output fps=${result.joinToString { "${it.lower}-${it.upper}(exact=${it.isExact})" }}"
    )

    return result
}

internal fun resolveRecordingFpsOption(
    selectedOption: CameraFpsOption,
    supportedOptions: List<CameraFpsOption>
): CameraFpsOption? {
    return supportedOptions.firstOrNull { option -> option == selectedOption }
}