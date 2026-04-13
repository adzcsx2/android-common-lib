package com.hoyn.common.utils;

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 屏幕状态枚举
 * update at 2026.3.25
 * 表示设备可能处于的不同屏幕状态
 */
enum class ScreenState {
    /** 普通直屏设备 */
    NORMAL_SCREEN,

    /** 折叠屏完全展开状态 */
    FOLDING_FLAT,

    /** 折叠屏半折叠状态（桌面模式/悬停模式） */
    FOLDING_HALF_OPEN,

    /** 折叠屏完全折叠状态 */
    FOLDING_FOLD,

    /** 未知状态 */
    UNKNOWN
}

/**
 * 折叠方向枚举
 *
 * 表示折叠屏设备的铰链方向
 */
enum class FoldOrientation {
    /** 水平折叠（横向铰链） */
    HORIZONTAL,

    /** 垂直折叠（纵向铰链） */
    VERTICAL,

    /** 无折叠 */
    NONE
}

/**
 * 折叠屏设备类型枚举
 *
 * 表示不同类型的折叠屏设备
 */
enum class FoldingDeviceType {
    /** 书本式大折叠屏（纵向铰链，如 Galaxy Fold/Z Fold 系列）- 展开后类似平板 */
    BOOK_FOLD,

    /** 翻盖式小折叠屏（横向铰链，如 Galaxy Z Flip 系列）- 展开后类似普通手机 */
    FLIP_FOLD,

    /** 非折叠屏设备或无法确定类型 */
    UNKNOWN
}

/**
 * 屏幕信息数据类
 *
 * 封装屏幕相关的所有信息，包括状态、方向、设备类型等
 *
 * @property state 当前屏幕状态
 * @property orientation 折叠方向
 * @property isFoldingDevice 是否为折叠屏设备
 * @property deviceType 折叠屏设备类型
 */
data class ScreenInfo(
    val state: ScreenState,
    val orientation: FoldOrientation,
    val isFoldingDevice: Boolean,
    val deviceType: FoldingDeviceType
)

data class FloatingOverlayMetrics(
    val widthPx: Int,
    val heightPx: Int,
    val source: String
)

/**
 * 屏幕状态变化监听器接口
 *
 * 用于在 Activity 不重建时接收屏幕状态变化通知
 */
interface ScreenStateListener {
    /**
     * 屏幕状态变化时回调
     *
     * @param screenInfo 最新的屏幕信息
     */
    fun onScreenStateChanged(screenInfo: ScreenInfo)
}

/**
 * 引入Window,但是无法获取到VTL-202502的悬停状态,暂时取消
 * 如需使用,需取消build.gradle注释
 * //    implementation "androidx.window:window:1.3.0"
 */
object ScreenUtils {

    const val TAG = "ScreenUtils"

    // 通用屏幕使用状态常量：避免依赖业务模块中的 FoldStateManager。
    const val DISPLAY_IN_USE_STATE_PRIMARY = 0
    const val DISPLAY_IN_USE_STATE_SECONDARY = 1
    const val DISPLAY_IN_USE_STATE_BOTH = 2

    // 缓存最后检测的屏幕状态
    @Volatile
    private var lastScreenInfo: ScreenInfo = ScreenInfo(
        state = ScreenState.UNKNOWN,
        orientation = FoldOrientation.NONE,
        isFoldingDevice = false,
        deviceType = FoldingDeviceType.UNKNOWN
    )

    // 缓存设备类型（一旦检测到是折叠屏，永久记住）
    @Volatile
    private var deviceTypeConfirmed: Boolean = false

    @Volatile
    private var confirmedIsFoldingDevice: Boolean = false

    // 缓存具体的折叠屏设备类型（BOOK_FOLD 或 FLIP_FOLD）
    @Volatile
    private var confirmedDeviceType: FoldingDeviceType = FoldingDeviceType.UNKNOWN

    // 标记是否已初始化
    @Volatile
    private var isInitialized: Boolean = false

