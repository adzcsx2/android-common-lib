package com.hoyn.common.base

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * BaseFragment
 *
 * 提供通用的 Fragment 基类功能
 * 支持协程、ViewBinding、ViewModel
 *
 * @param VB ViewBinding 类型
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel<*>> :
    Fragment(),
    CoroutineScope by MainScope() {

    protected lateinit var binding: VB
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

    // 页面基础信息
    internal lateinit var mActivity: BaseActivity<*, *>
    internal lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity() as BaseActivity<*, *>
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewBindingClassResolver.inflateFragmentBinding(
            owner = this,
            baseClass = BaseFragment::class.java,
            inflater = inflater,
            container = container
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onVisible()
        initView(view, savedInstanceState)
        initData()
    }

    override fun onResume() {
        super.onResume()
        onVisible()
    }

    protected open fun viewModelStoreOwner(): ViewModelStoreOwner = this

    protected open fun savedStateRegistryOwner(): SavedStateRegistryOwner = this

    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            lazyLoadData()
            isFirst = false
        }
    }

    /**
     * 懒加载数据
     */
    protected open fun lazyLoadData() {}

    private fun resolveViewModelClass(): Class<VM> {
        return ViewModelClassResolver.resolve(this, BaseFragment::class.java)
    }

    protected open fun initView(view: View, savedInstanceState: Bundle?) {}

    protected open fun initData() {}
    /**
     * 返回键处理
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
        cancel()
        super.onDestroyView()
    }

    open fun onBackPressed() {
        mActivity.onBackPressedDispatcher.onBackPressed()
    }
}
