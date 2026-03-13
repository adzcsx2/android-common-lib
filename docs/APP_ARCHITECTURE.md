# App Architecture Guide

> **重要**: 后续 AI 开发者必须首先阅读此文档，了解项目架构后再进行修改。
>
> **Version**: 1.0
> **Updated**: 2026-03-13
> **Purpose**: 唯一的 App 开发基线文档

---

## 1. Project Goals

This project provides a modular Android common library with:

- Reusable architecture foundation (BaseActivity, BaseFragment, BaseViewModel)
- Common utilities (Logger, MMKV, Context extensions)
- Network layer abstraction (Retrofit + OkHttp)
- UI presentation helpers (Toast, StatusBar, Permissions)
- Image loading (Glide)

## 2. Module Responsibilities

### 2.1 common-core

**Core abstractions layer** - stable contracts and models.

| File | Purpose |
|------|---------|
| UIState.kt | Sealed class for page state (Loading/Success/Error/Empty) |
| IBaseResponse.kt | Interface for API response contracts |
| Message.kt | Data class for event communication |
| ThrowableBean.kt | Data class for error encapsulation |

**Rules:**
- Do NOT add Android-specific code here
- Do NOT add UI components here
- Keep API stable and minimal

### 2.2 common-base

**Architecture foundation layer** - base classes for app development.

| File | Purpose |
|------|---------|
| BaseActivity.kt | Base Activity with ViewBinding, coroutines, touch dispatch |
| BaseFragment.kt | Base Fragment with lazy loading, ViewBinding |
| BaseViewModel.kt | Base ViewModel with network request helpers, event dispatch |
| ViewModelFactory.kt | Factory for creating ViewModels with parameters |
| event/SingleLiveEvent.kt | One-time event stream using SharedFlow |
| event/EventBus.kt | Global event bus for cross-component communication |
| event/LifecycleObserver.kt | Lifecycle observer DSL |
| ext/ViewModelExtensions.kt | Extension functions for observing ViewModel events |

**Rules:**
- Depends on common-core and common-utils
- Do NOT depend on common-ui (avoid circular dependency)
- Do NOT add generic UI tools here (use common-ui instead)

### 2.3 common-utils

**Utilities layer** - reusable non-UI utilities.

| File | Purpose |
|------|---------|
| Logger.kt | Unified logging with debug/release support |
| MMKVUtils.kt | Key-value storage using MMKV |
| ContextExtensions.kt | Context extension functions |
| device/DeviceHelper.kt | Device information utilities |
| device/DisplayUtils.kt | Display/screen utilities |
| CoroutinesExtensions.kt | Coroutine helper functions |

**Rules:**
- Logger exists only here, do not create duplicate implementations
- Keep utilities cross-cutting and reusable

### 2.4 common-network

**Network layer** - Retrofit + OkHttp abstraction.

| File | Purpose |
|------|---------|
| RetrofitFactory.kt | Retrofit service creation |
| OkHttpClientFactory.kt | OkHttpClient configuration |
| ApiResponse.kt | Generic API response wrapper (implements IBaseResponse) |
| BaseRepository.kt | Base class for data repositories |
| ExceptionHandle.kt | Network error handling |
| LoggingInterceptor.kt | HTTP request/response logging |

**Rules:**
- ApiResponse.isSuccess() returns true when code == 0
- All API responses should implement IBaseResponse

### 2.5 common-ui

**UI presentation layer** - UI helpers and widgets.

| File | Purpose |
|------|---------|
| toast/ToastUtils.kt | Toast notification utilities |
| utils/StatusBarHelper.kt | Status bar styling |
| utils/NotchHelper.kt | Notch device support |
| utils/PressEffectHelper.kt | View press effects |
| permission/LivePermissions.kt | Runtime permission handling |
| ext/ViewExtensions.kt | View extension functions |

**Rules:**
- Depends on common-base (has access to BaseActivity, etc.)
- Do NOT add architecture classes here (they belong in common-base)

### 2.6 common-compose

**Compose foundation layer** - Jetpack Compose components and theme.

| File | Purpose |
|------|---------|
| theme/Theme.kt | App theme with light/dark mode support |
| base/BaseComposeActivity.kt | Base class for pure Compose activities |
| base/BaseComposeFragment.kt | Base class for Compose fragments (supports hybrid) |
| ext/ComposeExtensions.kt | Compose extension functions |
| ext/StateExtensions.kt | UIState observation extensions |

**Rules:**
- Depends on common-core and common-base
- Provides Material3 theme by default
- Supports Compose + View hybrid development
- Use Coil for image loading in Compose

