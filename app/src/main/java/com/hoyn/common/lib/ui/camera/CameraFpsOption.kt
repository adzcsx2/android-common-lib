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

internal fun filterExactFpsOptions(
    options: List<CameraFpsOption>
): List<CameraFpsOption> {
    return options.filter { it.isExact && it.upper >= MIN_DISPLAY_FPS }
}

internal fun resolveRecordingFpsOption(
    selectedOption: CameraFpsOption,
    supportedOptions: List<CameraFpsOption>
): CameraFpsOption? {
    return supportedOptions.firstOrNull { option -> option == selectedOption }
}