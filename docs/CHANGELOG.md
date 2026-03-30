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

## [1.2.1] - 2026-03-30

### Changed
- aligned `libVersion` with `1.2.1`, updating all dependency snippets in README and module checklist

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
| 1.2.1 | 2026-03-30 | libVersion aligned to 1.2.1, dependency snippets refreshed |
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

1. Update `libVersion` in gradle.properties
2. Update CHANGELOG.md
3. Commit changes with message format: `chore: release version X.X.X`
4. Create git tag: `git tag X.X.X`
5. Push tag to trigger JitPack build

---

[Unreleased]: https://github.com/adzcsx2/android-common-lib
[1.2.1]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.2.1
[1.0.9]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.9
[1.0.8]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.8
[1.0.7]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.7
[1.0.6]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.6
[1.0.5]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.5
[1.0.4]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.4
[1.0.0]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.0