### 2.7 common-image

**Image loading layer** - Glide abstraction.

| File | Purpose |
|------|---------|
| ImageLoader.kt | Image loading utilities |
| GlideExtensions.kt | Extension functions for image loading |

---

## 3. Dependency Direction

```
                    common-core
                         │
         ┌───────────────┼───────────────┐
         │               │               │
   common-utils    common-base    common-compose
         │               │               │
         └───────┬───────┘               │
                 │                       │
         ┌───────┴───────┐               │
         │               │               │
   common-network   common-image         │
         │               │               │
         └───────┬───────┘               │
                 │                       │
            common-ui ←──────────────────┘
                 │
              app
```

**Note**: common-compose depends on common-base, allowing the same architecture patterns in Compose.

### Forbidden Dependencies

| Module | MUST NOT depend on |
|--------|-------------------|
| common-core | Any other module |
| common-base | common-ui, common-network, common-image |
| common-utils | common-ui, common-base, common-network, common-image |
| common-network | common-ui |
| common-image | common-ui |

---

## 4. Recommended App Package Structure

```
app/
├── src/main/java/com/your/app/
│   ├── YourApplication.kt          # Application class
│   │
│   ├── data/
│   │   ├── model/                  # Data models
│   │   │   └── User.kt
│   │   ├── api/                    # API interfaces
│   │   │   └── UserApi.kt
│   │   └── repository/             # Repositories
│   │       └── UserRepository.kt
│   │
│   ├── ui/
│   │   ├── main/                   # Main page
│   │   │   ├── MainActivity.kt
│   │   │   ├── MainViewModel.kt
│   │   │   └── activity_main.xml
│   │   │
│   │   ├── user/                   # User page
│   │   │   ├── UserActivity.kt
│   │   │   ├── UserViewModel.kt
│   │   │   ├── UserRepository.kt (if complex)
│   │   │   └── activity_user.xml
│   │   │
│   │   └── base/                   # App-specific base classes (optional)
│   │       └── AppBaseActivity.kt
│   │
│   └── util/                        # App-specific utilities
│       └── AppExtensions.kt
│
└── src/main/res/
    ├── layout/
    ├── drawable/
    └── values/
```

---

## 5. Application Initialization

```kotlin
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Logger
        Logger.init(
            debug = BuildConfig.DEBUG,
            tag = getString(R.string.app_name)
        )

        // Initialize MMKV
        MMKVUtils.init(this)

        // Optional: Configure network
        NetworkConfig.connectTimeout = 30
        NetworkConfig.readTimeout = 30
        NetworkConfig.isDebug = BuildConfig.DEBUG
    }
}
```

---

## 6. BaseActivity / BaseFragment Responsibilities

### BaseActivity

```kotlin
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), CoroutineScope {

    // Provides:
    // - ViewBinding inflation via createBinding()
    // - Coroutine scope tied to Activity lifecycle
    // - Touch event dispatch to registered listeners
    // - Fragment result dispatch

    // Required implementations:
    protected abstract fun createBinding(): VB
    protected open fun initView(savedInstanceState: Bundle?) {}
    protected open fun initData() {}

    // Optional overrides:
    protected open fun onCreateBefore() {} // Before setContentView
    protected open fun onRestartNavigate() {} // After returning from another activity
    protected open fun onCleanUp() {} // Cleanup resources in onDestroy
}
```

### BaseFragment

```kotlin
abstract class BaseFragment<VB : ViewBinding> : Fragment(), CoroutineScope {

    // Provides:
    // - ViewBinding inflation via createBinding()
    // - Lazy loading support (loadData only when visible)
    // - Coroutine scope tied to Fragment view lifecycle

    // Required implementations:
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    protected open fun initView(view: View, savedInstanceState: Bundle?) {}
    protected open fun initData() {}
    protected open fun lazyLoadData() {} // Called once when fragment becomes visible
}
```

---

## 7. ViewModel Pattern

### BaseViewModel Usage

```kotlin
class UserViewModel : BaseViewModel<UserRepository>() {

    // Required: provide repository instance
    override val repository: UserRepository by lazy { UserRepository() }

    // Optional: customize exception handling
    override fun handleException(throwable: Throwable): ThrowableBean {
        val handled = ExceptionHandle.handleException(throwable)
        return ThrowableBean(handled.code, handled.errMsg)
    }

    // UI state
    private val _uiState = MutableStateFlow<UIState<User>>(UIState.Loading)
    val uiState: StateFlow<UIState<User>> = _uiState

    // Business methods
    fun loadUser(id: Int) {
        launchOnlyResult(
            block = { repository.api.getUser(id) },
            success = { user -> _uiState.value = UIState.Success(user) },
            error = { e -> _uiState.value = UIState.Error(-1, e.message ?: "Unknown") }
        )
    }

    // Convenience methods from BaseViewModel:
    // - showToast(message)
    // - showDialog(message?)
    // - dismissDialog()
    // - sendMessage(message)
}
```

