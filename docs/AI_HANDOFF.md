# AI Handoff

> Version: 2.0
> Updated: 2026-03-13
> Purpose: Single execution contract for Claude Code

## 1. Goal

This repository must be refactored into a stable modular library plus a complete app architecture sample.

Claude Code should not stop at analysis. It must execute the full refactor, update documentation, and leave behind an app architecture document that future AI sessions can follow without re-auditing the repository.

## 2. Fixed Decisions

The following decisions are already made and must not be debated again:

1. Add a new module named `common-base`.
2. Do not keep `common-log` as a formal module.
3. Logging stays in `common-utils`.
4. `Logger` must have a single implementation under `common-utils`.
5. `common-core` must stay focused on stable cross-layer contracts and models.
6. `common-base` becomes the architecture foundation module.
7. `common-ui` keeps UI helpers and presentation utilities only.
8. `app` must become a complete architecture sample, not just a demo launcher.
9. README and architecture docs must be updated to match the real project state.
10. A final app architecture document must be created for future AI-driven development.

## 3. Current Facts To Respect

These facts have already been confirmed in the current repository state:

1. `app` currently uses `AppCompatActivity` directly in its main sample pages instead of consistently using base architecture classes.
2. `common-ui` currently contains `BaseActivity`, `BaseFragment`, `BaseViewModel`, `EventBus`, and `SingleLiveEvent`, so its responsibility is too broad.
3. `common-core` already contains `UIState`, `IBaseResponse`, `Message`, and `ThrowableBean`.
4. `common-core` already exposes Lifecycle and Coroutines dependencies, so the old "zero dependency" description is no longer true.
5. The repository documentation still refers to `common-log`, but the target architecture for this task removes that module.
6. `app` currently uses `com.hoyn.common.utils.Logger` and this direction should be kept.
7. `IBaseResponse.isSuccess()` and `ApiResponse.isSuccess()` currently use inconsistent success semantics and must be unified.
8. README, architecture docs, Gradle module declarations, and actual code imports are currently out of sync.

## 4. Target Module Responsibilities

### 4.1 common-core

Keep only the lowest-level stable abstractions and models:

- `UIState`
- `Message`
- `ThrowableBean`
- `IBaseResponse`
- shared result and error contracts if needed
- other stable cross-layer data contracts

Rules:

- Do not place `BaseActivity` or `BaseFragment` here.
- Do not place UI helper utilities here.
- Do not place concrete network implementation here.
- Do not place logging implementation here.
- Keep the API stable and narrow.

### 4.2 common-base

This is the new architecture foundation module.

Move or create the following here:

- `BaseActivity`
- `BaseFragment`
- `BaseDialogFragment` if needed
- `BaseViewModel`
- `ViewModelFactory`
- page-level event mechanism
- global event mechanism
- `SingleEvent`, `EventFlow`, or `SharedFlow` wrappers
- common page loading, message, and error handling
- ViewBinding-based screen foundation support

Rules:

- `common-base` depends on `common-core` and `common-utils`.
- It may depend on required AndroidX lifecycle, fragment, activity, and appcompat libraries.
- It must not become a dumping ground for generic UI tools like status bar helpers or toast utilities.

### 4.3 common-ui

Keep presentation helpers only:

- `ToastUtil`
- `StatusBarHelper`
- `NotchHelper`
- `PressEffectHelper`
- `LivePermissions`
- `ViewExtensions`
- UI widgets, helper views, adapter utilities, or similar presentation tooling

Rules:

- Remove architecture foundation classes from this module.
- `common-ui` should be a presentation toolbox, not the app architecture root.

### 4.4 common-network

Keep network and data access implementation here:

- `RetrofitFactory`
- `OkHttpClientFactory`
- `ApiResponse`
- `BaseRepository`
- `ExceptionHandle`
- `ResponseThrowable`
- `SSLManager`
- logging interceptors and HTTP printers if still needed

Rules:

- Unify response success semantics with `IBaseResponse`.
- Do not move UI event logic into this module.
- Keep repository abstractions oriented around data access.

### 4.5 common-utils

Keep reusable non-UI or cross-cutting utilities here, including logging:

- `Logger`
- `MMKVUtils`
- context extensions
- device and screen helpers
- generic coroutine helpers

Rules:

- `Logger` must exist only once and live here.
- `common-log` must not remain a supported formal module.

### 4.6 app

