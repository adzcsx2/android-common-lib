# Changelog

All notable changes to Android Common Library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Room database utilities module
- File and preferences utilities
- Runtime permissions handling
- Pagination support

## [1.2.5] - 2026-04-02

### Changed
- Upgrade Java compatibility from 11 to 17 across all modules

### Added
- `jitpack.yml` to specify JDK 17 for JitPack builds
- Map JitPack `-Pgroup`/`-Pversion` to `project.group`/`project.version` for correct Maven artifact publishing

## [1.2.4] - 2026-04-02

### Added
- `BaseResponse<T>` typealias for naming consistency with business projects
- `ResponseCodeException` and `EmptyResponseDataException` in BaseViewModel for structured error handling
- `onApiErrorCode()` hook in BaseViewModel for custom business error code handling
- `toUiErrorStateOrNull()` extension in BaseViewModel for Throwable to UIState.Error conversion
- Non-standard response format warning log in `LoggingInterceptor`
- `ApiResponseTest` unit tests (success, error, deserialization with message/msg fields)

### Changed
- `ApiResponse` fields annotated with `@SerializedName` (`code`, `message`/`msg`, `data`) for robust deserialization
- `launchOnlyResult` error callback signature changed from `(Throwable) -> Unit` to `(code: Int, message: String) -> Unit`
- `BaseViewModel.handleException()` now handles `ResponseCodeException` with proper error code extraction
- Empty response data (null or empty String) now throws `EmptyResponseDataException` instead of raw RuntimeException
- Business error responses now throw `ResponseCodeException` instead of raw RuntimeException
- Non-handled `EmptyResponseDataException` and handled `onApiErrorCode` responses no longer trigger error toast
- Demo `loadComments()` refactored to use `launchOnlyResult` with new error callback

## [1.2.3] - 2026-04-02

### Added
- `BaseDialogFragment` DialogFragment 基类，支持 ViewBinding 和协程生命周期管理
- `DialogController` Dialog 生命周期安全管理器
- `FragmentArgumentsDsl` Fragment 参数传递 DSL
- `MultiAdapterItem` 多类型适配器项抽象
- `ActivityStackManager` Activity 栈管理工具
- `CommentApi` 评论数据接口 (Demo)
- `Comment` 评论数据模型
- `DialogSafetyDemoActivity` Dialog 安全示例页面
- `StackManagerDemoActivity` Activity 栈管理示例页面
- `MultiPostAdapter` 多类型帖子适配器
- `Logger` 日志工具类 (基于 LogUtils)
- `ScreenUtils` 折叠屏设备检测工具
- `LanguageHelper` 应用语言管理工具
- `ThemeManager` 应用主题管理工具

### Changed
- 全面补充中文 KDoc 注释（覆盖所有模块源文件）
- 更新项目文档（INTERFACES/NAVIGATION/COMPONENTS/API）
- 移除 `libVersion` 和 `scripts/publish.gradle.kts`，简化为纯 git tag 发布流程

## [1.0.9] - 2026-03-17

### Changed
- aligned `libVersion` with the published `1.0.9` tag
- refreshed README and release documentation snippets to use `1.0.9` as the latest stable version

## [1.0.8] - 2026-03-17

### Changed
- lowered the project compileSdk baseline from 35 to 34 across app and all library modules
- lowered the sample app targetSdk from 35 to 34 to match the compileSdk baseline
- downgraded AndroidX and Compose dependency lines to a compileSdk 34 compatible set, including `androidx.core` 1.13.1, `androidx.activity` 1.8.2, `androidx.lifecycle` 2.8.7, `androidx.navigation` 2.8.9, and Compose BOM `2024.04.01`

## [1.0.7] - 2026-03-17

### Changed
- lowered the project compileSdk baseline from 36 to 35 across app and all library modules
- lowered the sample app targetSdk from 36 to 35 to match the compileSdk baseline
- downgraded `androidx.core` from `1.17.0` to `1.16.0` so the project can build with compileSdk 35
- refreshed API documentation and README dependency snippets to reflect the 35 baseline and new release version

## [1.0.6] - 2026-03-17

### Added
- `GlideUtils` image helper with direct `ImageView`-scoped Glide requests
- local resource overloads for circle and rounded image loading
- minimal `common-image` instrumentation smoke tests for resource loading and cache APIs

### Changed
- renamed `ImageLoader` to `GlideUtils`
- switched image request lifecycle binding from `Context` to `ImageView`
- implemented rounded corner transforms with `CenterCrop` and `RoundedCorners`
- standardized rounded corner usage around `radiusPx`, with extension defaults such as `10.dp`
- refreshed `common-image` API documentation and migration guidance

## [1.0.5] - 2026-03-17

### Added
- Fragment demo screen with activity recreation coverage for `SavedStateHandle`
- generic ViewBinding and ViewModel type resolvers for `BaseActivity` and `BaseFragment`
- `NoViewModel` marker for screens that do not need a concrete ViewModel