### Creating ViewModel in Activity/Fragment

```kotlin
class UserActivity : BaseActivity<ActivityUserBinding>() {

    // Using ViewModelFactory
    private val viewModel: UserViewModel by lazy {
        ViewModelFactory.create(this) { UserViewModel() }
    }

    // Or using extension function
    private val viewModel: UserViewModel by lazy {
        createViewModel { UserViewModel() }
    }
}
```

---

## 8. UIState Pattern

```kotlin
// Definition (in common-core)
sealed class UIState<out T> {
    data object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val code: Int, val message: String) : UIState<Nothing>()
    data object Empty : UIState<Nothing>()
}

// Usage in Activity/Fragment
private fun renderState(state: UIState<User>) {
    when (state) {
        is UIState.Loading -> {
            showLoading()
        }
        is UIState.Success -> {
            hideLoading()
            showUser(state.data)
        }
        is UIState.Error -> {
            hideLoading()
            showError(state.message)
        }
        is UIState.Empty -> {
            hideLoading()
            showEmpty()
        }
    }
}
```

---

## 9. Event System

### Page Events (ViewModel -> Activity/Fragment)

```kotlin
// In ViewModel
class MyViewModel : BaseViewModel<MyRepository>() {
    fun doSomething() {
        showToast("Operation completed")
        showDialog("Loading...")
        // ... do work ...
        dismissDialog()
    }
}

// In Activity/Fragment
observeAllUIEvents(
    viewModel = viewModel,
    onToast = { message -> ToastUtils.show(this, message) },
    onShowDialog = { message -> showLoading(message) },
    onDismissDialog = { hideLoading() },
    onError = { error -> showError(error.errMsg) }
)
```

### Global Events (Cross-component)

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

---

## 10. Standard Page Template

### Activity Template

```kotlin
class UserActivity : BaseActivity<ActivityUserBinding>() {

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"

        fun start(context: Context, userId: Int) {
            context.startActivity(
                Intent(context, UserActivity::class.java).apply {
                    putExtra(EXTRA_USER_ID, userId)
                }
            )
        }
    }

    private val viewModel: UserViewModel by lazy { createViewModel { UserViewModel() } }
    private var userId: Int = 0

    override fun createBinding(): ActivityUserBinding {
        return ActivityUserBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        setupViews()
        observeData()
    }

    override fun initData() {
        viewModel.loadUser(userId)
    }

    private fun setupViews() {
        // Setup RecyclerView, adapters, etc.
    }

    private fun observeData() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle)
                .collect { state -> renderState(state) }
        }

        // Observe UI events
        observeAllUIEvents(
            viewModel = viewModel,
            onToast = { ToastUtils.show(this, it) },
            onShowDialog = { showLoading(it) },
            onDismissDialog = { hideLoading() }
        )
    }

    private fun renderState(state: UIState<User>) {
        // Render UI based on state
    }
}
```

### ViewModel Template

```kotlin
class UserViewModel : BaseViewModel<UserRepository>() {

    override val repository: UserRepository by lazy { UserRepository() }

    private val _uiState = MutableStateFlow<UIState<User>>(UIState.Loading)
    val uiState: StateFlow<UIState<User>> = _uiState

    override fun handleException(throwable: Throwable): ThrowableBean {
        val handled = ExceptionHandle.handleException(throwable)
        return ThrowableBean(handled.code, handled.errMsg)
    }

    fun loadUser(id: Int) {
        _uiState.value = UIState.Loading
        launchOnlyResult(
            block = { repository.api.getUser(id) },
            success = { user -> _uiState.value = UIState.Success(user) },
            error = { e -> _uiState.value = UIState.Error(-1, e.message ?: "Unknown error") }
        )
    }
}
```

### Repository Template

```kotlin
class UserRepository : BaseRepository<UserApi>() {

    companion object {
        private const val BASE_URL = "https://api.example.com/"
    }

    override val api: UserApi by lazy {
        createApi(BASE_URL, UserApi::class.java)
    }

    // Additional methods if needed
}
```

---

## 11. Adding a New Page - Step by Step

1. **Create data model** (if needed)
   ```kotlin
   // data/model/Post.kt
   data class Post(val id: Int, val title: String, val body: String)
   ```