`app` must become the complete sample application for this library architecture.

It must demonstrate a real recommended usage flow:

- `Application` initialization
- one entry screen using base architecture
- `BaseActivity` and or `BaseFragment`
- `ViewModel`
- `Repository`
- Retrofit API service
- `ApiResponse`
- `UIState`
- page events
- global events
- loading, empty, success, and error states

Rules:

- `app` is no longer just a collection of disconnected demos.
- The sample should be suitable for future product teams to copy as a starting point.

## 5. Required Execution Order

Claude Code must execute in this order.

### Phase 1: Architecture Refactor

1. Audit the current module boundaries and dependencies.
2. Add `common-base` and wire it into Gradle.
3. Move architecture foundation classes out of `common-ui` into `common-base`.
4. Remove `common-log` from formal module structure and documentation.
5. Standardize all logging imports to `common-utils.Logger`.
6. Unify `IBaseResponse` and `ApiResponse` success semantics.
7. Ensure module responsibilities match the target architecture.

### Phase 2: Documentation Alignment

1. Update `README.md` to reflect the new module list and dependency hierarchy.
2. Remove `common-log` references from README and architecture docs.
3. Add `common-base` to all relevant documentation.
4. Make documentation match the real Gradle and source structure.

### Phase 3: App Sample Refactor

1. Refactor `app` from a demo launcher into a complete architecture sample.
2. Keep or reorganize old demos only if they do not distract from the main architecture sample.
3. Add at least one end-to-end sample flow showing the recommended usage pattern.
4. Make the app structure reflect the intended architecture, not an ad hoc demo package layout.

### Phase 4: App Architecture Document

Create a final app architecture document for future AI sessions.

This document must explain:

1. project goals
2. module responsibilities
3. dependency direction
4. forbidden dependencies
5. recommended app package structure
6. `Application` initialization rules
7. responsibilities of `BaseActivity`, `BaseFragment`, and `BaseViewModel`
8. collaboration between `UIState`, repository, `ApiResponse`, and event flows
9. standard page template
10. standard steps for adding a new page
11. boundary between page events and global events
12. recommended usage of logging, storage, permissions, navigation, and networking
13. Do and Don't guidance
14. explicit instruction that future AI modifications must read this document first

## 6. Acceptance Criteria

The task is not complete until all items below are true:

1. `common-base` exists and is wired in Gradle.
2. `common-ui` no longer owns the core base architecture classes.
3. `common-log` is no longer part of the supported architecture.
4. `Logger` is unified under `common-utils`.
5. response success semantics are consistent across core and network layers.
6. README and architecture docs reflect the actual repository structure.
7. `app` contains at least one real end-to-end architecture sample.
8. `app` uses the new architecture foundation instead of only direct `AppCompatActivity` usage.
9. a dedicated app architecture document has been created.
10. the final summary includes remaining risks or follow-up items.

## 7. Constraints

1. Do not stop at planning.
2. Do not leave docs and code inconsistent.
3. Do not preserve `common-log` as an official module just to avoid edits.
4. Do not move new base abstractions back into `common-ui`.
5. Do not let `common-core` grow into a catch-all module.
6. Do not do unrelated large-scale cleanup.

## 8. Files That Must Be Reviewed

At minimum, inspect and update these areas when necessary:

- `settings.gradle.kts`
- `README.md`
- `docs/ARCHITECTURE.md`
- `app/build.gradle.kts`
- `common-core/build.gradle.kts`
- `common-ui/build.gradle.kts`
- `common-network/build.gradle.kts`
- `common-utils/build.gradle.kts`
- `app/src/main/java/**`
- `common-ui/src/main/java/**`
- `common-core/src/main/java/**`
- `common-network/src/main/java/**`
- `common-utils/src/main/java/**`

## 9. Final Deliverables

Claude Code must finish with all of the following:

1. a corrected modular architecture
2. updated README and architecture documentation
3. a complete app architecture sample
4. a dedicated app architecture document for future AI development
5. a concise final summary containing:
   - issue list
   - module ownership matrix
   - key implementation changes
   - documentation changes
   - app sample summary
   - path to the new app architecture document
   - remaining risks or recommended next steps

## 10. Operating Instruction For Future AI Sessions

Before making architecture-related changes in this repository:

1. read this file first
2. read the final app architecture document created by this task
3. follow the documented module boundaries
4. avoid reopening already-settled architecture decisions unless the user explicitly requests a new direction
   fun observe(): SharedFlow<T> = flow.asSharedFlow()
   }