### Changed
- upgraded base screens to auto-create ViewModels with `SavedStateHandle` support
- replaced legacy `startActivityForResult` usage with Activity Result API helpers for Activity and Fragment
- wrapped `BaseComposeActivity` content in `AppTheme`
- fixed `MyAppGlideModule` package alignment for the sample app

## [1.0.4] - 2026-03-17

### Added
- `common-all` aggregate module for all-in-one dependency import
- missing `common-base` and `common-compose` API docs

### Changed
- fixed JitPack dependency coordinates to `com.github.adzcsx2.android-common-lib:*`
- fixed Android library publishing to generate consumable `aar` artifacts instead of pom-only publications
- added lightweight release validation tasks: `qualityCheck` and `libraryPublishDryRun`
- centralized Android test dependencies in the version catalog
- refreshed README, getting started guide, and API docs for modular import vs all-in-one import
- added app lint baseline and cleaned part of the LiveEvent translation debt

## [1.0.0] - 2024-03-12

### Added
- Initial release of Android Common Library
- **common-core** module
  - Core foundation with minimal dependencies
  - CommonCore object with version information
- **common-utils** module
  - Context.showToast() extensions
  - Coroutine dispatcher extensions (launchIO, launchMain, withIO, withMain)
- **common-log** module
  - Logger singleton with debug/release support
  - Tag-based logging (v, d, i, w, e)
  - Exception logging support
  - JSON formatting
- **common-network** module
  - NetworkConfig for timeout and debug settings
  - ApiResponse wrapper for unified API responses
  - OkHttpClientFactory with interceptor support
  - RetrofitFactory for service creation
  - Automatic HTTP logging in debug mode
- **common-image** module
  - ImageLoader utility based on Glide
  - ImageView extensions (loadImage, loadCircleImage, loadRoundedImage)
  - Cache management (clear, clearDiskCache)
  - Automatic transition animations
- **common-ui** module
  - BaseActivity with ViewBinding support
  - BaseFragment with ViewBinding support
  - View extensions (visible, invisible, gone, isVisible, setVisible)
  - Click handler extensions (onClick, onLongClick)
  - Enabled state extensions (enable, disable)
- Maven publishing support
- JitPack integration
- Complete API documentation
- Architecture documentation
- Getting started guide

### Dependencies
- Kotlin 2.1.0
- AGP 9.0.0-rc01
- compileSdk 34
- minSdk 24
- targetSdk 34
- AndroidX Core 1.17.0
- AppCompat 1.7.1
- Material 1.13.0
- Retrofit 2.11.0
- OkHttp 4.12.0
- Gson 2.11.0
- Glide 4.16.0
- Coroutines 1.9.0

---

## Version History Summary

| Version | Date | Changes |
|---------|------|---------|
| 1.2.6 | 2026-04-02 | JitPack publishing fix, group/version mapping, updated 1.2.5 changelog |
| 1.2.5 | 2026-04-02 | Java 11 to 17 compatibility upgrade, jitpack.yml, Maven publishing fix |
| 1.2.4 | 2026-04-02 | launchOnlyResult error callback enhancement, ApiResponse @SerializedName, BaseResponse typealias, structured error handling, ApiResponseTest |
| 1.2.3 | 2026-04-02 | BaseDialogFragment, DialogController, FragmentArgumentsDsl, MultiAdapterItem, ActivityStackManager, full code comments, docs update |
| 1.0.9 | 2026-03-17 | documentation and release metadata aligned with the published 1.0.9 tag |
| 1.0.8 | 2026-03-17 | compileSdk and targetSdk baseline unified to 34, AndroidX and Compose stack downgraded for 34 compatibility |
| 1.0.7 | 2026-03-17 | compileSdk and targetSdk baseline unified to 35, androidx.core downgraded to 1.16.0 |
| 1.0.6 | 2026-03-17 | GlideUtils rename, ImageView lifecycle binding, rounded image implementation, image smoke tests |
| 1.0.5 | 2026-03-17 | Generic base screen injection, SavedStateHandle demo, Activity Result API cleanup |
| 1.0.4 | 2026-03-17 | Publishing fix, `common-all` aggregate module, docs refresh |
| 1.0.0 | 2024-03-12 | Initial release with 6 core modules |

## Contributing

When contributing to this project, please:

1. Follow the existing code style
2. Add tests for new features
3. Update documentation
4. Add an entry to this changelog under "Unreleased"

## Release Process

1. Update CHANGELOG.md and documentation
2. Commit changes with message format: `chore: release version X.X.X`
3. Create git tag: `git tag X.X.X`
4. Push code and tag to trigger JitPack build

---

[Unreleased]: https://github.com/adzcsx2/android-common-lib
[1.2.6]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.2.6
[1.2.5]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.2.5
[1.2.4]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.2.4
[1.2.3]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.2.3
[1.0.9]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.9
[1.0.8]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.8
[1.0.7]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.7
[1.0.6]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.6
[1.0.5]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.5
[1.0.4]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.4
[1.0.0]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.0