2. **Create API interface**
   ```kotlin
   // data/api/PostApi.kt
   interface PostApi {
       @GET("posts")
       suspend fun getPosts(): ApiResponse<List<Post>>
   }
   ```

3. **Create Repository** (if complex logic needed)
   ```kotlin
   // data/repository/PostRepository.kt
   class PostRepository : BaseRepository<PostApi>() {
       override val api: PostApi by lazy {
           createApi(BASE_URL, PostApi::class.java)
       }
   }
   ```

4. **Create ViewModel**
   ```kotlin
   // ui/post/PostViewModel.kt
   class PostViewModel : BaseViewModel<PostRepository>() {
       override val repository: PostRepository by lazy { PostRepository() }
       // ... state and methods
   }
   ```

5. **Create Activity**
   ```kotlin
   // ui/post/PostActivity.kt
   class PostActivity : BaseActivity<ActivityPostBinding>() {
       // ... implementation
   }
   ```

6. **Create layout**
   ```xml
   <!-- res/layout/activity_post.xml -->
   ```

---

## 12. Do and Don't

### Do

- Use BaseActivity/BaseFragment for all pages
- Use UIState for page state management
- Use launchOnlyResult for network requests in ViewModel
- Observe UI events using observeAllUIEvents
- Use Logger for all logging (not Android Log)
- Use MMKVUtils for key-value storage
- Use ToastUtils for toast notifications
- Handle exceptions using ExceptionHandle

### Don't

- DO NOT use AppCompatActivity directly - extend BaseActivity
- DO NOT create duplicate Logger implementations
- DO NOT add architecture classes to common-ui
- DO NOT add UI utilities to common-base
- DO NOT use reflection for ViewModel/Repository creation
- DO NOT use deprecated @OnLifecycleEvent annotation
- DO NOT let common-base depend on common-ui

---

## 13. Response Success Semantics

All API responses use unified success semantics:

```kotlin
// IBaseResponse default
fun isSuccess(): Boolean = code() == 0

// ApiResponse implementation
override fun isSuccess(): Boolean = code == 0
```

When creating custom response types, ensure they follow this convention.

---

## 14. For Future AI Developers

Before making architecture-related changes:

1. **READ THIS DOCUMENT FIRST**
2. Read `/docs/AI_HANDOFF.md` for decision history
3. Follow the module boundaries defined here
4. Do not reopen settled architecture decisions without explicit user request
5. Keep common-base focused on architecture, not utilities
6. Keep common-ui focused on presentation, not architecture

---

## 15. Quick Reference

| Task | Module | Key Classes |
|------|--------|-------------|
| Add new page (View) | app | BaseActivity, BaseViewModel, UIState |
| Add new page (Compose) | app | BaseComposeActivity, BaseViewModel, UIState |
| Add network API | app + common-network | RetrofitFactory, ApiResponse |
| Add utility | common-utils | Logger, MMKVUtils |
| Add UI helper | common-ui | ToastUtils, StatusBarHelper |
| Add architecture class | common-base | BaseActivity, BaseViewModel |
| Add Compose component | common-compose | BaseComposeActivity, Theme |
| Add Compose component | common-compose | BaseComposeActivity, Theme |

---

## 13. Compose + View Hybrid Development

This project supports mixing Compose and traditional Views in the same app.

### Pure Compose Activity

```kotlin
class MyComposeActivity : BaseComposeActivity() {
    @Composable
    override fun Content() {
        val viewModel: MyViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        MyScreen(state = state)
    }
}
```

### Compose Fragment (in traditional Activity)

```kotlin
class MyHybridFragment : BaseComposeFragment() {
    // The composeView is automatically created
    // You can add it to any ViewGroup
}
```

### Using Theme Colors

```kotlin
@Composable
fun MyScreen(state: UIState<User>) {
    val colors = useAppThemeColors()

    when (state) {
        is UIState.Loading -> CircularProgressIndicator(color = colors.primary)
        is UIState.Success -> UserCard(state.data)
        is UIState.Error -> ErrorMessage(state.message)
        is UIState.Empty -> EmptyView()
    }
}
```

### Image Loading in Compose

```kotlin
// Using Coil (included in common-compose)
AsyncImage(
    model = Model.Builder()
        .data("https://example.com/image.jpg")
        .build(),
    contentScale = ContentScale.Crop,
    modifier = Modifier.size(128.dp, 128.dp)
)
```

---

## 14. For Future AI Developers

**Document End**

> This document is the single source of truth for app architecture. Update it when making architectural changes.
