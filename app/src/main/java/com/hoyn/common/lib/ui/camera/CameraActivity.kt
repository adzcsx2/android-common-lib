package com.hoyn.common.lib.ui.camera

import android.Manifest
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityCameraBinding
import com.hoyn.common.lib.ui.camera.CameraViewModel.Fps
import com.hoyn.common.lib.ui.camera.CameraViewModel.Resolution
import com.hoyn.common.ui.ext.click
import com.hoyn.common.ui.ext.gone
import com.hoyn.common.ui.ext.visible
import com.hoyn.common.ui.toast.ToastUtil
import com.hoyn.common.ui.utils.StatusBarHelper
import com.hoyn.common.utils.Logger

/**
 * 慢动作录像页面
 *
 * 功能：
 * - 分辨率选择（720p / 1080p）
 * - 帧率选择（120 / 240 / 480 fps）
 * - 高帧率慢动作录像
 * - 缩放功能
 */
class CameraActivity : BaseActivity<ActivityCameraBinding, CameraViewModel>() {

    companion object {
        private const val TAG = "CameraActivity"
        private const val ZOOM_SLIDER_MAX = 1000
    }

    // 慢动作录像控制器
    private lateinit var recorder: SlowMotionRecorder

    private var isStartingRecording = false
    private var isCameraSetup = false

    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        Logger.d(TAG, "Permission result cameraGranted=$cameraGranted")

