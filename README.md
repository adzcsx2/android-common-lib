# Android Common Library

Android 模块化公共库，提供常用的工具类、网络请求、图片加载、UI 组件等功能。

## Language

- [English README](./README.en.md)

## 开发环境

| 项目                   | 版本                     |
| ---------------------- | ------------------------ |
| Android Studio         | Panda 2 \| 2025.3.2      |
| Android Studio Runtime | JetBrains Runtime 21.0.9 |
| Kotlin                 | 2.3.10                   |
| Kotlin JVM target      | 17                       |
| compileSdk             | 34                       |
| minSdk                 | 24                       |
| targetSdk              | 34                       |

> 依赖兼容性说明
>
> 通过 JitPack 直接引用已发布产物时，消费者项目除了添加依赖外，还需要满足以下最低要求：
>
> - Kotlin Gradle Plugin 必须对齐到 `2.3.10`
> - Kotlin/JVM 编译目标必须为 `17`
> - `compileSdk` 需要大于等于 `34`
> - `minSdk` 为 `24`
>
> 如果只升级 Kotlin 到 `2.3.10`，但消费者项目仍使用更低的 JVM target，依然可能在编译期出现 Kotlin 分析异常或 metadata 兼容问题。源码复制到业务工程后可以编译，通过的是“由业务工程重新编译源码”这条路径；直接依赖 AAR 时，必须以发布产物的 Kotlin 编译参数为准。

## Documentation

- [Getting Started Guide](/docs/guides/getting-started.md) - 快速开始使用本库
- [API Documentation](/docs/api/) - 各模块的详细 API 文档
  - [common-core](/docs/api/common-core.md) - 核心基础模块
  - [common-base](/docs/api/common-base.md) - 架构基础模块
  - [common-utils](/docs/api/common-utils.md) - 工具类扩展
  - [common-compose](/docs/api/common-compose.md) - Compose 组件
  - [common-network](/docs/api/common-network.md) - 网络请求
  - [common-image](/docs/api/common-image.md) - 图片加载
  - [common-ui](/docs/api/common-ui.md) - UI 组件
- [Architecture Documentation](/docs/ARCHITECTURE.md) - 架构设计文档
- [App Architecture Guide](/docs/APP_ARCHITECTURE.md) - 模块职责与接入基线
- [Changelog](/docs/CHANGELOG.md) - 版本更新日志
- [Release Notes](/docs/releases/) - 各版本发布说明
- [Migration Guide](/docs/releases/migration-guide.md) - 版本迁移指南

### 文档导航

| 文档 | 描述 |
|------|------|
| [项目概览](docs/PROJECT_OVERVIEW.md) | 项目简介、版本信息、技术栈 |
| [界面文档](docs/INTERFACES.md) | Activity/Fragment 界面与控件 |
| [导航文档](docs/NAVIGATION.md) | 页面跳转与通信流程 |
| [四大组件](docs/COMPONENTS.md) | Activity/Service/Receiver 清单 |
| [依赖文档](docs/DEPENDENCIES.md) | Version Catalog 与模块依赖 |
| [API 文档](docs/API.md) | 网络接口与框架 API |

> 查看文档更新记录: [文档更新日志](docs/DOC_CHANGELOG.md)

## 项目结构

```
common-lib/
├── build.gradle.kts           # 根配置
├── settings.gradle.kts        # 模块配置
├── gradle/libs.versions.toml  # 版本目录
├── scripts/publish.gradle.kts # 通用发布脚本
├── common-core/               # 核心模块 (稳定抽象层)
├── common-base/               # 架构基础模块 (BaseActivity/BaseFragment/BaseViewModel)
├── common-compose/            # Compose 组件 (主题/基础组件/扩展)
├── common-utils/              # 工具类扩展 (含 Logger)
├── common-network/            # 网络请求
├── common-image/              # 图片加载
├── common-ui/                 # UI 组件 (Toast/状态栏/权限等)
└── app/                       # 示例应用 (Compose + View 混用)
```

## 模块说明

