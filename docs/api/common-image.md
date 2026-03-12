# Common Image API Documentation

## Overview

`common-image` provides a simple yet powerful image loading solution built on Glide, with convenient extension functions for ImageView.

## Package: `com.hoyn.common.image`

### ImageLoader

Core image loading utility based on Glide.

```kotlin
object ImageLoader
```

#### Methods

##### `load(context: Context, url: String?, imageView: ImageView, options: RequestOptions.() -> Unit)`

Load an image from URL into ImageView.

```kotlin
fun load(
    context: Context,
    url: String?,
    imageView: ImageView,
    options: RequestOptions.() -> Unit = {}
)
```

**Parameters:**
- `context` - Context
- `url` - Image URL
- `imageView` - Target ImageView
- `options` - Optional RequestOptions configuration

**Example:**
```kotlin
ImageLoader.load(context, "https://example.com/image.jpg", imageView) {
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
}
```

##### `load(context: Context, resourceId: Int, imageView: ImageView, options: RequestOptions.() -> Unit)`

Load an image from resource ID.

```kotlin
fun load(
    context: Context,
    resourceId: Int,
    imageView: ImageView,
    options: RequestOptions.() -> Unit = {}
)
```

**Example:**
```kotlin
ImageLoader.load(context, R.drawable.avatar, imageView)
```

##### `clear(context: Context)`

Clear memory cache.

```kotlin
fun clear(context: Context)
```

**Example:**
```kotlin
ImageLoader.clear(context)
```

##### `clearDiskCache(context: Context)`

Clear disk cache (runs on background thread).

```kotlin
fun clearDiskCache(context: Context)
```

**Example:**
```kotlin
ImageLoader.clearDiskCache(context)
```

### Image Extensions

Extension functions for ImageView for simplified image loading.

#### `loadImage(url: String?, placeholder: Int, error: Int)`

Load image from URL with optional placeholder and error drawables.

```kotlin
fun ImageView.loadImage(
    url: String?,
    placeholder: Int = 0,
    error: Int = 0
)
```

**Example:**
```kotlin
imageView.loadImage("https://example.com/image.jpg")
imageView.loadImage(url, R.drawable.placeholder, R.drawable.error)
```

#### `loadImage(resourceId: Int, placeholder: Int, error: Int)`

Load image from resource ID.

```kotlin
fun ImageView.loadImage(
    resourceId: Int,
    placeholder: Int = 0,
    error: Int = 0
)
```

**Example:**
```kotlin
imageView.loadImage(R.drawable.profile_image)
```

#### `loadCircleImage(url: String?, placeholder: Int, error: Int)`

Load a circular cropped image.

```kotlin
fun ImageView.loadCircleImage(
    url: String?,
    placeholder: Int = 0,
    error: Int = 0
)
```

**Example:**
```kotlin
avatarImageView.loadCircleImage(user.avatarUrl, R.drawable.default_avatar)
```

#### `loadRoundedImage(url: String?, radius: Int, placeholder: Int, error: Int)`

Load an image with rounded corners.

```kotlin
fun ImageView.loadRoundedImage(
    url: String?,
    radius: Int,
    placeholder: Int = 0,
    error: Int = 0
)
```

**Example:**
```kotlin
imageView.loadRoundedImage(url, radius = 16)
```

## Usage Examples

### Basic Image Loading

```kotlin
class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.loadImage(item.imageUrl)
    }
}
```

### Avatar/Profile Image

```kotlin
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load circular avatar
        avatarView.loadCircleImage(user.avatarUrl, R.drawable.ic_person)
    }
}
```

### Product Image with Placeholder

```kotlin
class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productImage.loadImage(
            product.imageUrl,
            placeholder = R.drawable.product_placeholder,
            error = R.drawable.image_error
        )
    }
}
```

### Using ImageLoader Directly

```kotlin
class AdvancedImageLoading {
    fun loadImageWithOptions(imageView: ImageView) {
        ImageLoader.load(context, "https://example.com/image.jpg", imageView) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
            centerCrop()
            transform(RoundedCorners(16))
        }
    }
}
```

### Memory Management

```kotlin
class MyApplication : Application() {
    override fun onLowMemory() {
        super.onLowMemory()
        ImageLoader.clear(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            ImageLoader.clear(this)
        }
    }
}
```

## Dependencies

| Dependency | Type |
|------------|------|
| common-core | api |
| common-utils | api |
| com.github.bumptech.glide:glide | api |
| androidx.core:core-ktx | implementation |

## Module Information

| Property | Value |
|---------|-------|
| Package | `com.hoyn.common.image` |
| Min SDK | 24 |
| Compile SDK | 36 |
| Java Version | 11 |

## Integration

```gradle
dependencies {
    api(project(":common-image"))
}
```

## Best Practices

1. Use extension functions for simple cases
2. Use `ImageLoader` directly for advanced configuration
3. Always provide placeholders for better UX
4. Provide error drawables for failed loads
5. Use `loadCircleImage` for avatars/profiles
6. Clear cache in `onLowMemory()` callbacks
7. Consider image sizes for your use case
8. Use appropriate image formats (WebP recommended)

## Glide Configuration

This module uses Glide defaults. For advanced configuration:

```kotlin
@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Custom configuration
    }
}
```