    // 异步更新 Job，用于节流控制
    @Volatile
    private var updateJob: Job? = null

    // 监听器 Job 映射表（Activity -> Job）
    private val listenerJobs = mutableMapOf<Activity, Job>()

    // 生命周期观察者映射表（用于自动注销）
    private val lifecycleObservers = mutableMapOf<Activity, DefaultLifecycleObserver>()

    /**
     * 获取当前屏幕状态
     *
     * 同步返回基于屏幕尺寸的状态，同时异步获取更精确的折叠状态
     * 首次调用时会检测设备硬件能力（如铰链角度传感器）
     *
     * @param activity Activity上下文
     * @return 屏幕信息（包含状态、方向等）
     */
    @JvmStatic
    fun getScreenInfo(activity: Activity): ScreenInfo {
        // 如果是首次调用，先检测设备硬件能力
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    // 先通过硬件特性检测设备类型
                    val hasHingeAngleSensor = checkHingeAngleSensor(activity)
                    if (hasHingeAngleSensor) {
                        Log.d(TAG, "检测到铰链角度传感器，确认为折叠屏设备")
                        deviceTypeConfirmed = true
                        confirmedIsFoldingDevice = true
                    }
                    isInitialized = true
                }
            }
        }

        // 每次调用都同步更新基于屏幕尺寸的状态（确保展开/折叠状态准确）
        lastScreenInfo = getScreenInfoFromDisplayMetrics(activity)

        // 异步更新更精确的折叠特性信息（带节流）
        asyncUpdateScreenInfo(activity)

        // 立即返回同步更新的值
        return lastScreenInfo
    }

    /**
     * 获取当前屏幕状态枚举
     *
     * @param activity Activity上下文
     * @return 屏幕状态枚举
     */
    @JvmStatic
    fun getScreenState(activity: Activity): ScreenState {
        return getScreenInfo(activity).state
    }

    /**
     * 判断是否为折叠屏设备
     *
     * @param activity Activity上下文
     * @return true表示是折叠屏设备，false表示不是
     */
    @JvmStatic
    fun isFoldingDevice(activity: Activity): Boolean {
        return getScreenInfo(activity).isFoldingDevice
    }

    /**
     * 判断是否为书本式大折叠屏（纵向铰链，如 Galaxy Fold/Z Fold 系列）
     *
     * 特点：展开后类似平板，适合左右分屏、阅读等场景
     *
     * @param activity Activity上下文
     * @return true表示是书本式大折叠屏设备，false表示不是
     */
    @JvmStatic
    fun isBookFoldDevice(activity: Activity): Boolean {
        return getScreenInfo(activity).deviceType == FoldingDeviceType.BOOK_FOLD
    }

    /**
     * 判断是否为翻盖式小折叠屏（横向铰链，如 Galaxy Z Flip 系列）
     *
     * 特点：展开后类似普通手机，适合自拍、悬停拍摄等场景
     *
     * @param activity Activity上下文
     * @return true表示是翻盖式小折叠屏设备，false表示不是
     */
    @JvmStatic
    fun isFlipFoldDevice(activity: Activity): Boolean {
        return getScreenInfo(activity).deviceType == FoldingDeviceType.FLIP_FOLD
    }

    /**
     * 判断设备是否处于悬浮暂停状态（折叠屏半折叠状态）
     *
     * 同步返回缓存值，同时异步更新最新状态
     *
     * @param activity Activity上下文
     * @return true表示处于悬浮暂停状态，false表示正常状态
     */
    @JvmStatic
    fun isInTableTopMode(activity: Activity): Boolean {
        // 复用 getScreenInfo 更新状态并返回判断结果
        return getScreenInfo(activity).state == ScreenState.FOLDING_HALF_OPEN
    }

    /**
     * 判断是否为书本式折叠屏的展开状态
     *
     * @param activity Activity上下文
     * @return true表示书本式折叠屏已展开，false表示不是或不是书本式折叠屏
     */
    fun isBookOpen(activity: Activity): Boolean {
        return isBookFoldDevice(activity) && getScreenInfo(activity).state == ScreenState.FOLDING_FLAT
    }

    /**
     * 解析当前可用的副屏 displayId。
     *
     * 解析顺序：
     * 1. 优先使用业务约定的 preferredDisplayId（且必须有效并点亮）
     * 2. 在常见候选 id（preferred/2/4）中探测可用项
     * 3. 遍历系统所有 display，选择点亮且非主屏的 display
     * 4. 兜底返回 0（主屏）
     *
     * @param context Context上下文
     * @param preferredDisplayId 业务约定的副屏 displayId
     * @return 当前可用的 displayId
     */
    @JvmStatic
    fun resolveActiveSecondaryDisplayId(context: Context, preferredDisplayId: Int): Int {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            ?: return preferredDisplayId
        // 先尝试业务约定的候选 id；要求“有效 + 点亮”同时成立，避免绑定到存在但未使用的 display。
        val preferredDisplay = displayManager.getDisplay(preferredDisplayId)
        if (preferredDisplay != null && preferredDisplay.isValid && preferredDisplay.state == Display.STATE_ON) {
            return preferredDisplayId
        }

        // 机型差异下 secondary displayId 可能在 2/4 间变化，这里做兼容探测。
        val candidateIds = linkedSetOf(preferredDisplayId, 2, 4).filter { it != 0 }
        val candidateDisplay = candidateIds.firstNotNullOfOrNull { candidateId ->
            displayManager.getDisplay(candidateId)
                ?.takeIf { it.isValid && it.state == Display.STATE_ON }
        }
        if (candidateDisplay != null) {
            return candidateDisplay.displayId
        }

        val activeSecondary = displayManager.displays.firstOrNull { display ->
            display.displayId != 0 && display.isValid && display.state == Display.STATE_ON
        }

        if (activeSecondary != null) {
            return activeSecondary.displayId
        }

        return 0
    }

    /**
     * 解析悬浮窗可用的屏幕宽高。
     *
     * 规则：
     * 1. 折叠态（secondary）优先使用 WindowMetrics（与实际可拖动窗口一致）
     * 2. 若可获取到 realMetrics，则对 WindowMetrics 做上限裁剪，避免短时宽度异常放大
     * 3. 其余场景优先使用 display.getRealMetrics()
     * 4. 最终兜底使用 WindowManager/defaultDisplay 或 density 估算
     *
     * @param context Context上下文
     * @param windowManager 当前窗口的 WindowManager
     * @param display 当前目标 Display，可为 null
     * @param displayInUseState 屏幕使用状态（使用 ScreenUtils.DISPLAY_IN_USE_STATE_* 常量）
     * @return 悬浮窗可用宽高与来源标记
     */
    @RequiresApi(Build.VERSION_CODES.R)
    @JvmStatic
    fun resolveFloatingOverlayMetrics(
        context: Context,
        windowManager: android.view.WindowManager,
        display: Display?,
        displayInUseState: Int
    ): FloatingOverlayMetrics {
        if (displayInUseState == DISPLAY_IN_USE_STATE_SECONDARY) {
            var realWidth = 0
            var realHeight = 0
            display?.let { safeDisplay ->
                runCatching {
                    val displayMetrics = DisplayMetrics()
                    safeDisplay.getRealMetrics(displayMetrics)
                    realWidth = displayMetrics.widthPixels
                    realHeight = displayMetrics.heightPixels
                }
            }

            runCatching {
                val bounds = windowManager.currentWindowMetrics.bounds
                if (bounds.width() > 0 && bounds.height() > 0) {
                    // 外屏上 WindowMetrics 可能短暂返回偏大的宽度（如 1320），
                    // 用 realMetrics 做上限，避免拖动右边界被放大导致可拖出屏幕。
                    val safeWidth =
                        if (realWidth > 0) minOf(bounds.width(), realWidth) else bounds.width()
                    val safeHeight =
                        if (realHeight > 0) minOf(bounds.height(), realHeight) else bounds.height()
                    return FloatingOverlayMetrics(
                        widthPx = safeWidth,
                        heightPx = safeHeight,
                        source = if (realWidth > 0 && realHeight > 0) {
                            "WindowMetricsCappedByReal"
                        } else {
                            "WindowMetrics"
                        }
                    )
                }
            }
        }

        display?.let { safeDisplay ->
            runCatching {
                val displayMetrics = DisplayMetrics()
                safeDisplay.getRealMetrics(displayMetrics)
                if (displayMetrics.widthPixels > 0 && displayMetrics.heightPixels > 0) {
                    return FloatingOverlayMetrics(
                        widthPx = displayMetrics.widthPixels,
                        heightPx = displayMetrics.heightPixels,
                        source = "DisplayRealMetrics"
                    )
                }
            }
        }

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                FloatingOverlayMetrics(
                    widthPx = bounds.width(),
                    heightPx = bounds.height(),
                    source = "WindowMetricsFallback"
                )
            } else {
                val displayMetrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                FloatingOverlayMetrics(
                    widthPx = displayMetrics.widthPixels,
                    heightPx = displayMetrics.heightPixels,
                    source = "DefaultDisplayRealMetrics"
                )
            }
        }.getOrElse {
            Log.e(TAG, "resolveFloatingOverlayMetrics failed: ${it.message}")
            val density = context.resources.displayMetrics.density
            FloatingOverlayMetrics(
                widthPx = (360 * density).toInt(),
                heightPx = (640 * density).toInt(),
                source = "DensityFallback"
            )
        }
    }

    /**
     * 设置屏幕状态监听器
     *
     * 监听器会在 Activity 生命周期结束时自动注销
     * Activity 必须实现 LifecycleOwner 接口
     *
     * @param activity Activity上下文（必须实现 LifecycleOwner）
     * @param listener 屏幕状态变化监听器
     */
    @OptIn(InternalCoroutinesApi::class)
    @JvmStatic
    fun setScreenStateListener(activity: Activity, listener: ScreenStateListener) {
        if (activity !is LifecycleOwner) {
            Log.w(TAG, "Activity 未实现 LifecycleOwner，无法设置监听器")
            return
        }

        // 如果该 Activity 已有监听器，先取消旧的
        listenerJobs[activity]?.cancel()

        // 创建生命周期观察者，用于自动注销
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                Log.d(TAG, "Activity onDestroy，自动注销监听器")
                removeScreenStateListener(activity)
            }
        }

        // 注册生命周期观察者
        activity.lifecycle.addObserver(lifecycleObserver)
        lifecycleObservers[activity] = lifecycleObserver

        // 启动 Flow 监听
        val job = activity.lifecycleScope.launch {
            try {
                val windowInfoTracker = WindowInfoTracker.getOrCreate(activity)
                windowInfoTracker.windowLayoutInfo(activity).collect { layoutInfo ->
                    val screenInfo = parseScreenInfo(activity, layoutInfo)
                    lastScreenInfo = screenInfo
                    Log.d(
                        TAG,
                        "监听到屏幕状态变化: state=${screenInfo.state}, deviceType=${screenInfo.deviceType}"
                    )
                    listener.onScreenStateChanged(screenInfo)
                }
            } catch (e: CancellationException) {
                // 协程取消是正常行为
            } catch (e: Exception) {
                Log.e(TAG, "监听屏幕状态失败: ${e.message}")
            }
        }

        listenerJobs[activity] = job
        Log.d(TAG, "已设置屏幕状态监听器")
    }

    /**
     * 移除屏幕状态监听器
     *
     * 取消监听 Job 并移除生命周期观察者
     *
     * @param activity Activity上下文
     */
    @JvmStatic
    fun removeScreenStateListener(activity: Activity) {
        // 取消监听 Job
        listenerJobs[activity]?.let {
            it.cancel()
            listenerJobs.remove(activity)
            Log.d(TAG, "已取消监听 Job")
        }

        // 移除生命周期观察者
        lifecycleObservers[activity]?.let {
            if (activity is LifecycleOwner) {
                activity.lifecycle.removeObserver(it)
            }
            lifecycleObservers.remove(activity)
            Log.d(TAG, "已移除生命周期观察者")
        }
    }


    /**
     * 检测设备是否有铰链角度传感器（折叠屏特有）
     *
     * 通过 PackageManager 检测设备的硬件特性
     *
     * @param context Context上下文
     * @return true表示有铰链传感器（是折叠屏设备），false表示没有
     */
    private fun checkHingeAngleSensor(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // 使用字符串字面量而非 PackageManager.FEATURE_SENSOR_HINGE_ANGLE 常量
                // 避免在 API < 30 设备上类加载时的 NoSuchFieldError
                val hasHingeSensor =
                    context.packageManager.hasSystemFeature("android.hardware.sensor.hinge_angle")
                Log.d(TAG, "铰链角度传感器检测: $hasHingeSensor")
                hasHingeSensor
            } else {
                Log.d(TAG, "API < 30，无法检测铰链传感器")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "检测铰链传感器失败: ${e.message}")
            false
        }
    }

    /**
     * 通过屏幕尺寸和密度判断设备状态
     *
     * 用于首次启动或无法获取折叠信息时的降级方案
     * 根据最小宽度（sw）推断设备类型和状态
     *
     * @param activity Activity上下文
     * @return 屏幕信息
     */
    private fun getScreenInfoFromDisplayMetrics(activity: Activity): ScreenInfo {
        return try {
            val smallestWidthDp = getSmallestWidthDp(activity)
            Log.d(TAG, "通过屏幕尺寸判断设备状态，最小宽度: ${smallestWidthDp}dp")

            // 优先使用已确认的设备类型
            val deviceType = when {
                confirmedDeviceType != FoldingDeviceType.UNKNOWN -> {
                    confirmedDeviceType
                }

                confirmedIsFoldingDevice && smallestWidthDp >= 600 -> {
                    // 展开状态下的大屏折叠设备，推断为 BOOK_FOLD 并缓存
                    Log.d(TAG, "根据大屏尺寸推测为书本式折叠屏并缓存")
                    confirmedDeviceType = FoldingDeviceType.BOOK_FOLD
                    FoldingDeviceType.BOOK_FOLD
                }

                confirmedIsFoldingDevice && smallestWidthDp < 600 -> {
                    // 折叠状态下，如果已确认是折叠设备但设备类型未知，尝试基于屏幕比例推断
                    val inferredType = inferDeviceTypeFromScreenSize(activity)
                    if (inferredType != FoldingDeviceType.UNKNOWN) {
                        Log.d(TAG, "折叠状态下根据屏幕特征推断设备类型: $inferredType")
                        confirmedDeviceType = inferredType
                        inferredType
                    } else {
                        FoldingDeviceType.UNKNOWN
                    }
                }

                else -> FoldingDeviceType.UNKNOWN
            }

            // 根据屏幕尺寸判断状态
            val (state, logMsg) = when {
                smallestWidthDp >= 600 -> {
                    val s =
                        if (confirmedIsFoldingDevice) ScreenState.FOLDING_FLAT else ScreenState.UNKNOWN
                    s to "大屏设备（sw >= 600dp），可能是书本式折叠屏展开状态"
                }

                else -> {
                    val s =
                        if (confirmedIsFoldingDevice) ScreenState.FOLDING_FOLD else ScreenState.UNKNOWN
                    s to "中小屏幕，可能是普通设备或折叠状态"
                }
            }
            Log.d(TAG, "检测到$logMsg")

            ScreenInfo(
                state = state,
                orientation = FoldOrientation.NONE,
                isFoldingDevice = confirmedIsFoldingDevice,
                deviceType = deviceType
            )
        } catch (e: Exception) {
            Log.e(TAG, "通过屏幕尺寸获取设备信息失败: ${e.message}")
            ScreenInfo(
                state = ScreenState.UNKNOWN,
                orientation = FoldOrientation.NONE,
                isFoldingDevice = confirmedIsFoldingDevice,
                deviceType = confirmedDeviceType
            )
        }
    }

    /**
     * 基于屏幕尺寸特征推断折叠屏设备类型
     *
     * BOOK_FOLD: 展开后宽度较大（通常 sw >= 600dp），折叠后为普通手机尺寸
     * FLIP_FOLD: 展开后为普通手机尺寸，折叠后更窄
     *
     * @param activity Activity上下文
     * @return 推断的设备类型
     */
    private fun inferDeviceTypeFromScreenSize(activity: Activity): FoldingDeviceType {
        return try {
            val screenDimensions = getScreenDimensionsDp(activity)
            val widthDp = screenDimensions.first
            val heightDp = screenDimensions.second
            val smallestWidth = minOf(widthDp, heightDp)
            val largestWidth = maxOf(widthDp, heightDp)

            Log.d(
                TAG,
                "推断设备类型 - 宽度: ${widthDp}dp, 高度: ${heightDp}dp, 最小宽度: ${smallestWidth}dp"
            )

            // BOOK_FOLD 设备特征：
            // - 折叠状态下 smallestWidth 通常在 360-420dp 范围（普通手机尺寸）
            // - 展开后 smallestWidth >= 600dp
            // - 如果设备有铰链传感器且当前是普通手机尺寸，很可能是 BOOK_FOLD 折叠状态

            // FLIP_FOLD 设备特征：
            // - 展开后与普通手机尺寸相似
            // - 折叠后宽度更窄（通常 < 360dp）
            // - 由于我们只在确认是折叠设备时才调用此方法，且当前 sw < 600
            //   如果 sw 在正常手机范围内，更可能是 BOOK_FOLD 的折叠状态

            when {
                // 如果之前曾经检测到大屏状态（通过某种方式记录），可以确定是 BOOK_FOLD
                // 这里使用保守策略：确认是折叠设备且 sw 在正常范围，优先判断为 BOOK_FOLD
                smallestWidth >= 350 && smallestWidth < 600 -> {
                    Log.d(TAG, "屏幕尺寸在正常手机范围，推断为 BOOK_FOLD 折叠状态")
                    FoldingDeviceType.BOOK_FOLD
                }

                smallestWidth < 350 -> {
                    // 很窄的屏幕，可能是 FLIP_FOLD 折叠状态
                    Log.d(TAG, "屏幕尺寸较窄，可能是 FLIP_FOLD 折叠状态")
                    FoldingDeviceType.FLIP_FOLD
                }

                else -> {
                    Log.d(TAG, "无法确定设备类型")
                    FoldingDeviceType.UNKNOWN
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "推断设备类型失败: ${e.message}")
            FoldingDeviceType.UNKNOWN
        }
    }

    /**
     * 获取屏幕尺寸（dp）
     *
     * 根据不同 API 版本使用相应的方法获取屏幕尺寸
     *
     * @param activity Activity上下文
     * @return Pair<宽度dp, 高度dp>
     */
    private fun getScreenDimensionsDp(activity: Activity): Pair<Float, Float> {
        return try {
            val widthPixels: Int
            val heightPixels: Int
            val density: Float

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                widthPixels = bounds.width()
                heightPixels = bounds.height()
                density = activity.resources.displayMetrics.density
            } else {
                val displayMetrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                widthPixels = displayMetrics.widthPixels
                heightPixels = displayMetrics.heightPixels
                density = displayMetrics.density
            }

            val widthDp = widthPixels / density
            val heightDp = heightPixels / density
            Pair(widthDp, heightDp)
        } catch (e: Exception) {
            Log.e(TAG, "获取屏幕尺寸失败: ${e.message}")
            Pair(0f, 0f)
        }
    }

    /**
     * 异步更新屏幕状态信息
     *
     * 使用协程并带节流控制，避免频繁启动
     * 通过 WindowInfoTracker 获取更精确的折叠屏状态
     *
     * @param activity Activity上下文，必须实现 LifecycleOwner
     */
    private fun asyncUpdateScreenInfo(activity: Activity) {
        if (activity !is LifecycleOwner) {
            return
        }

        // 如果已有正在运行的更新任务，跳过本次更新（节流）
        if (updateJob?.isActive == true) {
            return
        }

        updateJob = activity.lifecycleScope.launch {
            try {
                val windowInfoTracker = WindowInfoTracker.getOrCreate(activity)
                val layoutInfo = withContext(Dispatchers.IO) {
                    windowInfoTracker.windowLayoutInfo(activity).first()
                }
                val screenInfo = parseScreenInfo(activity, layoutInfo)
                lastScreenInfo = screenInfo
                Log.d(
                    TAG,
                    "异步更新屏幕状态完成: state=${screenInfo.state}, deviceType=${screenInfo.deviceType}"
                )
            } catch (e: CancellationException) {
                // 协程取消是正常行为（Activity 销毁等），不打印错误
            } catch (e: Exception) {
                Log.e(TAG, "异步更新屏幕状态失败: ${e.message}")
            }
        }
    }

    /**
     * 解析屏幕详细信息
     *
     * 从 WindowLayoutInfo 中解析折叠屏相关的状态信息
     *
     * @param activity Activity上下文
     * @param layoutInfo 窗口布局信息
     * @return 屏幕信息
     */
    private fun parseScreenInfo(activity: Activity, layoutInfo: WindowLayoutInfo): ScreenInfo {
        val folds = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()

        Log.d(
            TAG,
            "displayFeatures 数量: ${layoutInfo.displayFeatures.size}, FoldingFeature 数量: ${folds.size}"
        )

        // 如果检测到折叠特性，永久记住这是折叠屏设备
        val isFoldingDevice = if (folds.isNotEmpty()) {
            Log.d(TAG, "检测到 FoldingFeature，确认为折叠屏设备")
            if (!deviceTypeConfirmed) {
                deviceTypeConfirmed = true
                confirmedIsFoldingDevice = true
            }
            true
        } else {
            // 如果之前已确认是折叠屏，保持该状态（即使当前检测不到 FoldingFeature）
            if (deviceTypeConfirmed) {
                Log.d(TAG, "当前未检测到 FoldingFeature，但设备已确认为折叠屏，保持状态")
                confirmedIsFoldingDevice
            } else {
                Log.d(TAG, "未检测到 FoldingFeature，判定为普通直屏设备")
                false
            }
        }

        // 如果没有折叠特性，需要结合屏幕尺寸判断真实状态
        if (folds.isEmpty()) {
            // 如果之前已确认设备类型，使用缓存值（即使当前检测不到 FoldingFeature）
            val deviceType = if (confirmedDeviceType != FoldingDeviceType.UNKNOWN) {
                Log.d(TAG, "未检测到 FoldingFeature，但使用已确认的设备类型: $confirmedDeviceType")
                confirmedDeviceType
            } else {
                FoldingDeviceType.UNKNOWN
            }

            // 通过屏幕尺寸判断当前状态（某些设备展开时也检测不到 FoldingFeature）
            val state = if (isFoldingDevice) {
                // 获取当前屏幕尺寸来判断展开/折叠状态
                val smallestWidthDp = getSmallestWidthDp(activity)
                Log.d(TAG, "未检测到 FoldingFeature，根据屏幕最小宽度判断状态: ${smallestWidthDp}dp")

                when {
                    // 大屏（sw >= 600dp），判定为展开状态
                    smallestWidthDp >= 600 -> {
                        Log.d(TAG, "屏幕尺寸较大，判定为展开状态")
                        ScreenState.FOLDING_FLAT
                    }
                    // 中小屏幕，判定为折叠状态
                    else -> {
                        Log.d(TAG, "屏幕尺寸较小，判定为折叠状态")
                        ScreenState.FOLDING_FOLD
                    }
                }
            } else {
                ScreenState.NORMAL_SCREEN
            }

            return ScreenInfo(
                state = state,
                orientation = FoldOrientation.NONE,
                isFoldingDevice = isFoldingDevice,
                deviceType = deviceType
            )
        }

        // 有折叠特性，解析详细状态
        val fold = folds[0]
        Log.d(TAG, "FoldingFeature State: ${fold.state}, Orientation: ${fold.orientation}")

        val orientation = when (fold.orientation) {
            FoldingFeature.Orientation.HORIZONTAL -> {
                Log.d(TAG, "折叠方向: 水平（横向铰链）")
                FoldOrientation.HORIZONTAL
            }

            FoldingFeature.Orientation.VERTICAL -> {
                Log.d(TAG, "折叠方向: 垂直（纵向铰链）")
                FoldOrientation.VERTICAL
            }

            else -> FoldOrientation.NONE
        }

        val state = when (fold.state) {
            FoldingFeature.State.HALF_OPENED -> {
                Log.d(TAG, "当前设备处于半折叠状态（桌面模式/悬停模式）")
                ScreenState.FOLDING_HALF_OPEN
            }

            FoldingFeature.State.FLAT -> {
                // FLAT 状态表示完全展开
                Log.d(TAG, "当前设备完全展开")
                ScreenState.FOLDING_FLAT
            }

            else -> {
                Log.d(TAG, "未知折叠状态")
                ScreenState.UNKNOWN
            }
        }

        // 根据折叠方向判断设备类型
        val deviceType = when (orientation) {
            FoldOrientation.VERTICAL -> {
                Log.d(TAG, "设备类型: 书本式大折叠屏（纵向铰链）")
                FoldingDeviceType.BOOK_FOLD
            }

            FoldOrientation.HORIZONTAL -> {
                Log.d(TAG, "设备类型: 翻盖式小折叠屏（横向铰链）")
                FoldingDeviceType.FLIP_FOLD
            }

            else -> FoldingDeviceType.UNKNOWN
        }

        // 永久记住设备类型（一旦检测到就缓存，避免折叠状态下丢失）
        if (deviceType != FoldingDeviceType.UNKNOWN && confirmedDeviceType == FoldingDeviceType.UNKNOWN) {
            confirmedDeviceType = deviceType
            Log.d(TAG, "已确认并缓存设备类型: $confirmedDeviceType")
        }

        return ScreenInfo(
            state = state,
            orientation = orientation,
            isFoldingDevice = isFoldingDevice,
            deviceType = deviceType
        )
    }

    /**
     * 获取当前屏幕的最小宽度（dp）
     *
     * 用于判断屏幕尺寸类型，是确定折叠屏状态的重要指标
     *
     * @param activity Activity上下文
     * @return 最小宽度（dp）
     */
    private fun getSmallestWidthDp(activity: Activity): Float {
        return try {
            val widthPixels: Int
            val heightPixels: Int
            val density: Float

            // 使用新API获取屏幕尺寸（API 30+），否则使用废弃的API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                widthPixels = bounds.width()
                heightPixels = bounds.height()
                density = activity.resources.displayMetrics.density
            } else {
                val displayMetrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                widthPixels = displayMetrics.widthPixels
                heightPixels = displayMetrics.heightPixels
                density = displayMetrics.density
            }

            // 转换为 dp 值并返回最小宽度
            val widthDp = widthPixels / density
            val heightDp = heightPixels / density
            minOf(widthDp, heightDp)
        } catch (e: Exception) {
            Log.e(TAG, "获取屏幕最小宽度失败: ${e.message}")
            0f
        }
    }
}