| 模块           | 说明                                                            | 依赖                      |
| -------------- | --------------------------------------------------------------- | ------------------------- |
| common-core    | 核心基础模块 (UIState/IBaseResponse/Message/ThrowableBean)      | Lifecycle, Coroutines     |
| common-base    | 架构基础模块 (BaseActivity/BaseFragment/BaseViewModel/事件机制) | common-core, common-utils |
| common-compose | Compose 组件 (主题/基础Activity&Fragment/扩展)                  | common-core, common-base  |
| common-utils   | Context 扩展、协程扩展、Logger、MMKV                            | common-core               |
| common-network | Retrofit + OkHttp 封装                                          | common-core, common-utils |
| common-image   | Glide 图片加载封装                                              | common-core, common-utils |
| common-ui      | Toast/状态栏/权限/View 扩展                                     | common-base               |

## 依赖关系

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

**注意**: common-compose 依赖 common-base，可以在 Compose 中使用相同的架构模式。

## 推荐接入策略

- 新页面推荐使用 `UIState` 配合 `Flow/StateFlow` 管理页面状态。
- 已接入页面可以继续使用 `LiveData` 和现有事件机制，不需要一次性迁移。
- `common-base` 提供架构基类，`common-ui` 只负责 UI helper 和展示层工具。

## 使用方式 (通过 JitPack)

### 1. 在项目根目录 settings.gradle.kts 中添加仓库

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. 在模块 build.gradle.kts 中添加依赖

#### 方式一：按需引入单个模块 (推荐)

```kotlin
dependencies {
    // 只引入需要的模块，减少 APK 体积
    implementation("com.github.adzcsx2.android-common-lib:common-core:1.2.7")      // 核心基础模块
    implementation("com.github.adzcsx2.android-common-lib:common-base:1.2.7")      // 架构基础模块
    implementation("com.github.adzcsx2.android-common-lib:common-utils:1.2.7")     // 工具类扩展
    implementation("com.github.adzcsx2.android-common-lib:common-compose:1.2.7")   // Compose 组件
    implementation("com.github.adzcsx2.android-common-lib:common-network:1.2.7")   // 网络请求
    implementation("com.github.adzcsx2.android-common-lib:common-image:1.2.7")     // 图片加载
    implementation("com.github.adzcsx2.android-common-lib:common-ui:1.2.7")        // UI 组件
}
```

#### 方式二：引入全部模块

```kotlin
dependencies {
    // 通过聚合模块引入全部库模块
    implementation("com.github.adzcsx2.android-common-lib:common-all:1.2.7")
}
```

> 注意：当前并不存在可直接使用的根坐标 `com.github.adzcsx2.android-common-lib:1.2.7`。如果你想一次性引入全部模块，请使用 `common-all`。

### 3. 使用最新版本 (SNAPSHOT)

如果想一直使用最新版本而不指定具体版本号：

```kotlin
dependencies {
    // 开发阶段 - 使用 SNAPSHOT，始终获取最新代码
    implementation("com.github.adzcsx2.android-common-lib:common-all:-SNAPSHOT")

    // 生产环境 - 使用固定版本，稳定可靠
    implementation("com.github.adzcsx2.android-common-lib:common-all:1.2.7")
}
```

#### SNAPSHOT 说明

**SNAPSHOT 是分支的最新版本，不是 tag 的最新版本。**

| 写法             | 含义                       |
| ---------------- | -------------------------- |
| `1.0.0`          | 固定版本，对应 `1.0.0` tag |
| `1.2.8`          | 固定版本，对应 `1.2.8` tag |
| `-SNAPSHOT`      | master 分支的最新提交      |
| `1.2.8-SNAPSHOT` | 1.2.8 tag 之后的最新提交   |

#### 工作原理

```
master 分支:  ──●──●──●──●──●──●──●──●──>
                 │     │           │
               tag   tag         最新提交
                                                                                                                                                                                                 1.0.0  1.2.8       (SNAPSHOT)
```

- **固定版本 (1.0.0, 1.2.8)**: 永远对应那个 tag 的代码，不会变
- **SNAPSHOT**: 每次构建都会从 master 拉取最新代码

> **注意**: SNAPSHOT 版本每次构建都会检查更新，可能会增加构建时间。生产环境建议使用固定版本。

## 发布新版本

