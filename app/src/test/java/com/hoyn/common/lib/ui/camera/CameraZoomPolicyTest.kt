package com.hoyn.common.lib.ui.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraZoomPolicyTest {

    @Test
    fun `120fps uses device max zoom`() {
        val range = resolveZoomRange(CameraViewModel.Fps.FPS_120, 8.0f)

        assertEquals(1.0f, range.minRatio, 0.001f)
        assertEquals(8.0f, range.maxRatio, 0.001f)
        assertTrue(range.isAdjustable)
    }

    @Test
    fun `480fps disables zoom`() {
        val range = resolveZoomRange(CameraViewModel.Fps.FPS_480, 8.0f)

        assertEquals(1.0f, range.minRatio, 0.001f)
        assertEquals(1.0f, range.maxRatio, 0.001f)
        assertFalse(range.isAdjustable)
    }

    @Test
    fun `clamp zoom ratio keeps value in current range`() {
        val range = CameraZoomRange(minRatio = 1.0f, maxRatio = 5.0f)

        assertEquals(1.0f, clampZoomRatio(0.5f, range), 0.001f)
        assertEquals(5.0f, clampZoomRatio(8.0f, range), 0.001f)
        assertEquals(3.2f, clampZoomRatio(3.2f, range), 0.001f)
    }

    @Test
    fun `seek progress maps back to stored zoom`() {
        val range = CameraZoomRange(minRatio = 1.0f, maxRatio = 6.0f)
        val progress = zoomRatioToProgress(3.5f, range, 1000)
        val restoredRatio = progressToZoomRatio(progress, range, 1000)

        assertEquals(3.5f, restoredRatio, 0.01f)
    }
}