# Common Image API Documentation

## Overview

`common-image` provides a Glide-based image loading solution with lifecycle-aware APIs and convenient ImageView extension functions.

## Package: `com.hoyn.common.image`

### GlideUtils

Core image loading utility based on Glide.

```kotlin
object GlideUtils
```

#### Methods

##### `load(imageView: ImageView, url: String?, options: RequestOptions.() -> Unit)`

Load an image from URL into ImageView.

```kotlin
fun load(
    imageView: ImageView,
    url: String?,
    options: RequestOptions.() -> Unit = {}
)
```

**Parameters:**
- `url` - Image URL
- `imageView` - Target ImageView
- `options` - Optional RequestOptions configuration

**Notes:**
- Requests are bound to `imageView` instead of a plain `context`.
- This gives Glide the correct view lifecycle for recycling and cleanup.

**Example:**
```kotlin
GlideUtils.load(imageView, "https://example.com/image.jpg") {
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
}
```

##### `load(imageView: ImageView, resourceId: Int, options: RequestOptions.() -> Unit)`

Load an image from resource ID.

```kotlin
fun load(
    imageView: ImageView,
    resourceId: Int,
    options: RequestOptions.() -> Unit = {}
)
```

**Example:**
```kotlin
GlideUtils.load(imageView, R.drawable.avatar)
```

##### `loadCircle(imageView: ImageView, url: String?, options: RequestOptions.() -> Unit)`

Load a circular cropped network image.

```kotlin
fun loadCircle(
    imageView: ImageView,
    url: String?,
    options: RequestOptions.() -> Unit = {}
)
```

##### `loadRounded(imageView: ImageView, url: String?, radiusPx: Int, options: RequestOptions.() -> Unit)`

Load a rounded image with `CenterCrop` and `RoundedCorners`.

```kotlin
fun loadRounded(
    imageView: ImageView,
    url: String?,
    radiusPx: Int,
    options: RequestOptions.() -> Unit = {}
)
```

**Parameters:**
- `radiusPx` - Corner radius in pixels, usually passed as `10.dp`

##### `clear(imageView: ImageView)`

Clear the current Glide request bound to an ImageView.

```kotlin
fun clear(imageView: ImageView)
```

##### `clearMemory(context: Context)`

Clear memory cache.

```kotlin
fun clearMemory(context: Context)
```

**Example:**
```kotlin
GlideUtils.clearMemory(context)
```

##### `clearDiskCache(context: Context)`

Clear disk cache (runs on background thread).

```kotlin
fun clearDiskCache(context: Context)
```

**Example:**
```kotlin
GlideUtils.clearDiskCache(context)
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

#### `loadCircleImage(resourceId: Int, placeholder: Int, error: Int)`

Load a circular cropped image from local resources.

```kotlin
fun ImageView.loadCircleImage(
    resourceId: Int,
    placeholder: Int = 0,
    error: Int = 0
)
```

#### `loadRoundedImage(url: String?, radiusPx: Int, placeholder: Int, error: Int)`

Load an image with rounded corners.

```kotlin
fun ImageView.loadRoundedImage(
    url: String?,
    radiusPx: Int = 10.dp,
    placeholder: Int = 0,
    error: Int = 0
)
```

**Example:**
```kotlin
import com.hoyn.common.utils.PxUtils.dp

imageView.loadRoundedImage(url, radiusPx = 10.dp)
```

#### `loadRoundedImage(resourceId: Int, radiusPx: Int, placeholder: Int, error: Int)`

Load a rounded local resource image.

```kotlin
fun ImageView.loadRoundedImage(
    resourceId: Int,
    radiusPx: Int = 10.dp,
    placeholder: Int = 0,
    error: Int = 0
)
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

### Rounded Image with `PxUtils.dp`

```kotlin
import com.hoyn.common.utils.PxUtils.dp

class CardViewHolder {
    fun bind(imageView: ImageView, url: String?) {
        imageView.loadRoundedImage(
            url = url,
            radiusPx = 10.dp,
            placeholder = R.drawable.placeholder,
            error = R.drawable.image_error
        )
    }
}
```

### Using GlideUtils Directly

```kotlin
import com.hoyn.common.utils.PxUtils.dp

class AdvancedImageLoading {
    fun loadImageWithOptions(imageView: ImageView) {
        GlideUtils.loadRounded(imageView, "https://example.com/image.jpg", 12.dp) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
        }
    }
}
```

### Memory Management

```kotlin
class MyApplication : Application() {
    override fun onLowMemory() {
        super.onLowMemory()
        GlideUtils.clearMemory(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            GlideUtils.clearMemory(this)
        }
    }
}
```

## Breaking Change Note

- `ImageLoader` has been renamed to `GlideUtils`.
- Direct utility calls should migrate from `ImageLoader.load(context, model, imageView)` to `GlideUtils.load(imageView, model)`.
- Rounded corner APIs now use `radiusPx`, and callers typically pass values such as `10.dp` from `PxUtils`.

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
| Compile SDK | 35 |
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
