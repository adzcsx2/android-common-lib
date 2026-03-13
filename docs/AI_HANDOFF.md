# AI 协作交接文档

> **文档版本**: 1.0
> **创建时间**: 2026-03-13
> **创建窗口**: 1号窗口（审计与交接）
> **目标读者**: 2号窗口、3号窗口

---

## 1. 项目现状摘要

### 1.1 模块结构

当前 `common-lib` 项目包含以下模块：

| 模块 | 包名 | 职责 | 当前状态 |
|------|------|------|----------|
| `app` | `com.hoyn.common.lib` | Demo 应用 | 基础结构，仅 MainActivity/MainApplication |
| `common-core` | `com.hoyn.common.core` | 核心抽象层 | 几乎空模块，仅 CommonLib.kt |
| `common-utils` | `com.hoyn.common.utils` | 工具类 | 已有 MMKVUtils、Logger、ScreenUtils、DeviceHelper、DisplayUtils、ContextExtensions、CoroutinesExtensions |
| `common-network` | `com.hoyn.common.network` | 网络层 | 已有 ApiResponse、RetrofitFactory、OkHttpClientFactory、ExceptionHandle、ResponseThrowable、ERROR、SSLManager、LoggingInterceptor、Printer |
| `common-ui` | `com.hoyn.common.ui` | UI 层 | 已有 BaseActivity、BaseFragment、ToastUtils、StatusBarHelper、NotchHelper、PressEffectHelper、LivePermissions、ViewExtensions |
| `common-image` | `com.hoyn.common.image` | 图片加载 | 已有 ImageLoader、ImageExtensions |

### 1.2 已有基础能力

**common-ui 模块**:
- ✅ `BaseActivity<VB : ViewBinding>` - 支持协程、ViewBinding、触摸事件分发、Fragment 结果回调
- ✅ `BaseFragment<VB : ViewBinding>` - 支持协程、ViewBinding、懒加载、返回键处理
- ✅ `ToastUtils` - 完整 Toast 封装
- ✅ `StatusBarHelper` - 状态栏辅助
- ✅ `NotchHelper` - 刘海屏适配
- ✅ `PressEffectHelper` - 按压效果
- ✅ `LivePermissions` - 权限请求封装

**common-network 模块**:
- ✅ `ApiResponse<T>` - 通用响应封装
- ✅ `RetrofitFactory` - Retrofit 工厂
- ✅ `OkHttpClientFactory` - OkHttp 工厂
- ✅ `ExceptionHandle` - 异常处理
- ✅ `ResponseThrowable` - 响应异常
- ✅ `ERROR` - 错误码枚举
- ✅ `SSLManager` - SSL 证书管理

**common-utils 模块**:
- ✅ `MMKVUtils` - MMKV 封装（腾讯 MMKV 2.3.0）
- ✅ `Logger` - 日志工具
- ✅ `ScreenUtils` - 屏幕工具
- ✅ `DeviceHelper` / `DisplayUtils` - 设备/显示工具
- ✅ `ContextExtensions` / `CoroutinesExtensions` - 扩展函数

### 1.3 缺失能力（需从 ybase 迁移或新建）

- ❌ `BaseViewModel` - 无
- ❌ `BaseRepository` - 无
- ❌ `IBaseResponse` 接口 - 无（当前只有 ApiResponse data class）
- ❌ `ViewModelFactory` - 无
- ❌ `SingleLiveEvent` / 事件机制 - 无
- ❌ `UpdateGlobal` / `ThrowableReportGlobal` - 全局事件总线
- ❌ `SubscribeLifecycle` - Lifecycle DSL
- ❌ `Message` / `ThrowableBean` - 事件载体
- ❌ `StateNavigator` - 导航状态
- ❌ Compose 配置 - 无

---

## 2. 来源项目审计摘要

### 2.1 项目路径

- **来源项目**: `/Users/hoyn/Downloads/dzbl-android`
- **主要迁移来源**: `ybase` 模块
- **次要来源**: `basic` 模块（腾讯音视频相关，非主要迁移目标）

### 2.2 ybase 模块分析

**包路径**: `com.screson.baselibrary`

| 子目录 | 内容 | 迁移优先级 |
|--------|------|-----------|
| `base/` | BaseActivity, BaseFragment, BaseViewModel, BaseRepository, IBaseResponse, ViewModelFactory, Clazz, SubscribeLifecycle, StateNavigator | **高** |
| `http/` | ExceptionHandle, ResponseThrowable, ERROR, SSLManager, UpdateGlobal, ThrowableReportGlobal, LoggingInterceptor, Printer, ClientUtils | 中（部分已有） |
| `http/event/` | SingleLiveEvent, Message | **高** |
| `extention/` | ContextEx, ViewEx, StatusBarHelper, NotchHelper, NavigationExt, DeviceHelper, DisplayHelper | 中（部分已有） |
| `toast/` | YToast, 内部实现类 | 低（已有 ToastUtils） |
| `utils/` | LivePermissions, PressEffectHelper, SharePreferencesUtils, NetUtils | 低（已有或废弃） |
| `widget/` | RoundImageView | 可选 |

### 2.3 basic 模块分析

**包路径**: `com.tencent.liteav.basic`

| 文件 | 内容 | 迁移建议 |
|------|------|---------|
| `AvatarConstant.java` | 腾讯头像常量 | **不迁移** - 业务特定 |
| `Constants.java` | 腾讯常量 | **不迁移** - 业务特定 |
| `ImageLoader.java` | 图片加载 | **不迁移** - 已有 common-image |
| `OnSingleClickListener.java` | 防抖点击 | 可考虑迁移到 common-ui |
| `MD5Utils.java` | MD5 工具 | 可考虑迁移到 common-utils |
| `UserModel*.java` | 用户模型 | **不迁移** - 业务特定 |

**结论**: `basic` 主要是腾讯音视频 SDK 相关工具和业务杂项，**不是主迁移目标**。`ybase` 才是 base、event、viewmodel、repository 的主要来源。

---

## 3. 迁移矩阵

