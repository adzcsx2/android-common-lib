# Android Common Library - Claude AI 开发规范

> 本文件定义 Android Common Library 项目的开发规范，使用 Claude Code 或其他 AI 工具辅助开发时，请将此文件复制到项目根目录。

## 项目概述

**Android Common Library** 是一个模块化 Android 公共库，提供常用的工具类、网络请求、图片加载、UI 组件等功能。

**Maven 坐标**: `com.github.adzcsx2.android-common-lib`

**模块依赖方式**:
```kotlin
// 按需引入单个模块
implementation("com.github.adzcsx2.android-common-lib:common-core:1.2.0")
implementation("com.github.adzcsx2.android-common-lib:common-base:1.2.0")
implementation("com.github.adzcsx2.android-common-lib:common-utils:1.2.0")
// ... 其他模块

// 或引入全部模块
implementation("com.github.adzcsx2.android-common-lib:common-all:1.2.0")
```

## 架构原则

### 1. Clean Architecture 分层

```
data/
├── remote/          # 远程数据源 (API)
│   ├── api/         # API 接口定义
│   └── datasource/  # Remote DataSource 实现
├── local/           # 本地数据源 (Database/Cache)
│   ├── dao/         # Room DAO
│   ├── entity/      # 数据库实体
│   └── datasource/  # Local DataSource 实现
├── model/           # 领域模型
└── repository/      # Repository（协调数据源）

ui/
├── viewmodel/       # ViewModel (状态管理)
├── activity/        # Activity
└── fragment/        # Fragment

di/                  # 依赖注入 (Koin 模块)
```

### 2. 关键设计原则

| 原则 | 说明 |
|------|------|
| **单一职责** | Repository 只协调数据源，不包含业务逻辑 |
| **依赖注入** | 使用 Koin，**禁止单例模式** |
| **数据源独立** | Remote/Local DataSource 分别实现 |
| **状态管理** | 使用 `UIState` + `StateFlow` |
| **不可变性** | 优先使用 `val` 和不可变数据类 |

### 3. 模块依赖层次

```
                    common-core (核心抽象层)
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
   common-utils       common-base      common-compose
         │                 │                 │
         └────────┬────────┘                 │
                  │                          │
         ┌────────┴────────┐                 │
         │                 │                 │
   common-network     common-image          │
         │                 │                 │
         └────────┬────────┘                 │
                  │                          │
             common-ui ←────────────────────┘
                  │
              Your App
```

## 模块优先级原则 (CRITICAL)

**当功能已存在于 common-lib 模块时，必须优先使用库内功能，禁止重复实现。**

### 使用前检查清单

在编写新代码前，先检查以下模块是否已提供所需功能：

| 需求 | 对应模块 | 使用方式 |
|------|----------|----------|
| 网络请求 | common-network | `RetrofitFactory.createService()` |
| 图片加载 | common-image | `imageView.loadImage(url)` |
| 日志记录 | common-utils | `Logger.d()`, `Logger.e()` |
| 状态管理 | common-core | `StateFlow<UIState<T>>` |
| Toast | common-ui | `ToastUtils.show()` |
| 状态栏 | common-ui | `StatusBarHelper` |
| 权限请求 | common-ui | `LivePermissions` |
| 键值存储 | common-utils | `MMKVUtils` |
| 协程扩展 | common-utils | `withIO`, `withMain` |

详细功能清单请查看 [docs/checklist/modules.md](./docs/checklist/modules.md)

## 代码规范

### 1. 依赖注入规范

**禁止使用单例模式，全部由 Koin 管理。**

```kotlin
// ✅ 正确：使用 Koin 注入
class MyViewModel(
    private val repository: MyRepository
) : BaseViewModel() {
    // ...
}

// 定义在 di/Module.kt
val appModule = module {
    single { MyRepository(get(), get()) }
    viewModel { MyViewModel(get()) }
}

// ❌ 错误：使用单例
object MyRepository {
    // ...
}
```

### 2. Repository 规范

Repository 只负责协调数据源，不包含业务逻辑。

