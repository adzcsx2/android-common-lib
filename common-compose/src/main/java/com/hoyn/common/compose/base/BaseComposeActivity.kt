package com.hoyn.common.compose.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.hoyn.common.base.ActivityStackManager
import com.hoyn.common.base.ViewModelClassResolver
import com.hoyn.common.base.ViewModelFactory
import com.hoyn.common.compose.theme.AppTheme
import com.hoyn.common.utils.LanguageHelper

/**
 * Base Compose Activity
 *
 * 提供纯 Compose Activity 的基类
 * 支持多语言设置、Activity 栈管理、ViewModel 自动注入
 *
 * @param VM ViewModel 类型
 */
abstract class BaseComposeActivity<VM : ViewModel> : ComponentActivity() {

    /** 销毁时需要执行的清理操作列表 */
    private val cleanupActions = mutableListOf<() -> Unit>()

    /**
     * 自动生成的 TAG，使用类名
     * 子类可以直接使用，无需手动定义
     */
    val TAG: String get() = javaClass.simpleName

    /**
     * ViewModel 实例
     * 通过 ViewModelFactory 自动创建，无需在 Koin 中注册
     */
    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelFactory.createAuto(
            owner = this,
            savedStateOwner = this,
            application = application,
            modelClass = ViewModelClassResolver.resolve(this, BaseComposeActivity::class.java),
            defaultArgs = intent?.extras
        )
    }

    /**
     * 附加基础 Context
     *
     * 统一应用语言设置
     *
     * @param newBase 新的 Context
     */
    override fun attachBaseContext(newBase: Context) {
        // 统一应用语言设置
        val context = LanguageHelper.applyLanguage(newBase)
        super.attachBaseContext(context)
    }

    /**
     * Activity 创建时的初始化
     *
     * 注册到 Activity 栈管理器，并使用 [AppTheme] 包裹 Compose 内容
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityStackManager.registerActivity(this)
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content()
            }
        }
    }

    /**
     * Compose 内容
     *
     * 子类实现此方法以提供 Compose UI 内容
     */
    @Composable
    protected abstract fun Content()

    /**
     * Activity 销毁时的清理
     *
     * 从 Activity 栈管理器注销，执行子类清理和注册的清理操作
     */
    override fun onDestroy() {
        ActivityStackManager.unregisterActivity(this)
        try {
            onCleanUp()
        } finally {
            runCleanupActions()
            super.onDestroy()
        }
    }

    /**
     * 子类可重写的清理方法
     *
     * 在 Activity 销毁时调用，用于释放子类持有的资源
     */
    protected open fun onCleanUp() {}

    /**
     * 注册清理操作
     *
     * 在 Activity 销毁时按逆序执行所有注册的清理操作
     *
     * @param action 清理操作 lambda
     */
    protected fun registerCleanupAction(action: () -> Unit) {
        cleanupActions.add(action)
    }

    /**
     * 注册 Dialog 以便在 Activity 销毁时自动关闭
     *
     * 仅关闭仍在显示中的 Dialog
     *
     * @param dialogProvider Dialog 提供者 lambda
     */
    protected fun registerDialogForCleanup(dialogProvider: () -> Dialog?) {
        registerCleanupAction {
            dialogProvider()?.takeIf { it.isShowing }?.dismiss()
        }
    }

    /**
     * 按逆序执行所有注册的清理操作
     *
     * 使用 runCatching 确保单个操作失败不影响后续操作
     */
    private fun runCleanupActions() {
        cleanupActions.asReversed().forEach { action ->
            runCatching(action)
        }
        cleanupActions.clear()
    }
}