        if (cameraGranted) {
            setupCamera()
        } else {
            ToastUtil.show(getString(R.string.camera_permission_required))
            finish()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        // 设置透明状态栏
        StatusBarHelper.translucent(this)

        // 请求权限
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA
            )
        )

        // 初始化控件
        setupControls()

        // 观察状态
        observeState()
    }

    /**
     * 初始化相机
     */
    private fun setupCamera() {
        if (isCameraSetup) {
            Logger.d(TAG, "setupCamera skipped: already initialized")
            return
        }
        Logger.d(TAG, "setupCamera start")
        recorder = SlowMotionRecorder(this, this)
        isCameraSetup = true

        // 初始化相机预览
        recorder.initialize(binding.previewView) {
            Logger.d(TAG, "Camera initialized")

            viewModel.setSupportedFpsByResolution(recorder.supportedFpsByResolution.value)
            viewModel.setDeviceMaxZoomRatio(recorder.maxZoomRatio.value)
        }

        // 设置录像回调
        recorder.setOnRecordingSavedListener { _ ->
            Logger.d(TAG, "Recording saved callback")
            resetRecordingUi()
        }

        recorder.setOnRecordingStartedListener {
            Logger.d(TAG, "Recording started callback")
            isStartingRecording = false
            viewModel.startRecording()
            binding.btnRecord.isSelected = true
            animateRecordButton(toRecording = true)
        }

        recorder.setOnRecordingFailedListener { error ->
            Logger.e(TAG, "Recording failed callback: $error")
            resetRecordingUi()
            ToastUtil.show(buildRecordingFailedMessage(error))
        }

    }

    /**
     * 设置控件点击事件
     */
    private fun setupControls() {
        binding.seekZoom.max = ZOOM_SLIDER_MAX
        binding.seekZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                val zoomRatio = progressToZoomRatio(progress, viewModel.zoomRange.value, ZOOM_SLIDER_MAX)
                viewModel.updateZoomRatio(zoomRatio)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        // 分辨率按钮
        binding.btnResolution720p.click {
            viewModel.selectResolution(Resolution.HD_720P)
            updateResolutionButtons(Resolution.HD_720P)
        }

        binding.btnResolution1080p.click {
            viewModel.selectResolution(Resolution.HD_1080P)
            updateResolutionButtons(Resolution.HD_1080P)
        }

        // 帧率按钮
        binding.btnFps120.click {
            selectFpsIfSupported(Fps.FPS_120, binding.btnFps120)
        }

        binding.btnFps240.click {
            selectFpsIfSupported(Fps.FPS_240, binding.btnFps240)
        }

        binding.btnFps480.click {
            selectFpsIfSupported(Fps.FPS_480, binding.btnFps480)
        }

        // 录像按钮
        binding.btnRecord.click {
            toggleRecording()
        }
    }

    /**
     * 选择帧率（如果设备支持）
     */
    private fun selectFpsIfSupported(fps: Fps, button: android.widget.TextView) {
        if (viewModel.isFpsSupported(fps)) {
            viewModel.selectFps(fps)
            updateFpsButtons(fps)
        } else {
            ToastUtil.show(getString(R.string.camera_unsupported_fps, fps.value))
        }
    }

    /**
     * 更新分辨率按钮状态
     */
    private fun updateResolutionButtons(resolution: Resolution) {
        binding.btnResolution720p.isSelected = resolution == Resolution.HD_720P
        binding.btnResolution1080p.isSelected = resolution == Resolution.HD_1080P

        // 根据 1080p 隐藏/显示 480fps 按钮
        updateFpsButtonsVisibility(resolution)
    }

    /**
     * 更新帧率按钮状态
     */
    private fun updateFpsButtons(fps: Fps) {
        binding.btnFps120.isSelected = fps == Fps.FPS_120
        binding.btnFps240.isSelected = fps == Fps.FPS_240
        binding.btnFps480.isSelected = fps == Fps.FPS_480
    }

    /**
     * 更新帧率按钮可见性
     */
    private fun updateFpsButtonsVisibility(resolution: Resolution) {
        if (resolution == Resolution.HD_1080P) {
            binding.btnFps480.gone()
        } else {
            binding.btnFps480.visible()
        }
    }

    /**
     * 切换录像状态
     */
    private fun toggleRecording() {
        if (viewModel.isRecording.value || isStartingRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    /**
     * 开始录像
     */
    private fun startRecording() {
        val resolution = viewModel.selectedResolution.value
        val fps = viewModel.selectedFps.value
        Logger.d(TAG, "startRecording requested resolution=${resolution.name} fps=${fps.value}")

        isStartingRecording = true
        val started = recorder.startRecording(resolution, fps)
        if (!started) {
            isStartingRecording = false
            return
        }
    }

    /**
     * 停止录像
     */
    private fun stopRecording() {
        Logger.d(TAG, "stopRecording requested")
        recorder.stopRecording()
        resetRecordingUi()
    }

    private fun resetRecordingUi() {
        Logger.d(TAG, "resetRecordingUi viewModelRecording=${viewModel.isRecording.value} isStartingRecording=$isStartingRecording")
        isStartingRecording = false
        if (viewModel.isRecording.value) {
            viewModel.stopRecording()
        }
        binding.btnRecord.isSelected = false
        animateRecordButton(toRecording = false)
    }

    /**
     * 录像按钮缩放动画
     *
     * @param toRecording true 表示进入录像状态（缩小到 50%），false 表示停止录像（恢复到 100%）
     */
    private fun animateRecordButton(toRecording: Boolean) {
        val targetScale = if (toRecording) 0.5f else 1.0f
        val currentScale = binding.btnRecord.scaleX

        // 只有当目标值和当前值不同时才执行动画
        if (currentScale != targetScale) {
            binding.btnRecord.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(200)
                .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun buildRecordingFailedMessage(error: String): String {
        val baseMessage = getString(R.string.camera_recording_failed)
        val normalizedError = error.trim()
        return if (normalizedError.isEmpty()) {
            baseMessage
        } else if (normalizedError == baseMessage || normalizedError.startsWith(baseMessage + ",")) {
            normalizedError
        } else {
            baseMessage + ", " + normalizedError
        }
    }

    /**
     * 观察状态变化
     */
    private fun observeState() {
        // 观察录像状态
        lifecycleScope.launchWhenStarted {
            viewModel.isRecording.collect { isRecording ->
                if (isRecording) {
                    binding.tvTimer.visible()
                } else {
                    binding.tvTimer.gone()
                }
            }
        }

        // 观察录像时长
        lifecycleScope.launchWhenStarted {
            viewModel.recordingDurationSeconds.collect { seconds ->
                binding.tvTimer.text = formatDuration(seconds)
            }
        }

        // 观察缩放范围
        lifecycleScope.launchWhenStarted {
            viewModel.zoomRange.collect { range ->
                binding.seekZoom.isEnabled = range.isAdjustable
                binding.seekZoom.alpha = if (range.isAdjustable) 1.0f else 0.45f
                binding.seekZoom.progress = zoomRatioToProgress(viewModel.zoomRatio.value, range, ZOOM_SLIDER_MAX)
            }
        }

        // 观察缩放比例
        lifecycleScope.launchWhenStarted {
            viewModel.zoomRatio.collect { ratio ->
                binding.tvZoomRatio.text = getString(R.string.camera_zoom_label, ratio)
                binding.seekZoom.progress = zoomRatioToProgress(ratio, viewModel.zoomRange.value, ZOOM_SLIDER_MAX)
                if (::recorder.isInitialized) {
                    recorder.setZoom(ratio)
                }
            }
        }

        // 观察可用帧率选项（用于 1080p 时隐藏 480fps）
        lifecycleScope.launchWhenStarted {
            viewModel.availableFpsOptions.collect { options ->
                if (options.contains(Fps.FPS_120)) binding.btnFps120.visible() else binding.btnFps120.gone()
                if (options.contains(Fps.FPS_240)) binding.btnFps240.visible() else binding.btnFps240.gone()
                if (options.contains(Fps.FPS_480)) binding.btnFps480.visible() else binding.btnFps480.gone()
            }
        }

        // 观察分辨率变化，更新按钮选中状态
        lifecycleScope.launchWhenStarted {
            viewModel.selectedResolution.collect { resolution ->
                updateResolutionButtons(resolution)
            }
        }

        // 观察帧率变化，更新按钮选中状态
        lifecycleScope.launchWhenStarted {
            viewModel.selectedFps.collect { fps ->
                updateFpsButtons(fps)
            }
        }
    }

    /**
     * 格式化时长
     */
    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    override fun onResume() {
        super.onResume()
        Logger.d(
            TAG,
            "onResume cameraSetup=$isCameraSetup previewAvailable=${binding.previewView.isAvailable} finishing=$isFinishing"
        )
        if (isCameraSetup && hasCameraPermission()) {
            recorder.onHostResume()
        }
    }

    override fun onPause() {
        Logger.d(
            TAG,
            "onPause cameraSetup=$isCameraSetup previewAvailable=${binding.previewView.isAvailable} recording=${viewModel.isRecording.value} starting=$isStartingRecording"
        )
        if (isCameraSetup) {
            recorder.onHostPause()
            resetRecordingUi()
        }
        super.onPause()
    }

    override fun onDestroy() {
        Logger.d(TAG, "onDestroy cameraSetup=$isCameraSetup")
        if (::recorder.isInitialized) {
            recorder.release()
        }
        super.onDestroy()
    }

    private fun hasCameraPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
