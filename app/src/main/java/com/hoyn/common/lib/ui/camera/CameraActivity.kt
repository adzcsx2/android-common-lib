package com.hoyn.common.lib.ui.camera

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityCameraBinding
import com.hoyn.common.lib.logging.AppRuntimeLogCapture
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
 * - 帧率选择（120 / 240 fps）
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

    private val fpsButtons = linkedMapOf<CameraFpsOption, TextView>()
    private val permissionRequirement by lazy {
        CameraStartupPermissionPolicy.resolve(Build.VERSION.SDK_INT)
    }

    private var isStartingRecording = false
    private var isCameraSetup = false
    private var isPreviewVisible = true
    private var awaitingManageStorageResult = false

    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedPermissions = permissionRequirement.runtimePermissions
            .filter { permission -> permissions[permission] == true || hasPermission(permission) }
            .toSet()
        Logger.d(TAG, "Permission result granted=${grantedPermissions.joinToString()}")
        if (CameraStartupPermissionPolicy.hasAllRuntimePermissions(permissionRequirement, grantedPermissions)) {
            ensureStorageAccessAndSetupCamera()
            return@registerForActivityResult
        }
        handleRuntimePermissionDenied()
    }

    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        awaitingManageStorageResult = false
        handleManageStorageResult()
    }

    override fun initView(savedInstanceState: Bundle?) {
        // 设置透明状态栏
        StatusBarHelper.translucent(this)

        // 初始化控件
        setupControls()
        updateRuntimeLogPath()

        // 观察状态
        observeState()

        // 请求权限
        requestStartupPermissions()
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
        recorder.initialize(binding.previewView, binding.surfacePreviewView) {
            Logger.d(TAG, "Camera initialized")

            viewModel.setSupportedFpsByResolution(recorder.supportedFpsByResolution.value)
            viewModel.setDeviceMaxZoomRatio(recorder.maxZoomRatio.value)
        }
        recorder.setPreviewVisible(isPreviewVisible)
        updatePreviewToggleButton()

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
                viewModel.updateZoomRatio(zoomRatio, persist = false)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val zoomRatio = progressToZoomRatio(
                    seekBar?.progress ?: 0,
                    viewModel.zoomRange.value,
                    ZOOM_SLIDER_MAX
                )
                viewModel.updateZoomRatio(zoomRatio)
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

        binding.btnTogglePreview.click {
            togglePreviewVisibility()
        }

        renderFpsButtons(viewModel.availableFpsOptions.value)

        // 录像按钮
        binding.btnRecord.click {
            toggleRecording()
        }
    }

    /**
     * 选择帧率（如果设备支持）
     */
    private fun selectFpsIfSupported(fpsOption: CameraFpsOption) {
        if (viewModel.isFpsSupported(fpsOption)) {
            viewModel.selectFps(fpsOption)
            updateFpsButtons(fpsOption)
        } else {
            ToastUtil.show(formatUnsupportedFpsMessage(fpsOption))
        }
    }

    /**
     * 更新分辨率按钮状态
     */
    private fun updateResolutionButtons(resolution: Resolution) {
        binding.btnResolution720p.isSelected = resolution == Resolution.HD_720P
        binding.btnResolution1080p.isSelected = resolution == Resolution.HD_1080P
    }

    /**
     * 更新帧率按钮状态
     */
    private fun updateFpsButtons(fpsOption: CameraFpsOption) {
        fpsButtons.forEach { (value, button) ->
            button.isSelected = value == fpsOption
        }
    }

    private fun renderFpsButtons(options: List<CameraFpsOption>) {
        val normalizedOptions = options.distinct().sorted()
        if (fpsButtons.keys.toList() != normalizedOptions) {
            binding.fpsContainer.removeAllViews()
            fpsButtons.clear()

            normalizedOptions.forEachIndexed { index, fpsOption ->
                val button = TextView(this).apply {
                    background = ContextCompat.getDrawable(context, R.drawable.selector_setting_button)
                    text = formatFpsOptionLabel(fpsOption)
                    setTextColor(ContextCompat.getColor(context, R.color.camera_setting_button_text))
                    textSize = 14f
                    setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) {
                            marginStart = dpToPx(16)
                        }
                    }
                }
                button.click {
                    selectFpsIfSupported(fpsOption)
                }
                binding.fpsContainer.addView(button)
                fpsButtons[fpsOption] = button
            }
        }
        if (normalizedOptions.isEmpty()) binding.fpsScroll.gone() else binding.fpsScroll.visible()
        updateFpsButtons(viewModel.selectedFps.value)
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
        val fpsOption = viewModel.selectedFps.value
        Logger.d(
            TAG,
            "startRecording requested resolution=${resolution.name} fps=${fpsOption.lower}-${fpsOption.upper}"
        )

        isStartingRecording = true
        updateZoomControls()
        updateSettingControls()
        val started = recorder.startRecording(resolution, fpsOption)
        if (!started) {
            isStartingRecording = false
            updateZoomControls()
            updateSettingControls()
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
        updateZoomControls()
        updateSettingControls()
    }

    private fun togglePreviewVisibility() {
        isPreviewVisible = !isPreviewVisible
        if (::recorder.isInitialized) {
            recorder.setPreviewVisible(isPreviewVisible)
        }
        updatePreviewToggleButton()
    }

    private fun updatePreviewToggleButton() {
        binding.btnTogglePreview.text = getString(
            if (isPreviewVisible) R.string.camera_hide_preview else R.string.camera_show_preview
        )
        binding.btnTogglePreview.isSelected = isPreviewVisible
    }

    private fun updateRuntimeLogPath() {
        val logPath = AppRuntimeLogCapture.currentLogFilePath()
        binding.tvRuntimeLogPath.text = if (logPath.isNullOrBlank()) {
            getString(R.string.camera_runtime_log_path_unavailable)
        } else {
            getString(R.string.camera_runtime_log_path, logPath)
        }
    }

    private fun updateZoomControls() {
        val range = viewModel.zoomRange.value
        val shouldLockZoom = isStartingRecording || viewModel.isRecording.value
        val isEnabled = range.isAdjustable && !shouldLockZoom
        binding.seekZoom.isEnabled = isEnabled
        binding.seekZoom.alpha = if (isEnabled) 1.0f else 0.45f
    }

    private fun updateSettingControls() {
        val isLocked = isStartingRecording || viewModel.isRecording.value
        val alpha = if (isLocked) 0.45f else 1.0f
        binding.btnResolution720p.isEnabled = !isLocked
        binding.btnResolution1080p.isEnabled = !isLocked
        binding.btnTogglePreview.isEnabled = !isLocked
        binding.btnResolution720p.alpha = alpha
        binding.btnResolution1080p.alpha = alpha
        binding.btnTogglePreview.alpha = alpha
        fpsButtons.values.forEach { button ->
            button.isEnabled = !isLocked
            button.alpha = alpha
        }
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

    private fun formatFpsOptionLabel(fpsOption: CameraFpsOption): String {
        return when (fpsOption.upper) {
            120 -> getString(R.string.camera_fps_label_a)
            240 -> getString(R.string.camera_fps_label_b)
            else -> getString(R.string.camera_fps_exact_format, fpsOption.upper)
        }
    }

    private fun formatUnsupportedFpsMessage(fpsOption: CameraFpsOption): String {
        return getString(R.string.camera_unsupported_setting)
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
                updateZoomControls()
                updateSettingControls()
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
                binding.seekZoom.progress = zoomRatioToProgress(viewModel.zoomRatio.value, range, ZOOM_SLIDER_MAX)
                updateZoomControls()
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

        // 观察可用帧率选项
        lifecycleScope.launchWhenStarted {
            viewModel.availableFpsOptions.collect { options ->
                renderFpsButtons(options)
                updateSettingControls()
            }
        }

        // 观察分辨率变化，更新按钮选中状态
        lifecycleScope.launchWhenStarted {
            viewModel.selectedResolution.collect { resolution ->
                updateResolutionButtons(resolution)
                updateSettingControls()
            }
        }

        // 观察帧率变化，更新按钮选中状态
        lifecycleScope.launchWhenStarted {
            viewModel.selectedFps.collect { fps ->
                updateFpsButtons(fps)
                updateZoomControls()
                updateSettingControls()
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
        return hasPermission(Manifest.permission.CAMERA)
    }

    private fun requestStartupPermissions() {
        val missingPermissions = permissionRequirement.runtimePermissions
            .filterNot { permission -> hasPermission(permission) }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
            return
        }
        ensureStorageAccessAndSetupCamera()
    }

    private fun ensureStorageAccessAndSetupCamera() {
        val grantedPermissions = permissionRequirement.runtimePermissions
            .filter { permission -> hasPermission(permission) }
            .toSet()
        if (!CameraStartupPermissionPolicy.canProceed(permissionRequirement, grantedPermissions)) {
            handleRuntimePermissionDenied()
            return
        }
        if (permissionRequirement.promptsManageExternalStorage && !hasManageStorageAccess()) {
            requestManageStorageAccess()
            return
        }
        setupCamera()
    }

    private fun requestManageStorageAccess() {
        if (!permissionRequirement.promptsManageExternalStorage || hasManageStorageAccess()) {
            setupCamera()
            return
        }
        awaitingManageStorageResult = true
        ToastUtil.show(getString(R.string.camera_storage_settings_required))
        manageStorageLauncher.launch(buildManageStorageIntent())
    }

    private fun handleManageStorageResult() {
        if (hasManageStorageAccess()) {
            setupCamera()
            return
        }
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.camera_storage_permission_title)
            .setMessage(R.string.camera_storage_permission_denied)
            .setPositiveButton(R.string.retry) { _, _ ->
                requestManageStorageAccess()
            }
            .setNegativeButton(R.string.continue_label) { _, _ ->
                setupCamera()
            }
            .show()
    }

    private fun handleRuntimePermissionDenied() {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.camera_storage_permission_title)
            .setMessage(R.string.camera_permission_required)
            .setPositiveButton(R.string.retry) { _, _ ->
                requestStartupPermissions()
            }
            .setNegativeButton(R.string.back) { _, _ ->
                finish()
            }
            .show()
    }

    private fun buildManageStorageIntent(): Intent {
        val packageUri = Uri.parse("package:$packageName")
        return Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, packageUri)
            .takeIf { intent -> intent.resolveActivity(packageManager) != null }
            ?: Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    }

    private fun hasManageStorageAccess(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
    }

    private fun hasPermission(permission: String): Boolean {
        return checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
