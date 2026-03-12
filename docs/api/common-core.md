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
| Compile SDK | 36 |
| Java Version | 11 |

## Integration

```gradle
dependencies {
    api(project(":common-core"))
}
```

## Related Modules

- `common-utils` - Extends core with context and coroutine utilities
- `common-log` - Provides logging functionality
- `common-ui` - UI components and base classes
- `common-network` - Network request handling
- `common-image` - Image loading capabilities
