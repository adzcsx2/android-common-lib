package com.hoyn.common.lib.ui.camera

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CameraViewModelTest {

    private lateinit var viewModel: CameraViewModel

    @Before
    fun setup() {
        viewModel = CameraViewModel()
    }

    // ========== Resolution Tests ==========

    @Test
    fun `initial resolution should be 720p`() = runTest {
        assertEquals(CameraViewModel.Resolution.HD_720P, viewModel.selectedResolution.value)
    }

    @Test
    fun `select 720p resolution`() = runTest {
        // Given: initial state is 1080p
        viewModel.selectResolution(CameraViewModel.Resolution.HD_1080P)
        assertEquals(CameraViewModel.Resolution.HD_1080P, viewModel.selectedResolution.value)

        // When
        viewModel.selectResolution(CameraViewModel.Resolution.HD_720P)

        // Then
        assertEquals(CameraViewModel.Resolution.HD_720P, viewModel.selectedResolution.value)
        assertEquals(
            listOf(CameraViewModel.Fps.FPS_120, CameraViewModel.Fps.FPS_240, CameraViewModel.Fps.FPS_480),
            viewModel.availableFpsOptions.value
        )
    }

    @Test
    fun `select 1080p resolution hides 480fps option`() = runTest {
        // When
        viewModel.selectResolution(CameraViewModel.Resolution.HD_1080P)

        // Then
        assertEquals(CameraViewModel.Resolution.HD_1080P, viewModel.selectedResolution.value)
        assertEquals(
            listOf(CameraViewModel.Fps.FPS_120, CameraViewModel.Fps.FPS_240),
            viewModel.availableFpsOptions.value
        )
    }

    @Test
    fun `select 1080p when 480fps is selected switches to 240fps`() = runTest {
        // Given: 480fps is selected (first switch to 720p to enable 480fps option)
        viewModel.selectResolution(CameraViewModel.Resolution.HD_720P)
        viewModel.selectFps(CameraViewModel.Fps.FPS_480)
        assertEquals(CameraViewModel.Fps.FPS_480, viewModel.selectedFps.value)

        // When
        viewModel.selectResolution(CameraViewModel.Resolution.HD_1080P)

        // Then
        assertEquals(CameraViewModel.Fps.FPS_240, viewModel.selectedFps.value)
        assertEquals(
            listOf(CameraViewModel.Fps.FPS_120, CameraViewModel.Fps.FPS_240),
            viewModel.availableFpsOptions.value
        )
    }

    // ========== FPS Tests ==========

    @Test
    fun `initial fps should be 120`() = runTest {
        assertEquals(CameraViewModel.Fps.FPS_120, viewModel.selectedFps.value)
    }

    @Test
    fun `initial available fps options should include all three`() = runTest {
        assertEquals(
            listOf(CameraViewModel.Fps.FPS_120, CameraViewModel.Fps.FPS_240, CameraViewModel.Fps.FPS_480),
            viewModel.availableFpsOptions.value
        )
    }

    @Test
    fun `selectFps returns false when not supported`() = runTest {
        // Given: current resolution only supports 120 and 240 fps
        viewModel.setSupportedFpsByResolution(
            mapOf(
                CameraViewModel.Resolution.HD_720P to setOf(120, 240),
                CameraViewModel.Resolution.HD_1080P to setOf(120)
            )
        )

        // When selecting 480fps
        val result = viewModel.selectFps(CameraViewModel.Fps.FPS_480)

        // Then
        assertFalse(result)
        assertEquals(CameraViewModel.Fps.FPS_120, viewModel.selectedFps.value) // remains unchanged
    }

    @Test
    fun `selectFps returns true when supported`() = runTest {
        // Given: device supports all frame rates for 720p
        viewModel.setSupportedFpsByResolution(
            mapOf(
                CameraViewModel.Resolution.HD_720P to setOf(120, 240, 480),
                CameraViewModel.Resolution.HD_1080P to setOf(120, 240)
            )
        )

        // When selecting 240fps
        val result = viewModel.selectFps(CameraViewModel.Fps.FPS_240)

        // Then
        assertTrue(result)
        assertEquals(CameraViewModel.Fps.FPS_240, viewModel.selectedFps.value)
    }

    @Test
    fun `selectFps succeeds when supported list is empty`() = runTest {
        // Given: supported list is empty (no restriction)
        viewModel.setSupportedFpsByResolution(emptyMap())

        // When selecting 480fps
        val result = viewModel.selectFps(CameraViewModel.Fps.FPS_480)

        // Then
        assertTrue(result)
        assertEquals(CameraViewModel.Fps.FPS_480, viewModel.selectedFps.value)
    }

    @Test
    fun `isFpsSupported returns true when list is empty`() = runTest {
        // Given
        viewModel.setSupportedFpsByResolution(emptyMap())

        // Then
        assertTrue(viewModel.isFpsSupported(CameraViewModel.Fps.FPS_120))
        assertTrue(viewModel.isFpsSupported(CameraViewModel.Fps.FPS_240))
        assertTrue(viewModel.isFpsSupported(CameraViewModel.Fps.FPS_480))
    }

    @Test
    fun `isFpsSupported returns correct value based on supported list`() = runTest {
        // Given: device only supports 120 and 240 fps for current resolution
        viewModel.setSupportedFpsByResolution(
            mapOf(
                CameraViewModel.Resolution.HD_720P to setOf(120, 240),
                CameraViewModel.Resolution.HD_1080P to setOf(120)
            )
        )

        // Then
        assertTrue(viewModel.isFpsSupported(CameraViewModel.Fps.FPS_120))
        assertTrue(viewModel.isFpsSupported(CameraViewModel.Fps.FPS_240))
        assertFalse(viewModel.isFpsSupported(CameraViewModel.Fps.FPS_480))
    }

    @Test
    fun `supported fps changes with resolution`() = runTest {
        viewModel.setSupportedFpsByResolution(
            mapOf(
                CameraViewModel.Resolution.HD_720P to setOf(120, 240),
                CameraViewModel.Resolution.HD_1080P to setOf(120)
            )
        )

        assertEquals(
            listOf(CameraViewModel.Fps.FPS_120, CameraViewModel.Fps.FPS_240),
            viewModel.availableFpsOptions.value
        )

        viewModel.selectResolution(CameraViewModel.Resolution.HD_1080P)

        assertEquals(
            listOf(CameraViewModel.Fps.FPS_120),
            viewModel.availableFpsOptions.value
        )
        assertEquals(CameraViewModel.Fps.FPS_120, viewModel.selectedFps.value)
    }

    // ========== Recording Timer Tests ==========

    @Test
    fun `initial recording state should be false`() = runTest {
        assertFalse(viewModel.isRecording.value)
        assertEquals(0, viewModel.recordingDurationSeconds.value)
        assertEquals("00:00", viewModel.recordingDurationFormatted.value)
    }

    @Test
    fun `startRecording sets isRecording to true and resets duration`() = runTest {
        // When
        viewModel.startRecording()

        // Then
        assertTrue(viewModel.isRecording.value)
        assertEquals(0, viewModel.recordingDurationSeconds.value)
    }

    @Test
    fun `recording timer increments duration`() = runTest {
        // When
        viewModel.startRecording()
        assertTrue(viewModel.isRecording.value)
        assertEquals(0, viewModel.recordingDurationSeconds.value)

        // Advance time by 3 seconds
        advanceTimeAndIdle(3000)

        // Then
        assertEquals(3, viewModel.recordingDurationSeconds.value)
        assertEquals("00:03", viewModel.recordingDurationFormatted.value)

        // Cleanup
        viewModel.stopRecording()
    }

    @Test
    fun `recording timer formats duration correctly`() = runTest {
        // When
        viewModel.startRecording()

        // Advance time by 65 seconds (1 minute 5 seconds)
        advanceTimeAndIdle(65000)

        // Then
        assertEquals(65, viewModel.recordingDurationSeconds.value)
        assertEquals("01:05", viewModel.recordingDurationFormatted.value)

        // Cleanup
        viewModel.stopRecording()
    }

    @Test
    fun `stopRecording sets isRecording to false and resets duration`() = runTest {
        // Given
        viewModel.startRecording()
        advanceTimeAndIdle(3000)
        assertEquals(3, viewModel.recordingDurationSeconds.value)

        // When
        viewModel.stopRecording()

        // Then
        assertFalse(viewModel.isRecording.value)
        assertEquals(0, viewModel.recordingDurationSeconds.value)
        assertEquals("00:00", viewModel.recordingDurationFormatted.value)
    }

    // ========== Zoom Tests ==========

    @Test
    fun `initial zoom ratio should be 1_0`() = runTest {
        assertEquals(1.0f, viewModel.zoomRatio.value, 0.01f)
        assertFalse(viewModel.showZoomIndicator.value)
    }

    @Test
    fun `updateZoomRatio updates value and shows indicator`() = runTest {
        // When
        viewModel.updateZoomRatio(2.0f)

        // Then
        assertEquals(2.0f, viewModel.zoomRatio.value, 0.01f)
        assertTrue(viewModel.showZoomIndicator.value)
    }

    @Test
    fun `zoom indicator shows and hides automatically`() = runTest {
        // When
        viewModel.updateZoomRatio(2.0f)

        // Then
        assertEquals(2.0f, viewModel.zoomRatio.value, 0.01f)
        assertTrue(viewModel.showZoomIndicator.value)

        // Wait for auto-hide (2 seconds)
        advanceTimeAndIdle(2500)

        // Then
        assertFalse(viewModel.showZoomIndicator.value)
    }

    @Test
    fun `updateZoomRatio coerces to minimum 1_0`() = runTest {
        // When: trying to set zoom below 1.0
        viewModel.updateZoomRatio(0.5f)

        // Then
        assertEquals(1.0f, viewModel.zoomRatio.value, 0.01f)
    }

    @Test
    fun `updateZoomRatio resets auto-hide timer`() = runTest {
        // Given
        viewModel.updateZoomRatio(2.0f)
        advanceTimeAndIdle(1500) // 1.5 seconds passed, indicator still visible
        assertTrue(viewModel.showZoomIndicator.value)

        // When: update zoom again before auto-hide
        viewModel.updateZoomRatio(3.0f)
        advanceTimeAndIdle(1500) // 1.5 seconds from second update
        assertTrue(viewModel.showZoomIndicator.value) // still visible

        // Wait for auto-hide from second update
        advanceTimeAndIdle(1000) // total 2.5 seconds from second update
        assertFalse(viewModel.showZoomIndicator.value) // now hidden
    }

    @Test
    fun `multiple zoom updates keep indicator visible`() = runTest {
        // When
        viewModel.updateZoomRatio(2.0f)
        advanceTimeAndIdle(1000)
        viewModel.updateZoomRatio(3.0f)
        advanceTimeAndIdle(1000)
        viewModel.updateZoomRatio(4.0f)

        // Then: indicator should still be visible
        assertTrue(viewModel.showZoomIndicator.value)
        assertEquals(4.0f, viewModel.zoomRatio.value, 0.01f)

        // Cleanup
        advanceTimeAndIdle(2500)
    }

    // ========== Helper Methods ==========

    /**
     * Advance main looper time and idle to allow coroutine execution
     */
    @Suppress("DEPRECATION")
    private fun advanceTimeAndIdle(millis: Long) {
        ShadowLooper.idleMainLooper()
        org.robolectric.Robolectric.getForegroundThreadScheduler().advanceBy(millis)
        ShadowLooper.idleMainLooper()
        Thread.sleep(100) // Small delay to allow coroutine execution
    }
}
