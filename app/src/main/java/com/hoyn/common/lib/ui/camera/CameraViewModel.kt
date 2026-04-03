package com.hoyn.common.lib.ui.camera

import androidx.lifecycle.viewModelScope
import com.hoyn.common.base.BaseViewModel
import com.hoyn.common.utils.MMKVUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CameraActivity 的 ViewModel
 *
 * 管理相机设置状态：
 * - 分辨率选择（720p / 1080p）
 * - 帧率选择（120 / 240 fps）
 * - 录像状态
 * - 缩放比例
 * - 设置持久化（MMKV）
 */
class CameraViewModel : BaseViewModel<Nothing?>() {

    override val repository: Nothing? = null

    companion object {
        private const val KEY_RESOLUTION = "camera_resolution"
        private const val KEY_FPS = "camera_fps"
        private const val KEY_FPS_LOWER = "camera_fps_lower"
        private const val KEY_ZOOM_RATIO = "camera_zoom_ratio"
        private val DEFAULT_RESOLUTION = Resolution.HD_1080P
        private const val DEFAULT_FPS = 240
        private val DEFAULT_FPS_OPTION = CameraFpsOption(DEFAULT_FPS, DEFAULT_FPS)
        private const val DEFAULT_ZOOM_RATIO_STORED = 100
    }

    // 分辨率枚举
    enum class Resolution {
        HD_720P,  // 1280x720
        HD_1080P  // 1920x1080
    }

    // 当前选中的分辨率
    private val _selectedResolution = MutableStateFlow(DEFAULT_RESOLUTION)
    val selectedResolution: StateFlow<Resolution> = _selectedResolution.asStateFlow()

    // 当前选中的帧率
    private val _selectedFps = MutableStateFlow(DEFAULT_FPS_OPTION)
    val selectedFps: StateFlow<CameraFpsOption> = _selectedFps.asStateFlow()

    // 可用的帧率选项
    private val _availableFpsOptions = MutableStateFlow<List<CameraFpsOption>>(emptyList())
    val availableFpsOptions: StateFlow<List<CameraFpsOption>> = _availableFpsOptions.asStateFlow()

    // 各分辨率对应支持的帧率列表
    private val _supportedFpsByResolution = MutableStateFlow<Map<Resolution, List<CameraFpsOption>>>(emptyMap())
    val supportedFpsByResolution: StateFlow<Map<Resolution, List<CameraFpsOption>>> = _supportedFpsByResolution.asStateFlow()

    // 是否正在录像
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // 录像时长（秒）
    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    // 格式化的录像时长字符串 (mm:ss)
    private val _recordingDurationFormatted = MutableStateFlow("00:00")
    val recordingDurationFormatted: StateFlow<String> = _recordingDurationFormatted.asStateFlow()