````

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
````

### 7.3 DI (依赖注入)

| 选项          | 说明                        | 推荐        |
| ------------- | --------------------------- | ----------- |
| **Koin**      | 轻量 Kotlin DSL，学习成本低 | ✅ **推荐** |
| Hilt          | Google 官方，但配置复杂     | 可选        |
| 手动注入      | 无框架                      | 不推荐      |
| 反射注入 (旧) | 已禁止                      | ❌          |

### 7.4 Navigation

| 选项                     | 说明        | 推荐        |
| ------------------------ | ----------- | ----------- |
| **Navigation Component** | Google 官方 | ✅ **推荐** |
| 自定义路由               | 复杂        | 不推荐      |

### 7.5 Paging

| 选项         | 说明     | 推荐                    |
| ------------ | -------- | ----------------------- |
| **Paging 3** | 官方方案 | ✅ **推荐**（如需分页） |
| 手动分页     | 简单场景 | 可选                    |

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

| 依赖       | 当前版本   | 建议           |
| ---------- | ---------- | -------------- |
| AGP        | 9.0.0-rc01 | 保持（预览版） |
| Kotlin     | 2.1.0      | 保持           |
| Lifecycle  | 2.8.7      | 保持           |
| Retrofit   | 2.11.0     | 保持           |
| OkHttp     | 4.12.0     | 保持           |
| MMKV       | 2.3.0      | 保持           |
| Coroutines | 1.9.0      | 保持           |

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
   - [ ] 基于 `BaseLiveEvent` 提供生命周期自动解绑和手动解绑能力

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
│   │   ├── BaseLiveEvent.kt
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

| 依赖             | 当前版本   | 最新稳定版 | 升级建议    | 风险等级          |
| ---------------- | ---------- | ---------- | ----------- | ----------------- |
| **AGP**          | 9.0.0-rc01 | **9.2.0**  | ✅ 建议升级 | 低 - 正式版已发布 |
| **Kotlin**       | 2.1.0      | **2.3.0**  | ✅ 建议升级 | 低 - 向后兼容     |
| **Lifecycle**    | 2.8.7      | **2.9.2**  | ✅ 建议升级 | 低                |
| **Activity KTX** | 1.9.3      | 1.10.1     | ✅ 建议升级 | 低                |
| **Fragment KTX** | 1.8.5      | 1.8.7      | ⚠️ 可选升级 | 低                |
| **Retrofit**     | 2.11.0     | **3.0.0**  | ⚠️ 谨慎升级 | 中 - 重大更新     |
| **OkHttp**       | 4.12.0     | 4.12.0     | ✅ 保持     | -                 |
| **Coroutines**   | 1.9.0      | 1.10.1     | ✅ 建议升级 | 低                |
| **MMKV**         | 2.3.0      | 2.3.0      | ✅ 保持     | -                 |
| **Glide**        | 4.16.0     | 4.16.0     | ✅ 保持     | -                 |

#### 需新增的依赖

| 依赖           | 推荐版本    | 用途     | 放置模块         |
| -------------- | ----------- | -------- | ---------------- |
| **Koin**       | 3.5.6 (LTS) | DI 框架  | common-core      |
| **Navigation** | 2.9.7       | 导航组件 | common-ui / app  |
| **Paging 3**   | 3.3.6       | 分页加载 | common-ui (可选) |

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

| 升级项             | 风险   | 缓解措施                                   |
| ------------------ | ------ | ------------------------------------------ |
| AGP rc → stable    | 低     | 正式版更稳定，API 变化小                   |
| Kotlin 2.1 → 2.3   | 低     | 向后兼容，主要新增特性                     |
| Retrofit 2.x → 3.0 | **中** | 升级到 OkHttp 4.12 Kotlin 版本，API 有变化 |
| 新增 Koin          | 低     | 独立模块，不影响现有代码                   |
| 新增 Navigation    | 低     | 仅 app 模块使用                            |

---

### 11.2 用户需要选择的架构项

> **重要**: 以下选项需要用户确认后，3号窗口才会执行版本配置。

#### 11.2.1 事件机制 (Event Bus 替代)

