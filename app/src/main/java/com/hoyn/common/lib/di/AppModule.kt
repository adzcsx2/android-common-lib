package com.hoyn.common.lib.di

import com.hoyn.common.lib.MainApplication
import com.hoyn.common.lib.data.local.datasource.PostLocalDataSource
import com.hoyn.common.lib.data.local.datasource.PostLocalDataSourceImpl
import com.hoyn.common.lib.data.local.db.AppDatabase
import com.hoyn.common.lib.data.remote.api.CommentApi
import com.hoyn.common.lib.data.remote.api.PostApi
import com.hoyn.common.lib.data.remote.datasource.PostRemoteDataSource
import com.hoyn.common.lib.data.remote.datasource.PostRemoteDataSourceImpl
import com.hoyn.common.lib.data.repository.PostRepository
import com.hoyn.common.lib.ui.compose.ComposeDemoViewModel
import com.hoyn.common.network.RetrofitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/** API 基础地址（JSONPlaceholder 模拟接口） */
private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

/**
 * Koin 应用模块
 *
 * 定义应用级别的依赖注入配置
 */
val appModule = module {
    // API
    single<PostApi> { RetrofitFactory.createService(BASE_URL) }
    single<CommentApi> { RetrofitFactory.createService(BASE_URL) }

    // Remote DataSource
    single<PostRemoteDataSource> { PostRemoteDataSourceImpl(get()) }

    // Local DataSource
    single<PostLocalDataSource> { PostLocalDataSourceImpl(get()) }

    // Database
    single { AppDatabase.getInstance(androidContext()) }

    // Repository
    single<PostRepository> { PostRepository(get(), get()) }

    // ViewModel
    viewModel { ComposeDemoViewModel(get()) }
}

/**
 * 初始化 Koin 依赖注入框架
 *
 * @param application Application 实例
 */
fun initKoin(application: MainApplication) {
    org.koin.core.context.startKoin {
        androidContext(application)
        modules(appModule)
    }
}