| 来源类/能力 | 目标模块 | 处理方式 | 原因 |
|------------|----------|----------|------|
| `BaseActivity` | common-ui | **已实现**，需对比补充 | 当前版本已支持 ViewBinding，缺少 MyTouchListener（已改名 OnTouchListener） |
| `BaseFragment` | common-ui | **已实现**，需对比补充 | 当前版本已支持 ViewBinding，缺少 ViewModel 泛型支持 |
| `BaseViewModel` | common-core | **需新建** | 核心架构类，放入 common-core |
| `BaseRepository` | common-network 或 common-core | **需新建** | 数据层基类，建议放入 common-network |
| `IBaseResponse` | common-network | **需新建接口** | 当前只有 ApiResponse data class，需抽象为接口 |
| `SingleLiveEvent` | common-core | **需重写** | 原实现有单观察者限制，建议用 SharedFlow 或 Channel 替代 |
| `UpdateGlobal` | common-core | **需重写** | 依赖 LiveEventBus，建议用 SharedFlow 替代 |
| `ThrowableReportGlobal` | common-core | **需重写** | 依赖 LiveEventBus，建议用 SharedFlow 替代 |
| `Message` | common-core | **需迁移** | 简单数据类，可直接迁移 |
| `ThrowableBean` | common-core | **需迁移** | 简单数据类，可直接迁移 |
| `ViewModelFactory` | common-core | **需重写** | 原实现用反射 newInstance，需改用 AndroidX ViewModelProvider.Factory |
| `SubscribeLifecycle` | common-core | **需重写** | 原实现用 `@OnLifecycleEvent` 注解（已废弃），需改用 DefaultLifecycleObserver |
| `Clazz` | common-core | **不迁移** | 反射工具类，不推荐使用 |
| `StateNavigator` | common-ui | **可选迁移** | 导航状态管理，视业务需求 |
| `NavigationExt.kt` | common-ui | **不迁移** | 简单扩展，已在项目中有类似实现 |

---

## 4. 禁止直接迁移的内容

以下内容**禁止照搬**，需要重新设计或使用现代替代方案：

| 禁止内容 | 原因 | 替代方案 |
|----------|------|----------|
| `LiveEventBus` | 第三方库，维护状态不明 | Kotlin SharedFlow / Channel |
| `SingleLiveEvent` 原实现 | 有单观察者限制，Google 示例代码已过时 | `SharedFlow<T>(replay=1, extraBufferCapacity=0, onBufferOverflow=DROP_OLDEST)` |
| `AndroidAutoSize` | 屏幕适配库，现代方案已变化 | 今日头条方案 / 不使用（推荐） |
| `kotlin-android-extensions` | 已废弃 | ViewBinding（已配置） |
| 反射式 Repository 注入 (`Clazz.getClass`) | 反射性能差，不安全 | Koin / Hilt DI 或手动注入 |
| `@OnLifecycleEvent` 注解写法 | 已废弃 | `DefaultLifecycleObserver` 接口 |
| `ViewModelProvider.NewInstanceFactory` | 已废弃 | `ViewModelProvider.AndroidViewModelFactory` |
| 废弃的 Navigation 扩展 | 简单包装，无实际价值 | 直接使用 Navigation API |
| `SharePreferencesUtils` | 旧实现 | 已有 MMKVUtils |

---

## 5. 模块边界

### 5.1 模块职责定义

```
common-core (最底层，无 Android 依赖或最小依赖)
├── 架构抽象接口
├── BaseViewModel
├── 事件机制 (EventBus 替代)
├── 通用数据类 (Message, ThrowableBean)
└── Lifecycle 相关工具

common-utils (依赖 common-core)
├── Android 工具类
├── 扩展函数
├── MMKV 封装
└── 设备/屏幕工具

common-network (依赖 common-core)
├── Retrofit/OkHttp 封装
├── ApiResponse / IBaseResponse
├── ExceptionHandle / ResponseThrowable
├── BaseRepository (可选)
└── SSL/日志拦截器

common-ui (依赖 common-utils, common-core)
├── BaseActivity / BaseFragment
├── ViewBinding 支持
├── Toast / StatusBar / Dialog
├── 权限请求
└── UI 扩展函数

common-image (依赖 common-core)
├── Glide 封装
└── 图片加载扩展

app (依赖所有模块)
├── Demo 展示
└── 不定义任何 lib 架构
```

### 5.2 依赖规则

- `common-core` → 只依赖 AndroidX Core，保持最轻量
- `common-utils` → 依赖 `common-core`
- `common-network` → 依赖 `common-core`
- `common-ui` → 依赖 `common-core`, `common-utils`
- `common-image` → 依赖 `common-core`
- `app` → 依赖所有模块，但**不反向定义 lib 架构**

### 5.3 禁止的依赖

- ❌ `common-core` 不能依赖 `common-ui`
- ❌ `common-core` 不能依赖 `common-network`
- ❌ `app` 不能定义被 lib 使用的类

---

## 6. 并发协作规则

### 6.1 窗口职责划分

| 窗口 | 职责 | 允许修改的模块 | 禁止修改 |
|------|------|---------------|----------|
| **1号窗口** | 审计与交接 | 仅 `docs/AI_HANDOFF.md` | 所有业务代码、Gradle |
| **2号窗口** | 架构实现 | `common-core`, `common-network`, `common-ui` | Gradle、app、版本配置 |
| **3号窗口** | 配置与集成 | `gradle/`, `build.gradle.kts`, `app`, 版本目录 | 架构代码 |

### 6.2 并发限制

1. **Gradle 文件互斥**: 任何人都不能并行修改 Gradle 文件
   - `settings.gradle.kts`
   - `build.gradle.kts` (root)
   - `gradle/libs.versions.toml`
   - 各模块 `build.gradle.kts`

2. **app 模块独占**: app 模块只能在最后阶段由 3号窗口独占修改

3. **common-core 优先**: 2号窗口必须先完成 common-core 的基础类，才能开始 common-ui 的补充

### 6.3 文件锁定规则

```
锁定给 2号窗口:
- common-core/src/main/java/**/*.kt
- common-network/src/main/java/**/*.kt (除已有文件需谨慎)
- common-ui/src/main/java/**/*.kt (除已有 BaseActivity/BaseFragment 需协调)

锁定给 3号窗口:
- gradle/libs.versions.toml
- */build.gradle.kts
- app/src/**/*.kt
```

---

## 7. 架构升级候选项

