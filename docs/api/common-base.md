# Common Base API Documentation

## Overview

`common-base` provides architecture foundation classes for View-based Android apps, including base Activity and Fragment implementations, a coroutine-aware ViewModel base class, and lifecycle-aware event helpers.

## Package: `com.hoyn.common.base`

### Core Types

#### `BaseActivity<VB : ViewBinding>`

Base Activity with ViewBinding setup, lifecycle hooks, and common dispatch helpers.

**Key responsibilities:**
- Inflate and expose a typed `ViewBinding`
- Provide `initView()` and `initData()` hooks
- Centralize common Activity behavior for library consumers

#### `BaseFragment<VB : ViewBinding>`

Base Fragment with safe ViewBinding lifecycle handling and lazy initialization hooks.

**Key responsibilities:**
- Manage binding lifecycle safely
- Provide `initView()` and `initData()` hooks
- Reduce repeated Fragment setup code

#### `BaseViewModel<R>`

Coroutine-friendly base ViewModel with repository access and UI event helpers.

**Key responsibilities:**
- Provide structured coroutine launch helpers
- Dispatch toast, dialog, and error events
- Standardize error handling entry points

### Event APIs

#### `SingleLiveEvent<T>`

Lifecycle-aware one-shot event stream for legacy and View-based UIs.

#### `GlobalLiveEvent`

Global event dispatcher for cross-component communication.

#### `BaseLiveEvent<T>`

Shared event foundation with lifecycle-aware and manual observer support.

### Factories and Extensions

#### `ViewModelFactory`

Factory for constructing ViewModels with custom arguments.

#### `ViewModelExtensions`

Helpers for observing ViewModel UI events from Activities and Fragments.

## Recommended Usage

- Use `BaseActivity` and `BaseFragment` for ViewBinding-based screens.
- Prefer `UIState` + `Flow/StateFlow` for new page state.
- Keep `SingleLiveEvent` and `GlobalLiveEvent` for compatibility with existing screens that already rely on LiveData-based events.

## Dependencies

| Dependency | Type |
|------------|------|
| common-core | api |
| common-utils | api |
| androidx.appcompat | api |
| androidx.activity-ktx | api |
| androidx.fragment-ktx | api |
| androidx.lifecycle-runtime-ktx | api |
| androidx.lifecycle-viewmodel-ktx | api |
| kotlinx-coroutines-core | implementation |
| kotlinx-coroutines-android | implementation |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.base` |
| Min SDK | 24 |
| Compile SDK | 34 |
| Java Version | 17 |

## Integration

```gradle
dependencies {
    implementation("com.github.adzcsx2.android-common-lib:common-base:1.0.3")
}
```

## Typical Example

```kotlin
class UserActivity : BaseActivity<ActivityUserBinding>() {
    override fun createBinding() = ActivityUserBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.toolbar.title = "User"
    }
}
```