```kotlin
// ✅ 正确：Repository 只协调数据源
class PostRepository(
    private val remoteDataSource: PostRemoteDataSource,
    private val localDataSource: PostLocalDataSource
) {
    suspend fun getPosts(): Result<PostLoadResult> {
        // 1. 尝试从远程获取
        val remoteResult = remoteDataSource.getPosts()
        if (remoteResult.isSuccess) {
            // 2. 成功时缓存到本地
            persistRemotePosts(remoteResult.getOrThrow())
            return remoteResult
        }
        // 3. 失败时从本地加载
        return localDataSource.getPosts()
    }
}

// ❌ 错误：Repository 包含业务逻辑
class PostRepository {
    suspend fun getPostsAndProcessAndFilterAndSort() {
        // 业务逻辑应该在 ViewModel 或 UseCase 中
    }
}
```

### 3. DataSource 规范

Remote 和 Local DataSource 独立实现，职责清晰。

```kotlin
// Remote DataSource - 只负责网络请求
interface PostRemoteDataSource {
    suspend fun getPosts(): Result<PostLoadResult>
}

// Local DataSource - 只负责本地数据
interface PostLocalDataSource {
    suspend fun getPosts(): Result<PostLoadResult>
    suspend fun replacePosts(entities: List<PostEntity>, cachedAt: Long)
}
```

### 4. ViewModel 规范

使用 `UIState` 管理页面状态。

```kotlin
class MyViewModel(
    private val repository: MyRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UIState<Data>>(UIState.Loading)
    val uiState: StateFlow<UIState<Data>> = _uiState

    fun loadData() {
        launchOnlyResult(
            block = { repository.getData() },
            success = { data -> _uiState.value = UIState.Success(data) },
            error = { e -> _uiState.value = UIState.Error(-1, e.message) }
        )
    }
}
```

### 5. 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| Repository | `XxxRepository` | `PostRepository` |
| DataSource | `XxxDataSource` / `XxxDataSourceImpl` | `PostRemoteDataSource` |
| ViewModel | `XxxViewModel` | `PostViewModel` |
| Activity | `XxxActivity` | `MainActivity` |
| Fragment | `XxxFragment` | `HomeFragment` |
| API 接口 | `XxxApi` | `PostApi` |
| DAO | `XxxDao` | `PostDao` |
| Entity | `XxxEntity` | `PostEntity` |
| Model | `Xxx` | `Post` |

## Koin 模块规范

```kotlin
// di/AppModule.kt
val appModule = module {
    // 1. API
    single<PostApi> { RetrofitFactory.createService(BASE_URL) }

    // 2. DataSource
    single<PostRemoteDataSource> { PostRemoteDataSourceImpl(get()) }
    single<PostLocalDataSource> { PostLocalDataSourceImpl(get()) }

    // 3. Database
    single { AppDatabase.getInstance(androidContext()) }

    // 4. Repository
    single<PostRepository> { PostRepository(get(), get()) }

    // 5. ViewModel
    viewModel { PostViewModel(get()) }
}

// Application 初始化
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
```

## 测试规范

- 单元测试覆盖率达到 80%
- 使用 MockK 进行 mock
- 测试文件与源文件放在同一包下

```kotlin
class PostRepositoryTest {
    private val mockRemote = mockk<PostRemoteDataSource>()
    private val mockLocal = mockk<PostLocalDataSource>()
    private val repository = PostRepository(mockRemote, mockLocal)

    @Test
    fun `getPosts returns remote result when successful`() = runTest {
        // Given
        coEvery { mockRemote.getPosts() } returns Result.success(mockData)

        // When
        val result = repository.getPosts()

        // Then
        assertTrue(result.isSuccess)
    }
}
```

## 常见错误

| 错误 | 正确做法 |
|------|----------|
| 使用单例 | 使用 Koin 注入 |
| Repository 包含业务逻辑 | 业务逻辑放在 ViewModel/UseCase |
| 直接在 ViewModel 调用 API | 通过 Repository 协调 |
| 使用 LiveData | 优先使用 StateFlow + UIState |
| 重复实现已有功能 | 检查 MODULES_CHECKLIST.md |

## 项目版本信息

- compileSdk: 34
- minSdk: 24
- targetSdk: 34
- Kotlin: 2.3.10
- AGP: 9.1.0

## 相关文档

- [README.md](./README.md) - 项目说明
- [MODULES_CHECKLIST.md](./MODULES_CHECKLIST.md) - 模块功能清单
- [ARCHITECTURE.md](./docs/ARCHITECTURE.md) - 架构详解
