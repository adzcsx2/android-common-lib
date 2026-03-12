# Common Network API Documentation

## Overview

`common-network` provides a comprehensive network layer built on Retrofit and OkHttp, with automatic logging, timeout configuration, and unified response handling.

## Package: `com.hoyn.common.network`

### NetworkConfig

Configuration object for network settings.

```kotlin
object NetworkConfig {
    var connectTimeout: Long = 30
    var readTimeout: Long = 30
    var writeTimeout: Long = 30
    var isDebug: Boolean = true
}
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `connectTimeout` | `Long` | 30 | Connection timeout in seconds |
| `readTimeout` | `Long` | 30 | Read timeout in seconds |
| `writeTimeout` | `Long` | 30 | Write timeout in seconds |
| `isDebug` | `Boolean` | true | Enable debug logging |

**Example:**
```kotlin
NetworkConfig.connectTimeout = 60
NetworkConfig.readTimeout = 60
NetworkConfig.isDebug = BuildConfig.DEBUG
```

### ApiResponse

Generic API response wrapper with success checking.

```kotlin
data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
)
```

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `code` | `Int` | Response code (0 indicates success) |
| `message` | `String` | Response message |
| `data` | `T?` | Response data payload |

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `isSuccess` | `Boolean` | True if code == 0 |

#### Companion Object Methods

##### `success(data: T?): ApiResponse<T>`

Creates a success response.

```kotlin
fun <T> success(data: T?): ApiResponse<T>
```

**Example:**
```kotlin
val response = ApiResponse.success(user)
```

##### `error(code: Int, message: String): ApiResponse<T>`

Creates an error response.

```kotlin
fun <T> error(code: Int, message: String): ApiResponse<T>
```

**Example:**
```kotlin
val response = ApiResponse.error<User>(404, "User not found")
```

### OkHttpClientFactory

Factory for creating configured OkHttp clients.

```kotlin
object OkHttpClientFactory
```

#### Methods

##### `create(interceptors: List<Interceptor>, networkInterceptors: List<Interceptor>): OkHttpClient`

Creates an OkHttp client with configuration and interceptors.

```kotlin
fun create(
    interceptors: List<Interceptor> = emptyList(),
    networkInterceptors: List<Interceptor> = emptyList()
): OkHttpClient
```

**Parameters:**
- `interceptors` - Application interceptors
- `networkInterceptors` - Network interceptors

**Example:**
```kotlin
val client = OkHttpClientFactory.create(
    interceptors = listOf(authInterceptor, headerInterceptor)
)
```

### RetrofitFactory

Factory for creating Retrofit instances and API services.

```kotlin
object RetrofitFactory
```

#### Methods

##### `create(baseUrl: String, okHttpClient: OkHttpClient): Retrofit`

Creates a Retrofit instance (cached per baseUrl).

```kotlin
fun create(
    baseUrl: String,
    okHttpClient: OkHttpClient = OkHttpClientFactory.create()
): Retrofit
```

**Example:**
```kotlin
val retrofit = RetrofitFactory.create("https://api.example.com/")
```

##### `createService(baseUrl: String, serviceClass: Class<T>, okHttpClient: OkHttpClient): T`

Creates an API service implementation.

```kotlin
fun <T> createService(
    baseUrl: String,
    serviceClass: Class<T>,
    okHttpClient: OkHttpClient = OkHttpClientFactory.create()
): T
```

**Example:**
```kotlin
val apiService = RetrofitFactory.createService(
    "https://api.example.com/",
    ApiService::class.java
)
```

##### `createService(baseUrl: String, okHttpClient: OkHttpClient): T` (inline)

Creates an API service using reified type parameter.

```kotlin
inline fun <reified T> createService(
    baseUrl: String,
    okHttpClient: OkHttpClient = OkHttpClientFactory.create()
): T
```

**Example:**
```kotlin
val apiService = RetrofitFactory.createService<ApiService>("https://api.example.com/")
```

## Usage Examples

### Basic Setup

```kotlin
// Application class
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configure network
        NetworkConfig.connectTimeout = 30
        NetworkConfig.readTimeout = 30
        NetworkConfig.isDebug = BuildConfig.DEBUG
    }
}
```

### Define API Service

```kotlin
interface ApiService {
    @GET("user/info")
    suspend fun getUserInfo(): ApiResponse<UserInfo>

    @POST("user/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @GET("posts")
    suspend fun getPosts(@Query("page") page: Int): ApiResponse<List<Post>>
}
```

### Create Service and Make Requests

```kotlin
class UserRepository {
    private val apiService = RetrofitFactory.createService<ApiService>(
        "https://api.example.com/"
    )

    suspend fun getUserInfo(): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserInfo()
            if (response.isSuccess) {
                Result.success(response.data!!)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Custom Interceptors

```kotlin
class AuthInterceptor(private val tokenProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenProvider()}")
            .build()
        return chain.proceed(request)
    }
}

// Usage
val client = OkHttpClientFactory.create(
    interceptors = listOf(AuthInterceptor { getToken() })
)
val service = RetrofitFactory.createService<ApiService>(
    "https://api.example.com/",
    okHttpClient = client
)
```

## Dependencies

| Dependency | Type |
|------------|------|
| common-core | api |
| common-log | api |
| com.squareup.retrofit2:retrofit | api |
| com.squareup.retrofit2:converter-gson | api |
| com.squareup.okhttp3:okhttp | api |
| com.squareup.okhttp3:logging-interceptor | api |
| com.google.code.gson:gson | api |
| kotlinx.coroutines:core | implementation |
| kotlinx.coroutines:android | implementation |
| androidx.core:core-ktx | implementation |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.network` |
| Min SDK | 24 |
| Compile SDK | 36 |
| Java Version | 11 |

## Integration

```gradle
dependencies {
    api(project(":common-network"))
}
```

## Best Practices

1. Configure `NetworkConfig` in Application class
2. Use `BuildConfig.DEBUG` for `isDebug` setting
3. Create service instances once and reuse
4. Handle network errors gracefully
5. Use Kotlin coroutines for async operations
6. Add authentication via interceptors
7. Cache Retrofit instances per baseUrl (automatic)