    // 缩放比例
    private val _zoomRatio = MutableStateFlow(1.0f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    // 当前可用缩放范围
    private val _zoomRange = MutableStateFlow(resolveZoomRange(DEFAULT_FPS_OPTION.upper, 1.0f))
    val zoomRange: StateFlow<CameraZoomRange> = _zoomRange.asStateFlow()

    // 录像计时器 Job
    private var recordingTimerJob: Job? = null

    private var deviceMaxZoomRatio = 1.0f
    private var restoredZoomRatio = 1.0f
    private var hasResolvedDeviceZoomRatio = false

    init {
        // 从 MMKV 恢复保存的设置
        restoreSettings()

        // 监听录像时长变化，更新格式化字符串
        viewModelScope.launch {
            _recordingDurationSeconds.collect { seconds ->
                _recordingDurationFormatted.value = formatDuration(seconds)
            }
        }
    }

    /**
     * 从 MMKV 恢复保存的设置
     */
    private fun restoreSettings() {
        // 恢复分辨率设置
        val savedResolutionOrdinal = MMKVUtils.getInt(KEY_RESOLUTION, DEFAULT_RESOLUTION.ordinal)
        _selectedResolution.value = Resolution.entries.getOrNull(savedResolutionOrdinal) ?: DEFAULT_RESOLUTION

        // 恢复帧率设置
        val savedUpperFps = normalizeStoredFpsValue(MMKVUtils.getInt(KEY_FPS, DEFAULT_FPS))
        val savedLowerFps = normalizeStoredFpsValue(MMKVUtils.getInt(KEY_FPS_LOWER, savedUpperFps))
        _selectedFps.value = CameraFpsOption(
            lower = savedLowerFps.coerceAtMost(savedUpperFps),
            upper = savedUpperFps
        )

        restoredZoomRatio = storedValueToZoomRatio(
            MMKVUtils.getInt(KEY_ZOOM_RATIO, DEFAULT_ZOOM_RATIO_STORED)
        )

        // 根据分辨率更新可用帧率选项
        updateAvailableFpsOptions(_selectedResolution.value)
    }

    /**
     * 选择分辨率
     *
     * @param resolution 目标分辨率
     */
    fun selectResolution(resolution: Resolution) {
        _selectedResolution.value = resolution

        // 保存到 MMKV
        MMKVUtils.put(KEY_RESOLUTION, resolution.ordinal)

        // 更新可用帧率选项
        updateAvailableFpsOptions(resolution)
    }

    /**
     * 更新可用帧率选项
     */
    private fun updateAvailableFpsOptions(resolution: Resolution) {
        val resolvedOptions = filterExactFpsOptions(
            _supportedFpsByResolution.value[resolution]
                ?.sorted()
                .orEmpty()
        )

        _availableFpsOptions.value = resolvedOptions

        if (resolvedOptions.isNotEmpty() && _selectedFps.value !in resolvedOptions) {
            val nextFpsOption = preferredFpsFor(resolvedOptions)
            _selectedFps.value = nextFpsOption
            persistSelectedFps(nextFpsOption)
        }

        refreshZoomState(
            preferredRatio = if (hasResolvedDeviceZoomRatio) _zoomRatio.value else restoredZoomRatio,
            shouldPersist = hasResolvedDeviceZoomRatio
        )
    }

    /**
     * 选择帧率
     *
     * @param fps 目标帧率
     * @return 是否选择成功（设备不支持时返回 false）
     */
    fun selectFps(fpsOption: CameraFpsOption): Boolean {
        if (fpsOption !in _availableFpsOptions.value) {
            return false
        }

        _selectedFps.value = fpsOption

        persistSelectedFps(fpsOption)

        refreshZoomState(preferredRatio = _zoomRatio.value, shouldPersist = hasResolvedDeviceZoomRatio)

        return true
    }

    /**
     * 设置支持的帧率列表
     */
    fun setSupportedFpsByResolution(supportedFpsByResolution: Map<Resolution, List<CameraFpsOption>>) {
        _supportedFpsByResolution.value = supportedFpsByResolution
        updateAvailableFpsOptions(_selectedResolution.value)
    }

    fun setDeviceMaxZoomRatio(maxZoomRatio: Float) {
        deviceMaxZoomRatio = maxZoomRatio.coerceAtLeast(1.0f)
        hasResolvedDeviceZoomRatio = true
        refreshZoomState(preferredRatio = restoredZoomRatio, shouldPersist = false)
    }

    /**
     * 检查帧率是否支持
     */
    fun isFpsSupported(fpsOption: CameraFpsOption): Boolean {
        return fpsOption in _availableFpsOptions.value
    }

    /**
     * 开始录像
     */
    fun startRecording() {
        _isRecording.value = true
        _recordingDurationSeconds.value = 0

        // 启动计时器
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _recordingDurationSeconds.value += 1
            }
        }
    }

    /**
     * 停止录像
     */
    fun stopRecording() {
        _isRecording.value = false
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        _recordingDurationSeconds.value = 0
    }

    /**
     * 更新缩放比例
     */
    fun updateZoomRatio(ratio: Float, persist: Boolean = true) {
        val clampedRatio = clampZoomRatio(ratio, _zoomRange.value)
        _zoomRatio.value = clampedRatio
        restoredZoomRatio = clampedRatio
        if (persist) {
            MMKVUtils.put(KEY_ZOOM_RATIO, zoomRatioToStoredValue(clampedRatio))
        }
    }

    private fun refreshZoomState(preferredRatio: Float, shouldPersist: Boolean) {
        val resolvedZoomState = resolveZoomState(
            preferredRatio = preferredRatio,
            restoredRatio = restoredZoomRatio,
            fps = resolveZoomPolicyFps(
                selectedOption = _selectedFps.value,
                supportedOptions = _availableFpsOptions.value
            ),
            deviceMaxZoomRatio = deviceMaxZoomRatio,
            hasResolvedDeviceZoomRatio = hasResolvedDeviceZoomRatio
        )
        _zoomRange.value = resolvedZoomState.range
        _zoomRatio.value = resolvedZoomState.appliedRatio
        restoredZoomRatio = resolvedZoomState.restoredRatio

        if (shouldPersist) {
            MMKVUtils.put(KEY_ZOOM_RATIO, zoomRatioToStoredValue(resolvedZoomState.appliedRatio))
        }
    }

    /**
     * 格式化时长为 mm:ss 格式
     */
    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    override fun onCleared() {
        super.onCleared()
        recordingTimerJob?.cancel()
    }

    private fun preferredFpsFor(options: List<CameraFpsOption>): CameraFpsOption {
        return options.firstOrNull { option -> option.upper == DEFAULT_FPS && option.isExact }
            ?: options.firstOrNull { option -> option.isExact }
            ?: options.filter { option -> option.upper == DEFAULT_FPS }.maxByOrNull { option -> option.lower }
            ?: options.firstOrNull()
            ?: DEFAULT_FPS_OPTION
    }

    private fun normalizeStoredFpsValue(rawValue: Int): Int {
        return when (rawValue) {
            0 -> 120
            1 -> 240
            2 -> DEFAULT_FPS
            else -> if (rawValue == 480) DEFAULT_FPS else rawValue
        }
    }

    private fun persistSelectedFps(fpsOption: CameraFpsOption) {
        MMKVUtils.put(KEY_FPS, fpsOption.upper)
        MMKVUtils.put(KEY_FPS_LOWER, fpsOption.lower)
    }
}