以下框架选择需要用户确认，文档中给出推荐值：

### 7.1 事件机制

| 选项 | 说明 | 推荐 |
|------|------|------|
| **Kotlin SharedFlow** | 官方协程方案，轻量 | ✅ **推荐** |
| Kotlin Channel | 更像队列，适合生产消费 | 可选 |
| RxJava | 重量级 | 不推荐 |
| LiveEventBus (旧) | 已禁止 | ❌ |

**推荐实现**:
```kotlin
// 在 common-core 中
class EventFlow<T> {
    private val flow = MutableSharedFlow<T>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    fun emit(value: T) = flow.tryEmit(value)
    fun observe(): SharedFlow<T> = flow.asSharedFlow()
}
```

### 7.2 页面状态模式

| 选项 | 说明 | 推荐 |
|------|------|------|
| **Sealed Class** | 简单直观，Kotlin 原生 | ✅ **推荐** |
| StateFlow | 响应式 | 可选，结合 Sealed Class |

**推荐实现**:
```kotlin
// 在 common-core 中
sealed class UIState<out T> {
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val code: Int, val message: String) : UIState<Nothing>()
    object Empty : UIState<Nothing>()
}
```

### 7.3 DI (依赖注入)

| 选项 | 说明 | 推荐 |
|------|------|------|
| **Koin** | 轻量 Kotlin DSL，学习成本低 | ✅ **推荐** |
| Hilt | Google 官方，但配置复杂 | 可选 |
| 手动注入 | 无框架 | 不推荐 |
| 反射注入 (旧) | 已禁止 | ❌ |

### 7.4 Navigation

| 选项 | 说明 | 推荐 |
|------|------|------|
| **Navigation Component** | Google 官方 | ✅ **推荐** |
| 自定义路由 | 复杂 | 不推荐 |

### 7.5 Paging

| 选项 | 说明 | 推荐 |
|------|------|------|
| **Paging 3** | 官方方案 | ✅ **推荐**（如需分页） |
| 手动分页 | 简单场景 | 可选 |

### 7.6 Compose 预留方式

```kotlin
// 在 common-ui/build.gradle.kts 中预留
buildFeatures {
    compose = true  // 暂不启用
}
// 或创建单独的 common-compose 模块
```

**推荐**: 暂不引入 Compose，但保持架构可扩展性。

---

## 8. 版本升级建议

### 8.1 当前版本入口

- **版本目录**: `gradle/libs.versions.toml`
- **各模块 Gradle**: `*/build.gradle.kts`

### 8.2 当前关键版本

| 依赖 | 当前版本 | 建议 |
|------|----------|------|
| AGP | 9.0.0-rc01 | 保持（预览版） |
| Kotlin | 2.1.0 | 保持 |
| Lifecycle | 2.8.7 | 保持 |
| Retrofit | 2.11.0 | 保持 |
| OkHttp | 4.12.0 | 保持 |
| MMKV | 2.3.0 | 保持 |
| Coroutines | 1.9.0 | 保持 |

### 8.3 升级原则

1. **稳定性优先**: 避免使用 alpha/beta 版本（AGP rc 除外）
2. **版本对齐**: Lifecycle、Activity、Fragment 版本需兼容
3. **Kotlin 兼容**: 确保 Kotlin 版本与 Compose Compiler 兼容（如未来引入）
4. **3号窗口负责**: 所有版本升级由 3号窗口统一处理

---

## 9. 给 2号窗口的实施指引

### 9.1 任务列表

1. **Phase 1: common-core 基础建设**
   - [ ] 创建 `IBaseResponse` 接口（参考 ybase）
   - [ ] 创建 `Message` 数据类
   - [ ] 创建 `ThrowableBean` 数据类
   - [ ] 创建 `UIState` sealed class（页面状态）
   - [ ] 创建 `EventFlow` 事件机制（替代 LiveEventBus）
   - [ ] 创建 `DefaultLifecycleObserver` 实现的 SubscribeLifecycle

2. **Phase 2: BaseViewModel 实现**
   - [ ] 创建 `BaseViewModel<BR>` 类
   - [ ] 实现 UIChange 内部类（使用 EventFlow）
   - [ ] 实现 launchUI/launchFlow 协程方法
   - [ ] 实现 launchOnlyResult 网络请求封装
   - [ ] 集成 ExceptionHandle 异常处理
   - [ ] **禁止使用反射注入 Repository**

3. **Phase 3: BaseRepository 实现**
   - [ ] 在 common-network 创建 `BaseRepository`
   - [ ] 提供 Retrofit API 访问入口
   - [ ] 可选：提供泛型 API 方法

4. **Phase 4: ViewModelFactory 实现**
   - [ ] 使用 `ViewModelProvider.AndroidViewModelFactory`
   - [ ] 支持带 Repository 参数的 ViewModel

5. **Phase 5: common-ui 补充**
   - [ ] 为 BaseFragment 添加 ViewModel 泛型支持
   - [ ] 集成 ViewModelFactory
   - [ ] 添加 defUI 观察方法

### 9.2 关键代码参考

**IBaseResponse 接口** (common-core):
```kotlin
interface IBaseResponse<T> {
    fun code(): Int
    fun msg(): String
    fun data(): T?
    fun isSuccess(): Boolean = code() == 0
}
```

**EventFlow 实现** (common-core):
```kotlin
class SingleEvent<T> {
    private val flow = MutableSharedFlow<T>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun emit(value: T) { flow.tryEmit(value) }
    fun asFlow(): SharedFlow<T> = flow.asSharedFlow()
}
```

### 9.3 禁止事项

- ❌ 不要使用 `@OnLifecycleEvent` 注解
- ❌ 不要使用反射注入 Repository
- ❌ 不要引入 LiveEventBus
- ❌ 不要在 common-core 中依赖 common-ui

---

## 10. 给 3号窗口的实施指引

### 10.1 任务列表

1. **Phase 1: 依赖配置**
   - [ ] 在 `libs.versions.toml` 添加 Koin（如用户选择）
   - [ ] 在 `libs.versions.toml` 添加 Navigation（如需要）
   - [ ] 在 `libs.versions.toml` 添加 Paging 3（如需要）

