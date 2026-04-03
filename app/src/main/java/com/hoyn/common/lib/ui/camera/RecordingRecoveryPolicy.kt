package com.hoyn.common.lib.ui.camera

internal fun isEmptyRecordingOutput(sizeBytes: Long, durationMs: Long?): Boolean {
    if (sizeBytes <= 0L) {
        return true
    }
    return durationMs != null && durationMs <= 0L
}

internal fun shouldSuppressPreviewRecoveryFailure(
    suppressFlag: Boolean,
    isRecording: Boolean,
    isStartingRecording: Boolean
): Boolean {
    return suppressFlag && !isRecording && !isStartingRecording
}