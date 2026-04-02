package com.hoyn.common.base

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.hoyn.common.utils.LanguageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * BaseActivity
 *
 * 提供通用的 Activity 基类功能
 * 支持协程、ViewBinding、触摸事件分发、多语言、Activity 栈管理
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 */
abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel<*>> :
    AppCompatActivity(),
    CoroutineScope by MainScope() {

    private val dialogController by lazy(LazyThreadSafetyMode.NONE) {
        DialogController(::canShowManagedDialogs)
    }

    protected lateinit var binding: VB
    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelFactory.createAuto(
            owner = this,
            savedStateOwner = this,
            application = application,
            modelClass = ViewModelClassResolver.resolve(this, BaseActivity::class.java),
            defaultArgs = intent?.extras
        )
    }

    /**
     * 自动生成的 TAG，使用类名
     * 子类可以直接使用，无需手动定义
     */
    val TAG: String get() = javaClass.simpleName

    // 保存 MyTouchListener 接口的列表
    private val myTouchListeners = mutableListOf<OnTouchListener>()

    private val cleanupActions = mutableListOf<() -> Unit>()

    init {
        registerCleanupAction {
            dialogController.dismissAll()
        }
    }

    /**
     * 记录是否需要跳转
     */
    private var startNavigation: Boolean = false

    /**
     * 获取是否正在进行页面导航
     *
     * @return true 表示正在进行导航，false 表示没有
     */
    fun getIsNavigate(): Boolean = startNavigation

    override fun attachBaseContext(newBase: Context) {
        // 统一应用语言设置
        val context = LanguageHelper.applyLanguage(newBase)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityStackManager.registerActivity(this)
        onCreateBefore()
        super.onCreate(savedInstanceState)
        binding = ViewBindingClassResolver.inflateActivityBinding(this, BaseActivity::class.java)
        setContentView(binding.root)
        initView(savedInstanceState)
        initData()
    }

    override fun onResume() {
        if (startNavigation) {
            onRestartNavigate()
            startNavigation = false
        }
        super.onResume()
    }

    override fun onStop() {
        startNavigation = true
        super.onStop()
    }

    /**
     * 页面跳转后返回的回调
     */
    protected open fun onRestartNavigate() {}

    /**
     * 初始化视图
     *
     * 在 setContentView 之后调用，用于设置视图的初始状态、绑定适配器等
     *
     * @param savedInstanceState 保存的实例状态 Bundle
     */
    protected open fun initView(savedInstanceState: Bundle?) {}

    /**
     * 初始化数据
     *
     * 在 initView 之后调用，用于加载数据、发起网络请求等
     */
    protected open fun initData() {}

    /**
     * 这里可以做一些 setContentView 之前的操作
     * 如全屏、常亮、设置 Navigation 颜色、状态栏颜色等
     */
    protected open fun onCreateBefore() {
        // 取消严格模式 FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
    }

    /**
     * 提供给 Fragment 注册自己的触摸事件的方法
     */
    fun registerTouchListener(listener: OnTouchListener) {
        myTouchListeners.add(listener)
    }

    /**
     * 提供给 Fragment 取消注册自己的触摸事件的方法
     */
    fun unregisterTouchListener(listener: OnTouchListener?) {
        myTouchListeners.remove(listener)
    }

    /**
     * 分发触摸事件给所有注册了 OnTouchListener 的接口
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        for (listener in myTouchListeners) {
            ev?.let { listener.onTouchEvent(it) }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        ActivityStackManager.unregisterActivity(this)
        try {
            onCleanUp()
        } finally {
            runCleanupActions()
            cancel()
            super.onDestroy()
        }
    }

    /**
     * 清理资源，子类可以重写此方法来执行额外的清理操作
     * 例如取消 Toast、关闭对话框等
     */
    protected open fun onCleanUp() {
        // 默认实现为空，子类可以重写
    }

    /**
     * 注册 Activity 销毁时的清理动作。
     */
    protected fun registerCleanupAction(action: () -> Unit) {
        cleanupActions.add(action)
    }

    /**
     * 注册一个 Dialog，在 Activity 销毁时自动 dismiss。
     */
    protected fun registerDialogForCleanup(dialogProvider: () -> Dialog?) {
        registerCleanupAction {
            dialogProvider()?.takeIf { it.isShowing }?.dismiss()
        }
    }

    /**
     * 将普通 Dialog 纳入 BaseActivity 生命周期管理。
     */
    protected fun <T : Dialog> manageDialog(dialog: T): T {
        return dialogController.manage(dialog)
    }

    /**
     * 安全显示已托管的 Dialog；若宿主已结束则直接返回 false。
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
     * 关闭当前 Activity 管理的全部普通 Dialog。
     */
    protected fun dismissAllManagedDialogs() {
        dialogController.dismissAll()
    }

    private fun canShowManagedDialogs(): Boolean {
        return !isFinishing && !isDestroyed
    }

    private fun runCleanupActions() {
        cleanupActions.asReversed().forEach { action ->
            runCatching(action)
        }
        cleanupActions.clear()
    }

    /**
     * 获取 Fragment 列表中最后一个 Fragment
     */
    fun getFragmentListLast(): Fragment? {
        return try {
            supportFragmentManager.fragments.firstOrNull()
                ?.childFragmentManager?.fragments?.lastOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取 Fragment 列表
     */
    fun getFragmentLists(): List<Fragment> {
        return try {
            supportFragmentManager.fragments.firstOrNull()
                ?.childFragmentManager?.fragments ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 触摸事件监听接口
     *
     * 用于 Fragment 向 Activity 注册触摸事件监听
     */
    interface OnTouchListener {
        /**
         * 触摸事件回调
         *
         * @param ev 触摸事件
         */
        fun onTouchEvent(ev: MotionEvent)
    }
}