2. **Phase 2: 模块 Gradle 更新**
   - [ ] common-core: 添加 Lifecycle 依赖
   - [ ] common-ui: 确认 ViewBinding 已启用
   - [ ] 按需添加新依赖

3. **Phase 3: app 模块集成**
   - [ ] 更新 app/build.gradle.kts 依赖
   - [ ] 创建 Demo ViewModel
   - [ ] 创建 Demo Repository
   - [ ] 创建 Demo 页面验证架构

4. **Phase 4: 最终验证**
   - [ ] 执行 `./gradlew build` 确认编译通过
   - [ ] 执行 `./gradlew test` 运行单元测试
   - [ ] 运行 app 确认功能正常

### 10.2 Gradle 修改规范

```kotlin
// 在 libs.versions.toml 添加新依赖示例
[versions]
koin = "3.5.6"
navigation = "2.8.5"

[libraries]
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
navigation-fragment = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
navigation-ui = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }
```

### 10.3 禁止事项

- ❌ 不要并行修改多个 Gradle 文件
- ❌ 不要在 2号窗口完成前修改 app 模块
- ❌ 不要升级已有依赖版本（除非有明确 bug）
- ❌ 不要在 lib 模块中添加业务代码

---

## 附录 A: 文件路径速查

### 当前项目 (common-lib)

```
/Users/hoyn/Documents/AndroidWorkplace/common-lib/
├── gradle/libs.versions.toml          # 版本目录
├── settings.gradle.kts                # 模块配置
├── build.gradle.kts                   # 根 Gradle
├── common-core/
│   └── build.gradle.kts
├── common-utils/
│   └── build.gradle.kts
├── common-network/
│   └── build.gradle.kts
├── common-ui/
│   └── build.gradle.kts
├── common-image/
│   └── build.gradle.kts
└── app/
    └── build.gradle.kts
```

### 来源项目 (dzbl-android)

```
/Users/hoyn/Downloads/dzbl-android/
├── ybase/src/main/java/com/screson/baselibrary/
│   ├── base/                          # 主要迁移来源
│   │   ├── BaseActivity.kt
│   │   ├── BaseFragment.kt
│   │   ├── BaseViewModel.kt
│   │   ├── BaseRepository.kt
│   │   ├── IBaseResponse.kt
│   │   ├── ViewModelFactory.kt
│   │   ├── SubscribeLifecycle.kt
│   │   ├── Clazz.kt
│   │   └── ThrowableBean.kt
│   └── http/
│       ├── event/
│       │   ├── SingleLiveEvent.kt
│       │   └── Message.kt
│       ├── UpdateGlobal.kt
│       └── ThrowableReportGlobal.kt
└── basic/                             # 非主要来源
    └── src/main/java/com/tencent/liteav/basic/
```

---

## 附录 B: 协作检查清单

在开始工作前，各窗口应确认：

### 2号窗口
- [ ] 已阅读完整 AI_HANDOFF.md
- [ ] 确认 common-core、common-network、common-ui 的修改权限
- [ ] 确认不修改任何 Gradle 文件
- [ ] 确认使用现代替代方案（非 LiveEventBus/反射）

### 3号窗口
- [ ] 已阅读完整 AI_HANDOFF.md
- [ ] 确认 Gradle 文件修改权限
- [ ] 等待 2号窗口完成架构实现
- [ ] 确认 app 模块最后独占修改

---

---

## 11. 3号窗口实施记录 - 版本升级与架构选项

> **追加时间**: 2026-03-13
> **追加窗口**: 3号窗口（版本升级、架构选项整合、Demo）

### 11.1 版本升级计划

#### 当前版本审计

| 依赖 | 当前版本 | 最新稳定版 | 升级建议 | 风险等级 |
|------|----------|-----------|----------|----------|
| **AGP** | 9.0.0-rc01 | **9.2.0** | ✅ 建议升级 | 低 - 正式版已发布 |
| **Kotlin** | 2.1.0 | **2.3.0** | ✅ 建议升级 | 低 - 向后兼容 |
| **Lifecycle** | 2.8.7 | **2.9.2** | ✅ 建议升级 | 低 |
| **Activity KTX** | 1.9.3 | 1.10.1 | ✅ 建议升级 | 低 |
| **Fragment KTX** | 1.8.5 | 1.8.7 | ⚠️ 可选升级 | 低 |
| **Retrofit** | 2.11.0 | **3.0.0** | ⚠️ 谨慎升级 | 中 - 重大更新 |
| **OkHttp** | 4.12.0 | 4.12.0 | ✅ 保持 | - |
| **Coroutines** | 1.9.0 | 1.10.1 | ✅ 建议升级 | 低 |
| **MMKV** | 2.3.0 | 2.3.0 | ✅ 保持 | - |
| **Glide** | 4.16.0 | 4.16.0 | ✅ 保持 | - |

#### 需新增的依赖

| 依赖 | 推荐版本 | 用途 | 放置模块 |
|------|----------|------|----------|
| **Koin** | 3.5.6 (LTS) | DI 框架 | common-core |
| **Navigation** | 2.9.7 | 导航组件 | common-ui / app |
| **Paging 3** | 3.3.6 | 分页加载 | common-ui (可选) |

#### 升级顺序

```
1. AGP 9.0.0-rc01 → 9.2.0     (先升级构建工具)
2. Kotlin 2.1.0 → 2.3.0       (语言版本)
3. Coroutines 1.9.0 → 1.10.1  (协程版本)
4. Lifecycle 2.8.7 → 2.9.2    (架构组件)
5. Activity 1.9.3 → 1.10.1    (与 Lifecycle 对齐)
6. 新增 Koin 3.5.6            (DI)
7. 新增 Navigation 2.9.7      (导航)
8. 新增 Paging 3.3.6 (可选)   (分页)
9. Retrofit 2.11.0 → 3.0.0    (最后升级，风险最高)
```

#### 升级风险分析

| 升级项 | 风险 | 缓解措施 |
|--------|------|----------|
| AGP rc → stable | 低 | 正式版更稳定，API 变化小 |
| Kotlin 2.1 → 2.3 | 低 | 向后兼容，主要新增特性 |
| Retrofit 2.x → 3.0 | **中** | 升级到 OkHttp 4.12 Kotlin 版本，API 有变化 |
| 新增 Koin | 低 | 独立模块，不影响现有代码 |
| 新增 Navigation | 低 | 仅 app 模块使用 |

