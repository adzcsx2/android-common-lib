package com.hoyn.common.ui.toast

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.hjq.toast.ToastParams
import com.hjq.toast.ToastStrategy
import com.hjq.toast.Toaster
import com.hjq.toast.config.IToastInterceptor
import com.hjq.toast.config.IToastStrategy
import com.hjq.toast.config.IToastStyle
import com.hjq.toast.style.BlackToastStyle
import com.hjq.toast.style.CustomToastStyle
import com.hjq.toast.style.LocationToastStyle
import com.hjq.toast.style.WhiteToastStyle
import com.hoyn.common.utils.Logger

/**
 * Toast 统一入口。
 *
 * 基于 Toaster 封装，统一使用 Application 初始化，避免页面级 Context 持有带来的泄露风险。
 *
 * 支持全局配置和单次调用覆盖：
 * ```
 * // 全局配置
 * ToastUtils.init(this) {
 *     gravity = Gravity.CENTER
 *     stackSkips = 2
 * }
 *
 * // 单次调用覆盖
 * ToastUtils.show("message") {
 *     xOffset = 10
 *     yOffset = 100
 * }
 * ```
 */
object ToastUtils {

    private const val TOASTER_LOG_TAG = "Toaster"

    /** 全局 Application 引用，用于初始化 Toaster 和解析字符串资源 */
    private var app: Application? = null

    /** 全局配置，@Volatile 保证多线程可见性 */
    @Volatile
    private var globalConfig: ToastConfig = ToastConfig.defaults()

    /** 用于输出真实 Toast 调用位置的拦截器 */
    private val callSiteInterceptor: IToastInterceptor = CallSiteToastInterceptor()

    /**
     * 初始化 ToastUtils，必须在 Application.onCreate() 中调用
     * @param application 全局 Application 实例
     * @param config 可选的全局配置 lambda，修改后影响所有后续 Toast
     */
    fun init(application: Application, config: (ToastConfig.() -> Unit)? = null) {
        app = application
        if (!Toaster.isInit()) {
            Toaster.init(application)
        }
        config?.invoke(globalConfig)
    }

    /**
     * 判断 Toaster 是否已初始化
     * @return 已初始化返回 true
     */
    fun isInit(): Boolean = Toaster.isInit()

    /**
     * 显示短时 Toast（底部），仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun show(message: CharSequence?, config: ToastConfig.() -> Unit = {}) {
        showWithStyle(
            message = message,
            duration = Toast.LENGTH_SHORT,
            debugOnly = true,
            config = config
        )
    }

    /**
     * 显示短时 Toast（底部），仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun show(@StringRes messageRes: Int, config: ToastConfig.() -> Unit = {}) {
        show(resolveText(messageRes), config)
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
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun delayedShow(
        message: CharSequence?,
        delayMillis: Long,
        config: ToastConfig.() -> Unit = {}
    ) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        show(
            ToastParams().apply {
                this.text = text
                this.delayMillis = delayMillis
            },
            config = config
        )
    }

    /**
     * 延迟显示 Toast，仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     * @param delayMillis 延迟时间（毫秒）
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun delayedShow(
        @StringRes messageRes: Int,
        delayMillis: Long,
        config: ToastConfig.() -> Unit = {}
    ) {
        delayedShow(resolveText(messageRes), delayMillis, config)
    }

    /**
     * 显示全局优先级 Toast，不会被其他 Toast 覆盖
     * @param message 显示文本，为空或空白时不显示
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showGlobal(message: CharSequence?, config: ToastConfig.() -> Unit = {}) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        show(
            ToastParams().apply {
                this.text = text
                priorityType = ToastParams.PRIORITY_TYPE_GLOBAL
            },
            config = config
        )
    }

    /**
     * 显示全局优先级 Toast，不会被其他 Toast 覆盖
     * @param messageRes 字符串资源 ID
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showGlobal(@StringRes messageRes: Int, config: ToastConfig.() -> Unit = {}) {
        showGlobal(resolveText(messageRes), config)
    }

    /**
     * 显示长时 Toast（底部），仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showLong(message: CharSequence?, config: ToastConfig.() -> Unit = {}) {
        showWithStyle(
            message = message,
            duration = Toast.LENGTH_LONG,
            debugOnly = true,
            config = config
        )
    }

    /**
     * 显示长时 Toast（底部），仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showLong(@StringRes messageRes: Int, config: ToastConfig.() -> Unit = {}) {
        showLong(resolveText(messageRes), config)
    }

    /**
     * 显示短时 Toast（居中），仅 Debug 包生效
     * @param message 显示文本，为空或空白时不显示
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showCenter(message: CharSequence?, config: ToastConfig.() -> Unit = {}) {
        showWithStyle(
            message = message,
            duration = Toast.LENGTH_SHORT,
            debugOnly = true,
            config = {
                gravity = Gravity.CENTER
                yOffset = 0
                config()
            }
        )
    }

    /**
     * 显示短时 Toast（居中），仅 Debug 包生效
     * @param messageRes 字符串资源 ID
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showCenter(@StringRes messageRes: Int, config: ToastConfig.() -> Unit = {}) {
        showCenter(resolveText(messageRes), config)
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

    /**
     * 使用指定样式显示 Toast
     * @param message 显示文本，为空或空白时不显示
     * @param style Toast 样式
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showWithStyle(
        message: CharSequence?,
        style: IToastStyle<*>,
        config: ToastConfig.() -> Unit = {}
    ) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        show(
            ToastParams().apply {
                this.text = text
                this.style = style
            },
            config = config
        )
    }

    /**
     * 使用指定样式显示 Toast
     * @param messageRes 字符串资源 ID
     * @param style Toast 样式
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showWithStyle(
        @StringRes messageRes: Int,
        style: IToastStyle<*>,
        config: ToastConfig.() -> Unit = {}
    ) {
        showWithStyle(resolveText(messageRes), style, config)
    }

    /**
     * 使用自定义布局显示 Toast
     * @param message 显示文本，为空或空白时不显示
     * @param layoutRes 自定义布局资源 ID
     * @param gravity 显示位置，默认居中
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showWithLayout(
        message: CharSequence?,
        @LayoutRes layoutRes: Int,
        gravity: Int = Gravity.CENTER,
        config: ToastConfig.() -> Unit = {}
    ) {
        showWithStyle(message, CustomToastStyle(layoutRes, gravity), config)
    }

    /**
     * 使用自定义布局显示 Toast
     * @param messageRes 字符串资源 ID
     * @param layoutRes 自定义布局资源 ID
     * @param gravity 显示位置，默认居中
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun showWithLayout(
        @StringRes messageRes: Int,
        @LayoutRes layoutRes: Int,
        gravity: Int = Gravity.CENTER,
        config: ToastConfig.() -> Unit = {}
    ) {
        showWithLayout(resolveText(messageRes), layoutRes, gravity, config)
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
     * 设置本工具类的默认显示位置，仅影响通过 show/showLong/showCenter 等方法显示的 Toast
     * @param gravity 显示位置
     * @param xOffset X 轴偏移量（px），默认 0
     * @param yOffset Y 轴偏移量（px），默认底部偏移
     */
    fun setGravity(
        gravity: Int,
        xOffset: Int = ToastConfig.DEFAULT_X_OFFSET,
        yOffset: Int = ToastConfig.defaults().yOffset
    ) {
        val current = globalConfig
        globalConfig = current.copy().apply {
            this.gravity = gravity
            this.xOffset = xOffset
            this.yOffset = yOffset
        }
    }