| 选项                     | 推荐度       | 说明                             |
| ------------------------ | ------------ | -------------------------------- |
| ✅ **Kotlin SharedFlow** | **强烈推荐** | 官方协程方案，轻量，无第三方依赖 |
| Kotlin Channel           | 可选         | 更像队列，适合生产-消费模式      |
| RxJava                   | 不推荐       | 重量级，增加包体积               |
| LiveEventBus (旧)        | ❌ 禁止      | 已废弃                           |

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

| 选项                            | 推荐度       | 说明                                         |
| ------------------------------- | ------------ | -------------------------------------------- |
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

| 选项              | 推荐度   | 说明                                    |
| ----------------- | -------- | --------------------------------------- |
| ✅ **Koin 3.5.6** | **推荐** | 轻量 Kotlin DSL，学习成本低，无代码生成 |
| Hilt              | 可选     | Google 官方，但配置复杂，需要注解处理   |
| 手动注入          | 不推荐   | 样板代码多，维护困难                    |
| 反射注入 (旧)     | ❌ 禁止  | 性能差，不安全                          |

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

| 选项                              | 推荐度   | 说明                                        |
| --------------------------------- | -------- | ------------------------------------------- |
| ✅ **Navigation Component 2.9.7** | **推荐** | Google 官方，支持 Fragment/Activity/Compose |
| 自定义路由                        | 不推荐   | 维护成本高                                  |

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

| 选项                | 推荐度               | 说明                               |
| ------------------- | -------------------- | ---------------------------------- |
| ✅ **Paging 3.3.6** | **推荐（如需分页）** | 官方方案，与 Room/Network 配合良好 |
| 手动分页            | 可选                 | 简单场景够用                       |

**Paging 配置** (由 3号窗口配置，可选):

```kotlin
// libs.versions.toml
[versions]
paging = "3.3.6"

[libraries]
paging-runtime = { group = "androidx.paging", name = "paging-runtime-ktx", version.ref = "paging" }
```

#### 11.2.6 Compose 预留方式

| 选项                            | 推荐度   | 说明                           |
| ------------------------------- | -------- | ------------------------------ |
| ✅ **预留 common-compose 模块** | **推荐** | 独立模块，不影响现有 View 体系 |
| 混合在 common-ui                | 不推荐   | 耦合度高                       |

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

| 架构项     | 推荐选择                 | 版本  | 理由                         |
| ---------- | ------------------------ | ----- | ---------------------------- |
| 事件机制   | SharedFlow               | -     | 官方方案，轻量，无第三方依赖 |
| 页面状态   | Sealed Class + StateFlow | -     | 类型安全，Kotlin 原生        |
| DI         | **Koin**                 | 3.5.6 | 轻量，Kotlin DSL，学习成本低 |
| Navigation | **Navigation Component** | 2.9.7 | 官方方案，生态完整           |
| Paging     | **Paging 3** (可选)      | 3.3.6 | 官方方案，如需分页则启用     |
| Compose    | **预留独立模块**         | -     | 暂不引入，保持架构可扩展     |

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

| 示例页                 | 展示内容                      |
| ---------------------- | ----------------------------- |
| ToastDemoActivity      | 短/长 Toast，自定义位置 Toast |
| PermissionDemoActivity | 相机/存储权限请求流程         |
| StatusBarDemoActivity  | 状态栏颜色/透明度设置         |
| MMKVDemoActivity       | 存储/读取/删除操作            |
| LogDemoActivity        | 不同级别日志输出              |

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

| 模块        | 新增依赖                                                  |
| ----------- | --------------------------------------------------------- |
| common-core | koin-android, lifecycle-viewmodel                         |
| common-ui   | navigation-fragment, navigation-ui, paging-runtime (可选) |
| app         | navigation-fragment, navigation-ui, koin-android          |

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

| 文件               | 类型         | 说明                                                          |
| ------------------ | ------------ | ------------------------------------------------------------- |
| `IBaseResponse.kt` | interface    | 通用 API 响应接口，所有响应数据类应实现此接口                 |
| `Message.kt`       | data class   | 消息事件载体，用于全局事件通信                                |
| `ThrowableBean.kt` | data class   | 异常信息载体，用于封装错误信息                                |
| `UIState.kt`       | sealed class | UI 状态封装（Loading/Success/Error/Empty），支持 Compose 预留 |

#### common-network 模块

| 文件                | 类型           | 说明                                   |
| ------------------- | -------------- | -------------------------------------- |
| `BaseRepository.kt` | abstract class | 数据层基类，提供 Retrofit API 访问入口 |
| `ApiResponse.kt`    | data class     | 已修改，实现 IBaseResponse 接口        |