---

### 11.2 用户需要选择的架构项

> **重要**: 以下选项需要用户确认后，3号窗口才会执行版本配置。

#### 11.2.1 事件机制 (Event Bus 替代)

| 选项 | 推荐度 | 说明 |
|------|--------|------|
| ✅ **Kotlin SharedFlow** | **强烈推荐** | 官方协程方案，轻量，无第三方依赖 |
| Kotlin Channel | 可选 | 更像队列，适合生产-消费模式 |
| RxJava | 不推荐 | 重量级，增加包体积 |
| LiveEventBus (旧) | ❌ 禁止 | 已废弃 |

**推荐实现** (由 2号窗口在 common-core 中实现):
```kotlin
// 单次消费事件
class SingleEvent<T> {
    private val flow = MutableSharedFlow<T>(
        replay = 0,           // 不重放
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun emit(value: T) = flow.tryEmit(value)
    fun asFlow(): SharedFlow<T> = flow.asSharedFlow()
}

// 全局事件总线
object GlobalEvents {
    private val _events = MutableSharedFlow<Any>(extraBufferCapacity = 16)
    val events: SharedFlow<Any> = _events.asSharedFlow()

    suspend fun emit(event: Any) = _events.emit(event)
    fun tryEmit(event: Any) = _events.tryEmit(event)
}
```

#### 11.2.2 页面状态模式

| 选项 | 推荐度 | 说明 |
|------|--------|------|
| ✅ **Sealed Class + StateFlow** | **强烈推荐** | Kotlin 原生，类型安全，与 ViewModel 完美配合 |

**推荐实现** (由 2号窗口在 common-core 中实现):
```kotlin
sealed class UIState<out T> {
    object Idle : UIState<Nothing>()
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val code: Int, val message: String) : UIState<Nothing>()
    object Empty : UIState<Nothing>()
}
```

#### 11.2.3 DI (依赖注入)

| 选项 | 推荐度 | 说明 |
|------|--------|------|
| ✅ **Koin 3.5.6** | **推荐** | 轻量 Kotlin DSL，学习成本低，无代码生成 |
| Hilt | 可选 | Google 官方，但配置复杂，需要注解处理 |
| 手动注入 | 不推荐 | 样板代码多，维护困难 |
| 反射注入 (旧) | ❌ 禁止 | 性能差，不安全 |

**Koin 配置示例** (由 3号窗口在 libs.versions.toml 中配置):
```kotlin
// libs.versions.toml
[versions]
koin = "3.5.6"

[libraries]
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-viewmodel = { group = "io.insert-koin", name = "koin-androidx-viewmodel", version.ref = "koin" }
```

#### 11.2.4 Navigation

| 选项 | 推荐度 | 说明 |
|------|--------|------|
| ✅ **Navigation Component 2.9.7** | **推荐** | Google 官方，支持 Fragment/Activity/Compose |
| 自定义路由 | 不推荐 | 维护成本高 |

**Navigation 配置** (由 3号窗口配置):
```kotlin
// libs.versions.toml
[versions]
navigation = "2.9.7"

[libraries]
navigation-fragment = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
navigation-ui = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }
```

#### 11.2.5 Paging

| 选项 | 推荐度 | 说明 |
|------|--------|------|
| ✅ **Paging 3.3.6** | **推荐（如需分页）** | 官方方案，与 Room/Network 配合良好 |
| 手动分页 | 可选 | 简单场景够用 |

**Paging 配置** (由 3号窗口配置，可选):
```kotlin
// libs.versions.toml
[versions]
paging = "3.3.6"

[libraries]
paging-runtime = { group = "androidx.paging", name = "paging-runtime-ktx", version.ref = "paging" }
```

#### 11.2.6 Compose 预留方式

| 选项 | 推荐度 | 说明 |
|------|--------|------|
| ✅ **预留 common-compose 模块** | **推荐** | 独立模块，不影响现有 View 体系 |
| 混合在 common-ui | 不推荐 | 耦合度高 |

**Compose 预留结构**:
```
common-compose/           # 未来创建
├── build.gradle.kts      # 配置 Compose
└── src/main/java/
    └── com/hoyn/common/compose/
        ├── theme/        # 主题
        ├── components/   # 通用组件
        └── extensions/   # 扩展函数
```

**当前阶段**: 暂不创建，仅在架构上预留扩展点。

---

### 11.3 架构选项决策汇总

| 架构项 | 推荐选择 | 版本 | 理由 |
|--------|----------|------|------|
| 事件机制 | SharedFlow | - | 官方方案，轻量，无第三方依赖 |
| 页面状态 | Sealed Class + StateFlow | - | 类型安全，Kotlin 原生 |
| DI | **Koin** | 3.5.6 | 轻量，Kotlin DSL，学习成本低 |
| Navigation | **Navigation Component** | 2.9.7 | 官方方案，生态完整 |
| Paging | **Paging 3** (可选) | 3.3.6 | 官方方案，如需分页则启用 |
| Compose | **预留独立模块** | - | 暂不引入，保持架构可扩展 |

---

### 11.4 app Demo 方案

#### 11.4.1 Demo 结构设计

```
app/
├── src/main/java/com/hoyn/common/lib/
│   ├── MainApplication.kt          # Application 入口
│   ├── MainActivity.kt             # Demo 列表入口
│   │
│   ├── demo/                       # Demo 示例页面
│   │   ├── network/                # 网络请求示例
│   │   │   ├── NetworkDemoActivity.kt
│   │   │   ├── NetworkDemoViewModel.kt
│   │   │   └── NetworkDemoRepository.kt
│   │   │
│   │   ├── ui/                     # UI 组件示例
│   │   │   ├── ToastDemoActivity.kt
│   │   │   ├── PermissionDemoActivity.kt
│   │   │   └── StatusBarDemoActivity.kt
│   │   │
│   │   └── utils/                  # 工具类示例
│   │       ├── MMKVDemoActivity.kt
│   │       └── LogDemoActivity.kt
│   │
│   └── compose/                    # Compose 预留目录 (暂空)
│       └── .gitkeep
│
└── src/main/res/
    ├── layout/
    │   ├── activity_main.xml       # Demo 列表
    │   ├── item_demo.xml           # Demo 列表项
    │   └── activity_network_demo.xml
    └── navigation/
        └── nav_graph.xml           # 导航图 (预留)
```

