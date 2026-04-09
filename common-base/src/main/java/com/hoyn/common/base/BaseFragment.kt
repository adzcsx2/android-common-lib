package com.hoyn.common.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import androidx.savedstate.SavedStateRegistryOwner
import com.hoyn.common.utils.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * BaseFragment
 *
 * 提供通用的 Fragment 基类功能
 * 支持协程、ViewBinding、ViewModel、懒加载
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel<*>> :
    Fragment(),
    CoroutineScope {

    private val dialogController by lazy(LazyThreadSafetyMode.NONE) {
        DialogController(::canShowManagedDialogs)
    }

    private var fragmentScope: CoroutineScope? = null

    override val coroutineContext: CoroutineContext
        get() = requireNotNull(fragmentScope) {
            "CoroutineScope is only available between onAttach and onDestroy."
        }.coroutineContext

    private var _binding: VB? = null
    protected val binding: VB
        get() = requireNotNull(_binding) {
            "Binding is only valid between onCreateView and onDestroyView."
        }
    protected val toast: ToastUtils
        get() = ToastUtils

    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelFactory.createAuto(
            owner = viewModelStoreOwner(),
            savedStateOwner = savedStateRegistryOwner(),
            application = requireActivity().application,
            modelClass = resolveViewModelClass(),
            defaultArgs = arguments
        )
    }

    // 是否第一次加载
    private var isFirst: Boolean = true

    private val viewCleanupActions = mutableListOf<() -> Unit>()

    protected val mActivity: BaseActivity<*, *>?
        get() = activity as? BaseActivity<*, *>

    protected val mContext: Context
        get() = requireContext()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (fragmentScope == null) {
            fragmentScope = MainScope()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        registerViewCleanupAction {
            dialogController.dismissAll()
        }
        _binding = ViewBindingClassResolver.inflateFragmentBinding(
            owner = this,
            baseClass = BaseFragment::class.java,
            inflater = inflater,
            container = container
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFirstVisibleState()
        initView(view, savedInstanceState)
        initData()
    }

    /**
     * 提供 ViewModelStoreOwner
     *
     * 默认返回 Fragment 自身，子类可重写以支持共享 ViewModel
     *
     * @return ViewModelStoreOwner 实例
     */
    protected open fun viewModelStoreOwner(): ViewModelStoreOwner = this

    /**
     * 提供 SavedStateRegistryOwner
     *
     * 默认返回 Fragment 自身，子类可重写以支持自定义状态保存
     *
     * @return SavedStateRegistryOwner 实例
     */
    protected open fun savedStateRegistryOwner(): SavedStateRegistryOwner = this

    private fun observeFirstVisibleState() {
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (!isFirst) {
                    owner.lifecycle.removeObserver(this)
                    return
                }
                isFirst = false
                lazyLoadData()
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    /**
     * 懒加载数据
     */
    protected open fun lazyLoadData() {}

    /**
     * 解析 ViewModel 的 Class 对象
     *
     * @return ViewModel 的 Class 对象
     */
    private fun resolveViewModelClass(): Class<VM> {
        return ViewModelClassResolver.resolve(this, BaseFragment::class.java)
    }

    /**
     * 初始化视图
     *
     * 在 onViewCreated 中调用，用于设置视图的初始状态、绑定适配器等
     *
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态 Bundle
     */
    protected open fun initView(view: View, savedInstanceState: Bundle?) {}

    /**
     * 初始化数据
     *
     * 在 initView 之后调用，用于加载数据、发起网络请求等
     */
    protected open fun initData() {}
    /**
     * 返回键处理
     *
     * 注册返回键回调，可控制是否拦截返回事件
     *
     * @param enabled 是否拦截返回键，true 表示拦截
     * @param onBackPressed 返回键按下时的回调
     */
    open fun onBack(enabled: Boolean, onBackPressed: () -> Unit) {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(enabled) {
                override fun handleOnBackPressed() {
                    onBackPressed.invoke()
                }
            })
    }

    /**
     * 页面跳转后返回的回调
     */
    open fun onRestartNavigate() {}

    /**
     * 是否需要默认填充状态栏
     */
    protected open fun fillStatus(): Boolean = true

    /**
     * 状态栏颜色
     */
    protected open fun statusColor(): Int = Color.TRANSPARENT

    /**
     * 是否使用浅色状态栏（白色字体图标）
     */
    protected open fun isLightStatusBar(): Boolean = true

    override fun onDestroyView() {
        runViewCleanupActions()
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        fragmentScope?.cancel()
        fragmentScope = null
        super.onDestroy()
    }

    /**
     * 注册 view 销毁时的清理动作。
     */
    protected fun registerViewCleanupAction(action: () -> Unit) {
        viewCleanupActions.add(action)
    }

    /**
     * 注册一个 Dialog，在 Fragment 的 View 销毁时自动 dismiss。
     */
    protected fun registerDialogForViewCleanup(dialogProvider: () -> Dialog?) {
        registerViewCleanupAction {
            dialogProvider()?.takeIf { it.isShowing }?.dismiss()
        }
    }

    /**
     * 将普通 Dialog 纳入 Fragment 的 View 生命周期管理。
     */
    protected fun <T : Dialog> manageDialog(dialog: T): T {
        return dialogController.manage(dialog)
    }

    /**
     * 安全显示已托管的 Dialog；若宿主视图已销毁则直接返回 false。
     */
    protected fun showManagedDialog(dialog: Dialog): Boolean {
        return dialogController.show(dialog)
    }

    /**
     * 安全关闭已托管的 Dialog。
     */
    protected fun dismissManagedDialog(dialog: Dialog?) {
        dialogController.dismiss(dialog)
    }

    /**
     * 关闭当前 Fragment View 管理的全部普通 Dialog。
     */
    protected fun dismissAllManagedDialogs() {
        dialogController.dismissAll()
    }

    private fun canShowManagedDialogs(): Boolean {
        val hostActivity = activity ?: return false
        return isAdded &&
            view != null &&
            !hostActivity.isFinishing &&
            !hostActivity.isDestroyed
    }

    private fun runViewCleanupActions() {
        viewCleanupActions.asReversed().forEach { action ->
            runCatching(action)
        }
        viewCleanupActions.clear()
    }

    /**
     * 处理返回键事件
     *
     * 委托给 Activity 处理返回键
     */
    open fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
