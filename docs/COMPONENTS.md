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
| PermissionDemoActivity | false | VIEW_PERMISSION_USAGE/HEALTH_PERMISSIONS | 权限请求演示 |

### 权限

#### 危险权限

| 权限 | 用途 |
|------|------|
| `android.permission.POST_NOTIFICATIONS` | 通知权限 |
| `android.permission.CAMERA` | 相机 |
| `android.permission.RECORD_AUDIO` | 录音 |
| `android.permission.ACCESS_COARSE_LOCATION` | 粗略定位 |
| `android.permission.ACCESS_FINE_LOCATION` | 精确定位 |
| `android.permission.ACCESS_BACKGROUND_LOCATION` | 后台定位 |
| `android.permission.READ_CONTACTS` | 读取联系人 |
| `android.permission.WRITE_CONTACTS` | 写入联系人 |
| `android.permission.READ_CALENDAR` | 读取日历 |
| `android.permission.WRITE_CALENDAR` | 写入日历 |
| `android.permission.READ_PHONE_STATE` | 电话状态 |
| `android.permission.READ_CALL_LOG` | 读取通话记录 |
| `android.permission.WRITE_CALL_LOG` | 写入通话记录 |
| `android.permission.ACTIVITY_RECOGNITION` | 活动识别 |
| `android.permission.BODY_SENSORS` | 身体传感器 |
| `android.permission.BODY_SENSORS_BACKGROUND` | 后台身体传感器 |
| `android.permission.BLUETOOTH_SCAN` | 蓝牙扫描 |
| `android.permission.BLUETOOTH_CONNECT` | 蓝牙连接 |
| `android.permission.BLUETOOTH_ADVERTISE` | 蓝牙广播 |
| `android.permission.NEARBY_WIFI_DEVICES` | 附近 Wi-Fi 设备 |
| `android.permission.health.READ_SLEEP` | 读取睡眠数据 |
| `android.permission.health.READ_ACTIVE_CALORIES_BURNED` | 读取活动卡路里 |
| `android.permission.health.READ_EXERCISE` | 读取运动数据 |
| `android.permission.health.READ_HEART_RATE` | 读取心率 |
| `android.permission.health.WRITE_HEART_RATE` | 写入心率 |

#### 存储权限

| 权限 | 用途 |
|------|------|
| `android.permission.READ_EXTERNAL_STORAGE` | 读取外部存储 |
| `android.permission.WRITE_EXTERNAL_STORAGE` | 写入外部存储 |
| `android.permission.READ_MEDIA_IMAGES` | 读取媒体图片 |
| `android.permission.READ_MEDIA_VIDEO` | 读取媒体视频 |
| `android.permission.READ_MEDIA_AUDIO` | 读取媒体音频 |
| `android.permission.READ_MEDIA_VISUAL_USER_SELECTED` | 用户选择媒体 |
| `android.permission.MANAGE_EXTERNAL_STORAGE` | 所有文件访问 |
| `android.permission.ACCESS_MEDIA_LOCATION` | 媒体位置信息 |

#### 特殊权限

| 权限 | 用途 |
|------|------|
| `android.permission.SYSTEM_ALERT_WINDOW` | 悬浮窗 |
| `android.permission.REQUEST_INSTALL_PACKAGES` | 安装应用 |
| `android.permission.WRITE_SETTINGS` | 修改系统设置 |
| `android.permission.PACKAGE_USAGE_STATS` | 使用情况访问 |
| `android.permission.SCHEDULE_EXACT_ALARM` | 精确闹钟 |
| `android.permission.ACCESS_NOTIFICATION_POLICY` | 勿扰模式 |
| `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 忽略电池优化 |
| `android.permission.USE_FULL_SCREEN_INTENT` | 全屏通知 |
| `android.permission.MANAGE_MEDIA` | 管理媒体 |
| `android.permission.QUERY_ALL_PACKAGES` | 获取已安装应用 |

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

### ExampleNotificationListenerService

- **类名**: `com.hoyn.common.lib.ui.permission_demo.ExampleNotificationListenerService`
- **exported**: false
- **权限**: `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`
- **功能**: 通知监听服务 Stub，用于演示通知监听权限请求

### ExampleVpnService

- **类名**: `com.hoyn.common.lib.ui.permission_demo.ExampleVpnService`
- **exported**: false
- **权限**: `android.permission.BIND_VPN_SERVICE`
- **功能**: VPN 服务 Stub，用于演示 VPN 权限请求

### ExampleAccessibilityService

- **类名**: `com.hoyn.common.lib.ui.permission_demo.ExampleAccessibilityService`
- **exported**: false
- **权限**: `android.permission.BIND_ACCESSIBILITY_SERVICE`
- **功能**: 无障碍服务 Stub，用于演示无障碍权限请求

---

## BroadcastReceiver

### LiveEventDemoReceiver

- **类名**: `com.hoyn.common.lib.ui.liveevent.LiveEventDemoReceiver`
- **exported**: false
- **功能**: 接收广播并转发到 GlobalLiveEvent

**处理 Action**:
- `ACTION_LIVE_EVENT_DEMO_BROADCAST`: 收到后通过 `GlobalLiveEvent.sendMessage(CODE_RECEIVER_RESPONSE, ...)` 转发

### ExampleDeviceAdminReceiver

- **类名**: `com.hoyn.common.lib.ui.permission_demo.ExampleDeviceAdminReceiver`
- **exported**: true
- **权限**: `android.permission.BIND_DEVICE_ADMIN`
- **功能**: 设备管理器 Receiver Stub，用于演示设备管理器权限请求
- **策略**: `force-lock`

---

## ContentProvider

项目中未注册自定义 ContentProvider。
