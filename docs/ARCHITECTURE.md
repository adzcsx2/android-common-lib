# Architecture Documentation

## Overview

Android Common Library follows a modular architecture with clear separation of concerns and minimal dependencies between modules. The design prioritizes reusability, maintainability, and flexibility.

## Design Principles

### 1. Modular Design

The library is organized into focused, single-responsibility modules:

- **common-core** - Core abstractions and stable contracts (UIState, IBaseResponse, Message)
- **common-base** - Architecture foundation (BaseActivity, BaseFragment, BaseViewModel, Event system)
- **common-utils** - Extension functions, Logger, MMKV utilities
- **common-network** - Network layer abstraction with Retrofit/OkHttp
- **common-image** - Image loading utilities with Glide
- **common-ui** - UI presentation helpers (Toast, StatusBar, Permissions)

### 2. Dependency Hierarchy

```
                    common-core (core abstractions)
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
   common-utils       common-base        (future modules)
         │                 │
         └────────┬────────┘
                  │
         ┌────────┴────────┐
         │                 │
   common-network     common-image
         │                 │
         └────────┬────────┘
                  │
             common-ui
                  │
              Your App
```

### 3. API Visibility Strategy

- **api**: Dependencies exposed to consumers
- **implementation**: Dependencies used internally only

This ensures:
- Smaller APK size for consumers
- No version conflicts with transitive dependencies
- Clear public API surface

## Module Architecture

### common-core

**Purpose**: Core abstractions and stable contracts

**Dependencies**:
- androidx.core:core-ktx (implementation)
- androidx.lifecycle (api)
- kotlinx-coroutines (api)

**Responsibilities**:
- UIState sealed class for page state management
- IBaseResponse interface for API response contracts
- Message data class for event communication
- ThrowableBean data class for error encapsulation

**Design Decisions**:
- Stable API for long-term support
- No business logic, only core abstractions
- Minimal but sufficient dependencies

### common-base

**Purpose**: Architecture foundation for app development

**Dependencies**:
- common-core (api)
- common-utils (api)
- AndroidX Activity/Fragment/AppCompat (api)
- AndroidX Lifecycle/ViewModel (api)

**Responsibilities**:
- BaseActivity with ViewBinding support
- BaseFragment with lazy loading
- BaseViewModel with coroutine management and network request helpers
- ViewModelFactory for custom ViewModel creation
- SingleLiveEvent for one-time UI events
- GlobalEventBus for cross-component communication
- ViewModel extensions for observing UI events

**Design Decisions**:
- Single place for all architecture foundation classes
- Not a dumping ground for generic UI tools
- ViewBinding for compile-time safety
- SharedFlow-based event system (replaces LiveData/LiveEventBus)

### common-utils

**Purpose**: Extension functions and utilities

**Dependencies**:
- common-core (api)
- AndroidX KTX libraries (implementation)
- Kotlin Coroutines (implementation)
- MMKV (api)
- Gson (api)

**Responsibilities**:
- Logger unified logging interface
- MMKVUtils key-value storage
- Context extensions
- Device and screen helpers
- Coroutine helpers

**Design Decisions**:
- Uses Kotlin extension functions for clean API
- Follows Android/Kotlin best practices
- Reactive design with coroutines

### common-network

**Purpose**: Network layer abstraction on Retrofit/OkHttp

**Dependencies**:
- common-core (api)
- common-utils (api)
- Retrofit libraries (api)
- OkHttp libraries (api)

**Responsibilities**:
- API service creation
- Request/Response handling
- Timeout configuration
- Interceptor management
- SSL/TLS support

**Design Decisions**:
- Factory pattern for instance creation
- Generic response wrapper (ApiResponse implements IBaseResponse)
- Automatic logging integration
- Configurable timeouts
- Interceptor support for auth, caching, etc.

### common-image

**Purpose**: Image loading abstraction on Glide

**Dependencies**:
- common-core (api)
- common-utils (api)
- Glide (api)

**Responsibilities**:
- Image loading from URLs/resources
- Placeholder and error handling
- Transformations (circle, rounded)
- Cache management

**Design Decisions**:
- Extension functions for simple API
- Direct ImageLoader access for advanced use
- Glide as underlying engine (stable, feature-rich)
- Automatic transition animations

### common-ui

**Purpose**: UI presentation helpers

**Dependencies**:
- common-base (api)
- Material Design (api)
- SmartRefreshLayout (api)
- BaseRecyclerViewAdapterHelper (api)
- Navigation (api)

**Responsibilities**:
- ToastUtils for toast notifications
- StatusBarHelper for status bar styling
- NotchHelper for notch device support
- PressEffectHelper for view press effects
- LivePermissions for runtime permissions
- View extensions (visibility, clicks, etc.)

