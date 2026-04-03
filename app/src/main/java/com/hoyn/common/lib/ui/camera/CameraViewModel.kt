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
 * - 分辨率选择（动态支持所有设备支持的分辨率）
 * - 帧率选择（120 / 240 fps）
 * - 录像状态
 * - 缩放比例
 * - 设置持久化（MMKV）
 */
class CameraViewModel : BaseViewModel<Nothing?>() {

    override val repository: Nothing? = null

    companion object {
        private const val KEY_RESOLUTION = "camera_resolution"
        private const val KEY_RESOLUTION_WIDTH = "camera_resolution_width"
        private const val KEY_RESOLUTION_HEIGHT = "camera_resolution_height"
        private const val KEY_FPS = "camera_fps"
        private const val KEY_FPS_LOWER = "camera_fps_lower"
        private const val KEY_ZOOM_RATIO = "camera_zoom_ratio"
        private val DEFAULT_RESOLUTION = CameraResolution.HD_1080P
        private const val DEFAULT_FPS = 240
        private val DEFAULT_FPS_OPTION = CameraFpsOption(DEFAULT_FPS, DEFAULT_FPS)
        private const val DEFAULT_ZOOM_RATIO_STORED = 100
    }

    // 当前选中的分辨率
    private val _selectedResolution = MutableStateFlow(DEFAULT_RESOLUTION)
    val selectedResolution: StateFlow<CameraResolution> = _selectedResolution.asStateFlow()

    // 可用的分辨率选项
    private val _availableResolutions = MutableStateFlow<List<CameraResolution>>(emptyList())
    val availableResolutions: StateFlow<List<CameraResolution>> = _availableResolutions.asStateFlow()

    // 当前选中的帧率
    private val _selectedFps = MutableStateFlow(DEFAULT_FPS_OPTION)
    val selectedFps: StateFlow<CameraFpsOption> = _selectedFps.asStateFlow()

    // 可用的帧率选项
    private val _availableFpsOptions = MutableStateFlow<List<CameraFpsOption>>(emptyList())
    val availableFpsOptions: StateFlow<List<CameraFpsOption>> = _availableFpsOptions.asStateFlow()

    // 技术调试日志（用于在 LogOverlay 显示）
    private val _debugLog = MutableStateFlow<String?>(null)
    val debugLog: StateFlow<String?> = _debugLog.asStateFlow()

    /**
     * 添加调试日志（会显示在 LogOverlay）
     */
    fun addDebugLog(message: String) {
        _debugLog.value = message
    }

    // 各分辨率对应支持的帧率列表
    private val _supportedFpsByResolution = MutableStateFlow<Map<CameraResolution, List<CameraFpsOption>>>(emptyMap())
    val supportedFpsByResolution: StateFlow<Map<CameraResolution, List<CameraFpsOption>>> = _supportedFpsByResolution.asStateFlow()

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
     *
     * 双重存储策略：
     * 1. 优先读取新格式（KEY_RESOLUTION_WIDTH + KEY_RESOLUTION_HEIGHT）
     * 2. 如果不存在，读取旧格式（KEY_RESOLUTION + ordinal）
     * 3. 保存时同时写入新旧两种格式
     */
    private fun restoreSettings() {
        // 恢复分辨率设置（双重存储）
        val savedWidth = MMKVUtils.getInt(KEY_RESOLUTION_WIDTH, -1)
        val savedHeight = MMKVUtils.getInt(KEY_RESOLUTION_HEIGHT, -1)

        _selectedResolution.value = if (savedWidth > 0 && savedHeight > 0) {
            // 新格式：从宽高恢复
            CameraResolution.fromSize(savedWidth, savedHeight)
        } else {
            // 旧格式：从 ordinal 恢复（向后兼容）
            val savedOrdinal = MMKVUtils.getInt(KEY_RESOLUTION, DEFAULT_RESOLUTION.toStorageOrdinal())
            CameraResolution.fromOrdinal(savedOrdinal)
        }

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
     * 双重存储策略：同时保存新格式（宽高）和旧格式（ordinal）
     *
     * @param resolution 目标分辨率
     */
    fun selectResolution(resolution: CameraResolution) {
        _selectedResolution.value = resolution

        // 双重存储：新格式（宽高）
        MMKVUtils.put(KEY_RESOLUTION_WIDTH, resolution.width)
        MMKVUtils.put(KEY_RESOLUTION_HEIGHT, resolution.height)

        // 双重存储：旧格式（ordinal），用于向后兼容
        MMKVUtils.put(KEY_RESOLUTION, resolution.toStorageOrdinal())

        // 更新可用帧率选项
        updateAvailableFpsOptions(resolution)
    }

    /**
     * 更新可用帧率选项
     */
    private fun updateAvailableFpsOptions(resolution: CameraResolution) {
        val rawOptions = _supportedFpsByResolution.value[resolution]
            ?.sorted()
            .orEmpty()

        val rawLogMsg = "原始fps[${rawOptions.size}]: ${rawOptions.joinToString { "${it.lower}-${it.upper}" }}"
        addDebugLog(rawLogMsg)
        com.hoyn.common.utils.Logger.d("CameraViewModel", "updateAvailableFpsOptions: $rawLogMsg")

        val resolvedOptions = filterExactFpsOptions(rawOptions)

        val filteredLogMsg = "显示fps[${resolvedOptions.size}]: ${resolvedOptions.joinToString { "${it.upper}fps" }}"
        addDebugLog(filteredLogMsg)
        com.hoyn.common.utils.Logger.d("CameraViewModel", "updateAvailableFpsOptions: $filteredLogMsg")

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
    fun setSupportedFpsByResolution(supportedFpsByResolution: Map<CameraResolution, List<CameraFpsOption>>) {
        _supportedFpsByResolution.value = supportedFpsByResolution

        // 输出调试日志：每个分辨率支持的 fps（显示在 LogOverlay）
        supportedFpsByResolution.forEach { (resolution, fpsList) ->
            val logMsg = "分辨率 ${resolution.displayName} 支持的fps: ${fpsList.joinToString { "${it.lower}-${it.upper}" }}"
            addDebugLog(logMsg)
            com.hoyn.common.utils.Logger.d("CameraViewModel", logMsg)
        }

        // 更新可用分辨率列表（按宽度升序）
        val resolutions = supportedFpsByResolution.keys
            .filter { resolution -> supportedFpsByResolution[resolution].isNullOrEmpty().not() }
            .sortedBy { it.width }
        _availableResolutions.value = resolutions

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

    /**
     * 规范化存储的 FPS 值
     *
     * 向后兼容旧版本的特殊值：
     * - 0 -> 120fps
     * - 1 -> 240fps
     * - 2 -> DEFAULT_FPS (240fps)
     * - 其他值直接使用（包括 480、960 等）
     */
    private fun normalizeStoredFpsValue(rawValue: Int): Int {
        return when (rawValue) {
            0 -> 120
            1 -> 240
            2 -> DEFAULT_FPS
            else -> rawValue  // 直接使用存储的值，支持 480、960 等
        }
    }

    private fun persistSelectedFps(fpsOption: CameraFpsOption) {
        MMKVUtils.put(KEY_FPS, fpsOption.upper)
        MMKVUtils.put(KEY_FPS_LOWER, fpsOption.lower)
    }
}
