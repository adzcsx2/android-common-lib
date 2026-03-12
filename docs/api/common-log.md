# Common Log API Documentation

## Overview

`common-log` provides a unified logging interface with debug/release mode support and formatted output capabilities.

## Package: `com.hoyn.common.log`

### Logger

A singleton object providing logging functionality with automatic tag management and debug mode control.

```kotlin
object Logger
```

### Initialization

#### `init(debug: Boolean, tag: String = "CommonLib")`

Initialize the logger with debug mode and default tag.

```kotlin
fun init(debug: Boolean, tag: String = "CommonLib")
```

**Parameters:**
- `debug` - Enable/disable debug logging (when false, only errors are logged)
- `tag` - Default tag for all log messages (default: "CommonLib")

**Example:**
```kotlin
// In Application onCreate
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init(debug = BuildConfig.DEBUG, tag = "MyApp")
    }
}
```

### Logging Methods

#### `v(message: String, tag: String = defaultTag)`

Log a verbose message.

```kotlin
fun v(message: String, tag: String = defaultTag)
```

**Parameters:**
- `message` - The message to log
- `tag` - Optional tag (uses default if not specified)

**Example:**
```kotlin
Logger.v("Verbose trace information")
Logger.v("Custom tag message", tag = "Network")
```

#### `d(message: String, tag: String = defaultTag)`

Log a debug message.

```kotlin
fun d(message: String, tag: String = defaultTag)
```

**Example:**
```kotlin
Logger.d("Debug information")
Logger.d("User logged in", tag = "Auth")
```

#### `i(message: String, tag: String = defaultTag)`

Log an info message.

```kotlin
fun i(message: String, tag: String = defaultTag)
```

**Example:**
```kotlin
Logger.i("Application started")
Logger.i("Sync completed", tag = "SyncService")
```

#### `w(message: String, tag: String = defaultTag)`

Log a warning message.

```kotlin
fun w(message: String, tag: String = defaultTag)
```

**Example:**
```kotlin
Logger.w("Deprecated method used")
Logger.w("Cache miss", tag = "Cache")
```

#### `e(message: String, tag: String = defaultTag)`

Log an error message.

```kotlin
fun e(message: String, tag: String = defaultTag)
```

**Example:**
```kotlin
Logger.e("Failed to load data")
Logger.e("Connection timeout", tag = "Network")
```

#### `e(message: String, throwable: Throwable, tag: String = defaultTag)`

Log an error message with exception.

```kotlin
fun e(message: String, throwable: Throwable, tag: String = defaultTag)
```

**Parameters:**
- `message` - Error message
- `throwable` - Exception to log
- `tag` - Optional tag

**Example:**
```kotlin
try {
    riskyOperation()
} catch (e: Exception) {
    Logger.e("Operation failed", e)
    Logger.e("Database error", e, tag = "DB")
}
```

#### `json(json: String, tag: String = defaultTag)`

Log a JSON string with formatting.

```kotlin
fun json(json: String, tag: String = defaultTag)
```

**Example:**
```kotlin
val jsonString = """{"name":"John","age":30}"""
Logger.json(jsonString)
Logger.json(apiResponse, tag = "API")
```

## Behavior

### Debug Mode

When `debug = true`:
- All log levels (v, d, i, w, e) are enabled
- JSON formatting is applied
- Full logging output

### Release Mode

When `debug = false`:
- Only error logs (e) are output
- Verbose, debug, info, and warning messages are suppressed
- Critical errors are always logged

## Dependencies

| Dependency | Type |
|------------|------|
| common-core | api |
| androidx.core:core-ktx | implementation |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.log` |
| Min SDK | 24 |
| Compile SDK | 36 |
| Java Version | 11 |

## Integration

```gradle
dependencies {
    api(project(":common-log"))
}
```

## Usage Examples

### Basic Usage

```kotlin
class MyRepository {
    fun loadData() {
        Logger.i("Loading data...")
        try {
            val data = apiService.fetchData()
            Logger.json(data.toString())
        } catch (e: Exception) {
            Logger.e("Failed to load data", e)
        }
    }
}
```

### Custom Tags

```kotlin
class NetworkManager {
    fun connect() {
        Logger.d("Connecting to server...", tag = "Network")
    }

    fun disconnect() {
        Logger.d("Disconnected", tag = "Network")
    }
}
```

### Best Practices

1. Initialize in Application class
2. Use `BuildConfig.DEBUG` for debug mode
3. Use descriptive tags for different modules
4. Log errors with exceptions for debugging
5. Use JSON logging for API responses