**Design Decisions**:
- Extension functions for clean API
- Separation of concerns from architecture classes
- Presentation toolbox, not architecture root

## Communication Patterns

### 1. Repository Pattern

For data access:

```kotlin
class UserRepository : BaseRepository<UserApi>() {
    override val api: UserApi by lazy { createApi("https://api.example.com/", UserApi::class.java) }

    suspend fun getUser(id: String): Result<User> = withIO {
        try {
            val response = api.getUser(id)
            if (response.isSuccess()) {
                Result.success(response.data!!)
            } else {
                Result.failure(Exception(response.msg()))
            }
        } catch (e: Exception) {
            Logger.e("Failed to get user", e)
            Result.failure(e)
        }
    }
}
```

### 2. ViewModel Pattern

With BaseViewModel:

```kotlin
class UserViewModel : BaseViewModel<UserRepository>() {
    override val repository: UserRepository by lazy { UserRepository() }

    private val _userState = MutableStateFlow<UIState<User>>(UIState.Loading)
    val userState: StateFlow<UIState<User>> = _userState

    fun loadUser(id: Int) {
        launchOnlyResult(
            block = { repository.api.getUser(id) },
            success = { user -> _userState.value = UIState.Success(user) },
            error = { e -> _userState.value = UIState.Error(-1, e.message ?: "Unknown error") }
        )
    }
}
```

### 3. Event System

Using SharedFlow-based events:

```kotlin
// In ViewModel
fun doSomething() {
    showToast("Operation completed")
    showDialog("Loading...")
    dismissDialog()
}

// In Activity/Fragment
observeAllUIEvents(
    viewModel = viewModel,
    onToast = { ToastUtils.show(this, it) },
    onShowDialog = { showLoading(it) },
    onDismissDialog = { hideLoading() },
    onError = { handleError(it) }
)
```

### 4. Global Events

For cross-component communication:

```kotlin
// Send global event
GlobalEventBus.sendMessage(Message(code = 1001, msg = "Login expired"))

// Observe global events
observeGlobalMessage { message ->
    when (message.code) {
        1001 -> navigateToLogin()
    }
}
```

## Error Handling Strategy

### Network Errors

```kotlin
try {
    val response = apiService.getData()
    if (response.isSuccess()) {
        // Handle success
    } else {
        // Handle API error (check response.code())
    }
} catch (e: Exception) {
    // Handle network/parse error
    Logger.e("Request failed", e)
}
```

### UI State Management

```kotlin
when (state) {
    is UIState.Loading -> showLoading()
    is UIState.Success -> showData(state.data)
    is UIState.Error -> showError(state.message)
    is UIState.Empty -> showEmpty()
}
```

## Testing Strategy

### Unit Tests

Test business logic without Android dependencies:

```kotlin
class UserRepositoryTest {
    @Test
    fun `getUser returns success when API succeeds`() = runTest {
        // Given
        val mockApi = mockk<ApiService>()
        val repository = UserRepository()

        // When
        coEvery { mockApi.getUser(any()) } returns ApiResponse.success(user)

        val result = repository.getUser("123")

        // Then
        assertTrue(result.isSuccess)
    }
}
```

## Performance Considerations

### 1. Lazy Initialization

```kotlin
class MyViewModel : BaseViewModel<MyRepository>() {
    override val repository: MyRepository by lazy { MyRepository() }
}
```

### 2. Caching

```kotlin
object RetrofitFactory {
    private val retrofits = mutableMapOf<String, Retrofit>()

    fun create(baseUrl: String): Retrofit {
        return retrofits.getOrPut(baseUrl) { /* ... */ }
    }
}
```

### 3. Memory Management

```kotlin
// Clear image cache on low memory
override fun onLowMemory() {
    super.onLowMemory()
    ImageLoader.clear(this)
}
```

## Security Considerations

### 1. Certificate Pinning

Add to OkHttpClientFactory:

```kotlin
val client = OkHttpClient.Builder()
    .certificatePinner(
        CertificatePinner.Builder()
            .add("yourdomain.com", "sha256/...")
            .build()
    )
    .build()
```

### 2. Token Management

```kotlin
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
```

## Versioning Strategy

### Semantic Versioning

- **MAJOR**: Breaking changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

### Deprecation Process

1. Mark as `@Deprecated`
2. Document replacement
3. Wait for one major version
4. Remove in next major version

## Future Considerations

### Potential Enhancements

1. **common-database** - Room database utilities
2. **common-compose** - Jetpack Compose components
3. **common-camera** - Camera and image capture
4. **common-location** - Location services
5. **common-analytics** - Analytics tracking
