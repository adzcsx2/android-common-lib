package com.hoyn.common.utils;

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowMetrics
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 屏幕状态枚举
 */
enum class ScreenState {
    /** 普通直屏设备 */
    NORMAL_SCREEN,

    /** 折叠屏完全展开状态 */
    FOLDING_FLAT_OPEN,

    /** 折叠屏半折叠状态（桌面模式/悬停模式） */
    FOLDING_HALF_OPENED,

    /** 折叠屏完全折叠状态 */
    FOLDING_CLOSED,

    /** 未知状态 */
    UNKNOWN
}

/**
 * 折叠方向枚举
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
 */
data class ScreenInfo(
    val state: ScreenState,
    val orientation: FoldOrientation,
    val isFoldingDevice: Boolean,
    val deviceType: FoldingDeviceType
)

/**
 * 引入Window,但是无法获取到VTL-202502的悬停状态,暂时取消
 * 如需使用,需取消build.gradle注释
 * //    implementation "androidx.window:window:1.3.0"
 */
object ScreenUtils {

    const val TAG = "ScreenUtils"

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

    /**
     * 获取当前屏幕状态
     * 同步返回基于屏幕尺寸的状态，同时异步获取更精确的折叠状态
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
     * @param activity Activity上下文
     * @return 屏幕状态枚举
     */
    @JvmStatic
    fun getScreenState(activity: Activity): ScreenState {
        return getScreenInfo(activity).state
    }

    /**
     * 判断是否为折叠屏设备
     * @param activity Activity上下文
     * @return true表示是折叠屏设备
     */
    @JvmStatic
    fun isFoldingDevice(activity: Activity): Boolean {
        return getScreenInfo(activity).isFoldingDevice
    }

    /**
     * 判断是否为书本式大折叠屏（纵向铰链，如 Galaxy Fold/Z Fold 系列）
     * 特点：展开后类似平板，适合左右分屏、阅读等场景
     * @param activity Activity上下文
     * @return true表示是书本式大折叠屏设备
     */
    @JvmStatic
    fun isBookFoldDevice(activity: Activity): Boolean {
        return getScreenInfo(activity).deviceType == FoldingDeviceType.BOOK_FOLD
    }

    /**
     * 判断是否为翻盖式小折叠屏（横向铰链，如 Galaxy Z Flip 系列）
     * 特点：展开后类似普通手机，适合自拍、悬停拍摄等场景
     * @param activity Activity上下文
     * @return true表示是翻盖式小折叠屏设备
     */
    @JvmStatic
    fun isFlipFoldDevice(activity: Activity): Boolean {
        return getScreenInfo(activity).deviceType == FoldingDeviceType.FLIP_FOLD
    }

    /**
     * 判断设备是否处于悬浮暂停状态（折叠屏半折叠状态）
     * 同步返回缓存值，同时异步更新最新状态
     * @param activity Activity上下文
     * @return true表示处于悬浮暂停状态，false表示正常状态
     */
    @JvmStatic
    fun isInTableTopMode(activity: Activity): Boolean {
        // 复用 getScreenInfo 更新状态并返回判断结果
        return getScreenInfo(activity).state == ScreenState.FOLDING_HALF_OPENED
    }

    /**
     * 判断是不是book设备打开状态
     */
    fun isBookOpen(activity: Activity): Boolean {
        return isBookFoldDevice(activity) && getScreenInfo(activity).state == ScreenState.FOLDING_FLAT_OPEN
    }


    /**
     * 检测设备是否有铰链角度传感器（折叠屏特有）
     * @param context Context上下文
     * @return true表示有铰链传感器（是折叠屏设备）
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
     * 用于首次启动或无法获取折叠信息时的降级方案
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
//                    Log.d(TAG,"使用已确认的设备类型: $confirmedDeviceType")
                    confirmedDeviceType
                }

                confirmedIsFoldingDevice && smallestWidthDp >= 600 -> {
                    Log.d(TAG, "根据大屏尺寸推测为书本式折叠屏")
                    FoldingDeviceType.BOOK_FOLD
                }

                else -> FoldingDeviceType.UNKNOWN
            }

            // 根据屏幕尺寸判断状态
            val (state, logMsg) = when {
                smallestWidthDp >= 600 -> {
                    val s =
                        if (confirmedIsFoldingDevice) ScreenState.FOLDING_FLAT_OPEN else ScreenState.UNKNOWN
                    s to "大屏设备（sw >= 600dp），可能是书本式折叠屏展开状态"
                }

                else -> {
                    val s =
                        if (confirmedIsFoldingDevice) ScreenState.FOLDING_CLOSED else ScreenState.UNKNOWN
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
     * 异步更新屏幕状态信息
     * 使用协程并带节流控制，避免频繁启动
     * @param activity Activity上下文
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
                        ScreenState.FOLDING_FLAT_OPEN
                    }
                    // 中小屏幕，判定为折叠状态
                    else -> {
                        Log.d(TAG, "屏幕尺寸较小，判定为折叠状态")
                        ScreenState.FOLDING_CLOSED
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
                ScreenState.FOLDING_HALF_OPENED
            }

            FoldingFeature.State.FLAT -> {
                // FLAT 状态表示完全展开
                Log.d(TAG, "当前设备完全展开")
                ScreenState.FOLDING_FLAT_OPEN
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