# Architecture Documentation

## Overview

Android Common Library follows a modular architecture with clear separation of concerns and minimal dependencies between modules. The design prioritizes reusability, maintainability, and flexibility.

## Design Principles

### 1. Modular Design

The library is organized into focused, single-responsibility modules:

- **common-core** - Foundation with zero external dependencies
- **common-utils** - Extension functions for common operations
- **common-log** - Unified logging interface
- **common-network** - Network layer abstraction
- **common-image** - Image loading utilities
- **common-ui** - UI components and base classes

### 2. Dependency Hierarchy

```
                    common-core (zero dependencies)
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
   common-log       common-utils        common-ui
         │                 │
         └─────────┬───────┘
                   │
         ┌─────────┴─────────┐
         │                   │
   common-network      common-image
         │                   │
         └─────────┬─────────┘
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

**Purpose**: Foundation module providing base functionality

**Dependencies**:
- androidx.core:core-ktx (implementation)

**Responsibilities**:
- Version information
- Common interfaces
- Base types

**Design Decisions**:
- Minimal dependencies to ensure maximum compatibility
- No business logic, only foundation
- Stable API for long-term support

### common-utils

**Purpose**: Extension functions for Android SDK and Kotlin

**Dependencies**:
- common-core (api)
- AndroidX KTX libraries (implementation)
- Kotlin Coroutines (implementation)

**Responsibilities**:
- Context extensions (toast, etc.)
- Coroutine dispatchers
- Common utilities

**Design Decisions**:
- Uses Kotlin extension functions for clean API
- Follows Android/Kotlin best practices
- Reactive design with coroutines

### common-log

**Purpose**: Unified logging with debug/release support

**Dependencies**:
- common-core (api)
- androidx.core:core-ktx (implementation)

**Responsibilities**:
- Tag-based logging
- Debug mode filtering
- Formatted output (JSON)

**Design Decisions**:
- Singleton pattern for global configuration
- Error logging always enabled
- Simple API without external logging libraries

### common-network

**Purpose**: Network layer abstraction on Retrofit/OkHttp

**Dependencies**:
- common-core (api)
- common-log (api)
- Retrofit libraries (api)
- OkHttp libraries (api)

**Responsibilities**:
- API service creation
- Request/Response handling
- Timeout configuration
- Interceptor management

**Design Decisions**:
- Factory pattern for instance creation
- Generic response wrapper
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

**Purpose**: UI components and base classes

**Dependencies**:
- common-core (api)
- AndroidX UI libraries (api)
- Material Design (api)

**Responsibilities**:
- BaseActivity with ViewBinding
- BaseFragment with ViewBinding
- View extensions (visibility, clicks, etc.)

**Design Decisions**:
- Generic base classes for type safety
- ViewBinding for compile-time safety
- Extension functions for clean API
- Separation of init and data loading

## Communication Patterns

### 1. Repository Pattern

For data access:

```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    suspend fun getUser(id: String): Result<User> = withIO {
        try {
            val response = apiService.getUser(id)
            if (response.isSuccess) {
                Result.success(response.data!!)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Logger.e("Failed to get user", e)
            Result.failure(e)
        }
    }
}
```

### 2. Factory Pattern

For creating instances:

```kotlin
// Network
RetrofitFactory.createService<ApiService>(baseUrl)

// Image
ImageLoader.load(context, url, imageView)
```

### 3. Extension Functions

For clean APIs:

```kotlin
// View
view.visible()
view.onClick { }

// Context
context.showToast("Message")

// ImageView
imageView.loadImage(url)
```

## Error Handling Strategy

### Network Errors

```kotlin
try {
    val response = apiService.getData()
    if (response.isSuccess) {
        // Handle success
    } else {
        // Handle API error (check response.code)
    }
} catch (e: Exception) {
    // Handle network/parse error
    Logger.e("Request failed", e)
}
```

### UI Errors

```kotlin
viewModel.error.observe(viewLifecycleOwner) { error ->
    if (error != null) {
        errorView.visible()
        errorMessage.text = error.message
    }
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
        val repository = UserRepository(mockApi)

        // When
        coEvery { mockApi.getUser(any()) } returns ApiResponse.success(user)

        val result = repository.getUser("123")

        // Then
        assertTrue(result.isSuccess)
    }
}
```

### Integration Tests

Test module interactions:

```kotlin
@Module
@TestInstallIn(components = AppComponent::class, replaces = NetworkModule::class)
object TestNetworkModule {
    @Provides
    @Singleton
    fun provideTestApiService(): ApiService = mockk()
}
```

## Performance Considerations

### 1. Lazy Initialization

```kotlin
class MyViewModel : ViewModel() {
    private val _apiService by lazy {
        RetrofitFactory.createService<ApiService>(baseUrl)
    }
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

## Migration Guide

### From 1.0.0 to 1.1.0

No breaking changes. New features include:

- New image transformations
- Additional view extensions

Simply update dependency version:

```gradle
implementation("com.gitee.Hoyn:android-common-lib:1.1.0")
```

## Future Considerations

### Potential Enhancements

1. **common-database** - Room database utilities
2. **common-storage** - File and preferences utilities
3. **common-camera** - Camera and image capture
4. **common-location** - Location services
5. **common-permissions** - Runtime permissions
6. **common-analytics** - Analytics tracking

### Architecture Evolution

- Consider Kotlin Flow for reactive streams
- Evaluate Paging 3 for pagination
- Investigate WorkManager for background tasks
