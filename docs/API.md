# API 文档

## 概述

本文档记录项目中定义的 API 接口。

---

## 网络接口

### PostApi

- **文件**: `app/src/main/java/com/hoyn/common/lib/data/remote/api/PostApi.kt`
- **用途**: 帖子数据接口 (Demo 用)
- **Base URL**: `https://jsonplaceholder.typicode.com/`

| 方法 | 路径 | 说明 | 返回类型 |
|------|------|------|----------|
| GET | `/posts` | 获取帖子列表 | `List<Post>` |

**请求示例**:

```kotlin
interface PostApi {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}
```

**数据模型**:

```kotlin
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
```

---

## 网络框架 API

以下为 `common-network` 模块提供的网络框架 API：

| 类 | 说明 |
|------|------|
| `RetrofitFactory` | 创建 Retrofit 服务实例 |
| `NetworkConfig` | 网络配置 (超时、Debug 模式) |
| `ApiResponse<T>` | 统一响应封装，实现 `IBaseResponse` |
| `OkHttpClientFactory` | OkHttp 客户端工厂 |
| `ExceptionHandle` | 异常处理 |

---

## 全局事件 API

以下为 `common-base` 模块提供的事件通信 API：

| API | 说明 |
|-----|------|
| `GlobalLiveEvent.sendMessage(code, msg)` | 发送普通消息 |
| `GlobalLiveEvent.sendMessageDelay(code, msg, delay)` | 延迟发送消息 |
| `GlobalLiveEvent.observeMessage(owner, callback)` | 订阅消息 (生命周期感知) |
| `GlobalLiveEvent.observeStickyMessage(owner, callback)` | 订阅粘性消息 |
| `GlobalLiveEvent.observeMessageForever(observer)` | 永久订阅 |
| `GlobalLiveEvent.removeMessageObserver(observer)` | 取消订阅 |