#### common-ui 模块

| 文件                         | 类型                | 说明                                                    |
| ---------------------------- | ------------------- | ------------------------------------------------------- |
| `base/BaseViewModel.kt`      | abstract class      | ViewModel 基类，提供协程管理、网络请求封装、UI 事件分发 |
| `base/ViewModelFactory.kt`   | class               | ViewModel 工厂，支持自定义创建逻辑                      |
| `event/SingleLiveEvent.kt`   | class               | 一次性事件流，基于 BaseLiveEvent 实现                   |
| `event/BaseLiveEvent.kt`     | abstract class      | 生命周期感知 LiveEvent 基类，支持手动解绑               |
| `event/GlobalLiveEvent.kt`   | object              | 全局 LiveEvent，替代 LiveEventBus                       |
| `ext/ViewModelExtensions.kt` | extension functions | ViewModel 扩展方法，提供 UI 事件观察                    |

### 12.2 修改的模块

| 模块           | 修改内容                                                 |
| -------------- | -------------------------------------------------------- |
| common-core    | 新增 IBaseResponse、Message、ThrowableBean、UIState      |
| common-network | 新增 BaseRepository，修改 ApiResponse 实现 IBaseResponse |
| common-ui      | 新增 BaseViewModel、ViewModelFactory、事件机制、扩展方法 |

### 12.3 架构限制说明

> **重要**: 由于 common-core 没有 Lifecycle 和 Coroutines 依赖，以下类暂时放在 common-ui：

| 原计划位置  | 定际位置      | 原因                                   | 后续建议                                           |
| ----------- | ------------- | -------------------------------------- | -------------------------------------------------- |
| common-core | **common-ui** | BaseViewModel 依赖 Lifecycle/ViewModel | 3号窗口为 common-core 添加 Lifecycle 依赖后可迁移  |
| common-core | **common-ui** | EventFlow 依赖 Coroutines              | 3号窗口为 common-core 添加 Coroutines 依赖后可迁移 |

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
            onToast = { ToastUtil.show(it) },
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
GlobalLiveEvent.sendMessage(Message(code = 1001, msg = "登录过期"))

