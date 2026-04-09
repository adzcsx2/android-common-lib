# Common Utils API Documentation

## Overview

`common-utils` provides Toast, Context, and Coroutine helpers for common Android development tasks.

## Package: `com.hoyn.common.utils`

### Toast

#### `ToastUtils`

Unified toast facade built on Toaster. It does not require page `Context` and should be initialized from `Application`.

```kotlin
ToastUtils.init(application)
ToastUtils.show("Hello, World!")
ToastUtils.showLong("Long message")
ToastUtils.showCenter("Centered message")
ToastUtils.debugShow("Only visible in debug builds")
```

In `BaseActivity`, `BaseFragment`, and `BaseDialogFragment`, prefer using the built-in `toast` shortcut.

### Context Extensions

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

| Dependency                     | Type           |
| ------------------------------ | -------------- |
| common-core                    | api            |
| androidx.core:core-ktx         | implementation |
| androidx.activity:activity-ktx | implementation |
| androidx.fragment:fragment-ktx | implementation |
| kotlinx.coroutines:core        | implementation |
| kotlinx.coroutines:android     | implementation |

## Module Information

| Property     | Value                   |
| ------------ | ----------------------- |
| Package      | `com.hoyn.common.utils` |
| Min SDK      | 24                      |
| Compile SDK  | 34                      |
| Java Version | 17                      |

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
