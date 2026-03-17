# Common Core API Documentation

## Overview

`common-core` is the foundational module of the Android Common Library. It provides zero-dependency core functionality that all other modules build upon.

## Package: `com.hoyn.common.core`

### CommonCore

The main entry point for the core module.

```kotlin
object CommonCore {
    const val VERSION = "1.0.0"
}
```

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `VERSION` | `String` | The version string of the core module |

#### Usage Example

```kotlin
import com.hoyn.common.core.CommonCore

// Get the core module version
val version = CommonCore.VERSION
```

## Dependencies

`common-core` has minimal dependencies to maintain lightweight:

- **androidx.core:core-ktx** - AndroidX Core KTX extensions

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.core` |
| Min SDK | 24 |
| Compile SDK | 34 |
| Java Version | 11 |

## Integration

```gradle
dependencies {
    api(project(":common-core"))
}
```

## Related Modules

- `common-utils` - Extends core with context and coroutine utilities
- `common-base` - Provides base Activity, Fragment, ViewModel, and event helpers
- `common-compose` - Provides Compose foundation classes and UIState helpers
- `common-ui` - UI components and base classes
- `common-network` - Network request handling
- `common-image` - Image loading capabilities

## JitPack Coordinates

```gradle
dependencies {
    implementation("com.github.adzcsx2.android-common-lib:common-core:1.0.3")
}
```
    override fun Content() {
        CommonLibTheme {
            FeedScreen()
        }
    }
}
```
