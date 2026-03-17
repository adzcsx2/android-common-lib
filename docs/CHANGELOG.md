# Changelog

All notable changes to Android Common Library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Room database utilities module
- File and preferences utilities
- Runtime permissions handling
- Additional image transformations
- Pagination support

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
- compileSdk 36
- minSdk 24
- targetSdk 36
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
[1.0.5]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.5
[1.0.4]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.4
[1.0.0]: https://github.com/adzcsx2/android-common-lib/releases/tag/1.0.0
