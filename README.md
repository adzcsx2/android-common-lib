# Android Common Library

Android 模块化公共库，提供常用的工具类、网络请求、图片加载、UI 组件等功能。

## Documentation

- [Getting Started Guide](/docs/guides/getting-started.md) - 快速开始使用本库
- [API Documentation](/docs/api/) - 各模块的详细 API 文档
  - [common-core](/docs/api/common-core.md) - 核心基础模块
  - [common-utils](/docs/api/common-utils.md) - 工具类扩展
  - [common-log](/docs/api/common-log.md) - 日志封装
  - [common-network](/docs/api/common-network.md) - 网络请求
  - [common-image](/docs/api/common-image.md) - 图片加载
  - [common-ui](/docs/api/common-ui.md) - UI 组件
- [Architecture Documentation](/docs/ARCHITECTURE.md) - 架构设计文档
- [Changelog](/docs/CHANGELOG.md) - 版本更新日志
- [Release Notes](/docs/releases/) - 各版本发布说明
- [Migration Guide](/docs/releases/migration-guide.md) - 版本迁移指南

## 项目结构

```
common-lib/
├── build.gradle.kts           # 根配置
├── settings.gradle.kts        # 模块配置
├── gradle/libs.versions.toml  # 版本目录
├── scripts/publish.gradle.kts # 通用发布脚本
├── common-core/               # 核心模块 (零依赖)
├── common-utils/              # 工具类扩展
├── common-log/                # 日志封装
├── common-network/            # 网络请求
├── common-image/              # 图片加载
├── common-ui/                 # UI 组件
└── app/                       # 示例应用
```

## 模块说明

| 模块 | 说明 | 依赖 |
|------|------|------|
| common-core | 核心基础模块 | 无 |
| common-utils | Context 扩展、协程扩展 | common-core |
| common-log | 统一日志工具 | common-core |
| common-network | Retrofit + OkHttp 封装 | common-core, common-log |
| common-image | Glide 图片加载封装 | common-core, common-utils |
| common-ui | BaseActivity/BaseFragment、View 扩展 | common-core |

## 依赖关系

```
            common-core (零依赖)
                  │
    ┌─────────────┼─────────────┐
    │             │             │
common-log   common-utils   common-ui
    │             │
    └──────┬──────┘
           │
    common-network
    common-image
```

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

```kotlin
dependencies {
    // 按需引入模块 (版本号使用 Gitee 仓库的 tag)
    implementation("com.gitee.Hoyn:android-common-lib:1.0.0")
}
```

> **注意**: JitPack 会将整个项目作为一个包发布，所有模块都会包含在内。

### 3. 使用最新版本 (SNAPSHOT)

如果想一直使用最新版本而不指定具体版本号：

```kotlin
dependencies {
    // 开发阶段 - 使用 SNAPSHOT，始终获取最新代码
    implementation("com.gitee.Hoyn:android-common-lib:-SNAPSHOT")

    // 生产环境 - 使用固定版本，稳定可靠
    implementation("com.gitee.Hoyn:android-common-lib:1.0.1")
}
```

#### SNAPSHOT 说明

**SNAPSHOT 是分支的最新版本，不是 tag 的最新版本。**

| 写法 | 含义 |
|------|------|
| `1.0.0` | 固定版本，对应 `1.0.0` tag |
| `1.0.1` | 固定版本，对应 `1.0.1` tag |
| `-SNAPSHOT` | master 分支的最新提交 |
| `1.0.1-SNAPSHOT` | 1.0.1 tag 之后的最新提交 |

#### 工作原理

```
master 分支:  ──●──●──●──●──●──●──●──●──>
                 │     │           │
               tag   tag         最新提交
             1.0.0  1.0.1       (SNAPSHOT)
```

- **固定版本 (1.0.0, 1.0.1)**: 永远对应那个 tag 的代码，不会变
- **SNAPSHOT**: 每次构建都会从 master 拉取最新代码

> **注意**: SNAPSHOT 版本每次构建都会检查更新，可能会增加构建时间。生产环境建议使用固定版本。

## 发布新版本

### 步骤 1: 更新版本号

编辑 `gradle.properties`：

```properties
# 修改为新的版本号
libVersion=1.0.1
```

### 步骤 2: 提交代码并创建 Tag

```bash
# 提交更改
git add .
git commit -m "chore: 版本更新至 1.0.1"

# 创建 Tag
git tag 1.0.1

# 推送代码和 Tag
git push origin master
git push origin 1.0.1
```

### 步骤 3: 等待 JitPack 构建

推送 Tag 后，JitPack 会自动检测并构建新版本：

- 访问 https://jitpack.io/#com.gitee.Hoyn/android-common-lib
- 查看构建状态（首次可能需要几分钟）

### 步骤 4: 使用新版本

```kotlin
dependencies {
    implementation("com.gitee.Hoyn:android-common-lib:1.0.1")
}
```

## 模块使用示例

### common-log

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
if (response.isSuccess) {
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

// BaseActivity
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun createBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
    }
}
```

## 本地发布

```bash
# 发布到本地 Maven 仓库
./gradlew publishToMavenLocal

# 然后在其他项目中使用
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.gitee.Hoyn:common-core:1.0.0")
}
```

## 构建项目

```bash
# 构建所有模块
./gradlew build

# 清理
./gradlew clean
```

## 版本信息

- compileSdk: 36
- minSdk: 24
- targetSdk: 36
- Kotlin: 2.1.0
- AGP: 9.0.0-rc01

## License

Apache License 2.0
