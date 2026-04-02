# 四大组件文档

## 概述

本文档描述项目中注册的 Android 四大组件。

---

## Activity

### 声明清单 (AndroidManifest.xml)

| Activity | exported | intent-filter | 说明 |
|----------|----------|---------------|------|
| MainActivity | true | LAUNCHER | Demo 列表入口 |
| ComposeDemoActivity | false | - | Compose 架构示例 |
| ToastDemoActivity | false | - | Toast 示例 |
| NetworkDemoActivity | false | - | 网络请求示例 |
| FragmentDemoActivity | false | - | Fragment 示例 |
| MmkvDemoActivity | false | - | MMKV 存储示例 |
| LogDemoActivity | false | - | Logger 日志示例 |
| StatusBarDemoActivity | false | - | 状态栏示例 |
| LiveEventDemoActivity | false | - | GlobalLiveEvent 示例 |
| DialogSafetyDemoActivity | false | - | Dialog 安全示例 |
| StackManagerDemoActivity | false | - | ActivityStackManager 示例 |

### 权限

| 权限 | 用途 |
|------|------|
| `android.permission.POST_NOTIFICATIONS` | 通知权限 (已声明) |

---

## Service

### LiveEventDemoService

- **类名**: `com.hoyn.common.lib.ui.liveevent.LiveEventDemoService`
- **exported**: false
- **功能**: 监听 GlobalLiveEvent 消息，演示 Service 中使用全局事件

**生命周期**:
- `onCreate()`: 订阅 GlobalLiveEvent (CODE_SERVICE_REQUEST)
- `onStartCommand()`: 返回 `START_STICKY`
- `onDestroy()`: 取消订阅，发送停止通知
- `onBind()`: 返回 null (非绑定 Service)

---

## BroadcastReceiver

### LiveEventDemoReceiver

- **类名**: `com.hoyn.common.lib.ui.liveevent.LiveEventDemoReceiver`
- **exported**: false
- **功能**: 接收广播并转发到 GlobalLiveEvent

**处理 Action**:
- `ACTION_LIVE_EVENT_DEMO_BROADCAST`: 收到后通过 `GlobalLiveEvent.sendMessage(CODE_RECEIVER_RESPONSE, ...)` 转发

---

## ContentProvider

项目中未注册自定义 ContentProvider。