    /** 重置默认显示位置为底部默认偏移 */
    fun resetGravity() {
        val defaults = ToastConfig.defaults()
        val current = globalConfig
        globalConfig = current.copy().apply {
            this.gravity = defaults.gravity
            this.xOffset = defaults.xOffset
            this.yOffset = defaults.yOffset
        }
    }

    /**
     * 使用 ToastParams 显示 Toast，仅 Debug 包生效
     * @param params Toast 参数配置
     * @param config 单次调用配置，覆盖全局默认值
     */
    fun show(params: ToastParams, config: ToastConfig.() -> Unit = {}) {
        val text = params.text?.takeIf { it.isNotBlank() } ?: return
        withToaster {
            if (!isDebugBuild()) {
                return@withToaster
            }
            showToastInternal(params, text, config)
        }
    }

    /**
     * 内部统一显示逻辑，不检查 debugOnly，由调用方决定
     */
    private fun showToastInternal(
        params: ToastParams,
        text: CharSequence,
        config: ToastConfig.() -> Unit = {}
    ) {
        val resolved = globalConfig.copy().apply(config)
        val downstreamInterceptor = params.interceptor ?: Toaster.getInterceptor()
        val interceptor = ToastInterceptorComposer.compose(
            callSiteInterceptor = callSiteInterceptor,
            globalInterceptor = downstreamInterceptor
        )
        val copiedParams = ToastParamsMapper.copyOf(params, text, interceptor)
        ToastStackSkipsStore.bind(copiedParams, resolved.stackSkips)
        Toaster.show(copiedParams)
    }

    /**
     * 内部统一显示方法，通过 LocationToastStyle 包装位置参数
     * @param message 显示文本
     * @param duration 显示时长（Toast.LENGTH_SHORT / Toast.LENGTH_LONG）
     * @param debugOnly 是否仅 Debug 包生效
     * @param config 单次调用配置，覆盖全局默认值
     */
    private fun showWithStyle(
        message: CharSequence?,
        duration: Int,
        debugOnly: Boolean = false,
        config: ToastConfig.() -> Unit = {}
    ) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        withToaster {
            if (debugOnly && !isDebugBuild()) {
                return@withToaster
            }
            val resolved = globalConfig.copy().apply(config)
            showToastInternal(
                params = ToastParams().apply {
                    this.text = text
                    this.duration = duration
                    this.style = LocationToastStyle(
                        Toaster.getStyle(),
                        resolved.gravity,
                        resolved.xOffset,
                        resolved.yOffset,
                        0F,
                        0F
                    )
                },
                text = text,
                config = {
                    stackSkips = resolved.stackSkips
                }
            )
        }
    }

    private class CallSiteToastInterceptor : IToastInterceptor {

        override fun intercept(params: ToastParams): Boolean {
            if (!isDebugBuild()) {
                return false
            }
            val stackSkips = ToastStackSkipsStore.take(params)
            val caller =
                ToastCallerLocator.findCallerStackTrace(Throwable().stackTrace, stackSkips)
                    ?: return false
            val location = ToastCallerLocator.resolveDisplayLocation(caller)
            Log.i(TOASTER_LOG_TAG, "${location.asLogPrefix()} ${params.text}")
            return false
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
