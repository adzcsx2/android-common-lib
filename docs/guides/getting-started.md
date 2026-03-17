# Getting Started Guide

## Introduction

Welcome to Android Common Library! This guide will help you integrate and use the library in your Android project.

## Installation

### Step 1: Add Repository

In your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add Dependency

In your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.adzcsx2.android-common-lib:common-all:1.0.5")
}
```

Need a single module only?

```kotlin
dependencies {
    implementation("com.github.adzcsx2.android-common-lib:common-base:1.0.5")
}
```

### Step 3: Sync Project

Sync your Gradle files to download the library.

## Initial Setup

### Application Configuration

Create or update your `Application` class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Logger
        Logger.init(debug = BuildConfig.DEBUG, tag = "MyApp")

        // Configure Network
        NetworkConfig.connectTimeout = 30
        NetworkConfig.readTimeout = 30
        NetworkConfig.writeTimeout = 30
        NetworkConfig.isDebug = BuildConfig.DEBUG
    }
}
```

Don't forget to register in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
    ...
</application>
```

## Module-by-Module Guide

### 1. Using common-utils Logger

The logger provides simple, debug-aware logging:

```kotlin
// Basic logging
Logger.d("Debug message")
Logger.i("Info message")
Logger.w("Warning message")
Logger.e("Error message")

// With exception
try {
    riskyOperation()
} catch (e: Exception) {
    Logger.e("Operation failed", e)
}

// JSON logging
val json = """{"name":"John","age":30}"""
Logger.json(json)

// Custom tag
Logger.d("Custom tag message", tag = "Network")
```

### 2. Using common-utils

Context and coroutine extensions:

```kotlin
// Show toast
context.showToast("Hello, World!")
showToast(R.string.error_message)

// Coroutine dispatchers
lifecycleScope.launchIO {
    // IO work
    val data = database.getAllItems()
    withMain {
        // Update UI
        adapter.submitList(data)
    }
}
```

### 3. Using common-network

Set up your API service:

```kotlin
interface ApiService {
    @GET("posts")
    suspend fun getPosts(): ApiResponse<List<Post>>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): ApiResponse<Post>

    @POST("posts")
    suspend fun createPost(@Body post: Post): ApiResponse<Post>
}
```

Create and use the service:

```kotlin
class PostRepository {
    private val apiService = RetrofitFactory.createService<ApiService>(
        "https://jsonplaceholder.typicode.com/"
    )

    suspend fun getPosts(): Result<List<Post>> = withIO {
        try {
            val response = apiService.getPosts()
            if (response.isSuccess) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Logger.e("Failed to fetch posts", e)
            Result.failure(e)
        }
    }
}
```

### 4. Using common-image

Load images easily:

```kotlin
// Basic loading
imageView.loadImage("https://example.com/image.jpg")

// With placeholder and error
imageView.loadImage(
    url,
    placeholder = R.drawable.placeholder,
    error = R.drawable.error
)

// Load from resources
imageView.loadImage(R.drawable.profile_image)

// Circular images (avatars)
avatarView.loadCircleImage(user.avatarUrl)