#### 11.4.2 MainActivity 设计

**功能**: 作为 Demo 列表入口，展示所有可用示例

**UI 结构**:
- RecyclerView 展示 Demo 列表
- 点击跳转到对应 Demo 页面
- 支持 Navigation 组件导航

**Demo 列表项**:
| Demo 名称 | 功能 | 涉及模块 |
|-----------|------|----------|
| 网络请求示例 | GET/POST 请求，错误处理，Loading 状态 | common-network, common-core |
| Toast 示例 | 各种 Toast 样式 | common-ui |
| 权限请求示例 | 运行时权限请求 | common-ui |
| 状态栏示例 | 状态栏沉浸式/颜色设置 | common-ui |
| MMKV 示例 | 键值存储 | common-utils |
| 日志示例 | 日志输出 | common-utils |
| 图片加载示例 | Glide 加载图片 | common-image |

#### 11.4.3 网络列表示例页

**NetworkDemoActivity** 功能:
- 展示网络请求完整流程
- 使用 ViewModel + Repository 模式
- 展示 Loading/Success/Error 状态
- 使用 RecyclerView 展示列表数据
- 支持下拉刷新

**API 示例** (使用公开 API):
```kotlin
// 使用 JSONPlaceholder 或类似公开 API
interface DemoApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): User
}
```

#### 11.4.4 简单示例页

| 示例页 | 展示内容 |
|--------|----------|
| ToastDemoActivity | 短/长 Toast，自定义位置 Toast |
| PermissionDemoActivity | 相机/存储权限请求流程 |
| StatusBarDemoActivity | 状态栏颜色/透明度设置 |
| MMKVDemoActivity | 存储/读取/删除操作 |
| LogDemoActivity | 不同级别日志输出 |

#### 11.4.5 Compose 混合接入预留

**预留结构**:
```kotlin
// 未来 Compose Demo 示例 (暂不实现)
// app/src/main/java/com/hoyn/common/lib/compose/
//
// @Composable
// fun ComposeDemoScreen() {
//     // Compose UI 实现
// }
//
// // 在 Activity 中使用
// class ComposeDemoActivity : ComponentActivity() {
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         setContent { ComposeDemoScreen() }
//     }
// }
```

**混合导航预留**:
```kotlin
// 未来在 nav_graph.xml 中添加 Compose 目的地
// <fragment
//     android:id="@+id/composeDemo"
//     android:name="com.hoyn.common.lib.compose.ComposeDemoFragment" />
```

---

### 11.5 实施前置条件

**3号窗口等待 2号窗口完成以下内容后方可执行版本配置和 Demo 开发**:

- [ ] common-core 模块完成 BaseViewModel 实现
- [ ] common-core 模块完成 UIState/EventFlow 事件机制
- [ ] common-network 模块完成 BaseRepository 实现
- [ ] common-ui 模块完成 BaseFragment 的 ViewModel 支持
- [ ] AI_HANDOFF.md 中标注"接口已稳定"

---

### 11.6 版本变更清单 (待执行)

> **注意**: 以下变更需等待用户确认架构选项后，由 3号窗口执行。

#### libs.versions.toml 变更

```toml
[versions]
# 升级
agp = "9.2.0"                    # 从 9.0.0-rc01 升级
kotlin = "2.3.0"                 # 从 2.1.0 升级
coroutines = "1.10.1"            # 从 1.9.0 升级
lifecycle = "2.9.2"              # 从 2.8.7 升级
activityKtx = "1.10.1"           # 从 1.9.3 升级
fragmentKtx = "1.8.7"            # 从 1.8.5 升级

# 新增
koin = "3.5.6"
navigation = "2.9.7"
paging = "3.3.6"

[libraries]
# 新增
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-viewmodel = { group = "io.insert-koin", name = "koin-androidx-viewmodel", version.ref = "koin" }
navigation-fragment = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
navigation-ui = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }
paging-runtime = { group = "androidx.paging", name = "paging-runtime-ktx", version.ref = "paging" }
```

#### 各模块依赖变更

| 模块 | 新增依赖 |
|------|----------|
| common-core | koin-android, lifecycle-viewmodel |
| common-ui | navigation-fragment, navigation-ui, paging-runtime (可选) |
| app | navigation-fragment, navigation-ui, koin-android |

---

**文档结束**

> 如需补充或修正，只允许修改本文件 `docs/AI_HANDOFF.md`。

---

## 12. 2号窗口实施记录

> **实施时间**: 2026-03-13
> **实施窗口**: 2号窗口（架构实现）
> **编译状态**: ✅ 通过

### 12.1 新增类型汇总

#### common-core 模块

| 文件 | 类型 | 说明 |
|------|------|------|
| `IBaseResponse.kt` | interface | 通用 API 响应接口，所有响应数据类应实现此接口 |
| `Message.kt` | data class | 消息事件载体，用于全局事件通信 |
| `ThrowableBean.kt` | data class | 异常信息载体，用于封装错误信息 |
| `UIState.kt` | sealed class | UI 状态封装（Loading/Success/Error/Empty），支持 Compose 预留 |

#### common-network 模块

| 文件 | 类型 | 说明 |
|------|------|------|
| `BaseRepository.kt` | abstract class | 数据层基类，提供 Retrofit API 访问入口 |
| `ApiResponse.kt` | data class | 已修改，实现 IBaseResponse 接口 |

#### common-ui 模块

| 文件 | 类型 | 说明 |
|------|------|------|
| `base/BaseViewModel.kt` | abstract class | ViewModel 基类，提供协程管理、网络请求封装、UI 事件分发 |
| `base/ViewModelFactory.kt` | class | ViewModel 工厂，支持自定义创建逻辑 |
| `event/SingleLiveEvent.kt` | class | 一次性事件流，使用 SharedFlow 替代 LiveData |
| `event/EventBus.kt` | object | 全局事件总线，替代 LiveEventBus |
| `event/LifecycleObserver.kt` | class | 生命周期观察者，使用 DefaultLifecycleObserver |
| `ext/ViewModelExtensions.kt` | extension functions | ViewModel 扩展方法，提供 UI 事件观察 |

