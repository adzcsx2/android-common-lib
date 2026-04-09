package com.hoyn.common.base

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.viewbinding.ViewBinding
import com.hoyn.common.utils.Logger
import com.hoyn.common.ui.toast.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.io.Serializable
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 安全的 DialogFragment 基类，提供 ViewBinding、ViewModel 和安全 show/dismiss 封装。
 */
abstract class BaseDialogFragment<VB : ViewBinding, VM : BaseViewModel<*>> :
    DialogFragment(),
    CoroutineScope {

    /**
     * Dialog 在 FragmentManager 中使用的默认标识。
     */
    protected open val dialogTag: String
        get() = javaClass.name

    /** Dialog 宽度，默认为 WRAP_CONTENT */
    var mWidth: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    /** Dialog 高度，默认为 WRAP_CONTENT */
    var mHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    /** Dialog 位置，默认为居中显示 */
    var mGravity: Int = Gravity.CENTER

    /** Dialog 水平偏移量（像素） */
    var mOffsetX: Int = 0

    /** Dialog 垂直偏移量（像素） */
    var mOffsetY: Int = 0

    /** Dialog 进入/退出动画资源 ID，null 表示不使用动画 */
    var mAnimation: Int? = null

    /** 是否允许点击 Dialog 外部区域关闭，默认为 true */
    var touchOutside: Boolean = true

    /** 背景遮罩透明度，0.0 为全透明，1.0 为全黑，默认 0.5 */
    var dimAmount: Float = 0.5F

    /** View 生命周期的协程作用域，在 onCreateView 创建、onDestroyView 销毁 */
    private var viewScope: CoroutineScope? = null

    /**
     * 协程上下文，仅在 onCreateView 与 onDestroyView 之间可用。
     *
     * @return 当前的协程上下文
     * @throws IllegalStateException 在 View 生命周期外访问时抛出
     */
    override val coroutineContext: CoroutineContext
        get() = requireNotNull(viewScope) {
            "CoroutineScope is only available between onCreateView and onDestroyView."
        }.coroutineContext

    /** ViewBinding 的可空引用，用于延迟清理 */
    private var _binding: VB? = null

    /**
     * ViewBinding 实例，仅在 onCreateView 与 onDestroyView 之间可访问。
     *
     * @return 非 null 的 ViewBinding 实例
     * @throws IllegalStateException 在 View 生命周期外访问时抛出
     */
    protected val binding: VB
        get() = requireNotNull(_binding) {
            "Binding is only valid between onCreateView and onDestroyView."
        }
    protected val toast: ToastUtils
        get() = ToastUtils

    /**
     * 懒加载的 ViewModel 实例，通过 ViewModelFactory 自动创建。
     * 使用 NONE 模式避免线程安全问题，因为 ViewModel 只在主线程访问。
     */
    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelFactory.createAuto(
            owner = viewModelStoreOwner(),
            savedStateOwner = savedStateRegistryOwner(),
            application = requireActivity().application,
            modelClass = resolveViewModelClass(),
            defaultArgs = arguments
        )
    }

    /**
     * 创建 Dialog 的根视图，同时初始化协程作用域和 ViewBinding。
     *
     * @param inflater 布局填充器
     * @param container 父容器
     * @param savedInstanceState 保存的状态
     * @return Dialog 的根视图
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewScope?.cancel()
        viewScope = MainScope()
        _binding = ViewBindingClassResolver.inflateFragmentBinding(
            owner = this,
            baseClass = BaseDialogFragment::class.java,
            inflater = inflater,
            container = container
        )
        return binding.root
    }

    /**
     * 视图创建完成后的回调，依次调用 [initView] 和 [initData]。
     *
     * @param view 创建的根视图
     * @param savedInstanceState 保存的状态
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view, savedInstanceState)
        initData()
    }

    /**
     * Dialog 可见时的回调，设置外部点击关闭策略和应用窗口配置。
     */
    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(resolveCanceledOnTouchOutside())
        applyWindowConfig()
    }

    /**
     * Dialog 关闭时的回调，触发 [onDialogDismissed] 通知子类。
     *
     * @param dialog 被关闭的 Dialog
     */
    override fun onDismiss(dialog: DialogInterface) {
        onDialogDismissed()
        super.onDismiss(dialog)
    }

    /**
     * 视图销毁时的回调，取消协程作用域并清理 ViewBinding 引用。
     */
    override fun onDestroyView() {
        viewScope?.cancel()
        viewScope = null
        _binding = null
        super.onDestroyView()
    }

    /**
     * 安全显示 DialogFragment，避免重复 add 和宿主结束态崩溃。
     *
     * @param manager FragmentManager，用于执行 Fragment 事务
     * @param tag Fragment 标识，默认使用 [dialogTag]
     * @return true 表示显示成功，false 表示已添加、宿主结束或异常
     */
    fun showSafely(manager: FragmentManager, tag: String = dialogTag): Boolean {
        if (isAdded || manager.isStateSaved) {
            Logger.w("Skip showing dialog fragment because it is already added or state is saved: ${javaClass.simpleName}")
            return false
        }
        if (activity?.isFinishing == true) {
            Logger.w("Skip showing dialog fragment because host activity is finishing: ${javaClass.simpleName}")
            return false
        }
        return runCatching {
            show(manager, tag)
            true
        }.getOrElse {
            Logger.e("Failed to show dialog fragment: ${javaClass.simpleName}", it)
            false
        }
    }

    /**
     * 安全关闭 DialogFragment。
     * 在状态已保存场景下自动降级为 dismissAllowingStateLoss，避免 IllegalStateException。
     */
    fun dismissSafely() {
        if (!isAdded) {
            return
        }
        runCatching {
            if (parentFragmentManager.isStateSaved) {
                dismissAllowingStateLoss()
            } else {
                dismiss()
            }
        }.onFailure {
            Logger.e("Failed to dismiss dialog fragment: ${javaClass.simpleName}", it)
        }
    }

    /**
     * 子类可覆写，定制点击外部区域是否允许关闭。
     */
    protected open fun canceledOnTouchOutside(): Boolean = true

    /**
     * 子类可覆写以进一步调整 Window 属性。
     */
    protected open fun onApplyWindowConfig(attributes: WindowManager.LayoutParams) = Unit

    /**
     * 初始化视图层交互，子类覆写以设置点击事件、列表适配器等。
     *
     * @param view Dialog 的根视图
     * @param savedInstanceState 保存的状态
     */
    protected open fun initView(view: View, savedInstanceState: Bundle?) {}

    /**
     * 初始化数据或绑定展示状态。
     */
    protected open fun initData() {}

    /**
     * Dialog 真正关闭后的回调。
     */
    protected open fun onDialogDismissed() {}

    /**
     * 子类可自定义 ViewModel 作用域宿主。
     *
     * @return ViewModelStoreOwner 实例，默认返回 this
     */
    protected open fun viewModelStoreOwner(): ViewModelStoreOwner = this

    /**
     * 子类可自定义 SavedState 作用域宿主。
     *
     * @return SavedStateRegistryOwner 实例，默认返回 this
     */
    protected open fun savedStateRegistryOwner(): SavedStateRegistryOwner = this

    /**
     * 将属性值自动映射到 arguments，支持基础类型、Parcelable、Serializable 与 nullable。
     *
     * @param T 参数类型
     * @param defaultValue 参数默认值，arguments 中不存在对应 key 时使用
     * @return 参数委托属性
     */
    protected inline fun <reified T> argument(defaultValue: T): ReadWriteProperty<BaseDialogFragment<VB, VM>, T> {
        val valueClass = T::class.java
        return object : ReadWriteProperty<BaseDialogFragment<VB, VM>, T> {
            override fun getValue(thisRef: BaseDialogFragment<VB, VM>, property: KProperty<*>): T {
                val bundle = thisRef.arguments ?: return defaultValue
                val key = property.name
                if (!bundle.containsKey(key)) {
                    return defaultValue
                }
                return readArgumentValue(bundle, key, valueClass, defaultValue)
            }

            override fun setValue(
                thisRef: BaseDialogFragment<VB, VM>,
                property: KProperty<*>,
                value: T
            ) {
                val bundle = (thisRef.arguments ?: Bundle()).also { thisRef.arguments = it }
                writeArgumentValue(bundle, property.name, value)
            }
        }
    }

    /**
     * 显式声明一个 Parcelable 类型参数委托，便于提高可读性。
     *
     * @param T Parcelable 参数类型
     * @param defaultValue 参数默认值，默认为 null
     * @return 可空 Parcelable 参数委托属性
     */
    protected inline fun <reified T : Parcelable> argumentParcelable(
        defaultValue: T? = null
    ): ReadWriteProperty<BaseDialogFragment<VB, VM>, T?> {
        return argument(defaultValue)
    }

    /**
     * 显式声明一个 Serializable 类型参数委托，便于提高可读性。
     *
     * @param T Serializable 参数类型
     * @param defaultValue 参数默认值，默认为 null
     * @return 可空 Serializable 参数委托属性
     */
    protected inline fun <reified T : Serializable> argumentSerializable(
        defaultValue: T? = null
    ): ReadWriteProperty<BaseDialogFragment<VB, VM>, T?> {
        return argument(defaultValue)
    }

    /**
     * 解析当前 Dialog 对应的 ViewModel 类型。
     */
    private fun resolveViewModelClass(): Class<VM> {
        return ViewModelClassResolver.resolve(this, BaseDialogFragment::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    /**
     * 按属性类型从 arguments 中读取参数值。
     *
     * @param T 参数的目标类型
     * @param bundle 参数 Bundle
     * @param key 参数键名
     * @param valueClass 参数类型的 Class 对象
     * @param defaultValue 未找到参数时返回的默认值
     * @return 从 Bundle 中读取的参数值
     * @throws IllegalArgumentException 当类型不支持或类型不匹配时抛出
     */
    @PublishedApi
    internal fun <T> readArgumentValue(
        bundle: Bundle,
        key: String,
        valueClass: Class<*>,
        defaultValue: T
    ): T {
        val rawValue = bundle.get(key)
        return when {
            valueClass == Int::class.java || valueClass == Integer::class.java -> readTypedValue<Int>(key, rawValue) as T
            valueClass == Long::class.java || valueClass == java.lang.Long::class.java -> readTypedValue<Long>(key, rawValue) as T
            valueClass == Float::class.java || valueClass == java.lang.Float::class.java -> readTypedValue<Float>(key, rawValue) as T
            valueClass == Double::class.java || valueClass == java.lang.Double::class.java -> readTypedValue<Double>(key, rawValue) as T
            valueClass == Boolean::class.java || valueClass == java.lang.Boolean::class.java -> readTypedValue<Boolean>(key, rawValue) as T
            valueClass == String::class.java -> (readTypedValue<String>(key, rawValue) as T?) ?: defaultValue
            CharSequence::class.java.isAssignableFrom(valueClass) ->
                (readTypedValue<CharSequence>(key, rawValue) as T?) ?: defaultValue
            Parcelable::class.java.isAssignableFrom(valueClass) -> {
                val parcelableClass = valueClass as Class<Parcelable>
                (BundleCompat.getParcelable(bundle, key, parcelableClass) as T?) ?: defaultValue
            }
            Serializable::class.java.isAssignableFrom(valueClass) -> {
                val serializableClass = valueClass as Class<Serializable>
                (BundleCompat.getSerializable(bundle, key, serializableClass) as T?) ?: defaultValue
            }
            else -> throw IllegalArgumentException(
                "Unsupported argument type for '$key': ${valueClass.name}"
            )
        }
    }

    /**
     * 将属性值写回 arguments，null 会自动移除对应 key。
     *
     * @param bundle 目标 Bundle
     * @param key 参数键名
     * @param value 要写入的参数值，为 null 时移除对应 key
     * @throws IllegalArgumentException 当值类型不支持时抛出
     */
    @PublishedApi
    internal fun writeArgumentValue(bundle: Bundle, key: String, value: Any?) {
        if (value == null) {
            bundle.remove(key)
            return
        }
        when (value) {
            is Int -> bundle.putInt(key, value)
            is Long -> bundle.putLong(key, value)
            is Float -> bundle.putFloat(key, value)
            is Double -> bundle.putDouble(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            is String -> bundle.putString(key, value)
            is CharSequence -> bundle.putCharSequence(key, value)
            is Parcelable -> bundle.putParcelable(key, value)
            is Serializable -> bundle.putSerializable(key, value)
            else -> throw IllegalArgumentException(
                "Unsupported argument type for '$key': ${value.javaClass.name}"
            )
        }
    }

    /**
     * 对基础类型做显式运行时校验，避免 Bundle 静默返回默认值掩盖参数错误。
     *
     * @param T 期望的基础类型
     * @param key 参数键名，用于错误信息
     * @param rawValue 从 Bundle 中取出的原始值
     * @return 类型转换后的值
     * @throws IllegalArgumentException 当类型不匹配时抛出
     */
    @PublishedApi
    internal inline fun <reified T> readTypedValue(key: String, rawValue: Any?): T {
        return rawValue as? T ?: throw IllegalArgumentException(
            "Argument '$key' expected ${T::class.java.name}, but was ${rawValue?.javaClass?.name}"
        )
    }

    /**
     * 综合字段配置与子类覆写结果，得到最终的外部点击关闭策略。
     */
    private fun resolveCanceledOnTouchOutside(): Boolean {
        return touchOutside && canceledOnTouchOutside()
    }

    /**
     * 将当前实例上的窗口配置应用到 Dialog Window。
     */
    private fun applyWindowConfig() {
        val currentDialog = dialog ?: return
        val window = currentDialog.window ?: return
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        currentDialog.setCanceledOnTouchOutside(resolveCanceledOnTouchOutside())
        val attributes = window.attributes
        attributes.width = mWidth
        attributes.height = mHeight
        attributes.gravity = mGravity
        attributes.x = mOffsetX
        attributes.y = mOffsetY
        attributes.dimAmount = dimAmount
        mAnimation?.let(window::setWindowAnimations)
        onApplyWindowConfig(attributes)
        window.attributes = attributes
    }
}