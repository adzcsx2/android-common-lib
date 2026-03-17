# Migration Guide

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