### 12.2 修改的模块

| 模块 | 修改内容 |
|------|----------|
| common-core | 新增 IBaseResponse、Message、ThrowableBean、UIState |
| common-network | 新增 BaseRepository，修改 ApiResponse 实现 IBaseResponse |
| common-ui | 新增 BaseViewModel、ViewModelFactory、事件机制、扩展方法 |

### 12.3 架构限制说明

> **重要**: 由于 common-core 没有 Lifecycle 和 Coroutines 依赖，以下类暂时放在 common-ui：

| 原计划位置 | 定际位置 | 原因 | 后续建议 |
|------------|----------|------|----------|
| common-core | **common-ui** | BaseViewModel 依赖 Lifecycle/ViewModel | 3号窗口为 common-core 添加 Lifecycle 依赖后可迁移 |
| common-core | **common-ui** | EventFlow 依赖 Coroutines | 3号窗口为 common-core 添加 Coroutines 依赖后可迁移 |

### 12.4 给 3号窗口的 app 接入说明

#### 12.4.1 创建 ViewModel

```kotlin
// 1. 定义 Repository
class UserRepository : BaseRepository<UserApi>() {
    override val api: UserApi by lazy { createApi("https://api.example.com", UserApi::class.java) }
}

// 2. 定义 ViewModel
class UserViewModel : BaseViewModel<UserRepository>() {
    override val repository: UserRepository by lazy { UserRepository() }

    // 可选：重写异常处理器以集成 ExceptionHandle
    override fun handleException(throwable: Throwable): ThrowableBean {
        val handled = ExceptionHandle.handleException(throwable)
        return ThrowableBean(handled.code, handled.errMsg)
    }

    // 业务方法
    fun getUser(id: Int) {
        launchOnlyResult(
            block = { repository.api.getUser(id) },
            success = { user -> /* 处理成功 */ },
            error = { e -> /* 处理错误 */ }
        )
    }
}
```

#### 12.4.2 在 Activity/Fragment 中观察事件

```kotlin
class UserActivity : BaseActivity<ActivityUserBinding>() {

    private val viewModel: UserViewModel by lazy { ViewModelFactory.create(this) { UserViewModel() } }

    override fun initView(savedInstanceState: Bundle?) {
        // 观察 UI 事件
        observeAllUIEvents<UserViewModel>(
            onToast = { ToastUtils.show(it) },
            onShowDialog = { /* 显示加载框 */ },
            onDismissDialog = { /* 隐藏加载框 */ },
            onError = { /* 处理错误 */ }
        )
    }
}
```

#### 12.4.3 使用全局事件总线

```kotlin
// 发送消息
GlobalEventBus.sendMessage(Message(code = 1001, msg = "登录过期"))

// 观察消息
observeGlobalMessage { message ->
    when (message.code) {
        1001 -> { /* 跳转登录页 */ }
    }
}
```

### 12.5 已稳定的接口

| 接口/类 | 位置 | 稳定性 | 说明 |
|--------|------|--------|------|
| `IBaseResponse<T>` | common-core | ✅ 稳定 | 可安全使用 |
| `Message` | common-core | ✅ 稳定 | 可安全使用 |
| `ThrowableBean` | common-core | ✅ 稳定 | 可安全使用 |
| `UIState<T>` | common-core | ✅ 稳定 | 可安全使用 |
| `BaseRepository<T>` | common-network | ✅ 稳定 | 可安全使用 |
| `ApiResponse<T>` | common-network | ✅ 稳定 | 已实现 IBaseResponse |
| `BaseViewModel<R>` | common-ui | ⚠️ 暂时位置 | 功能稳定，但建议后续迁移到 common-core |
| `SingleLiveEvent<T>` | common-ui | ✅ 稳定 | 可安全使用 |
| `GlobalEventBus` | common-ui | ✅ 稳定 | 可安全使用 |
| `ViewModelFactory` | common-ui | ✅ 稳定 | 可安全使用 |

### 12.6 绝对不能被 3号窗口改动的接口

| 接口/类 | 原因 |
|--------|------|
| `IBaseResponse` 接口签名 | 已被 ApiResponse 实现，修改会破坏兼容性 |
| `UIState` sealed class 结构 | 子类可能已使用模式匹配，修改会破坏编译 |
| `Message` 数据类字段 | 已用于事件通信，修改字段会破坏兼容性 |
| `ThrowableBean` 数据类字段 | 已用于错误处理，修改字段会破坏兼容性 |
| `BaseViewModel<R>` 抽象方法 | 子类需要实现 `repository` 和 `handleException`，修改签名会破坏子类 |
| `BaseViewModel.UIChange` 内部类结构 | 已用于事件分发，修改会破坏兼容性 |
| `SingleLiveEvent<T>` 构造参数 | SharedFlow 配置已优化，修改会影响事件语义 |
| `GlobalEventBus` object 结构 | 全局单例，修改会影响所有观察者 |

### 12.7 关键设计决策

#### 12.7.1 不使用反射注入 Repository

原 ybase 的 BaseViewModel 使用反射注入 Repository：
```kotlin
// ❌ 旧方式（已禁止）
val repository: BR by lazy { Clazz.getClass<BR>(this).newInstance() }
```

新方式要求子类显式声明 Repository：
```kotlin
// ✅ 新方式
protected abstract val repository: R
```

**原因**: 反射性能差，类型不安全，难以追踪依赖关系。

#### 12.7.2 使用 SharedFlow 替代 LiveData

原 ybase 使用 SingleLiveEvent (基于 MutableLiveData):
```kotlin
// ❌ 旧方式
inner class UIChange {
    val showDialog by lazy { SingleLiveEvent<String>() }
}
```

新方式使用 SharedFlow:
```kotlin
// ✅ 新方式
inner class UIChange {
    val showDialog by lazy { SingleLiveEvent<String>() } // 内部使用 SharedFlow
}
```

**原因**: SharedFlow 更轻量，无 LiveData 限制，协程原生支持。

#### 12.7.3 异常处理可子类可重写

