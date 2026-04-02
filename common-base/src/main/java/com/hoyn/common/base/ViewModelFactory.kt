package com.hoyn.common.base

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

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
            application: Application,
            noinline creator: (Application) -> VM
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

        /**
         * 自动创建 ViewModel 实例
         *
         * 优先尝试从 Koin 容器获取，失败后回退到反射创建。
         * 支持以下构造函数签名: (), (Application), (SavedStateHandle), (Application, SavedStateHandle)
         *
         * @param owner ViewModelStoreOwner
         * @param savedStateOwner SavedStateRegistryOwner，用于恢复状态
         * @param application Application 实例
         * @param modelClass ViewModel 的 Class 对象
         * @param defaultArgs 默认参数 Bundle
         * @return ViewModel 实例
         */
        fun <VM : ViewModel> createAuto(
            owner: ViewModelStoreOwner,
            savedStateOwner: SavedStateRegistryOwner,
            application: Application,
            modelClass: Class<VM>,
            defaultArgs: Bundle? = null
        ): VM {
            return ViewModelProvider(
                owner,
                object : AbstractSavedStateViewModelFactory(savedStateOwner, defaultArgs) {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        key: String,
                        modelClass: Class<T>,
                        handle: SavedStateHandle
                    ): T {
                        return instantiate(modelClass, application, handle)
                    }
                }
            )[modelClass]
        }

        /**
         * 实例化 ViewModel
         *
         * 按优先级尝试以下方式创建实例:
         * 1. 从 Koin 容器获取（传入 Application 和 SavedStateHandle 作为参数）
         * 2. 反射调用 (Application, SavedStateHandle) 构造函数
         * 3. 反射调用 (SavedStateHandle) 构造函数
         * 4. 反射调用无参构造函数
         * 5. 反射调用 (Application) 构造函数
         *
         * @param modelClass ViewModel 的 Class 对象
         * @param application Application 实例
         * @param savedStateHandle SavedStateHandle 实例
         * @return ViewModel 实例
         * @throws IllegalArgumentException 如果没有找到匹配的构造函数
         */
        @Suppress("UNCHECKED_CAST")
        private fun <VM : ViewModel> instantiate(
            modelClass: Class<VM>,
            application: Application,
            savedStateHandle: SavedStateHandle
        ): VM {
            // 首先尝试从 Koin 获取 ViewModel
            val koin = GlobalContext.getOrNull()
            if (koin != null) {
                val kClass = modelClass.kotlin as kotlin.reflect.KClass<ViewModel>
                val koinInstance = runCatching {
                    koin.get<ViewModel>(kClass, null) {
                        parametersOf(application, savedStateHandle)
                    } as VM
                }.getOrNull()
                if (koinInstance != null) {
                    return koinInstance
                }
            }

            // 回退到反射创建
            val appAndStateConstructor = runCatching {
                modelClass.getDeclaredConstructor(Application::class.java, SavedStateHandle::class.java)
            }.getOrNull()
            if (appAndStateConstructor != null) {
                appAndStateConstructor.isAccessible = true
                return appAndStateConstructor.newInstance(application, savedStateHandle)
            }

            val stateOnlyConstructor = runCatching {
                modelClass.getDeclaredConstructor(SavedStateHandle::class.java)
            }.getOrNull()
            if (stateOnlyConstructor != null) {
                stateOnlyConstructor.isAccessible = true
                return stateOnlyConstructor.newInstance(savedStateHandle)
            }

            val noArgsConstructor = runCatching {
                modelClass.getDeclaredConstructor()
            }.getOrNull()
            if (noArgsConstructor != null) {
                noArgsConstructor.isAccessible = true
                return noArgsConstructor.newInstance()
            }

            val applicationConstructor = runCatching {
                modelClass.getDeclaredConstructor(Application::class.java)
            }.getOrNull()
            if (applicationConstructor != null) {
                applicationConstructor.isAccessible = true
                return applicationConstructor.newInstance(application)
            }

            throw IllegalArgumentException(
                "${modelClass.name} must expose one of: (), (Application), (SavedStateHandle), (Application, SavedStateHandle)"
            )
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
    application: Application,
    noinline creator: (Application) -> VM
): VM = ViewModelFactory.createWithApplication(this, application, creator)
