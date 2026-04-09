# Android Common Library - AI 开发规范

> 本文件是 AI 辅助开发的唯一入口规则文件。修改代码前必须通读全文。

## AI 工作原则

1. **先搜索再写**: 修改前先搜索目标文件同目录和同类实现，优先复用已有代码
2. **禁止重复造轮子**: 新功能实现前，必须查阅 [docs/checklist/modules.md](./docs/checklist/modules.md) 确认库内是否已有对应组件
3. **最小改动**: 优先在已有类中扩展，不轻易新增平行封装；不做无关重构
4. **只改应该改的模块**: 严格遵循[模块依赖层次](#模块依赖层次与禁止依赖)，不越界
5. **保持接口稳定**: common-core 和 common-base 中的公开 API 不可破坏性修改，详见 [docs/AI_HANDOFF.md](./docs/AI_HANDOFF.md) 第 12.5/12.6 节
6. **不写通用教程**: 本文件只记录项目特有规则，Android 通用知识不在范围内

## 单一事实来源

| 内容                 | 权威文档                                                               |
| -------------------- | ---------------------------------------------------------------------- |
| 构建与版本           | `build.gradle.kts` / `gradle.properties` / `gradle/libs.versions.toml` |
| 模块列表             | `settings.gradle.kts`                                                  |
| 模块职责与 API 清单  | [docs/checklist/modules.md](./docs/checklist/modules.md)               |
| 架构设计与依赖方向   | [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md)                         |
| App 接入基线与模板   | [docs/APP_ARCHITECTURE.md](./docs/APP_ARCHITECTURE.md)                 |
| 历史决策与接口稳定性 | [docs/AI_HANDOFF.md](./docs/AI_HANDOFF.md)                             |
| 版本变更与迁移       | [docs/CHANGELOG.md](./docs/CHANGELOG.md)                               |

## 复用优先级

修改代码时必须按以下顺序检查，已有实现优先于新写：

1. **View 操作**: 必须使用 `ViewExtensions` (`click`, `visible`, `gone`, `invisible`, `enable`, `disable`, `pressEffectAlpha`, `pressEffectBgColor`, `holdClick`, `releaseClick`, `setClickNotNull` 等)，禁止直接 `setOnClickListener` 或手动设 `visibility`
2. **Toast**: 统一使用 `ToastUtils.show()` / `ToastUtils.showLong()` / `ToastUtils.showCenter()`（`com.hoyn.common.ui.toast`），BaseActivity / BaseFragment / BaseDialogFragment 中优先用 `toast`
3. **日志**: 统一使用 `com.hoyn.common.utils.Logger`
4. **存储**: 统一使用 `MMKVUtils`
5. **图片加载**: View 体系用 `GlideUtils` / `ImageExtensions`，Compose 用 `Coil`
6. **状态栏**: 统一使用 `StatusBarHelper`
7. **权限**: 统一使用 `LivePermissions`
8. **基类**: Activity 继承 `BaseActivity`，Fragment 继承 `BaseFragment`，ViewModel 继承 `BaseViewModel`

## 局部一致性规则

- **语言**: 纯 Kotlin；仅 Glide Module 等必要场景保留 Java，禁止新增 Java 业务代码
- **构建**: Version Catalog (`gradle/libs.versions.toml`) 管理版本，禁止硬编码版本号
- **View Binding**: 已启用，禁止引入 ButterKnife/DataBinding
- **Compose**: 仅 common-compose 和 app 启用 Compose 编译器；View 体系禁止混用 @Composable
- **发布版本**: 通过 git tag 发布，JitPack 自动构建，无需手动维护版本号

## 项目强约束

- **View 操作**: 必须使用 `ViewExtensions`（如 `view.click { }`），禁止直接 `setOnClickListener`；可见性用 `visible()`/`gone()`/`invisible()`
- **文本不硬编码**: 字符串优先查 `strings.xml`，禁止在代码中硬写中文/英文文案
- **颜色不硬编码**: 颜色优先查 `colors.xml`，禁止在代码中硬编码颜色值
- **API 成功语义**: `isSuccess()` 统一为 `code == 0`
- **禁止单例 object 用于 Repository**: 使用 Koin 或 `by lazy`
- **禁用反射注入 ViewModel/Repository**: 子类必须显式声明 `override val repository`
- **禁用 @OnLifecycleEvent 注解**: 使用 DefaultLifecycleObserver
- **Logger 唯一实现**: `com.hoyn.common.utils.Logger`，禁止重复日志工具
- **README.md 保护**: 只追加不删除，详见 [.claude/README.md](./.claude/README.md)
- **文档位置**: 所有文档放 `/docs/`，源码目录内不创建 md 文件

## 模块依赖层次与禁止依赖

依赖方向: common-core -> common-utils -> common-base -> common-network / common-image / common-compose -> common-ui -> common-all -> app

| 模块                          | 禁止依赖                                             |
| ----------------------------- | ---------------------------------------------------- |
| common-core                   | 任何其他 common-\* 模块                              |
| common-utils                  | common-base, common-ui, common-network, common-image |
| common-base                   | common-network, common-image                         |
| common-network / common-image | common-ui                                            |

## 模块速查

| 模块           | 包名                      | 关键类                                                                                                        |
| -------------- | ------------------------- | ------------------------------------------------------------------------------------------------------------- |
| common-core    | `com.hoyn.common.core`    | `UIState`, `IBaseResponse`, `ThrowableBean`, `Message`                                                        |
| common-base    | `com.hoyn.common.base`    | `BaseActivity`, `BaseFragment`, `BaseViewModel`, `ViewModelFactory`, `GlobalLiveEvent`, `SingleLiveEvent`     |
| common-utils   | `com.hoyn.common.utils`   | `Logger`, `MMKVUtils`, `ToastUtils`, `CoroutinesExtensions`, `ContextExtensions`                              |
| common-network | `com.hoyn.common.network` | `RetrofitFactory`, `OkHttpClientFactory`, `NetworkConfig`, `ApiResponse`, `BaseRepository`, `ExceptionHandle` |
| common-image   | `com.hoyn.common.image`   | `GlideUtils`, `ImageExtensions`                                                                               |
| common-ui      | `com.hoyn.common.ui`      | `StatusBarHelper`, `NotchHelper`, `PressEffectHelper`, `LivePermissions`, `ViewExtensions`, `ToastUtils`, `ToastConfig` |
| common-compose | `com.hoyn.common.compose` | `BaseComposeActivity`, `BaseComposeFragment`, `Theme`                                                         |

## 编码规则

- **DI**: Koin `single`/`factory`/`viewModel`；禁止 `object` Repository
- **状态管理**: `StateFlow<UIState<T>>`，ViewModel 中用 `launchOnlyResult` 封装网络请求
- **事件观察**: Activity/Fragment 中用 `observeAllUIEvents(viewModel, onToast, onShowDialog, onDismissDialog, onError)`
- **全局事件**: `GlobalLiveEvent.sendMessage(Message(code, msg))` / `observeGlobalMessage { }`
- **命名**: Repository `XxxRepository` | DataSource `XxxDataSource` | ViewModel `XxxViewModel` | API `XxxApi` | DAO `XxxDao` | Entity `XxxEntity`

## 质量门禁

```bash
./gradlew qualityCheck              # lint + debug 单元测试 (全模块)
./gradlew libraryPublishDryRun      # 验证所有库模块可发布到 Maven Local
```

## 关键依赖版本 (libs.versions.toml)

Kotlin 2.3.10 | AGP 9.1.0 | Lifecycle 2.8.7 | Coroutines 1.10.1 | Retrofit 2.11.0 | OkHttp 4.12.0 | Glide 4.16.0 | MMKV 2.3.0 | Koin 3.5.6 | Navigation 2.8.9 | Room 2.8.4 | Compose BOM 2024.04.01