### 步骤 1: 提交代码并创建 Tag

发布前先执行最小质量检查：

```bash
./gradlew qualityCheck
```

```bash
# 提交更改
git add .
git commit -m "chore: release version X.X.X"

# 创建 Tag（版本号即为 tag 名称）
git tag X.X.X

# 推送代码和 Tag
git push origin master
git push origin X.X.X
```

### 步骤 3: 等待 JitPack 构建

推送 Tag 后，JitPack 会自动检测并构建新版本：

- 访问 https://jitpack.io/#com.github.adzcsx2/android-common-lib
- 查看构建状态（首次可能需要几分钟）

### 步骤 4: 使用新版本

```kotlin
dependencies {
    implementation("com.github.adzcsx2.android-common-lib:common-all:1.2.7")
}
```

## 本地质量检查

```bash
# 运行所有 Android 模块的 lint + debug 单元测试
./gradlew qualityCheck

# 验证所有库模块都可以发布到 Maven Local
./gradlew libraryPublishDryRun
```

## 模块使用示例

### common-base (架构基础)

```kotlin
// BaseActivity 使用
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun createBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
    }
}

// BaseViewModel 使用
class UserViewModel : BaseViewModel<UserRepository>() {
    override val repository: UserRepository by lazy { UserRepository() }

    fun getUser(id: Int) {
        launchOnlyResult(
            block = { repository.api.getUser(id) },
            success = { user -> /* 处理成功 */ },
            error = { e -> /* 处理错误 */ }
        )
    }
}
```

### common-utils (Logger)

```kotlin
// 初始化
Logger.init(debug = BuildConfig.DEBUG, tag = "MyApp")

// 使用
Logger.d("Debug message")
Logger.e("Error message", throwable)
Logger.json(jsonString)
```

### common-network

```kotlin
// 配置
NetworkConfig.connectTimeout = 30
NetworkConfig.isDebug = true

// 创建服务
interface ApiService {
    @GET("user/info")
    suspend fun getUserInfo(): ApiResponse<UserInfo>
}

val api = RetrofitFactory.createService<ApiService>("https://api.example.com/")

// 使用
val response = api.getUserInfo()
if (response.isSuccess()) {
    // response.data
}
```

### common-image

```kotlin
// 使用扩展函数
imageView.loadImage(url, placeholder = R.drawable.placeholder)

// 加载圆形图片
imageView.loadCircleImage(avatarUrl)
```

### common-ui

```kotlin
// View 扩展
view.visible()
view.gone()
view.onClick { /* click */ }

// Toast
ToastUtil.show("Hello World")
```

### common-compose (Jetpack Compose)

```kotlin
// 纯 Compose Activity
class MyComposeActivity : BaseComposeActivity() {
    override fun Content() {
        AppTheme {
            MyScreen()
        }
    }
}
```

### Toast API Migration

New code should use `ToastUtil` as the unified toast entry point:

```kotlin
ToastUtil.show("Hello World")
ToastUtil.showLong("Long message")
ToastUtil.showCenter("Centered message")
```

Use `ToastUtil` for all toast calls.

This is a source-breaking change for any callers still using the old toast API name.

// 混用 Compose 和 View 的 Fragment
class MyHybridFragment : BaseComposeFragment() {
override fun onCreateView(...): View {
return FrameLayout(requireContext()).apply {
// 添加传统 View
addView(myNativeView)
// 添加 Compose
addView(composeView)
}
}
}

// 使用主题颜色
@Composable
fun MyScreen() {
val colors = useAppThemeColors()
Box(
modifier = Modifier.background(colors.primary)
) {
Text("Hello Compose", color = colors.onPrimary)
}
}

````

## 本地发布

```bash
# 发布到本地 Maven 仓库
./gradlew publishToMavenLocal

# 然后在其他项目中使用
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.github.adzcsx2.android-common-lib:common-core:1.0.5")
}
````

## 构建项目

```bash
# 构建所有模块
./gradlew build

# 清理
./gradlew clean
```

## 版本信息

- compileSdk: 34
- minSdk: 24
- targetSdk: 34
- Kotlin: 2.3.10
- AGP: 9.1.0

## License

Apache License 2.0
