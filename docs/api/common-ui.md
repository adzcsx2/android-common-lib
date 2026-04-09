# Common UI API Documentation

## Overview

`common-ui` provides base classes and extension functions for Activities, Fragments, and Views, simplifying Android UI development.

## Package: `com.hoyn.common.ui`

### ToastUtils

Toast 统一入口，基于 Toaster 库封装。统一使用 Application 初始化，避免页面级 Context 泄露。

```kotlin
object ToastUtils {
    fun init(application: Application, config: (ToastConfig.() -> Unit)? = null)
    fun show(message: CharSequence?, config: ToastConfig.() -> Unit = {})
    fun show(@StringRes messageRes: Int, config: ToastConfig.() -> Unit = {})
    fun showLong(message: CharSequence?, config: ToastConfig.() -> Unit = {})
    fun showCenter(message: CharSequence?, config: ToastConfig.() -> Unit = {})
    fun showGlobal(message: CharSequence?, config: ToastConfig.() -> Unit = {})
    fun debugShow(message: CharSequence?)
    fun delayedShow(message: CharSequence?, delayMillis: Long, config: ToastConfig.() -> Unit = {})
    fun cancel()
    fun setGlobalStyle(style: IToastStyle<*>)
    fun showWithStyle(message: CharSequence?, style: IToastStyle<*>, config: ToastConfig.() -> Unit = {})
    fun showWithLayout(message: CharSequence?, @LayoutRes layoutRes: Int, gravity: Int = Gravity.CENTER, config: ToastConfig.() -> Unit = {})
    fun setGravity(gravity: Int, xOffset: Int = 0, yOffset: Int = ToastConfig.defaults().yOffset)
    fun resetGravity()
}
```

#### Methods

| Method | Description |
|--------|-------------|
| `init(app, config?)` | 初始化 ToastUtils，必须在 Application.onCreate() 中调用 |
| `show(message, config)` | 显示短时 Toast（底部），仅 Debug 包生效 |
| `showLong(message, config)` | 显示长时 Toast（底部），仅 Debug 包生效 |
| `showCenter(message, config)` | 显示短时 Toast（居中），仅 Debug 包生效 |
| `showGlobal(message, config)` | 显示全局优先级 Toast，不会被覆盖 |
| `debugShow(message)` | 显示 Debug 专用 Toast |
| `delayedShow(message, delay)` | 延迟显示 Toast |
| `cancel()` | 取消当前 Toast |
| `setGlobalStyle(style)` | 设置全局 Toast 样式 |
| `showWithStyle(message, style)` | 使用指定样式显示 Toast |
| `showWithLayout(message, layoutRes)` | 使用自定义布局显示 Toast |
| `setGravity(gravity, x, y)` | 设置默认显示位置 |
| `resetGravity()` | 重置为默认底部位置 |

#### Usage Example

```kotlin
// 初始化（Application 中）
ToastUtils.init(this) {
    gravity = Gravity.CENTER
    stackSkips = 2
}

// 短时间显示
ToastUtils.show("提示信息")

// 长时间显示
ToastUtils.showLong("长提示")

// 居中显示
ToastUtils.showCenter("居中提示")

// 单次调用覆盖配置
ToastUtils.show("提示") {
    xOffset = 10
    yOffset = 100
}

// 在 BaseActivity/BaseFragment 中使用 toast 扩展
toast("操作成功")
```

### ToastConfig

Toast 配置类，支持 DSL 风格配置。

```kotlin
class ToastConfig private constructor(
    var gravity: Int,      // 显示位置，默认 Gravity.BOTTOM
    var xOffset: Int,      // X 轴偏移量（px），默认 0
    var yOffset: Int,      // Y 轴偏移量（px），默认底部 64dp
    var stackSkips: Int    // 堆栈跳过层数，默认 0
)
```

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
| com.hjq.toast:Toaster | implementation |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.ui` |
| Min SDK | 24 |
| Compile SDK | 34 |
| Java Version | 17 |
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
