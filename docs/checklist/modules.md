# 模块功能清单

> 在实现新功能前，请先检查此处是否已有可用的组件。优先使用 common-lib 提供的功能，避免重复造轮子。

## 使用说明

1. **在编写代码前**，先查看此清单
2. **找到对应的功能模块**
3. **使用提供的代码示例**

---

## 快速查找

| 需求                      | 模块           | 优先级  |
| ------------------------- | -------------- | ------- |
| [网络请求](#网络请求)     | common-network | 🔴 必须 |
| [图片加载](#图片加载)     | common-image   | 🔴 必须 |
| [状态管理](#状态管理)     | common-core    | 🔴 必须 |
| [日志记录](#日志记录)     | common-utils   | 🟡 推荐 |
| [Toast 提示](#toast-提示) | common-ui      | 🟡 推荐 |
| [状态栏设置](#状态栏设置) | common-ui      | 🟡 推荐 |
| [权限请求](#权限请求)     | common-ui      | 🟡 推荐 |
| [本地存储](#本地存储)     | common-utils   | 🟡 推荐 |
| [协程扩展](#协程扩展)     | common-utils   | 🟢 可选 |
| [View 扩展](#view-扩展)   | common-ui      | 🟢 可选 |

---

## 详细功能清单

### 网络请求

**模块**: `common-network`

**功能**: Retrofit + OkHttp 封装，统一的 API 响应处理

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-network:1.3.3")
```

**使用示例**:

```kotlin
// 1. 配置（Application 中）
NetworkConfig.connectTimeout = 30
NetworkConfig.readTimeout = 30
NetworkConfig.writeTimeout = 30
NetworkConfig.isDebug = BuildConfig.DEBUG

// 2. 定义 API 接口
interface UserService {
    @GET("user/info")
    suspend fun getUserInfo(): ApiResponse<UserInfo>

    @POST("user/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
}

// 3. 创建服务
val api = RetrofitFactory.createService<UserService>("https://api.example.com/")

// 4. 调用
val response = api.getUserInfo()
if (response.isSuccess()) {
    val data = response.data // 成功数据
} else {
    val msg = response.msg() // 错误信息
}
```

**相关类**:

- `RetrofitFactory` - 创建 Retrofit 服务
- `NetworkConfig` - 网络配置
- `ApiResponse<T>` - 统一响应封装，实现 `IBaseResponse`

---

### 图片加载

**模块**: `common-image`

**功能**: Glide 图片加载封装

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-image:1.3.3")
```

**使用示例**:

```kotlin
// 基础加载
imageView.loadImage(url)

// 带占位图
imageView.loadImage(url, placeholder = R.drawable.placeholder, error = R.drawable.error)

// 圆形图片
imageView.loadCircleImage(avatarUrl)

// 圆角图片
imageView.loadRoundImage(url, radius = 10f)

// 指定大小
imageView.loadImage(url, width = 200, height = 200)

// 使用 ImageLoader（高级用法）
ImageLoader.load(imageView)
    .url(url)
    .placeholder(R.drawable.placeholder)
    .circleCrop()
    .start()
```

---

### 状态管理

**模块**: `common-core`

**功能**: UIState 封装，配合 StateFlow 管理页面状态

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-core:1.3.3")
```

**使用示例**:

```kotlin
// ViewModel 中
class MyViewModel : BaseViewModel() {
    private val _uiState = MutableStateFlow<UIState<User>>(UIState.Loading)
    val uiState: StateFlow<UIState<User>> = _uiState

    fun loadUser() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            val result = repository.getUser()
            _uiState.value = when {
                result.isSuccess -> UIState.Success(result.getOrNull()!!)
                else -> UIState.Error(-1, "加载失败")
            }
        }
    }
}

// Activity/Fragment 中
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        when (state) {
            is UIState.Loading -> showLoading()
            is UIState.Success -> showData(state.data)
            is UIState.Error -> showError(state.message)
            is UIState.Empty -> showEmpty()
        }
    }
}
```

**相关类**:

- `UIState<T>` - 页面状态封装 (Loading/Success/Error/Empty)
- `IBaseResponse` - API 响应接口
- `Message` - 事件消息
- `ThrowableBean` - 错误封装

---

### 日志记录

**模块**: `common-utils`

**功能**: 统一日志管理

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-utils:1.3.3")
```

**使用示例**:

```kotlin
// 初始化（Application 中）
Logger.init(debug = BuildConfig.DEBUG, tag = "MyApp")

// 使用
Logger.d("Debug message")
Logger.i("Info message")
Logger.w("Warning message")
Logger.e("Error message", throwable)
Logger.json(jsonString)

// 自定义 tag
Logger.t("CustomTag").d("Message")
```

---

### JSON 解析

**模块**: `common-core`

**包名**: `com.hoyn.common.core.gson`

**功能**: 统一 Gson 容错解析与序列化入口

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-core:1.3.3")
```

**使用示例**:

```kotlin
// Application 中初始化，可选接收解析异常回调
GsonUtils.init { event ->
    Logger.w("Gson parse mismatch: ${event.fieldName} -> ${event.jsonToken}")
}

// 统一使用 GsonUtils，避免直接 new Gson()
val gson = GsonUtils.getGson()
val json = GsonUtils.toJson(user)
val model = GsonUtils.fromJson<User>(json)

// List 反序列化
val listJson = GsonUtils.toJson(users)
val userList = GsonUtils.fromJson<List<User>>(listJson).orEmpty()
```

---

### Toast 提示

**模块**: `common-ui`

**包名**: `com.hoyn.common.ui.toast`

**功能**: Toast 工具封装，支持 DSL 风格的全局和单次调用配置

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-ui:1.3.3")
```

**使用示例**:

```kotlin
// 初始化（支持 DSL 配置全局默认值）
ToastUtils.init(this) {
    gravity = Gravity.CENTER
    stackSkips = 2
}

// 短时间显示
ToastUtils.show("提示信息")

// 长时间显示
ToastUtils.showLong("长提示")

// 单次调用覆盖配置
ToastUtils.show("提示") {
    xOffset = 10
    yOffset = 100
}

// 在主线程安全显示
ToastUtils.debugShow("安全提示")
```

---

### 状态栏设置

**模块**: `common-ui`

**功能**: 状态栏颜色、沉浸式、刘海屏适配

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-ui:1.3.3")
```

**使用示例**:

```kotlin
// 设置状态栏颜色
StatusBarHelper.setColor(activity, Color.RED)

// 设置透明状态栏（沉浸式）
StatusBarHelper.setTransparent(activity)

// 设置状态栏深色图标
StatusBarHelper.setLightMode(activity)

// 设置状态栏浅色图标
StatusBarHelper.setDarkMode(activity)

// 获取状态栏高度
val height = StatusBarHelper.getHeight(context)

// 检查是否有刘海屏
val hasNotch = NotchHelper.hasNotch(activity)
```

---

### 权限请求

**模块**: `common-ui`

**功能**: 基于 XXPermissions 的运行时权限请求封装

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-ui:1.3.3")
```

**使用示例**:

```kotlin
// 请求单个权限
PermissionUtils.request(
    activity = this,
    permissions = arrayOf(PermissionLists.getCameraPermission()),
    onGranted = { /* 权限已授予 */ },
    onDenied = { /* 权限被拒绝 */ }
)

// 请求存储权限（自动适配 Android 10+ 分区存储）
PermissionUtils.requestStorage(this) {
    // 存储权限已授予
}

// 检查权限
if (PermissionUtils.isGranted(this, PermissionLists.getCameraPermission())) {
    // 已有权限
}
```

---

### 本地存储

**模块**: `common-utils`

**功能**: MMKV 键值存储

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-utils:1.3.3")
```

**使用示例**:

```kotlin
// 存储数据
MMKVUtils.encode("key", "value")
MMKVUtils.encode("number", 123)
MMKVUtils.encode("boolean", true)

// 读取数据
val value = MMKVUtils.decodeString("key")
val number = MMKVUtils.decodeInt("number")
val bool = MMKVUtils.decodeBool("boolean", false)

// 删除数据
MMKVUtils.remove("key")

// 清空所有数据
MMKVUtils.clearAll()
```

---

### 协程扩展

**模块**: `common-utils`

**功能**: 协程调度器扩展

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-utils:1.3.3")
```

**使用示例**:

```kotlin
// 在 IO 线程执行
suspend fun ioOperation() = withIO {
    // 网络请求、数据库操作
}

// 在主线程执行
suspend fun mainThreadOperation() = withMain {
    // UI 更新
}

// 在 Default 线程执行
suspend fun computeOperation() = withDefault {
    // 计算密集任务
}
```

---

### View 扩展

**模块**: `common-ui`

**功能**: View 常用操作扩展

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-ui:1.3.3")
```

**使用示例**:

```kotlin
// 显示/隐藏
view.visible()
view.gone()
view.invisible()

// 防抖点击
view.onClick(interval = 500) {
    // 点击事件
}

// 设置视图约束
view.setMatchWidth()
view.setMatchHeight()
view.setWrapContent()

// 获取视图宽度/高度
val width = view.width
val height = view.height
```

---

## 架构基类

### BaseViewModel

**模块**: `common-base`

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-base:1.3.3")
```

**使用示例**:

```kotlin
class UserViewModel : BaseViewModel<UserRepository>() {
    override val repository: UserRepository by lazy { UserRepository() }

    fun loadData() {
        // 使用 launchOnlyResult 自动处理 Loading/Success/Error
        launchOnlyResult(
            block = { repository.getData() },
            success = { data -> /* 成功 */ },
            error = { e -> /* 失败 */ }
        )
    }

    fun showToast() {
        // 使用内置事件方法
        showToast("操作成功")
        showDialog("加载中...")
        dismissDialog()
    }
}
```

### BaseActivity / BaseFragment

**模块**: `common-base`

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-base:1.3.3")
```

**使用示例**:

```kotlin
// Activity
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun createBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
    }
}

// Fragment
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
    }
}

// 观察 ViewModel 事件
observeAllUIEvents(
    viewModel = viewModel,
    onToast = { toast.debugShow(it) },
    onShowDialog = { showLoading(it) },
    onDismissDialog = { hideLoading() }
)
```

---

## Compose 支持

### BaseComposeActivity / BaseComposeFragment

**模块**: `common-compose`

**依赖**:

```kotlin
implementation("com.github.adzcsx2.android-common-lib:common-compose:1.3.3")
```

**使用示例**:

```kotlin
// Activity
class MyComposeActivity : BaseComposeActivity() {
    override fun Content() {
        AppTheme {
            MyScreen()
        }
    }
}

// Fragment
class MyComposeFragment : BaseComposeFragment() {
    override fun Content() {
        AppTheme {
            MyScreen()
        }
    }
}

// 使用主题颜色
@Composable
fun MyScreen() {
    val colors = useAppThemeColors()
    Box(modifier = Modifier.background(colors.primary)) {
        Text("Hello", color = colors.onPrimary)
    }
}
```

---

## 全局事件

**模块**: `common-base`

**功能**: 跨组件通信

**使用示例**:

```kotlin
// 发送全局事件
GlobalLiveEvent.sendMessage(Message(code = 1001, msg = "登录过期"))

// 观察全局事件
observeGlobalMessage { message ->
    when (message.code) {
        1001 -> navigateToLogin()
        1002 -> refreshData()
    }
}
```

---

## 依赖说明

### 模块间依赖关系

```
common-core (无其他依赖)
    ↓
common-utils (依赖 common-core)
    ↓
common-base (依赖 common-core, common-utils)
    ↓
common-network, common-image (依赖 common-core, common-utils)
    ↓
common-ui (依赖 common-base)
```

### 最小依赖配置

```kotlin
dependencies {
    // 核心功能
    implementation("com.github.adzcsx2.android-common-lib:common-core:1.3.3")
    implementation("com.github.adzcsx2.android-common-lib:common-utils:1.3.3")

    // 架构基础
    implementation("com.github.adzcsx2.android-common-lib:common-base:1.3.3")

    // 按需添加
    implementation("com.github.adzcsx2.android-common-lib:common-network:1.3.3")
    implementation("com.github.adzcsx2.android-common-lib:common-image:1.3.3")
    implementation("com.github.adzcsx2.android-common-lib:common-ui:1.3.3")
}
```

---

## 版本兼容性

| 项目       | 版本   |
| ---------- | ------ |
| compileSdk | 34     |
| minSdk     | 24     |
| targetSdk  | 34     |
| Kotlin     | 2.3.10 |
| JVM Target | 17     |

---

## 更新日志

- **1.3.1** - 当前版本，PermissionUtils 权限工具类（XXPermissions 封装）、国际化、Activity 扩展
- 详见 [CHANGELOG.md](../CHANGELOG.md)
