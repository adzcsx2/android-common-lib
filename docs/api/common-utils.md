# Common Utils API Documentation

## Overview

`common-utils` provides extension functions for Context and Coroutines, simplifying common Android development tasks.

## Package: `com.hoyn.common.utils`

### Context Extensions

#### `showToast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT)`

Displays a toast message with the specified text.

```kotlin
fun Context.showToast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT)
```

**Parameters:**
- `message` - The text message to display
- `duration` - Duration to show the toast (default: `Toast.LENGTH_SHORT`)

**Example:**
```kotlin
context.showToast("Hello, World!")
context.showToast("Long message", Toast.LENGTH_LONG)
```

#### `showToast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT)`

Displays a toast message using a string resource.

```kotlin
fun Context.showToast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT)
```

**Parameters:**
- `messageRes` - String resource ID
- `duration` - Duration to show the toast (default: `Toast.LENGTH_SHORT`)

**Example:**
```kotlin
context.showToast(R.string.error_message)
```

### Coroutine Extensions

#### `launchIO(block: suspend CoroutineScope.() -> Unit)`

Launches a coroutine in the IO dispatcher.

```kotlin
fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit)
```

**Example:**
```kotlin
lifecycleScope.launchIO {
    // IO operations like database or network
    val data = fetchDataFromDatabase()
}
```

#### `launchMain(block: suspend CoroutineScope.() -> Unit)`

Launches a coroutine in the Main dispatcher.

```kotlin
fun CoroutineScope.launchMain(block: suspend CoroutineScope.() -> Unit)
```

**Example:**
```kotlin
lifecycleScope.launchMain {
    // UI operations
    updateUI()
}
```

#### `withIO(block: suspend CoroutineScope.() -> T): T`

Switches to IO dispatcher, executes the block, and returns the result.

```kotlin
suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T
```

**Example:**
```kotlin
val result = withIO {
    // Perform IO operation
    readFileContent()
}
```

#### `withMain(block: suspend CoroutineScope.() -> T): T`

Switches to Main dispatcher, executes the block, and returns the result.

```kotlin
suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T
```

**Example:**
```kotlin
withMain {
    // Update UI
    textView.text = "Loaded"
}
```

## Dependencies

| Dependency | Type |
|------------|------|
| common-core | api |
| androidx.core:core-ktx | implementation |
| androidx.activity:activity-ktx | implementation |
| androidx.fragment:fragment-ktx | implementation |
| kotlinx.coroutines:core | implementation |
| kotlinx.coroutines:android | implementation |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.utils` |
| Min SDK | 24 |
| Compile SDK | 36 |
| Java Version | 11 |

## Integration

```gradle
dependencies {
    api(project(":common-utils"))
}
```

## Usage Examples

### Complete Example

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show a toast
        showToast("Welcome!")

        // Launch coroutine for IO work
        lifecycleScope.launchIO {
            val data = fetchData()
            withMain {
                showToast("Data loaded: ${data.size} items")
            }
        }
    }

    suspend fun fetchData(): List<Item> = withIO {
        // Simulate IO operation
        database.getAllItems()
    }
}
```
