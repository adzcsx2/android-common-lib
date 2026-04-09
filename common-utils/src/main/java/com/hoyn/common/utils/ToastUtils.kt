package com.hoyn.common.utils

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.view.Gravity
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.hjq.toast.ToastParams
import com.hjq.toast.ToastStrategy
import com.hjq.toast.Toaster
import com.hjq.toast.config.IToastStrategy
import com.hjq.toast.config.IToastStyle
import com.hjq.toast.style.BlackToastStyle
import com.hjq.toast.style.CustomToastStyle
import com.hjq.toast.style.LocationToastStyle
import com.hjq.toast.style.WhiteToastStyle

/**
 * Toast 统一入口。
 *
 * 基于 Toaster 封装，统一使用 Application 初始化，避免页面级 Context 持有带来的泄露风险。
 */
object ToastUtils {

    /** 默认底部 Y 轴偏移量（dp） */
    private const val DEFAULT_BOTTOM_Y_OFFSET_DP = 64F
    /** 默认 X 轴偏移量 */
    private const val DEFAULT_X_OFFSET = 0
    /** 默认 Y 轴偏移量（px），由 dp 转换而来 */
    private val DEFAULT_Y_OFFSET: Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        DEFAULT_BOTTOM_Y_OFFSET_DP,
        Resources.getSystem().displayMetrics
    ).toInt()

    /**
     * Toast 配置快照，用于保存和恢复当前的样式、策略及位置配置
     *
     * @property style 当前 Toast 样式
     * @property strategy 当前 Toast 显示策略
     * @property gravity 显示位置（如 Gravity.BOTTOM、Gravity.CENTER）
     * @property xOffset X 轴偏移量（px）
     * @property yOffset Y 轴偏移量（px）
     */
    data class Snapshot(
        val style: IToastStyle<*>,
        val strategy: IToastStrategy,
        val gravity: Int,
        val xOffset: Int,
        val yOffset: Int
    )

    /** 全局 Application 引用，用于初始化 Toaster 和解析字符串资源 */
    private var app: Application? = null
    /** 默认显示位置 */
    private var defaultGravity: Int = Gravity.BOTTOM
    /** 默认 X 轴偏移量 */
    private var defaultXOffset: Int = DEFAULT_X_OFFSET
    /** 默认 Y 轴偏移量 */
    private var defaultYOffset: Int = DEFAULT_Y_OFFSET

    /**
     * 初始化 ToastUtils，必须在 Application.onCreate() 中调用
     * @param application 全局 Application 实例
     */
    fun init(application: Application) {
        app = application
        if (!Toaster.isInit()) {
            Toaster.init(application)
        }
    }

    /**
     * 判断 Toaster 是否已初始化
     * @return 已初始化返回 true
     */
    fun isInit(): Boolean = Toaster.isInit()

    /**
     * 显示短时 Toast（底部），仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     */
    fun show(message: CharSequence?) {
        showWithStyle(
            message = message,
            duration = Toast.LENGTH_SHORT,
            gravity = defaultGravity,
            xOffset = defaultXOffset,
            yOffset = defaultYOffset,
            debugOnly = true
        )
    }

    /**
     * 显示短时 Toast（底部），仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     */
    fun show(@StringRes messageRes: Int) {
        show(resolveText(messageRes))
    }

    /**
     * 显示 Debug 专用 Toast，仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     */
    fun debugShow(message: CharSequence?) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        withToaster {
            Toaster.debugShow(text)
        }
    }

    /**
     * 显示 Debug 专用 Toast，仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     */
    fun debugShow(@StringRes messageRes: Int) {
        withToaster {
            Toaster.debugShow(messageRes)
        }
    }

    /**
     * 延迟显示 Toast，仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     * @param delayMillis 延迟时间（毫秒）
     */
    fun delayedShow(message: CharSequence?, delayMillis: Long) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        withToaster {
            if (!isDebugBuild()) {
                return@withToaster
            }
            Toaster.delayedShow(text, delayMillis)
        }
    }

    /**
     * 延迟显示 Toast，仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     * @param delayMillis 延迟时间（毫秒）
     */
    fun delayedShow(@StringRes messageRes: Int, delayMillis: Long) {
        withToaster {
            if (!isDebugBuild()) {
                return@withToaster
            }
            Toaster.delayedShow(messageRes, delayMillis)
        }
    }

    /**
     * 显示短时 Toast（底部），仅 Debug 包生效，等同于 [show]
     * @param message 显示文本
     */
    fun showShort(message: CharSequence?) {
        show(message)
    }

    /**
     * 显示短时 Toast（底部），仅 Debug 包生效，等同于 [show]
     * @param messageRes 字符串资源 ID
     */
    fun showShort(@StringRes messageRes: Int) {
        show(messageRes)
    }

    /**
     * 显示全局优先级 Toast，不会被其他 Toast 覆盖
     * @param message 显示文本，为空或空白时不显示
     */
    fun showGlobal(message: CharSequence?) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        show(
            ToastParams().apply {
                this.text = text
                priorityType = ToastParams.PRIORITY_TYPE_GLOBAL
            }
        )
    }

    /**
     * 显示全局优先级 Toast，不会被其他 Toast 覆盖
     * @param messageRes 字符串资源 ID
     */
    fun showGlobal(@StringRes messageRes: Int) {
        showGlobal(resolveText(messageRes))
    }

    /**
     * 显示长时 Toast（底部），仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     */
    fun showLong(message: CharSequence?) {
        showWithStyle(
            message = message,
            duration = Toast.LENGTH_LONG,
            gravity = defaultGravity,
            xOffset = defaultXOffset,
            yOffset = defaultYOffset,
            debugOnly = true
        )
    }

    /**
     * 显示长时 Toast（底部），仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     */
    fun showLong(@StringRes messageRes: Int) {
        showLong(resolveText(messageRes))
    }

    /**
     * 显示短时 Toast（居中），仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     */
    fun showCenter(message: CharSequence?) {
        showWithStyle(
            message = message,
            duration = Toast.LENGTH_SHORT,
            gravity = Gravity.CENTER,
            yOffset = 0,
            debugOnly = true
        )
    }

    /**
     * 显示短时 Toast（居中），仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     */
    fun showCenter(@StringRes messageRes: Int) {
        showCenter(resolveText(messageRes))
    }

    /**
     * 显示长时 Toast（居中），仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     */
    fun showCenterLong(message: CharSequence?) {
        showWithStyle(
            message = message,
            duration = Toast.LENGTH_LONG,
            gravity = Gravity.CENTER,
            yOffset = 0,
            debugOnly = true
        )
    }

    /**
     * 显示长时 Toast（居中），仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     */
    fun showCenterLong(@StringRes messageRes: Int) {
        showCenterLong(resolveText(messageRes))
    }

    /** 取消当前显示的 Toast */
    fun cancel() {
        if (!Toaster.isInit()) {
            return
        }
        Toaster.cancel()
    }

    /**
     * 设置全局 Toast 样式，影响所有后续 Toast
     * @param style Toast 样式实现（如 BlackToastStyle、WhiteToastStyle）
     */
    fun setGlobalStyle(style: IToastStyle<*>) {
        withToaster {
            Toaster.setStyle(style)
        }
    }

    /** 切换为黑色背景 Toast 样式 */
    fun useBlackStyle() {
        setGlobalStyle(BlackToastStyle())
    }

    /** 切换为白色背景 Toast 样式 */
    fun useWhiteStyle() {
        setGlobalStyle(WhiteToastStyle())
    }

    /**
     * 使用自定义布局作为 Toast 样式
     * @param layoutRes 自定义布局资源 ID
     * @param gravity 显示位置，默认居中
     */
    fun useCustomStyle(@LayoutRes layoutRes: Int, gravity: Int = Gravity.CENTER) {
        setGlobalStyle(CustomToastStyle(layoutRes, gravity))
    }

    /**
     * 使用指定样式显示 Toast
     * @param message 显示文本，为空或空白时不显示
     * @param style Toast 样式
     */
    fun showWithStyle(message: CharSequence?, style: IToastStyle<*>) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        show(
            ToastParams().apply {
                this.text = text
                this.style = style
            }
        )
    }

    /**
     * 使用指定样式显示 Toast
     * @param messageRes 字符串资源 ID
     * @param style Toast 样式
     */
    fun showWithStyle(@StringRes messageRes: Int, style: IToastStyle<*>) {
        showWithStyle(resolveText(messageRes), style)
    }

    /**
     * 使用自定义布局显示 Toast
     * @param message 显示文本，为空或空白时不显示
     * @param layoutRes 自定义布局资源 ID
     * @param gravity 显示位置，默认居中
     */
    fun showWithLayout(
        message: CharSequence?,
        @LayoutRes layoutRes: Int,
        gravity: Int = Gravity.CENTER
    ) {
        showWithStyle(message, CustomToastStyle(layoutRes, gravity))
    }

    /**
     * 使用自定义布局显示 Toast
     * @param messageRes 字符串资源 ID
     * @param layoutRes 自定义布局资源 ID
     * @param gravity 显示位置，默认居中
     */
    fun showWithLayout(
        @StringRes messageRes: Int,
        @LayoutRes layoutRes: Int,
        gravity: Int = Gravity.CENTER
    ) {
        showWithLayout(resolveText(messageRes), layoutRes, gravity)
    }

    /** 设置 Toast 显示策略为队列模式，依次排队显示 */
    fun setQueueStrategy() {
        withToaster {
            Toaster.setStrategy(ToastStrategy(ToastStrategy.SHOW_STRATEGY_TYPE_QUEUE))
        }
    }

    /** 设置 Toast 显示策略为立即模式，新 Toast 直接替换当前 Toast */
    fun setImmediateStrategy() {
        withToaster {
            Toaster.setStrategy(ToastStrategy(ToastStrategy.SHOW_STRATEGY_TYPE_IMMEDIATELY))
        }
    }

    /**
     * 设置 Toaster 全局显示位置，影响所有使用 Toaster 直接显示的 Toast
     * @param gravity 显示位置（如 Gravity.BOTTOM、Gravity.CENTER）
     * @param xOffset X 轴偏移量（px），默认 0
     * @param yOffset Y 轴偏移量（px），默认 0
     */
    fun setGlobalGravity(gravity: Int, xOffset: Int = 0, yOffset: Int = 0) {
        withToaster {
            Toaster.setGravity(gravity, xOffset, yOffset)
        }
    }

    /**
     * 设置本工具类的默认显示位置，仅影响通过 show/showLong/showCenter 等方法显示的 Toast
     * @param gravity 显示位置
     * @param xOffset X 轴偏移量（px），默认 0
     * @param yOffset Y 轴偏移量（px），默认底部偏移
     */
    fun setGravity(gravity: Int, xOffset: Int = DEFAULT_X_OFFSET, yOffset: Int = DEFAULT_Y_OFFSET) {
        defaultGravity = gravity
        defaultXOffset = xOffset
        defaultYOffset = yOffset
    }

    /** 重置默认显示位置为底部默认偏移 */
    fun resetGravity() {
        defaultGravity = Gravity.BOTTOM
        defaultXOffset = DEFAULT_X_OFFSET
        defaultYOffset = DEFAULT_Y_OFFSET
    }

    /**
     * 设置全局自定义 Toast 视图，同时指定显示位置
     * @param layoutRes 自定义布局资源 ID
     * @param gravity 显示位置，默认居中
     * @param xOffset X 轴偏移量（px），默认 0
     * @param yOffset Y 轴偏移量（px），默认 0
     */
    fun setGlobalCustomView(
        @LayoutRes layoutRes: Int,
        gravity: Int = Gravity.CENTER,
        xOffset: Int = 0,
        yOffset: Int = 0
    ) {
        withToaster {
            Toaster.setView(layoutRes)
            Toaster.setGravity(gravity, xOffset, yOffset)
        }
    }

    /**
     * 保存当前 Toast 配置快照
     * @return 当前配置快照，未初始化时返回 null
     */
    fun snapshot(): Snapshot? {
        if (!Toaster.isInit()) {
            return null
        }
        return Snapshot(
            style = Toaster.getStyle(),
            strategy = Toaster.getStrategy(),
            gravity = defaultGravity,
            xOffset = defaultXOffset,
            yOffset = defaultYOffset
        )
    }

    /**
     * 从快照恢复 Toast 配置
     * @param snapshot 之前保存的配置快照，为 null 时不执行
     */
    fun restore(snapshot: Snapshot?) {
        snapshot ?: return
        defaultGravity = snapshot.gravity
        defaultXOffset = snapshot.xOffset
        defaultYOffset = snapshot.yOffset
        withToaster {
            Toaster.setStyle(snapshot.style)
            Toaster.setStrategy(snapshot.strategy)
        }
    }

    /**
     * 使用 ToastParams 显示 Toast，仅 Debug 包生效
     * @param params Toast 参数配置
     */
    fun show(params: ToastParams) {
        val text = params.text?.takeIf { it.isNotBlank() } ?: return
        withToaster {
            if (!isDebugBuild()) {
                return@withToaster
            }
            params.text = text
            Toaster.show(params)
        }
    }

    /**
     * 内部统一显示方法，通过 LocationToastStyle 包装位置参数
     * @param message 显示文本
     * @param duration 显示时长（Toast.LENGTH_SHORT / Toast.LENGTH_LONG）
     * @param gravity 显示位置
     * @param xOffset X 轴偏移量（px）
     * @param yOffset Y 轴偏移量（px）
     * @param debugOnly 是否仅 Debug 包生效
     */
    private fun showWithStyle(
        message: CharSequence?,
        duration: Int,
        gravity: Int,
        xOffset: Int = 0,
        yOffset: Int = 0,
        debugOnly: Boolean = false
    ) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        withToaster {
            val params = ToastParams().apply {
                this.text = text
                this.duration = duration
                this.style =
                    LocationToastStyle(Toaster.getStyle(), gravity, xOffset, yOffset, 0F, 0F)
            }
            if (debugOnly) {
                if (!isDebugBuild()) {
                    return@withToaster
                }
            }
            Toaster.show(params)
        }
    }

    /**
     * 确保已初始化后执行操作，未初始化时跳过
     * @param action 初始化完成后执行的操作
     */
    private inline fun withToaster(action: () -> Unit) {
        ensureInitialized() ?: return
        action()
    }

    /**
     * 确保 Toaster 已初始化，若未初始化且 app 不为 null 则自动初始化
     * @return Application 实例，未初始化且无 app 时返回 null
     */
    private fun ensureInitialized(): Application? {
        if (Toaster.isInit()) {
            return app
        }
        val application = app
        if (application == null) {
            Logger.w("ToastUtils is not initialized. Call ToastUtils.init(application) first.")
            return null
        }
        Toaster.init(application)
        return application
    }

    /**
     * 判断当前是否为 Debug 构建
     * @return Debug 构建返回 true
     */
    private fun isDebugBuild(): Boolean {
        val application = ensureInitialized() ?: return false
        return (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * 解析字符串资源为文本
     * @param messageRes 字符串资源 ID
     * @return 解析后的文本，解析失败或未初始化时返回 null
     */
    private fun resolveText(@StringRes messageRes: Int): CharSequence? {
        val application = ensureInitialized() ?: return null
        return runCatching { application.getText(messageRes) }.getOrNull()
    }
}