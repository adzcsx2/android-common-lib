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
 * - 帧率选择（120 / 240 / 480 fps）
 * - 录像状态
 * - 缩放比例
 * - 设置持久化（MMKV）
 */
class CameraViewModel : BaseViewModel<Nothing?>() {

    override val repository: Nothing? = null

    companion object {
        private const val KEY_RESOLUTION = "camera_resolution"
        private const val KEY_FPS = "camera_fps"
        private const val KEY_ZOOM_RATIO = "camera_zoom_ratio"
        private val DEFAULT_RESOLUTION = Resolution.HD_720P
        private val DEFAULT_FPS = Fps.FPS_120
        private const val DEFAULT_ZOOM_RATIO_STORED = 100
    }

    // 分辨率枚举
    enum class Resolution {
        HD_720P,  // 1280x720
        HD_1080P  // 1920x1080
    }

    // 帧率枚举
    enum class Fps(val value: Int) {
        FPS_120(120),
        FPS_240(240),
        FPS_480(480)
    }

    // 当前选中的分辨率
    private val _selectedResolution = MutableStateFlow(DEFAULT_RESOLUTION)
    val selectedResolution: StateFlow<Resolution> = _selectedResolution.asStateFlow()

    // 当前选中的帧率
    private val _selectedFps = MutableStateFlow(DEFAULT_FPS)
    val selectedFps: StateFlow<Fps> = _selectedFps.asStateFlow()

    // 可用的帧率选项（1080p 时隐藏 480fps）
    private val _availableFpsOptions = MutableStateFlow(listOf(Fps.FPS_120, Fps.FPS_240, Fps.FPS_480))
    val availableFpsOptions: StateFlow<List<Fps>> = _availableFpsOptions.asStateFlow()

    // 各分辨率对应支持的帧率列表
    private val _supportedFpsByResolution = MutableStateFlow<Map<Resolution, Set<Int>>>(emptyMap())
    val supportedFpsByResolution: StateFlow<Map<Resolution, Set<Int>>> = _supportedFpsByResolution.asStateFlow()

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
    private val _zoomRange = MutableStateFlow(resolveZoomRange(DEFAULT_FPS, 1.0f))
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
        val savedFpsOrdinal = MMKVUtils.getInt(KEY_FPS, DEFAULT_FPS.ordinal)
        _selectedFps.value = Fps.entries.getOrNull(savedFpsOrdinal) ?: DEFAULT_FPS

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
        val fallbackOptions = when (resolution) {
            Resolution.HD_720P -> listOf(Fps.FPS_120, Fps.FPS_240, Fps.FPS_480)
            Resolution.HD_1080P -> listOf(Fps.FPS_120, Fps.FPS_240)
        }
        val resolvedOptions = _supportedFpsByResolution.value[resolution]
            ?.let { supported ->
                Fps.entries.filter { fps -> fps.value in supported }
            }
            ?: fallbackOptions

        _availableFpsOptions.value = resolvedOptions

        if (_selectedFps.value !in resolvedOptions) {
            val nextFps = resolvedOptions.firstOrNull() ?: DEFAULT_FPS
            _selectedFps.value = nextFps
            MMKVUtils.put(KEY_FPS, nextFps.ordinal)
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
    fun selectFps(fps: Fps): Boolean {
        if (fps !in _availableFpsOptions.value) {
            return false
        }

        _selectedFps.value = fps

        // 保存到 MMKV
        MMKVUtils.put(KEY_FPS, fps.ordinal)

        refreshZoomState(preferredRatio = _zoomRatio.value, shouldPersist = hasResolvedDeviceZoomRatio)

        return true
    }

    /**
     * 设置支持的帧率列表
     */
    fun setSupportedFpsByResolution(supportedFpsByResolution: Map<Resolution, Set<Int>>) {
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
    fun isFpsSupported(fps: Fps): Boolean {
        return fps in _availableFpsOptions.value
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
    fun updateZoomRatio(ratio: Float) {
        val clampedRatio = clampZoomRatio(ratio, _zoomRange.value)
        _zoomRatio.value = clampedRatio
        restoredZoomRatio = clampedRatio
        MMKVUtils.put(KEY_ZOOM_RATIO, zoomRatioToStoredValue(clampedRatio))
    }

    private fun refreshZoomState(preferredRatio: Float, shouldPersist: Boolean) {
        val resolvedRange = resolveZoomRange(_selectedFps.value, deviceMaxZoomRatio)
        _zoomRange.value = resolvedRange

        val clampedRatio = clampZoomRatio(preferredRatio, resolvedRange)
        _zoomRatio.value = clampedRatio
        restoredZoomRatio = clampedRatio

        if (shouldPersist) {
            MMKVUtils.put(KEY_ZOOM_RATIO, zoomRatioToStoredValue(clampedRatio))
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
}
