package com.hoyn.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

/**
 * ViewModel 工厂
 *
 * 用于创建带有参数的 ViewModel 实例
 * 替代废弃的 ViewModelProvider.NewInstanceFactory
 *
 * 使用示例：
 * ```kotlin
 * // 在 Activity/Fragment 中获取带参数的 ViewModel
 * val viewModel = ViewModelFactory.create(this) { MyViewModel(myRepository) }
 *
 * // 或者使用扩展函数
 * val viewModel = createViewModel { MyViewModel(myRepository) }
 * ```
 */
class ViewModelFactory<VM : ViewModel>(
    private val creator: () -> VM
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }

    companion object {
        /**
         * 创建 ViewModel 实例
         *
         * @param owner ViewModelStoreOwner（Activity/Fragment）
         * @param creator ViewModel 创建函数
         * @return ViewModel 实例
         */
        inline fun <reified VM : ViewModel> create(
            owner: ViewModelStoreOwner,
            noinline creator: () -> VM
        ): VM {
            return ViewModelProvider(owner, ViewModelFactory(creator))[VM::class.java]
        }

        /**
         * 创建带 Application Context 的 ViewModel 实例
         *
         * @param owner ViewModelStoreOwner（Activity/Fragment）
         * @param creator ViewModel 创建函数（带 Application 参数）
         * @return ViewModel 实例
         */
        inline fun <reified VM : ViewModel> createWithApplication(
            owner: ViewModelStoreOwner,
            application: android.app.Application,
            noinline creator: (android.app.Application) -> VM
        ): VM {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.AndroidViewModelFactory(application) {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return creator(application) as T
                    }
                }
            )[VM::class.java]
        }
    }
}

/**
 * 扩展函数：在 ViewModelStoreOwner 中创建 ViewModel
 */
inline fun <reified VM : ViewModel> ViewModelStoreOwner.createViewModel(
    noinline creator: () -> VM
): VM = ViewModelFactory.create(this, creator)

/**
 * 扩展函数：在 ViewModelStoreOwner 中创建带 Application 的 ViewModel
 */
inline fun <reified VM : ViewModel> ViewModelStoreOwner.createViewModelWithApplication(
    application: android.app.Application,
    noinline creator: (android.app.Application) -> VM
): VM = ViewModelFactory.createWithApplication(this, application, creator)
