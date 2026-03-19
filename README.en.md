# Android Common Library

Modular Android shared libraries for common utilities, networking, image loading, UI helpers, and base architecture components.

## Language

- [Chinese README](./README.md)

## Development Environment

| Item | Version |
|------|---------|
| Android Studio | Panda 2 \| 2025.3.2 |
| Android Studio Runtime | JetBrains Runtime 21.0.9 |
| Kotlin | 2.3.10 |
| Kotlin JVM target | 11 |
| compileSdk | 34 |
| minSdk | 24 |
| targetSdk | 34 |

> Published artifact compatibility
>
> When consuming published JitPack artifacts directly, downstream projects must satisfy these baseline requirements:
>
> - Kotlin Gradle Plugin must be aligned to `2.3.10`
> - Kotlin/JVM compilation target must be `11`
> - `compileSdk` must be `>= 34`
> - `minSdk` is `24`
>
> Copying the source files into an app can compile because the code is rebuilt by the consumer project. Depending on the published AAR is different: the consumer compiler must be compatible with the Kotlin metadata and JVM bytecode target used when this library was published.

## Documentation

- [Chinese documentation entry](./README.md)
- [Architecture Documentation](./docs/ARCHITECTURE.md)
- [App Architecture Guide](./docs/APP_ARCHITECTURE.md)
- [Changelog](./docs/CHANGELOG.md)

## Project Structure

```text
common-lib/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
├── scripts/publish.gradle.kts
├── common-core/
├── common-base/
├── common-compose/
├── common-utils/
├── common-network/
├── common-image/
├── common-ui/
└── app/
```

## Modules

| Module | Description | Depends On |
|--------|-------------|------------|
| common-core | Core abstractions such as `UIState`, `IBaseResponse`, `Message`, and `ThrowableBean` | Lifecycle, Coroutines |
| common-base | Base architecture components such as `BaseActivity`, `BaseFragment`, `BaseViewModel`, and event APIs | common-core, common-utils |
| common-compose | Compose foundation and shared UI architecture | common-core, common-base |
| common-utils | Context extensions, coroutine helpers, logger, MMKV helpers | common-core |
| common-network | Retrofit + OkHttp wrapper | common-core, common-utils |
| common-image | Glide-based image loading wrapper | common-core, common-utils |
| common-ui | Toast, status bar, permission, and view helpers | common-base |

## Installation Via JitPack

### 1. Add the repository in `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
	repositories {
		google()
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
	}
}
```

### 2. Add dependencies in your module

```kotlin
dependencies {
	implementation("com.github.adzcsx2.android-common-lib:common-core:1.1.0")
	implementation("com.github.adzcsx2.android-common-lib:common-base:1.1.0")
	implementation("com.github.adzcsx2.android-common-lib:common-utils:1.1.0")
	implementation("com.github.adzcsx2.android-common-lib:common-compose:1.1.0")
	implementation("com.github.adzcsx2.android-common-lib:common-network:1.1.0")
	implementation("com.github.adzcsx2.android-common-lib:common-image:1.1.0")
	implementation("com.github.adzcsx2.android-common-lib:common-ui:1.1.0")
}
```

To import all modules at once:

```kotlin
dependencies {
	implementation("com.github.adzcsx2.android-common-lib:common-all:1.1.0")
}
```

## Version Information

- compileSdk: 34
- minSdk: 24
- targetSdk: 34
- Kotlin: 2.3.10
- AGP: 9.1.0
