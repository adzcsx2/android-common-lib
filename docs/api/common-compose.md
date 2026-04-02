# Common Compose API Documentation

## Overview

`common-compose` provides a lightweight Compose foundation layer for projects that want to keep the same modular architecture while adopting Jetpack Compose incrementally.

## Package: `com.hoyn.common.compose`

### Core Types

#### `BaseComposeActivity`

Base Activity for Compose-first pages.

**Key responsibilities:**
- Host Compose content in a consistent base Activity
- Reuse architecture conventions from `common-base`

#### `BaseComposeFragment`

Base Fragment for hybrid Compose + View projects.

**Key responsibilities:**
- Support Compose content inside Fragment-based apps
- Help gradual migration instead of forcing a full Compose rewrite

#### `Theme.kt`

Provides the shared Material 3 theme entry point used by Compose screens.

#### `StateExtensions.kt`

Compose helpers for observing and rendering `UIState`.

## Recommended Usage

- Use `common-compose` only when the consuming app actually uses Compose.
- Keep page state in `UIState` and collect it from `Flow/StateFlow`.
- For hybrid apps, keep View-based screens on `common-base` and add Compose screen by screen.

## Dependencies

| Dependency | Type |
|------------|------|
| common-core | api |
| common-base | api |
| Compose BOM | implementation |
| compose-ui | implementation |
| compose-foundation | implementation |
| compose-material3 | implementation |
| activity-compose | implementation |
| lifecycle-runtime-compose | implementation |
| lifecycle-viewmodel-compose | implementation |
| coil-compose | implementation |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.compose` |
| Min SDK | 24 |
| Compile SDK | 34 |
| Java Version | 17 |

## Integration

```gradle
dependencies {
    implementation("com.github.adzcsx2.android-common-lib:common-compose:1.0.3")
}
```

## Typical Example

```kotlin
class FeedComposeActivity : BaseComposeActivity() {
    @Composable
    override fun Content() {
        CommonLibTheme {
            FeedScreen()
        }
    }
}
```