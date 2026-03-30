# Migration Guide

## Version 1.2.1 Migration

Update your dependency version to `1.2.1`:

```gradle
dependencies {
   implementation("com.github.adzcsx2.android-common-lib:common-all:1.2.1")
}
```

### Breaking Changes

- **`ToastUtils` removed**: Replace all `ToastUtils.show()` calls with `ToastUtil.show()` / `ToastUtil.showLong()` / `ToastUtil.showCenter()`.

### Recommended

- Replace raw `setOnClickListener` with `ViewExtensions.click { }` for built-in 600ms debounce.
- Use `StatusBarHelper.setTransparent()` / `setLightMode()` / `setDarkMode()` for status bar control.

## Version 1.0.9 Migration

Update your dependency version to `1.0.9` to stay aligned with the latest published tag:

```gradle
dependencies {
   implementation("com.github.adzcsx2.android-common-lib:common-all:1.0.9")
}
```

Notes:
- There are no API or baseline changes from `1.0.8` in this release.
- This release aligns published documentation and release metadata with tag `1.0.9`.

## Version 1.0.8 Migration

Update your dependency version to `1.0.8` to align with the new compileSdk 34 baseline:

```gradle
dependencies {
   implementation("com.github.adzcsx2.android-common-lib:common-all:1.0.8")
}
```

Notes:
- The project baseline is now `compileSdk 34`.
- The sample app baseline is now `targetSdk 34`.
- The dependency stack has been adjusted to a compileSdk 34 compatible set: `androidx.core` 1.13.1, `androidx.activity` 1.8.2, `androidx.lifecycle` 2.8.7, `androidx.navigation` 2.8.9, and Compose BOM `2024.04.01`.

## Version 1.0.6 Migration

Update your dependency version to `1.0.6` to get the new `GlideUtils` API and rounded image support in `common-image`:

```gradle
dependencies {
   implementation("com.github.adzcsx2.android-common-lib:common-image:1.0.6")
   // or
   implementation("com.github.adzcsx2.android-common-lib:common-all:1.0.6")
}
```

Notes:
- `ImageLoader` has been renamed to `GlideUtils`.
- Direct calls should migrate from `ImageLoader.load(context, model, imageView)` to `GlideUtils.load(imageView, model)`.
- Rounded image APIs now use `radiusPx`, and extension calls can pass values such as `10.dp` from `PxUtils`.

## Version 1.0.0 Migration

Since this is the initial release, there are no migration steps needed. Simply add the library to your project following the [Getting Started Guide](/docs/guides/getting-started.md).

## Future Migration Guides

As new versions are released, migration guides will be added here to help you upgrade your code smoothly.

## Version 1.0.5 Migration

Update your dependency version to `1.0.5` to get the new base screen generics and fragment saved-state demo:

```gradle
dependencies {
   implementation("com.github.adzcsx2.android-common-lib:common-base:1.0.5")
   // or
   implementation("com.github.adzcsx2.android-common-lib:common-all:1.0.5")
}
```

Notes:
- `BaseActivity` and `BaseFragment` subclasses now declare both ViewBinding and ViewModel generic parameters.
- Screens without a dedicated ViewModel should use `NoViewModel`.
- `startActivityForResult` helper usage should be migrated to `registerStartActivityForResult` plus `launchActivity`.

## Version 1.0.4 Migration

If you were using older documentation or unpublished local artifacts, update to the new JitPack coordinates:

```gradle
dependencies {
   implementation("com.github.adzcsx2.android-common-lib:common-base:1.0.4")
   // or
   implementation("com.github.adzcsx2.android-common-lib:common-all:1.0.4")
}
```

Notes:
- `com.github.adzcsx2.android-common-lib:common-all:1.0.4` is the all-in-one aggregate module.
- `com.github.adzcsx2.android-common-lib:1.0.4` is not a valid root artifact.
- If your app previously failed to resolve classes such as `BaseLiveEvent`, refresh dependencies after upgrading.

### Typical Migration Steps

When a new version is released with breaking changes, follow these general steps:

1. **Update Dependency**
   ```gradle
   // In build.gradle.kts
   implementation("com.github.adzcsx2.android-common-lib:common-all:X.X.X")
   ```

2. **Review Breaking Changes**
   - Check the specific version's release notes
   - Identify affected code areas
   - Review updated API documentation

3. **Update Code**
   - Replace deprecated methods
   - Update imports if packages changed
   - Adjust to new API signatures

4. **Test Thoroughly**
   - Unit tests
   - Integration tests
   - Manual testing of critical flows

5. **Monitor for Issues**
   - Check logs for deprecation warnings
   - Verify network requests work correctly
   - Test image loading
   - Validate UI components

## Common Patterns

### Pattern: Updating Extension Functions

If an extension function signature changes:

**Before (1.0.0):**
```kotlin
view.onClick()
```

**After (1.1.0):**
```kotlin
view.onClick { /* handle click */ }
```

### Pattern: Updating Network Calls

If the network module changes:

**Before (1.0.0):**
```kotlin
val response = apiService.getData()
if (response.code == 0) {
    // Success
}
```

**After (1.1.0):**
```kotlin
val response = apiService.getData()
if (response.isSuccess) {
    // Success
}
```

## Need Help?

If you encounter issues during migration:

1. Check the [API Documentation](/docs/api/) for updated signatures
2. Review the [Architecture Documentation](/docs/ARCHITECTURE.md) for design changes
3. Open an issue on [GitHub](https://github.com/adzcsx2/android-common-lib/issues)
4. Check existing issues for similar problems

## Version Compatibility

| Library Version | Min SDK | Compile SDK | Kotlin |
|----------------|---------|-------------|---------|
| 1.2.1 | 24 | 34 | 2.3.10 |
| 1.2.0 | 24 | 34 | 2.3.10 |
| 1.0.9 | 24 | 34 | 2.3.10 |
| 1.0.8 | 24 | 34 | 2.3.10 |
| 1.0.7 | 24 | 35 | 2.3.10 |
| 1.0.6 | 24 | 36 | 2.3.10 |
| 1.0.5 | 24 | 36 | 2.3.10 |
| 1.0.4 | 24 | 36 | 2.3.10 |
| 1.0.0 | 24 | 36 | 2.1.0 |

## Deprecation Policy

- Deprecated APIs will be marked with `@Deprecated` annotation
- Deprecation notices will include replacement guidance
- Deprecated APIs will be maintained for one major version
- After one major version, deprecated APIs may be removed

Example:
- Deprecated in 1.0.0 → Removed in 2.0.0
- Deprecated in 1.1.0 → Removed in 2.0.0
- Deprecated in 2.0.0 → Removed in 3.0.0
