# Common UI API Documentation

## Overview

`common-ui` provides base classes and extension functions for Activities, Fragments, and Views, simplifying Android UI development.

## Package: `com.hoyn.common.ui`

### BaseActivity

Generic base class for Activities with ViewBinding support.

```kotlin
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity()
```

#### Abstract Methods

##### `createBinding(): VB`

Create and return the ViewBinding instance.

```kotlin
protected abstract fun createBinding(): VB
```

#### Lifecycle Methods

##### `initView(savedInstanceState: Bundle?)`

Called after binding is created. Override to initialize views.

```kotlin
protected open fun initView(savedInstanceState: Bundle?) {}
```

##### `initData()`

Called after `initView`. Override to initialize data.

```kotlin
protected open fun initData() {}
```

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `binding` | `VB` | The ViewBinding instance |

#### Usage Example

```kotlin
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun createBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.titleTextView.text = "Hello"
        binding.submitButton.onClick { handleSubmit() }
    }

    override fun initData() {
        viewModel.loadData()
    }

    private fun handleSubmit() {
        // Handle submit
    }
}
```

### BaseFragment

Generic base class for Fragments with ViewBinding support.

```kotlin
abstract class BaseFragment<VB : ViewBinding> : Fragment()
```

#### Abstract Methods

##### `createBinding(inflater: LayoutInflater, container: ViewGroup?): VB`

Create and return the ViewBinding instance.

```kotlin
protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
```

#### Lifecycle Methods

##### `initView(view: View, savedInstanceState: Bundle?)`

Called after view is created. Override to initialize views.

```kotlin
protected open fun initView(view: View, savedInstanceState: Bundle?) {}
```

##### `initData()`

Called after `initView`. Override to initialize data.

```kotlin
protected open fun initData() {}
```

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `binding` | `VB` | The ViewBinding instance (valid between onCreateView and onDestroyView) |

#### Usage Example

```kotlin
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView(view: View, savedInstanceState: Bundle?) {
        binding.messageTextView.text = "Welcome"
        binding.refreshButton.onClick { refresh() }
    }

    override fun initData() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            binding.recyclerView.adapter = ItemsAdapter(items)
        }
    }

    private fun refresh() {
        viewModel.refresh()
    }
}
```

### View Extensions

Extension functions for View objects in `com.hoyn.common.ui.ext`.

#### Visibility Extensions

##### `visible()`

Set view visibility to VISIBLE.

```kotlin
fun View.visible()
```

##### `invisible()`

Set view visibility to INVISIBLE.

```kotlin
fun View.invisible()
```

##### `gone()`

Set view visibility to GONE.

```kotlin
fun View.gone()
```

##### `isVisible(): Boolean`

Check if view is visible.

```kotlin
fun View.isVisible(): Boolean
```

##### `setVisible(visible: Boolean)`

Set view visibility based on boolean.

```kotlin
fun View.setVisible(visible: Boolean)
```

**Examples:**
```kotlin
// Show/hide loading
loadingView.visible()
loadingView.gone()

// Conditional visibility
errorView.setVisible(hasError)

// Check visibility
if (submitButton.isVisible()) {
    // Button is visible
}
```

#### Click Extensions

##### `onClick(action: (View) -> Unit)`

Set click listener.

```kotlin
fun View.onClick(action: (View) -> Unit)
```

##### `onLongClick(action: (View) -> Boolean)`

Set long click listener.

```kotlin
fun View.onLongClick(action: (View) -> Boolean)
```

**Examples:**
```kotlin
submitButton.onClick { view ->
    handleSubmit()
}

deleteButton.onLongClick { view ->
    showDeleteConfirmation()
    true // Consumed
}
```

#### Enabled State Extensions

##### `enable()`

Enable the view.

```kotlin
fun View.enable()
```

##### `disable()`

Disable the view.

```kotlin
fun View.disable()
```

**Examples:**
```kotlin
// Enable/disable based on state
submitButton.enable()
submitButton.disable()

// Conditional
submitButton.setEnabled(isFormValid)
```

## Usage Examples

### Complete Activity Example

```kotlin
class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    private val viewModel by viewModels<LoginViewModel>()

    override fun createBinding() = ActivityLoginBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.loginButton.onClick { attemptLogin() }
        binding.forgotPasswordButton.onClick { showForgotPassword() }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.setVisible(isLoading)
            binding.loginButton.setEnabled(!isLoading)
        }
    }

    override fun initData() {
        // Check if user is already logged in
        viewModel.checkAuthStatus()
    }

    private fun attemptLogin() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        viewModel.login(email, password)
    }

    private fun showForgotPassword() {
        // Navigate to forgot password screen
    }
}
```

### Complete Fragment Example

```kotlin
class ProductListFragment : BaseFragment<FragmentProductListBinding>() {
    private val viewModel by viewModels<ProductListViewModel>()
    private lateinit var adapter: ProductAdapter

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProductListBinding.inflate(inflater, container, false)

    override fun initView(view: View, savedInstanceState: Bundle?) {
        adapter = ProductAdapter { product ->
            viewModel.onProductClicked(product)
        }

        binding.recyclerView.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.retryButton.onClick {
            viewModel.retry()
        }
    }

    override fun initData() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorView.visible()
                binding.recyclerView.gone()
            } else {
                binding.errorView.gone()
                binding.recyclerView.visible()
            }
        }

        viewModel.loadProducts()
    }
}
```

### View Extensions in Action

```kotlin
class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        val loadingView = findViewById<View>(R.id.loadingView)
        val contentView = findViewById<View>(R.id.contentView)
        val errorView = findViewById<View>(R.id.errorView)
        val submitButton = findViewById<Button>(R.id.submitButton)

        // Show loading
        loadingView.visible()
        contentView.gone()
        errorView.gone()

        // Handle button click
        submitButton.onClick {
            submitButton.disable()
            // Do work
        }

        // Handle long press
        submitButton.onLongClick {
            showToast("Hold for more options")
            true
        }
    }
}
```

## Dependencies

| Dependency | Type |
|------------|------|
| common-core | api |
| androidx.appcompat | api |
| androidx.recyclerview | api |
| androidx.activity:activity-ktx | api |
| androidx.fragment:fragment-ktx | api |
| androidx.lifecycle:lifecycle-runtime-ktx | api |
| androidx.lifecycle:lifecycle-viewmodel-ktx | api |
| com.google.android.material | api |
| kotlinx.coroutines:core | implementation |
| kotlinx.coroutines:android | implementation |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.ui` |
| Min SDK | 24 |
| Compile SDK | 34 |
| Java Version | 11 |
| ViewBinding | Enabled |

## Integration

```gradle
dependencies {
    api(project(":common-ui"))
}
```

## Best Practices

### BaseActivity

1. Always use ViewBinding for type-safe view access
2. Separate view initialization (`initView`) from data loading (`initData`)
3. Use view models for data management
4. Keep activity logic minimal

### BaseFragment

1. Be aware of binding lifecycle (null after onDestroyView)
2. Use viewLifecycleOwner for LiveData observation
3. Don't hold references to views after onDestroyView

### View Extensions

1. Use `setVisible()` for conditional visibility
2. Disable buttons during async operations
3. Use `gone()` instead of `invisible()` to remove from layout
4. Leverage Kotlin lambdas for click listeners

### Common Patterns

1. **Loading State**: Show progress, disable buttons
2. **Error State**: Show error message, hide content
3. **Empty State**: Show empty message when no data
4. **Success State**: Hide loading, show content
