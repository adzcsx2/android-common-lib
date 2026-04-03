package com.hoyn.common.lib.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Range
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.hoyn.common.lib.R
import com.hoyn.common.lib.ui.camera.CameraViewModel.Fps
import com.hoyn.common.lib.ui.camera.CameraViewModel.Resolution
import com.hoyn.common.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 慢动作录像控制器
 *
 * 使用 Camera2 constrained high speed session 进行真正的慢动作录像。
 */
class SlowMotionRecorder(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    companion object {
        private const val TAG = "SlowMotionRecorder"
        private const val OUTPUT_FRAME_RATE = 30
        private const val MOVIE_DIRECTORY = "Movies/SlowMotion"
        private const val ULTRA_HIGH_SPEED_PREVIEW_FALLBACK_INTERVAL_MS = 25.0
        private const val ULTRA_HIGH_SPEED_PREVIEW_FALLBACK_CONSECUTIVE_WINDOWS = 2
    }

    private data class HighSpeedProfile(
        val size: Size,
        val fpsRange: Range<Int>
    ) {
        val fps: Int
            get() = fpsRange.upper
    }

    private data class RecordingAttempt(
        val token: Long,
        val recorder: MediaRecorder,
        val outputPfd: ParcelFileDescriptor,
        val outputUri: android.net.Uri,
        val profile: HighSpeedProfile
    )

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val mainExecutor = ContextCompat.getMainExecutor(context)

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var previewView: AspectRatioTextureView? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewSurface: Surface? = null
    private var activeAttempt: RecordingAttempt? = null
    private var cameraId: String? = null
    private var cameraCharacteristics: CameraCharacteristics? = null
    private var sensorOrientation = 90
    private var activeArrayRect: Rect? = null
    private var currentProfile: HighSpeedProfile? = null
    private var pendingOnReady: (() -> Unit)? = null
    private var supportedProfilesByResolution: Map<Resolution, List<HighSpeedProfile>> = emptyMap()
    private var previewSize: Size? = null
    private var startSequence = 0L
    private var isStartingRecording = false
    private var isReleasing = false
    private var isHostResumed = false
    private var lifecycleGeneration = 0L
    private var recordingStatsRunnable: Runnable? = null
    private var recordingStartedAtMs = 0L
    private var lastCaptureTimestampNs = 0L
    private var captureDeltaCount = 0
    private var captureDeltaSumNs = 0L
    private var captureDeltaMaxNs = 0L
    private var lastExposureTimeNs = 0L
    private var lastFrameDurationNs = 0L
    private var isRecordingPreviewEnabled = true
    private var unstableRecordingPreviewWindows = 0

    private val recordingCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            if (!_isRecording.value) {
                return
            }
            val timestampNs = result.get(CaptureResult.SENSOR_TIMESTAMP) ?: return
            if (lastCaptureTimestampNs != 0L) {
                val deltaNs = timestampNs - lastCaptureTimestampNs
                if (deltaNs > 0L) {
                    captureDeltaCount += 1
                    captureDeltaSumNs += deltaNs
                    if (deltaNs > captureDeltaMaxNs) {
                        captureDeltaMaxNs = deltaNs
                    }
                }
            }
            lastCaptureTimestampNs = timestampNs
            lastExposureTimeNs = result.get(CaptureResult.SENSOR_EXPOSURE_TIME) ?: 0L
            lastFrameDurationNs = result.get(CaptureResult.SENSOR_FRAME_DURATION) ?: 0L
        }
    }

    private val surfaceTextureListener = object : android.view.TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
            Logger.d(
                TAG,
                "Surface available width=$width height=$height displayRotation=${previewView?.display?.rotation ?: Surface.ROTATION_0} resumed=$isHostResumed recording=${_isRecording.value} starting=$isStartingRecording"
            )
            openCameraIfReady()
        }

        override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
            Logger.d(TAG, "Surface size changed width=$width height=$height")
            applyPreviewTransform()
        }

        override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
            Logger.d(
                TAG,
                "Surface destroyed resumed=$isHostResumed recording=${_isRecording.value} starting=$isStartingRecording"
            )
            closeCameraResources(stopThread = !isHostResumed)
            return true
        }

        override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {
        }
    }

    // 当前录像状态
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // 缩放比例
    private val _zoomRatio = MutableStateFlow(1.0f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    // 最大缩放比例
    private val _maxZoomRatio = MutableStateFlow(1.0f)
    val maxZoomRatio: StateFlow<Float> = _maxZoomRatio.asStateFlow()

    // 各分辨率支持的帧率
    private val _supportedFpsByResolution = MutableStateFlow<Map<Resolution, Set<Int>>>(emptyMap())
    val supportedFpsByResolution: StateFlow<Map<Resolution, Set<Int>>> = _supportedFpsByResolution.asStateFlow()

    // 录像回调
    private var onRecordingStarted: (() -> Unit)? = null
    private var onRecordingSaved: ((String) -> Unit)? = null
    private var onRecordingFailed: ((String) -> Unit)? = null

    /**
     * 初始化相机
     *
     * @param previewView 预览视图
     * @param onReady 初始化完成回调
     */
    fun initialize(
        previewView: AspectRatioTextureView,
        onReady: () -> Unit = {}
    ) {
        Logger.d(TAG, "initialize previewAvailable=${previewView.isAvailable}")
        this.previewView = previewView
        previewView.setAspectRatio(9, 16)
        previewView.isOpaque = true
        pendingOnReady = onReady
        isReleasing = false
        if (!isHostResumed) {
            lifecycleGeneration += 1
        }
        isHostResumed = true
        startBackgroundThread()

        try {
            resolveCameraConfig()
            attachSurfaceTextureListener()
            openCameraIfReady()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize camera: ${e.message}")
        }
    }

    fun onHostResume() {
        val previewView = previewView ?: return
        Logger.d(
            TAG,
            "onHostResume previewAvailable=${previewView.isAvailable} displayRotation=${previewView.display?.rotation ?: Surface.ROTATION_0} cameraOpened=${cameraDevice != null} sessionOpened=${captureSession != null}"
        )
        if (!isHostResumed) {
            lifecycleGeneration += 1
        }
        isHostResumed = true
        isReleasing = false
        startBackgroundThread()
        if (cameraId == null) {
            resolveCameraConfig()
        }
        attachSurfaceTextureListener()
        openCameraIfReady()
    }

    fun onHostPause() {
        Logger.d(
            TAG,
            "onHostPause recording=${_isRecording.value} starting=$isStartingRecording cameraOpened=${cameraDevice != null}"
        )
        lifecycleGeneration += 1
        isHostResumed = false
        pendingOnReady = null
        if (_isRecording.value || isStartingRecording) {
            val handler = backgroundHandler
            if (handler != null) {
                handler.post {
                    stopRecording()
                }
            } else {
                Thread {
                    stopRecording()
                }.start()
            }
            return
        }
        closeCameraResources(stopThread = true)
    }

    private fun resolveCameraConfig() {
        try {
            val backCameraIds = cameraManager.cameraIdList.filter { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            val matchedCameraId = backCameraIds.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                supportedProfilesFor(characteristics).values.any { profiles -> profiles.isNotEmpty() }
            }
            val resolvedCameraId = matchedCameraId ?: backCameraIds.firstOrNull()
            if (resolvedCameraId == null) {
                throw IllegalStateException("No back camera available")
            }
            cameraId = resolvedCameraId

            val characteristics = cameraManager.getCameraCharacteristics(resolvedCameraId)
            cameraCharacteristics = characteristics
            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
            activeArrayRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            _maxZoomRatio.value = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1.0f
            supportedProfilesByResolution = supportedProfilesFor(characteristics)
            Logger.d(
                TAG,
                "resolveCameraConfig cameraId=$resolvedCameraId sensorOrientation=$sensorOrientation maxZoom=${_maxZoomRatio.value}"
            )
            _supportedFpsByResolution.value = supportedProfilesByResolution.mapValues { (_, profiles) ->
                profiles.map { profile -> profile.fps }.toSet()
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to resolve camera config: ${e.message}")
            supportedProfilesByResolution = emptyMap()
            _supportedFpsByResolution.value = emptyMap()
        }
    }

    private fun supportedProfilesFor(characteristics: CameraCharacteristics): Map<Resolution, List<HighSpeedProfile>> {
        val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: return emptyMap()
        if (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO !in capabilities) {
            return emptyMap()
        }
        val streamMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return emptyMap()
        return Resolution.entries.associateWith { resolution ->
            profilesForResolution(streamMap, resolution)
        }
    }

    private fun profilesForResolution(
        streamMap: StreamConfigurationMap,
        resolution: Resolution
    ): List<HighSpeedProfile> {
        val targetSize = when (resolution) {
            Resolution.HD_720P -> Size(1280, 720)
            Resolution.HD_1080P -> Size(1920, 1080)
        }
        return streamMap.highSpeedVideoSizes
            .filter { size -> size == targetSize }
            .flatMap { size ->
                val rawRanges = streamMap.getHighSpeedVideoFpsRangesFor(size)
                Logger.d(
                    TAG,
                    "rawHighSpeedRanges resolution=${resolution.name} size=${size.width}x${size.height} ranges=${rawRanges.joinToString(prefix = "[", postfix = "]") { range -> "${range.lower}-${range.upper}" }}"
                )
                rawRanges
                    .groupBy { range -> range.upper }
                    .mapNotNull { (fps, ranges) ->
                        if (fps !in setOf(120, 240, 480)) {
                            null
                        } else {
                            val exactRange = ranges.firstOrNull { range -> range.lower == range.upper }
                            val preferredRange = when {
                                fps >= 480 -> exactRange
                                else -> exactRange ?: ranges.maxByOrNull { range -> range.lower }
                            }
                            preferredRange?.let { range ->
                                Logger.d(
                                    TAG,
                                    "profilesForResolution resolution=${resolution.name} size=${size.width}x${size.height} fps=$fps selectedRange=${range.lower}-${range.upper} exact=${range.lower == range.upper}"
                                )
                                HighSpeedProfile(size, range)
                            }
                        }
                    }
            }
            .sortedBy { profile -> profile.fps }
    }

    private fun openCamera() {
        val previewView = previewView ?: return
        val cameraId = cameraId ?: return
        val generation = lifecycleGeneration
        if (!previewView.isAvailable || isReleasing) {
            Logger.d(
                TAG,
                "openCamera skipped previewAvailable=${previewView.isAvailable} isReleasing=$isReleasing resumed=$isHostResumed"
            )
            return
        }
        Logger.d(TAG, "openCamera cameraId=$cameraId resumed=$isHostResumed")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Logger.e(TAG, "Missing camera permission")
            return
        }
        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Logger.d(TAG, "Camera onOpened")
                    if (isReleasing || !isHostResumed || generation != lifecycleGeneration) {
                        camera.close()
                        return
                    }
                    cameraDevice = camera
                    createPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Logger.w(TAG, "Camera onDisconnected")
                    camera.close()
                    if (generation == lifecycleGeneration) {
                        cameraDevice = null
                    }
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (isReleasing || generation != lifecycleGeneration) {
                        camera.close()
                        return
                    }
                    Logger.e(TAG, "Camera open error: $error")
                    camera.close()
                    cameraDevice = null
                    notifyRecordingFailed(buildUnsupportedSettingMessage("camera open error code=$error"), generation)
                }
            }, backgroundHandler)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to open camera: ${e.message}")
            notifyRecordingFailed(buildUnsupportedSettingMessage(e.message), generation)
        }
    }

    private fun createPreviewSession() {
        val cameraDevice = cameraDevice ?: return
        val previewView = previewView ?: return
        val surfaceTexture = previewView.surfaceTexture ?: return
        val generation = lifecycleGeneration
        val targetPreviewSize = previewSize ?: supportedProfilesByResolution[Resolution.HD_720P]
            ?.firstOrNull()
            ?.size
            ?: Size(1280, 720)
        previewSize = targetPreviewSize
        Logger.d(
            TAG,
            "createPreviewSession streamSize=${targetPreviewSize.width}x${targetPreviewSize.height} sensorOrientation=$sensorOrientation"
        )
        previewSurface?.release()
        surfaceTexture.setDefaultBufferSize(targetPreviewSize.width, targetPreviewSize.height)
        previewSurface = Surface(surfaceTexture)
        val previewSurface = previewSurface ?: return
        applyPreviewFrameRateHint(previewSurface, currentProfile)
        applyPreviewTransform()
        try {
            captureSession?.close()
            cameraDevice.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        Logger.d(TAG, "Preview session onConfigured")
                        if (isReleasing || !isHostResumed || generation != lifecycleGeneration) {
                            session.close()
                            return
                        }
                        captureSession = session
                        updateRepeatingRequest()
                        pendingOnReady?.invoke()
                        pendingOnReady = null
                        Logger.d(TAG, "Camera preview initialized")
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        if (isReleasing || generation != lifecycleGeneration) {
                            session.close()
                            return
                        }
                        Logger.e(TAG, "Preview session configuration failed")
                        handlePreviewSessionFailure(buildUnsupportedSettingMessage("preview session configuration failed"), generation)
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to create preview session: ${e.message}")
            handlePreviewSessionFailure(buildUnsupportedSettingMessage(e.message), generation)
        }
    }

    /**
     * 开始录像
     *
     * @param resolution 分辨率
     * @param fps 帧率
     */
    fun startRecording(
        resolution: Resolution,
        fps: Fps
    ): Boolean {
        if (_isRecording.value || isStartingRecording) {
            Logger.w(TAG, "Already recording")
            return false
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Logger.e(TAG, "Missing camera permission")
            return false
        }

        try {
            val profile = supportedProfilesByResolution[resolution]
                ?.firstOrNull { profile -> profile.fps == fps.value }
            if (profile == null) {
                notifyRecordingFailed(
                    buildUnsupportedSettingMessage("resolution=${resolution.name}, fps=${fps.value}"),
                    lifecycleGeneration
                )
                return false
            }
            currentProfile = profile
            isRecordingPreviewEnabled = true
            unstableRecordingPreviewWindows = 0
            isStartingRecording = true
            startSequence += 1
            val startToken = startSequence
            Logger.d(TAG, "startRecording token=$startToken resolution=${resolution.name} fps=${fps.value}")

            val attempt = prepareMediaRecorder(profile, startToken)
            activeAttempt = attempt
            previewSize = profile.size
            createPreviewSessionFor(profile.size) {
                createHighSpeedSession(profile, startToken, attempt)
            }
            return true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to start recording: ${e.message}")
            isStartingRecording = false
            currentProfile = null
            isRecordingPreviewEnabled = true
            unstableRecordingPreviewWindows = 0
            releaseAttempt(activeAttempt, deleteOutput = true)
            activeAttempt = null
            notifyRecordingFailed(buildUnsupportedSettingMessage(e.message), lifecycleGeneration)
            return false
        }
    }

    private fun createPreviewSessionFor(size: Size, onConfigured: (() -> Unit)? = null) {
        previewSize = size
        Logger.d(TAG, "createPreviewSessionFor size=${size.width}x${size.height}")
        pendingOnReady = onConfigured ?: pendingOnReady
        createPreviewSession()
    }

    private fun prepareMediaRecorder(profile: HighSpeedProfile, token: Long): RecordingAttempt {
        val outputUri = createOutputUri() ?: throw IllegalStateException("Failed to create output uri")
        val outputPfd = context.contentResolver.openFileDescriptor(outputUri, "rw")
            ?: throw IllegalStateException("Failed to open output file")

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(calculateBitRate(profile))
            setVideoSize(profile.size.width, profile.size.height)
            setVideoFrameRate(OUTPUT_FRAME_RATE)
            setCaptureRate(profile.fps.toDouble())
            setOrientationHint(calculateVideoOrientation())
            setOutputFile(outputPfd.fileDescriptor)
            prepare()
        }
        return RecordingAttempt(
            token = token,
            recorder = recorder,
            outputPfd = outputPfd,
            outputUri = outputUri,
            profile = profile
        )
    }

    private fun createHighSpeedSession(profile: HighSpeedProfile, startToken: Long, attempt: RecordingAttempt) {
        val generation = lifecycleGeneration
        Logger.d(TAG, "createHighSpeedSession token=$startToken fps=${profile.fps} size=${profile.size.width}x${profile.size.height}")
        if (isReleasing || !isHostResumed || generation != lifecycleGeneration || startToken != startSequence || !isStartingRecording) {
            releaseAttempt(attempt, deleteOutput = true)
            if (activeAttempt?.token == attempt.token) {
                activeAttempt = null
            }
            return
        }
        val cameraDevice = cameraDevice ?: throw IllegalStateException("Camera not ready")
        val previewSurface = previewSurface ?: throw IllegalStateException("Preview surface unavailable")
        val recorderSurface = attempt.recorder.surface ?: throw IllegalStateException("Recorder surface unavailable")

        captureSession?.close()
        cameraDevice.createConstrainedHighSpeedCaptureSession(
            listOf(previewSurface, recorderSurface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        Logger.d(TAG, "High speed session onConfigured token=$startToken")
                        if (isReleasing || !isHostResumed || generation != lifecycleGeneration || startToken != startSequence) {
                            session.close()
                            releaseAttempt(attempt, deleteOutput = true)
                            isStartingRecording = false
                            return
                        }
                        captureSession = session
                        val includePreviewTarget = shouldIncludePreviewTargetInRecording(profile)
                        val requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                            if (includePreviewTarget) {
                                addTarget(previewSurface)
                            }
                            addTarget(recorderSurface)
                            applyHighSpeedVideoTuning(this, profile)
                            set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, profile.fpsRange)
                            applyZoom(this)
                        }
                        val highSpeedSession = session as CameraConstrainedHighSpeedCaptureSession
                        val burst = highSpeedSession.createHighSpeedRequestList(requestBuilder.build())
                        highSpeedSession.setRepeatingBurst(burst, recordingCaptureCallback, backgroundHandler)
                        attempt.recorder.start()
                        isStartingRecording = false
                        _isRecording.value = true
                        startRecordingStats(profile, attempt)
                        notifyRecordingStarted(attempt.token)
                        Logger.d(
                            TAG,
                            "Slow motion recording started at ${profile.fps}fps includePreviewTarget=$includePreviewTarget burstSize=${burst.size}"
                        )
                    } catch (e: Exception) {
                        Logger.e(TAG, "Failed to start high speed recording: ${e.message}")
                        isStartingRecording = false
                        isRecordingPreviewEnabled = true
                        unstableRecordingPreviewWindows = 0
                        stopRecordingStats()
                        session.close()
                        releaseAttempt(attempt, deleteOutput = true)
                        if (activeAttempt?.token == attempt.token) {
                            activeAttempt = null
                        }
                        currentProfile = null
                        notifyRecordingFailed(buildUnsupportedSettingMessage(e.message), generation)
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    if (isReleasing || !isHostResumed || generation != lifecycleGeneration || startToken != startSequence) {
                        session.close()
                        return
                    }
                    Logger.e(TAG, "High speed session configuration failed")
                    isStartingRecording = false
                    isRecordingPreviewEnabled = true
                    unstableRecordingPreviewWindows = 0
                    session.close()
                    releaseAttempt(attempt, deleteOutput = true)
                    if (activeAttempt?.token == attempt.token) {
                        activeAttempt = null
                    }
                    currentProfile = null
                    notifyRecordingFailed(buildUnsupportedSettingMessage("high speed session configuration failed"), generation)
                }
            },
            backgroundHandler
        )
    }

    private fun calculateBitRate(profile: HighSpeedProfile): Int {
        val bitRate = when {
            profile.fps >= 480 -> 24_000_000
            profile.size.width >= 1920 && profile.fps >= 240 -> 72_000_000
            profile.size.width >= 1920 -> 48_000_000
            profile.fps >= 240 -> 36_000_000
            else -> 20_000_000
        }
        Logger.d(
            TAG,
            "calculateBitRate size=${profile.size.width}x${profile.size.height} fps=${profile.fps} bitRate=$bitRate fpsRange=${profile.fpsRange.lower}-${profile.fpsRange.upper}"
        )
        return bitRate
    }

    private fun calculateVideoOrientation(): Int {
        return when (sensorOrientation) {
            270 -> 270
            else -> 90
        }
    }

    private fun applyPreviewTransform() {
        val previewView = previewView ?: return
        val previewSize = previewSize ?: return
        val viewWidth = previewView.width
        val viewHeight = previewView.height
        if (viewWidth == 0 || viewHeight == 0) {
            previewView.post { applyPreviewTransform() }
            return
        }

        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val rotationDegrees = calculatePreviewRotationDegrees()
        val displayRotation = previewView.display?.rotation ?: Surface.ROTATION_0
        val isBufferRotated = shouldSwapPreviewBuffer()
        val bufferWidth = if (isBufferRotated) previewSize.height.toFloat() else previewSize.width.toFloat()
        val bufferHeight = if (isBufferRotated) previewSize.width.toFloat() else previewSize.height.toFloat()
        Logger.d(
            TAG,
            "applyPreviewTransform view=${viewWidth}x$viewHeight stream=${previewSize.width}x${previewSize.height} rotatedBuffer=${bufferWidth.toInt()}x${bufferHeight.toInt()} displayRotation=$displayRotation rotationDegrees=$rotationDegrees sensorOrientation=$sensorOrientation swapped=${isBufferRotated}"
        )
        val bufferRect = RectF(0f, 0f, bufferWidth, bufferHeight)
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)

        val scale = maxOf(
            viewWidth.toFloat() / bufferWidth,
            viewHeight.toFloat() / bufferHeight
        )
        matrix.postScale(scale, scale, centerX, centerY)
        matrix.postRotate(rotationDegrees.toFloat(), centerX, centerY)
        previewView.setTransform(matrix)
    }

    private fun calculatePreviewRotationDegrees(): Int {
        val normalizedSensorOrientation = ((sensorOrientation % 360) + 360) % 360
        val result = ((normalizedSensorOrientation - 90) + 360) % 360
        Logger.d(
            TAG,
            "calculatePreviewRotationDegrees sensorOrientation=$sensorOrientation normalizedSensorOrientation=$normalizedSensorOrientation result=$result portraitLocked=true"
        )
        return result
    }

    private fun shouldSwapPreviewBuffer(): Boolean {
        val normalized = ((sensorOrientation % 360) + 360) % 360
        return normalized == 90 || normalized == 270
    }

    private fun createOutputUri(): android.net.Uri? {
        val name = "SlowMo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, MOVIE_DIRECTORY)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        return context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    /**
     * 停止录像
     */
    fun stopRecording() {
        val callbackGeneration = lifecycleGeneration
        Logger.d(TAG, "stopRecording recording=${_isRecording.value} starting=$isStartingRecording resumed=$isHostResumed")
        startSequence += 1
        if (isStartingRecording && !_isRecording.value) {
            isStartingRecording = false
            stopRecordingStats()
            pendingOnReady = null
            releaseAttempt(activeAttempt, deleteOutput = true)
            activeAttempt = null
            currentProfile = null
            isRecordingPreviewEnabled = true
            unstableRecordingPreviewWindows = 0
            if (!isReleasing && isHostResumed) {
                previewSize = supportedProfilesByResolution[Resolution.HD_720P]
                    ?.firstOrNull()
                    ?.size
                    ?: previewSize
                createPreviewSession()
            } else {
                closeCameraResources(stopThread = !isHostResumed)
            }
            return
        }
        if (!_isRecording.value) {
            return
        }
        val attempt = activeAttempt ?: return
        stopRecordingStats()
        try {
            captureSession?.stopRepeating()
            captureSession?.abortCaptures()
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to stop repeating request: ${e.message}")
        }

        try {
            attempt.recorder.stop()
            finalizeOutput(attempt.outputUri)
            val uri = attempt.outputUri.toString()
            Logger.d(TAG, "Recording saved: $uri")
            notifyRecordingSaved(uri, callbackGeneration)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to stop recording: ${e.message}")
            releaseAttempt(attempt, deleteOutput = true)
            activeAttempt = null
            _isRecording.value = false
            currentProfile = null
            notifyRecordingFailed(buildRecordingFailureReason(e.message), callbackGeneration)
            return
        }

        releaseAttempt(attempt, deleteOutput = false)
        activeAttempt = null
        _isRecording.value = false
        currentProfile = null
        isRecordingPreviewEnabled = true
        unstableRecordingPreviewWindows = 0
        if (!isReleasing && isHostResumed) {
            previewSize = supportedProfilesByResolution[Resolution.HD_720P]
                ?.firstOrNull()
                ?.size
                ?: previewSize
            createPreviewSession()
        } else {
            closeCameraResources(stopThread = !isHostResumed)
        }
    }

    /**
     * 设置缩放比例
     */
    fun setZoom(ratio: Float) {
        if (_isRecording.value && currentProfile?.fps ?: 0 >= 480) {
            Logger.d(TAG, "Ignore zoom update while recording 480fps")
            return
        }
        val maxZoom = _maxZoomRatio.value

        val clampedRatio = ratio.coerceIn(1.0f, maxZoom)
        _zoomRatio.value = clampedRatio
        updateRepeatingRequest()
    }

    /**
     * 设置录像保存回调
     */
    fun setOnRecordingSavedListener(listener: (String) -> Unit) {
        onRecordingSaved = listener
    }

    fun setOnRecordingStartedListener(listener: () -> Unit) {
        onRecordingStarted = listener
    }

    /**
     * 设置录像失败回调
     */
    fun setOnRecordingFailedListener(listener: (String) -> Unit) {
        onRecordingFailed = listener
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            Logger.d(TAG, "release start")
            isReleasing = true
            lifecycleGeneration += 1
            isHostResumed = false
            onRecordingStarted = null
            onRecordingSaved = null
            onRecordingFailed = null
            if (_isRecording.value) {
                stopRecording()
            } else if (isStartingRecording) {
                stopRecording()
            }
            pendingOnReady = null
            previewView?.surfaceTextureListener = null
            closeCameraResources(stopThread = true)
            releaseAttempt(activeAttempt, deleteOutput = false)
            activeAttempt = null
            Logger.d(TAG, "Camera resources released")
        } catch (e: Exception) {
            Logger.e(TAG, "Error releasing camera resources: ${e.message}")
        }
    }

    private fun updateRepeatingRequest() {
        val cameraDevice = cameraDevice ?: return
        val captureSession = captureSession ?: return
        val previewSurface = previewSurface ?: return
        val attempt = activeAttempt
        try {
            if (_isRecording.value) {
                val recorderSurface = attempt?.recorder?.surface ?: return
                val profile = currentProfile ?: return
                val includePreviewTarget = shouldIncludePreviewTargetInRecording(profile)
                val requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                    if (includePreviewTarget) {
                        addTarget(previewSurface)
                    }
                    addTarget(recorderSurface)
                    applyHighSpeedVideoTuning(this, profile)
                    set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, profile.fpsRange)
                    applyZoom(this)
                }
                val highSpeedSession = captureSession as? CameraConstrainedHighSpeedCaptureSession ?: return
                val burst = highSpeedSession.createHighSpeedRequestList(requestBuilder.build())
                highSpeedSession.setRepeatingBurst(
                    burst,
                    recordingCaptureCallback,
                    backgroundHandler
                )
                Logger.d(
                    TAG,
                    "updateRepeatingRequest recording fps=${profile.fps} includePreviewTarget=$includePreviewTarget burstSize=${burst.size}"
                )
            } else {
                val requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(previewSurface)
                    applyZoom(this)
                }
                captureSession.setRepeatingRequest(requestBuilder.build(), null, backgroundHandler)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to update repeating request: ${e.message}")
        }
    }

    private fun applyZoom(requestBuilder: CaptureRequest.Builder) {
        val activeArray = activeArrayRect ?: return
        val zoomRatio = _zoomRatio.value
        if (zoomRatio <= 1.0f) {
            requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, activeArray)
            return
        }
        val cropWidth = (activeArray.width() / zoomRatio).toInt()
        val cropHeight = (activeArray.height() / zoomRatio).toInt()
        val left = (activeArray.width() - cropWidth) / 2
        val top = (activeArray.height() - cropHeight) / 2
        requestBuilder.set(
            CaptureRequest.SCALER_CROP_REGION,
            Rect(
                activeArray.left + left,
                activeArray.top + top,
                activeArray.right - left,
                activeArray.bottom - top
            )
        )
    }

    private fun finalizeOutput(savedUri: android.net.Uri?) {
        if (savedUri == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(savedUri, values, null, null)
        }
    }

    private fun releaseAttempt(attempt: RecordingAttempt?, deleteOutput: Boolean) {
        if (attempt == null) {
            return
        }
        try {
            attempt.recorder.reset()
        } catch (_: Exception) {
        }
        try {
            attempt.recorder.release()
        } catch (_: Exception) {
        }
        try {
            attempt.outputPfd.close()
        } catch (_: Exception) {
        }
        if (deleteOutput) {
            runCatching {
                context.contentResolver.delete(attempt.outputUri, null, null)
            }
        }
    }

    private fun notifyRecordingFailed(message: String, generation: Long) {
        if (isReleasing) {
            return
        }
        Logger.e(TAG, "notifyRecordingFailed message=$message resumed=$isHostResumed")
        isStartingRecording = false
        _isRecording.value = false
        currentProfile = null
        isRecordingPreviewEnabled = true
        stopRecordingStats()
        mainExecutor.execute {
            if (!shouldDispatchUiCallback(generation)) {
                Logger.d(TAG, "Skip recording failed callback generation=$generation resumed=$isHostResumed")
                return@execute
            }
            onRecordingFailed?.invoke(message)
        }
        if (isHostResumed) {
            previewSize = supportedProfilesByResolution[Resolution.HD_720P]
                ?.firstOrNull()
                ?.size
                ?: previewSize
            createPreviewSession()
        } else {
            closeCameraResources(stopThread = true)
        }
    }

    private fun buildUnsupportedSettingMessage(reason: String?): String {
        val baseMessage = context.getString(R.string.camera_unsupported_setting)
        val normalizedReason = reason?.trim().orEmpty()
        return if (normalizedReason.isEmpty() || normalizedReason == baseMessage) {
            baseMessage
        } else {
            "$baseMessage: $normalizedReason"
        }
    }

    private fun buildRecordingFailureReason(reason: String?): String {
        val normalizedReason = reason?.trim().orEmpty()
        return if (normalizedReason.isEmpty()) {
            context.getString(R.string.camera_recording_failed)
        } else {
            normalizedReason
        }
    }

    private fun notifyRecordingStarted(token: Long) {
        if (isReleasing) {
            return
        }
        val generation = lifecycleGeneration
        mainExecutor.execute {
            if (!shouldDispatchUiCallback(generation) || activeAttempt?.token != token || !_isRecording.value) {
                Logger.d(
                    TAG,
                    "Skip recording started callback token=$token generation=$generation resumed=$isHostResumed activeToken=${activeAttempt?.token} recording=${_isRecording.value}"
                )
                return@execute
            }
            onRecordingStarted?.invoke()
        }
    }

    private fun notifyRecordingSaved(uri: String, generation: Long) {
        if (isReleasing) {
            return
        }
        mainExecutor.execute {
            if (!shouldDispatchUiCallback(generation)) {
                Logger.d(TAG, "Skip recording saved callback generation=$generation resumed=$isHostResumed")
                return@execute
            }
            onRecordingSaved?.invoke(uri)
        }
    }

    private fun startBackgroundThread() {
        if (backgroundThread != null) {
            return
        }
        Logger.d(TAG, "startBackgroundThread")
        backgroundThread = HandlerThread("SlowMotionCameraThread").apply { start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        Logger.d(TAG, "stopBackgroundThread")
        backgroundThread?.quitSafely()
        backgroundThread = null
        backgroundHandler = null
    }

    private fun attachSurfaceTextureListener() {
        previewView?.surfaceTextureListener = surfaceTextureListener
    }

    private fun openCameraIfReady() {
        val previewView = previewView ?: return
        if (!isHostResumed || isReleasing) {
            Logger.d(TAG, "openCameraIfReady skipped resumed=$isHostResumed isReleasing=$isReleasing")
            return
        }
        if (!previewView.isAvailable) {
            Logger.d(TAG, "openCameraIfReady waiting for surface")
            return
        }
        if (cameraDevice != null) {
            Logger.d(TAG, "openCameraIfReady reuse opened camera")
            if (captureSession == null) {
                createPreviewSession()
            }
            return
        }
        openCamera()
    }

    private fun handlePreviewSessionFailure(message: String, generation: Long) {
        pendingOnReady = null
        if (!isStartingRecording) {
            closeCameraResources(stopThread = !isHostResumed)
            return
        }
        isStartingRecording = false
        currentProfile = null
        releaseAttempt(activeAttempt, deleteOutput = true)
        activeAttempt = null
        notifyRecordingFailed(message, generation)
    }

    private fun applyHighSpeedVideoTuning(
        requestBuilder: CaptureRequest.Builder,
        profile: HighSpeedProfile
    ) {
        val characteristics = cameraCharacteristics ?: return
        val isUltraHighSpeed = profile.fps >= 480
        requestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)

        chooseSupportedMode(
            characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
            if (isUltraHighSpeed) {
                intArrayOf(CaptureRequest.CONTROL_AF_MODE_OFF, CaptureRequest.CONTROL_AF_MODE_AUTO)
            } else {
                intArrayOf(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO, CaptureRequest.CONTROL_AF_MODE_AUTO)
            }
        )?.let { mode ->
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, mode)
        }

        chooseSupportedMode(
            characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
            intArrayOf(CaptureRequest.CONTROL_AWB_MODE_AUTO)
        )?.let { mode ->
            requestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, mode)
        }

        if (isUltraHighSpeed) {
            if (characteristics.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE) == true) {
                requestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true)
            }
            if (characteristics.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE) == true) {
                requestBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true)
            }
        }

        chooseSupportedMode(
            characteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES),
            if (isUltraHighSpeed) {
                intArrayOf(
                    CaptureRequest.NOISE_REDUCTION_MODE_OFF,
                    CaptureRequest.NOISE_REDUCTION_MODE_FAST,
                    CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY
                )
            } else {
                intArrayOf(CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY, CaptureRequest.NOISE_REDUCTION_MODE_FAST)
            }
        )?.let { mode ->
            requestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, mode)
        }

        chooseSupportedMode(
            characteristics.get(CameraCharacteristics.HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES),
            if (isUltraHighSpeed) {
                intArrayOf(
                    CaptureRequest.HOT_PIXEL_MODE_OFF,
                    CaptureRequest.HOT_PIXEL_MODE_FAST,
                    CaptureRequest.HOT_PIXEL_MODE_HIGH_QUALITY
                )
            } else {
                intArrayOf(CaptureRequest.HOT_PIXEL_MODE_HIGH_QUALITY, CaptureRequest.HOT_PIXEL_MODE_FAST)
            }
        )?.let { mode ->
            requestBuilder.set(CaptureRequest.HOT_PIXEL_MODE, mode)
        }

        chooseSupportedMode(
            characteristics.get(CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES),
            intArrayOf(CaptureRequest.EDGE_MODE_OFF, CaptureRequest.EDGE_MODE_FAST)
        )?.let { mode ->
            requestBuilder.set(CaptureRequest.EDGE_MODE, mode)
        }

        if (isUltraHighSpeed) {
            chooseSupportedMode(
                characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES),
                intArrayOf(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF)
            )?.let { mode ->
                requestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, mode)
            }

            chooseSupportedMode(
                characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION),
                intArrayOf(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF)
            )?.let { mode ->
                requestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, mode)
            }
        }

        Logger.d(
            TAG,
            "applyHighSpeedVideoTuning fps=${profile.fps} fpsRange=${profile.fpsRange.lower}-${profile.fpsRange.upper} exact=${profile.fpsRange.lower == profile.fpsRange.upper} ultra=$isUltraHighSpeed"
        )
    }

    private fun shouldIncludePreviewTargetInRecording(profile: HighSpeedProfile): Boolean {
        return profile.fps < 480 || isRecordingPreviewEnabled
    }

    private fun chooseSupportedMode(availableModes: IntArray?, preferredModes: IntArray): Int? {
        val supportedModes = availableModes ?: return null
        return preferredModes.firstOrNull { candidate -> supportedModes.contains(candidate) }
    }

    private fun closeCameraResources(stopThread: Boolean) {
        try {
            captureSession?.close()
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to close capture session: ${e.message}")
        }
        captureSession = null
        try {
            cameraDevice?.close()
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to close camera device: ${e.message}")
        }
        cameraDevice = null
        try {
            previewSurface?.release()
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to release preview surface: ${e.message}")
        }
        previewSurface = null
        if (stopThread) {
            stopBackgroundThread()
        }
    }

    private fun applyPreviewFrameRateHint(surface: Surface, profile: HighSpeedProfile?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return
        }
        val previewView = previewView ?: return
        val supportedModes = previewView.display?.supportedModes ?: return
        val maxSupportedRefreshRate = supportedModes.maxOfOrNull { mode -> mode.refreshRate } ?: return
        val requestedRate = when {
            profile?.fps ?: 0 >= 480 -> minOf(60f, maxSupportedRefreshRate)
            profile?.fps ?: 0 >= 240 -> minOf(90f, maxSupportedRefreshRate)
            else -> minOf(60f, maxSupportedRefreshRate)
        }
        Logger.d(
            TAG,
            "applyPreviewFrameRateHint requestedRate=$requestedRate maxSupportedRefreshRate=$maxSupportedRefreshRate profileFps=${profile?.fps}"
        )
        surface.setFrameRate(requestedRate, Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE)
    }

    private fun shouldDispatchUiCallback(generation: Long): Boolean {
        return !isReleasing && isHostResumed && generation == lifecycleGeneration
    }

    private fun startRecordingStats(profile: HighSpeedProfile, attempt: RecordingAttempt) {
        stopRecordingStats()
        recordingStartedAtMs = SystemClock.elapsedRealtime()
        resetCaptureStats()
        val handler = backgroundHandler ?: return
        val runnable = object : Runnable {
            override fun run() {
                if (!_isRecording.value || activeAttempt?.token != attempt.token) {
                    return
                }
                val elapsedMs = SystemClock.elapsedRealtime() - recordingStartedAtMs
                val outputBytes = attempt.outputPfd.statSize
                val sessionName = captureSession?.javaClass?.simpleName ?: "null"
                val previewDimensions = previewSize?.let { size -> "${size.width}x${size.height}" } ?: "unknown"
                val avgCaptureIntervalMs = if (captureDeltaCount > 0) {
                    captureDeltaSumNs.toDouble() / captureDeltaCount / 1_000_000.0
                } else {
                    0.0
                }
                val maxCaptureIntervalMs = captureDeltaMaxNs / 1_000_000.0
                val exposureMs = lastExposureTimeNs / 1_000_000.0
                val frameDurationMs = lastFrameDurationNs / 1_000_000.0
                Logger.d(
                    TAG,
                    "recordingStats elapsedMs=$elapsedMs outputBytes=$outputBytes fps=${profile.fps} fpsRange=${profile.fpsRange.lower}-${profile.fpsRange.upper} zoom=${_zoomRatio.value} session=$sessionName previewSize=$previewDimensions previewAvailable=${previewView?.isAvailable == true} captureCount=$captureDeltaCount avgCaptureIntervalMs=${"%.3f".format(Locale.US, avgCaptureIntervalMs)} maxCaptureIntervalMs=${"%.3f".format(Locale.US, maxCaptureIntervalMs)} exposureMs=${"%.3f".format(Locale.US, exposureMs)} frameDurationMs=${"%.3f".format(Locale.US, frameDurationMs)}"
                )
                maybeFallbackRecordingPreview(profile, attempt, avgCaptureIntervalMs)
                resetCaptureStats(keepLastTimestamp = true)
                handler.postDelayed(this, 1000)
            }
        }
        recordingStatsRunnable = runnable
        handler.postDelayed(runnable, 1000)
    }

    private fun stopRecordingStats() {
        val handler = backgroundHandler
        val runnable = recordingStatsRunnable
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable)
        }
        recordingStatsRunnable = null
        recordingStartedAtMs = 0L
        resetCaptureStats()
        unstableRecordingPreviewWindows = 0
    }

    private fun resetCaptureStats(keepLastTimestamp: Boolean = false) {
        if (!keepLastTimestamp) {
            lastCaptureTimestampNs = 0L
        }
        captureDeltaCount = 0
        captureDeltaSumNs = 0L
        captureDeltaMaxNs = 0L
        lastExposureTimeNs = 0L
        lastFrameDurationNs = 0L
    }

    private fun maybeFallbackRecordingPreview(
        profile: HighSpeedProfile,
        attempt: RecordingAttempt,
        avgCaptureIntervalMs: Double
    ) {
        if (profile.fps < 480 || !isRecordingPreviewEnabled || activeAttempt?.token != attempt.token) {
            return
        }
        if (avgCaptureIntervalMs <= 0.0) {
            unstableRecordingPreviewWindows = 0
            return
        }
        if (avgCaptureIntervalMs < ULTRA_HIGH_SPEED_PREVIEW_FALLBACK_INTERVAL_MS) {
            unstableRecordingPreviewWindows = 0
            return
        }
        unstableRecordingPreviewWindows += 1
        Logger.w(
            TAG,
            "recording preview unstable fps=${profile.fps} avgCaptureIntervalMs=${"%.3f".format(Locale.US, avgCaptureIntervalMs)} unstableWindows=$unstableRecordingPreviewWindows"
        )
        if (unstableRecordingPreviewWindows < ULTRA_HIGH_SPEED_PREVIEW_FALLBACK_CONSECUTIVE_WINDOWS) {
            return
        }
        unstableRecordingPreviewWindows = 0
        isRecordingPreviewEnabled = false
        Logger.w(TAG, "Fallback to recorder-only high speed request for ${profile.fps}fps to protect recording cadence")
        if (_isRecording.value && activeAttempt?.token == attempt.token && currentProfile?.fps == profile.fps) {
            updateRepeatingRequest()
        }
    }
}
