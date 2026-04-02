# Common Core API Documentation

## Overview

`common-core` is the foundational module of the Android Common Library. It provides core abstractions and stable contracts that all other modules build upon, including UIState, IBaseResponse, ThrowableBean, and Koin DI support.

## Package: `com.hoyn.common.core`

### CommonCore

Core module version information.

```kotlin
object CommonCore {
    const val VERSION = "1.0.0"
}
```

### UIState<T>

UI state sealed class for page state management, supporting Compose and View systems.

```kotlin
sealed class UIState<out T> {
    data object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val code: Int, val message: String) : UIState<Nothing>()
    data object Empty : UIState<Nothing>()
}
```

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `isLoading` | `Boolean` | Whether the state is Loading |
| `isSuccess` | `Boolean` | Whether the state is Success |
| `isError` | `Boolean` | Whether the state is Error |
| `isEmpty` | `Boolean` | Whether the state is Empty |

#### Methods

| Method | Description |
|--------|-------------|
| `getDataOrNull(): T?` | Returns data if Success, otherwise null |
| `getErrorMsgOrNull(): String?` | Returns error message if Error, otherwise null |
| `UIState.loading(): UIState<Nothing>` | Create Loading state |
| `UIState.success(data: T): UIState<T>` | Create Success state |
| `UIState.error(code: Int, message: String): UIState<Nothing>` | Create Error state |
| `UIState.empty(): UIState<Nothing>` | Create Empty state |

#### Usage Example

```kotlin
// ViewModel
private val _uiState = MutableStateFlow<UIState<User>>(UIState.Loading)
val uiState: StateFlow<UIState<User>> = _uiState

// Collect
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        when (state) {
            is UIState.Loading -> showLoading()
            is UIState.Success -> showData(state.data)
            is UIState.Error -> showError(state.message)
            is UIState.Empty -> showEmpty()
        }
    }
}
```

### IBaseResponse<T>

Unified API response interface. All API response data classes should implement this interface.

```kotlin
interface IBaseResponse<T> {
    fun code(): Int
    fun msg(): String
    fun data(): T?
    fun isSuccess(): Boolean = code() == 0
}
```

#### Usage Example

```kotlin
data class MyResponse(
    val code: Int = 0,
    val message: String = "",
    val data: User? = null
) : IBaseResponse<User> {
    override fun code(): Int = code
    override fun msg(): String = message
    override fun data(): User? = data
}
```

### ThrowableBean

Error information carrier for UI-layer error display.

```kotlin
data class ThrowableBean(
    val code: Int = 0,
    val errMsg: String = ""
) {
    companion object {
        fun from(throwable: Throwable): ThrowableBean
    }
}
```

#### Usage Example

```kotlin
val bean = ThrowableBean.from(exception)
// bean.code -> error code
// bean.errMsg -> error message
```

## Dependencies

| Dependency | Type |
|------------|------|
| androidx.core:core-ktx | implementation |
| androidx.lifecycle:lifecycle-runtime-ktx | api |
| androidx.lifecycle:lifecycle-viewmodel-ktx | api |
| kotlinx-coroutines-core | api |
| kotlinx-coroutines-android | api |
| koin-core | api |
| koin-android | api |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.core` |
| Min SDK | 24 |
| Compile SDK | 34 |
| Java Version | 17 |

## Integration

```gradle
dependencies {
    implementation("com.github.adzcsx2.android-common-lib:common-core:1.2.7")
}
```

## Related Modules

- `common-utils` - Context and coroutine extensions, Logger, MMKV
- `common-base` - BaseActivity, BaseFragment, BaseViewModel, event system
- `common-compose` - Compose foundation classes and UIState helpers
- `common-ui` - UI components and presentation utilities
- `common-network` - Network request handling
- `common-image` - Image loading capabilities