// Rounded corners
imageView.loadRoundedImage(url, radius = 16)
```

### 5. Using common-base

#### BaseActivity

```kotlin
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val viewModel by viewModels<MainViewModel>()

    override fun createBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.titleTextView.text = "Home"
        binding.refreshButton.onClick { viewModel.refresh() }
    }

    override fun initData() {
        viewModel.items.observe(this) { items ->
            binding.recyclerView.adapter = MyAdapter(items)
        }
    }
}
```

### 6. State Management Recommendation

For new screens, prefer `UIState` with `Flow/StateFlow`. Keep `LiveData`-based APIs for existing pages that already depend on `BaseViewModel` event streams.

#### BaseFragment

```kotlin
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel by viewModels<HomeViewModel>()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView(view: View, savedInstanceState: Bundle?) {
        binding.messageTextView.text = "Welcome"
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

#### View Extensions

```kotlin
// Visibility
loadingView.visible()
errorView.gone()
contentView.setVisible(isContentLoaded)

// Click listeners
submitButton.onClick { handleSubmit() }
deleteButton.onLongClick { showDeleteDialog() }

// Enabled state
submitButton.enable()
submitButton.disable()
submitButton.setEnabled(isFormValid)
```

## Complete Example

Here's a complete example showing all modules working together:

```kotlin
class PostListActivity : BaseActivity<ActivityPostListBinding>() {
    private val viewModel by viewModels<PostListViewModel>()

    override fun createBinding() = ActivityPostListBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        binding.retryButton.onClick {
            viewModel.retry()
        }
    }

    override fun initData() {
        viewModel.posts.observe(this) { posts ->
            binding.recyclerView.adapter = PostAdapter(posts) { post ->
                showPostDetail(post)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.retryButton.setVisible(!isLoading)
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.errorView.visible()
                binding.recyclerView.gone()
                binding.errorText.text = error.message
            } else {
                binding.errorView.gone()
                binding.recyclerView.visible()
            }
        }

        viewModel.loadPosts()
    }

    private fun showPostDetail(post: Post) {
        // Navigate to detail
    }
}

class PostRepository {
    private val apiService = RetrofitFactory.createService<ApiService>(
        "https://jsonplaceholder.typicode.com/"
    )

    suspend fun getPosts(): Result<List<Post>> = withIO {
        try {
            val response = apiService.getPosts()
            if (response.isSuccess) {
                Logger.i("Loaded ${response.data?.size ?: 0} posts")
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Logger.e("Failed to load posts", e)
            Result.failure(e)
        }
    }
}

class PostAdapter(
    private val posts: List<Post>,
    private val onItemClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.titleText.text = post.title
            binding.bodyText.text = post.body

            // Load thumbnail if available
            if (post.thumbnailUrl != null) {
                binding.thumbnailView.loadImage(
                    post.thumbnailUrl,
                    placeholder = R.drawable.placeholder
                )
            } else {
                binding.thumbnailView.gone()
            }

            binding.root.onClick { onItemClick(post) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size
}
```

## Best Practices

### 1. Initialize Once

Initialize Logger and NetworkConfig in your Application class, not Activities or Fragments.

### 2. Use ViewBinding

Always use ViewBinding with BaseActivity and BaseFragment for type-safe view access.

### 3. Handle Errors Gracefully

Always handle network errors and show appropriate feedback to users.

### 4. Clear Resources

Clear image cache in onLowMemory:

```kotlin
override fun onLowMemory() {
    super.onLowMemory()
    ImageLoader.clear(this)
}
```

### 5. Use Coroutines

Use coroutine extensions for clean async code:

```kotlin
lifecycleScope.launchIO {
    val result = repository.getData()
    withMain {
        updateUI(result)
    }
}
```

## Troubleshooting

### Dependency Conflicts

If you encounter dependency conflicts, use exclude:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-all:1.0.5") {
    exclude(group = "com.google.android.material")
}
```

### ProGuard Rules

Add to your proguard-rules.pro:

```proguard
# Common Library
-keep class com.hoyn.common.** { *; }
-keep class com.bumptech.glide.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
```

### Network Issues

If network requests fail:

1. Check Internet permission in manifest
2. Verify NetworkConfig settings
3. Enable debug logging to see details
4. Check baseUrl is correct

## Next Steps

- Explore [API Documentation](/docs/api/) for detailed API reference
- Read [Architecture Documentation](/docs/ARCHITECTURE.md) to understand design
- Check [Examples](/docs/guides/examples.md) for more usage examples

## Support

For issues, questions, or contributions:

- GitHub: [Report an issue](https://github.com/adzcsx2/android-common-lib/issues)
- Documentation: [Full docs](/docs/)