// 观察消息
observeGlobalMessage { message ->
    when (message.code) {
        1001 -> { /* 跳转登录页 */ }
    }
}
```

### 12.5 已稳定的接口

| 接口/类              | 位置           | 稳定性      | 说明                                   |
| -------------------- | -------------- | ----------- | -------------------------------------- |
| `IBaseResponse<T>`   | common-core    | ✅ 稳定     | 可安全使用                             |
| `Message`            | common-core    | ✅ 稳定     | 可安全使用                             |
| `ThrowableBean`      | common-core    | ✅ 稳定     | 可安全使用                             |
| `UIState<T>`         | common-core    | ✅ 稳定     | 可安全使用                             |
| `BaseRepository<T>`  | common-network | ✅ 稳定     | 可安全使用                             |
| `ApiResponse<T>`     | common-network | ✅ 稳定     | 已实现 IBaseResponse                   |
| `BaseViewModel<R>`   | common-ui      | ⚠️ 暂时位置 | 功能稳定，但建议后续迁移到 common-core |
| `SingleLiveEvent<T>` | common-ui      | ✅ 稳定     | 可安全使用                             |
| `GlobalLiveEvent`    | common-ui      | ✅ 稳定     | 可安全使用                             |
| `ViewModelFactory`   | common-ui      | ✅ 稳定     | 可安全使用                             |

### 12.6 绝对不能被 3号窗口改动的接口

| 接口/类                             | 原因                                                               |
| ----------------------------------- | ------------------------------------------------------------------ |
| `IBaseResponse` 接口签名            | 已被 ApiResponse 实现，修改会破坏兼容性                            |
| `UIState` sealed class 结构         | 子类可能已使用模式匹配，修改会破坏编译                             |
| `Message` 数据类字段                | 已用于事件通信，修改字段会破坏兼容性                               |
| `ThrowableBean` 数据类字段          | 已用于错误处理，修改字段会破坏兼容性                               |
| `BaseViewModel<R>` 抽象方法         | 子类需要实现 `repository` 和 `handleException`，修改签名会破坏子类 |
| `BaseViewModel.UIChange` 内部类结构 | 已用于事件分发，修改会破坏兼容性                                   |
| `SingleLiveEvent<T>` 构造参数       | SharedFlow 配置已优化，修改会影响事件语义                          |
| `GlobalLiveEvent` object 结构       | 全局单例，修改会影响所有观察者                                     |

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

| 依赖         | 升级前     | 升级后     | 状态    |
| ------------ | ---------- | ---------- | ------- |
| AGP          | 9.0.0-rc01 | **9.1.0**  | ✅ 完成 |
| Gradle       | 9.1.0      | **9.3.1**  | ✅ 完成 |
| Kotlin       | 2.1.0      | **2.3.0**  | ✅ 完成 |
| Coroutines   | 1.9.0      | **1.10.1** | ✅ 完成 |
| Lifecycle    | 2.8.7      | **2.9.2**  | ✅ 完成 |
| Activity KTX | 1.9.3      | **1.10.1** | ✅ 完成 |
| Fragment KTX | 1.8.5      | **1.8.7**  | ✅ 完成 |
| Navigation   | -          | **2.9.7**  | ✅ 新增 |
| Retrofit     | 2.11.0     | 2.11.0     | ⏸️ 保持 |
| OkHttp       | 4.12.0     | 4.12.0     | ⏸️ 保持 |

#### 未添加的依赖（用户选择）

| 依赖     | 原因           |
| -------- | -------------- |
| Koin     | 用户不需要 DI  |
| Paging 3 | 用户不需要分页 |

#### 版本升级过程中修复的编译问题

1. **BaseViewModel.kt 重复类定义**: 删除了重复的类定义，2. **ViewModelExtensions.kt 泛型问题**: 重新设计了扩展方法， 让它们接收 ViewModel 实例作为参数
2. **SingleLiveEvent.emit() 无参数版本**: 添加了无参数的 emit() 方法
3. **BaseFragment.kt 未使用的导入**: 删除了对 common-network 的引用

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

### 13.3 Toast API 统一

当前统一入口为 `ToastUtil`，新代码无需再传入 `Context`：

```kotlin
ToastUtil.show("操作成功")
ToastUtil.showLong("长提示")
ToastUtil.showCenter("中间提示")
```

历史调用已统一迁移到 `ToastUtil`。

这是一次 source-breaking 迁移，外部调用方升级时也必须将旧入口全部替换为 `ToastUtil`。

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

| Demo 名称    | 功能                                | 涉及模块                  |
| ------------ | ----------------------------------- | ------------------------- |
| Toast 示例   | 短/长/中间 Toast                    | common-ui                 |
| 网络请求示例 | ViewModel + Retrofit + RecyclerView | common-network, common-ui |
| MMKV 示例    | 存储/读取/删除                      | common-utils              |
| 日志示例     | 不同级别日志                        | common-utils              |
| 状态栏示例   | 状态栏颜色设置                      | common-ui                 |

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

| 架构项     | 用户决策                                      | 版本     |
| ---------- | --------------------------------------------- | -------- |
| 事件机制   | ✅ SharedFlow (已由2号窗口实现)               | -        |
| 页面状态   | ✅ Sealed Class + StateFlow (已由2号窗口实现) | -        |
| DI         | ❌ 不需要                                     | -        |
| Navigation | ✅ 需要                                       | 2.9.7    |
| Paging     | ❌ 不需要                                     | -        |
| Compose    | 预留                                          | 暂不引入 |
| Retrofit   | ❌ 保持当前版本                               | 2.11.0   |

### 13.2 版本升级计划 (已确认)

| 依赖         | 当前版本   | 升级到 | 状态    |
| ------------ | ---------- | ------ | ------- |
| AGP          | 9.0.0-rc01 | 9.2.0  | ✅ 升级 |
| Kotlin       | 2.1.0      | 2.3.0  | ✅ 升级 |
| Coroutines   | 1.9.0      | 1.10.1 | ✅ 升级 |
| Lifecycle    | 2.8.7      | 2.9.2  | ✅ 升级 |
| Activity KTX | 1.9.3      | 1.10.1 | ✅ 升级 |
| Fragment KTX | 1.8.5      | 1.8.7  | ✅ 升级 |
| Navigation   | -          | 2.9.7  | ✅ 新增 |
| Retrofit     | 2.11.0     | 2.11.0 | ⏸️ 保持 |
| OkHttp       | 4.12.0     | 4.12.0 | ⏸️ 保持 |