BaseViewModel 提供默认异常处理，子类可重写以集成项目特定的 ExceptionHandle：
```kotlin
protected open fun handleException(throwable: Throwable): ThrowableBean {
    return ThrowableBean(errMsg = throwable.message ?: "Unknown error")
}

// 子类重写
override fun handleException(throwable: Throwable): ThrowableBean {
    val handled = ExceptionHandle.handleException(throwable)
    return ThrowableBean(handled.code, handled.errMsg)
}
```

**原因**: 不同项目可能有不同的异常处理策略，提供扩展点。

---

> **2号窗口实施完成，可移交 3号窗口执行版本配置和 app 集成**

---

## 13. 3号窗口实施记录 - 版本升级与 Demo

> **实施时间**: 2026-03-13
> **实施窗口**: 3号窗口

### 13.1 版本升级执行

#### 已完成的版本升级

| 依赖 | 升级前 | 升级后 | 状态 |
|------|--------|--------|------|
| AGP | 9.0.0-rc01 | **9.1.0** | ✅ 完成 |
| Gradle | 9.1.0 | **9.3.1** | ✅ 完成 |
| Kotlin | 2.1.0 | **2.3.0** | ✅ 完成 |
| Coroutines | 1.9.0 | **1.10.1** | ✅ 完成 |
| Lifecycle | 2.8.7 | **2.9.2** | ✅ 完成 |
| Activity KTX | 1.9.3 | **1.10.1** | ✅ 完成 |
| Fragment KTX | 1.8.5 | **1.8.7** | ✅ 完成 |
| Navigation | - | **2.9.7** | ✅ 新增 |
| Retrofit | 2.11.0 | 2.11.0 | ⏸️ 保持 |
| OkHttp | 4.12.0 | 4.12.0 | ⏸️ 保持 |

#### 未添加的依赖（用户选择）

| 依赖 | 原因 |
|------|------|
| Koin | 用户不需要 DI |
| Paging 3 | 用户不需要分页 |

#### 版本升级过程中修复的编译问题

1. **BaseViewModel.kt 重复类定义**: 删除了重复的类定义，2. **ViewModelExtensions.kt 泛型问题**: 重新设计了扩展方法，   让它们接收 ViewModel 实例作为参数
3. **SingleLiveEvent.emit() 无参数版本**: 添加了无参数的 emit() 方法
4. **BaseFragment.kt 未使用的导入**: 删除了对 common-network 的引用

### 13.2 common-core 依赖更新

为 common-core 添加了 Lifecycle 和 Coroutines 依赖：

```kotlin
// common-core/build.gradle.kts
dependencies {
    // Lifecycle (for BaseViewModel migration)
    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.lifecycle.viewmodel)

    // Coroutines (for EventFlow)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
}
```

### 13.3 ToastUtils 扩展

添加了便捷的 show() 和 showLong() 方法:

```kotlin
object ToastUtils {
    /**
     * 显示 Toast（底部显示）
     */
    fun show(mContext: Context?, msg: String?, duration: Int = DURATION_SHORT) {
        showBottomToast(mContext, msg, duration)
    }

    /**
     * 显示长 Toast
     */
    fun showLong(mContext: Context?, msg: String?) {
        showBottomToast(mContext, msg, DURATION_LONG)
    }
    // ...
}
```

### 13.4 App Demo 实现

#### Demo 结构

```
app/
├── src/main/java/com/hoyn/common/lib/
│   ├── MainApplication.kt          # Application 入口
│   ├── MainActivity.kt             # Demo 列表入口
│   │
│   └── demo/
│       ├── ToastDemoActivity.kt    # Toast 示例
│       ├── NetworkDemoActivity.kt  # 网络请求示例
│       ├── MmkvDemoActivity.kt     # MMKV 示例
│       ├── LogDemoActivity.kt      # 日志示例
│       └── StatusBarDemoActivity.kt # 状态栏示例
```

#### Demo 列表

| Demo 名称 | 功能 | 涉及模块 |
|-----------|------|----------|
| Toast 示例 | 短/长/中间 Toast | common-ui |
| 网络请求示例 | ViewModel + Retrofit + RecyclerView | common-network, common-ui |
| MMKV 示例 | 存储/读取/删除 | common-utils |
| 日志示例 | 不同级别日志 | common-utils |
| 状态栏示例 | 状态栏颜色设置 | common-ui |

#### 网络请求示例特点

- 使用 JSONPlaceholder 公开 API
- 简化版 ViewModel（不使用 Repository 和 BaseViewModel）
- 直接使用 StateFlow 管理状态
- 展示 Loading/Error/Success 状态

### 13.5 Compose 预留

在 app 模块中预留了 Compose 目录结构：

```
app/src/main/java/com/hoyn/common/lib/compose/
└── .gitkeep  # 预留目录，暂未实现
```

### 13.6 编译状态

✅ **BUILD SUCCESSFUL**

---

> **3号窗口实施完成，项目可正常编译运行**

### 13.1 用户架构决策确认

| 架构项 | 用户决策 | 版本 |
|--------|----------|------|
| 事件机制 | ✅ SharedFlow (已由2号窗口实现) | - |
| 页面状态 | ✅ Sealed Class + StateFlow (已由2号窗口实现) | - |
| DI | ❌ 不需要 | - |
| Navigation | ✅ 需要 | 2.9.7 |
| Paging | ❌ 不需要 | - |
| Compose | 预留 | 暂不引入 |
| Retrofit | ❌ 保持当前版本 | 2.11.0 |

### 13.2 版本升级计划 (已确认)

| 依赖 | 当前版本 | 升级到 | 状态 |
|------|----------|--------|------|
| AGP | 9.0.0-rc01 | 9.2.0 | ✅ 升级 |
| Kotlin | 2.1.0 | 2.3.0 | ✅ 升级 |
| Coroutines | 1.9.0 | 1.10.1 | ✅ 升级 |
| Lifecycle | 2.8.7 | 2.9.2 | ✅ 升级 |
| Activity KTX | 1.9.3 | 1.10.1 | ✅ 升级 |
| Fragment KTX | 1.8.5 | 1.8.7 | ✅ 升级 |
| Navigation | - | 2.9.7 | ✅ 新增 |
| Retrofit | 2.11.0 | 2.11.0 | ⏸️ 保持 |
| OkHttp | 4.12.0 | 4.12.0 | ⏸️ 保持 |
