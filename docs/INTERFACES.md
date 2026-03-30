# 界面文档

## 概述

本文档描述项目中所有 Activity 和 Fragment 的界面功能与控件。

---

## app 模块

### MainActivity

- **类名**: `com.hoyn.common.lib.ui.main.MainActivity`
- **基类**: `BaseActivity<ActivityMainBinding, NoViewModel>`
- **布局**: `activity_main.xml`
- **功能**: Demo 列表入口页面，展示所有功能演示入口

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `rvDemo` | RecyclerView | 功能列表 |

**功能列表**:

| 条目 | 跳转目标 |
|------|----------|
| Compose Demo (推荐) | ComposeDemoActivity |
| Toast Demo | ToastDemoActivity |
| Network Demo | NetworkDemoActivity |
| Fragment Demo (推荐) | FragmentDemoActivity |
| MMKV Demo | MmkvDemoActivity |
| Log Demo | LogDemoActivity |
| Status Bar Demo | StatusBarDemoActivity |
| LiveEvent Demo | LiveEventDemoActivity |

---

### ComposeDemoActivity

- **类名**: `com.hoyn.common.lib.ui.compose.ComposeDemoActivity`
- **基类**: `BaseComposeActivity<ComposeDemoViewModel>`
- **布局**: 纯 Compose (无 XML 布局)
- **功能**: Compose 架构示例，展示完整的 Compose 开发链路
- **说明**: Material3 主题、UIState 状态管理、帖子列表、网络优先策略

**Compose 组件**:

| 组件 | 功能 |
|------|------|
| ComposeDemoScreen | 主屏幕 Scaffold + TopAppBar |
| FeatureButtons | 功能按钮 (加载数据/测试Toast) |
| LoadingContent | 加载中 CircularProgressIndicator |
| ErrorContent | 错误状态 (重试按钮) |
| EmptyContent | 空数据状态 |
| PostList | 帖子列表 LazyColumn |
| PostItem | 帖子卡片 Card |

---

### ToastDemoActivity

- **类名**: `com.hoyn.common.lib.ui.toast_demo.ToastDemoActivity`
- **基类**: `BaseActivity<ActivityToastDemoBinding, NoViewModel>`
- **布局**: `activity_toast_demo.xml`
- **功能**: Toast 示例，展示 ToastUtil 各种用法

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `btnBack` | Button | 返回 |
| `btnShortToast` | Button | 短 Toast |
| `btnLongToast` | Button | 长 Toast |
| `btnCenterToast` | Button | 中间 Toast |
| `btnCenterLongToast` | Button | 中间长 Toast |
| `btnQueueTest` | Button | 队列优先级测试 |
| `btnCancel` | Button | 取消当前 Toast |

---

### NetworkDemoActivity

- **类名**: `com.hoyn.common.lib.ui.network.NetworkDemoActivity`
- **基类**: `BaseActivity<ActivityNetworkDemoBinding, NetworkDemoViewModel>`
- **布局**: `activity_network_demo.xml`
- **功能**: 网络请求示例，展示完整架构链路 (Activity -> ViewModel -> Repository -> Api/Database -> UIState)

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `rvPosts` | RecyclerView | 帖子列表 |
| `btnRefresh` | Button | 刷新数据 |
| `btnBack` | Button | 返回 |
| `progressBar` | ProgressBar | 加载指示器 |
| `tvEmpty` | TextView | 空数据提示 |
| `tvError` | TextView | 错误信息 |

**UIState 状态处理**: Loading / Success / Error / Empty

---

### FragmentDemoActivity

- **类名**: `com.hoyn.common.lib.ui.fragment_demo.FragmentDemoActivity`
- **基类**: `BaseActivity<ActivityFragmentDemoBinding, NoViewModel>`
- **布局**: `activity_fragment_demo.xml`
- **功能**: Fragment 示例，展示 Fragment 使用方式和 SavedStateHandle

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `btnBack` | Button | 返回 |
| `fragmentContainer` | FragmentContainerView | Fragment 容器 |

---

### FragmentDemoFragment

- **类名**: `com.hoyn.common.lib.ui.fragment_demo.FragmentDemoFragment`
- **基类**: `BaseFragment<FragmentFragmentDemoBinding, FragmentDemoViewModel>`
- **布局**: `fragment_fragment_demo.xml`
- **功能**: ViewModel + StateFlow 状态管理示例

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `tvSessionValue` | TextView | 会话 ID |
| `tvCounterValue` | TextView | 计数器 |
| `tvMessage` | TextView | 消息 |
| `btnIncrement` | Button | 计数器 +1 |
| `btnReset` | Button | 重置计数器 |

---

### MmkvDemoActivity

- **类名**: `com.hoyn.common.lib.ui.mmkv_demo.MmkvDemoActivity`
- **基类**: `BaseActivity<ActivityMmkvDemoBinding, NoViewModel>`
- **布局**: `activity_mmkv_demo.xml`
- **功能**: MMKV 存储示例

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `btnBack` | Button | 返回 |
| `etInput` | EditText | 输入框 |
| `btnSaveString` | Button | 保存数据 |
| `btnLoadString` | Button | 读取数据 |
| `btnDelete` | Button | 删除数据 |
| `tvResult` | TextView | 结果显示 |

---

### LogDemoActivity

- **类名**: `com.hoyn.common.lib.ui.log_demo.LogDemoActivity`
- **基类**: `BaseActivity<ActivityLogDemoBinding, NoViewModel>`
- **布局**: `activity_log_demo.xml`
- **功能**: Logger 日志示例

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `btnBack` | Button | 返回 |
| `btnDebug` | Button | DEBUG 日志 |
| `btnInfo` | Button | INFO 日志 |
| `btnWarn` | Button | WARN 日志 |
| `btnError` | Button | ERROR 日志 |
| `btnJson` | Button | JSON 日志 |
| `tvResult` | TextView | 结果显示 |

---

### StatusBarDemoActivity

- **类名**: `com.hoyn.common.lib.ui.statusbar_demo.StatusBarDemoActivity`
- **基类**: `BaseActivity<ActivityStatusBarDemoBinding, NoViewModel>`
- **布局**: `activity_status_bar_demo.xml`
- **功能**: 状态栏示例，展示 StatusBarHelper 各种用法

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `btnBack` | Button | 返回 |
| `btnDefault` | Button | 默认紫色 |
| `btnRed` | Button | 红色 |
| `btnGreen` | Button | 绿色 |
| `btnBlue` | Button | 蓝色 |
| `btnTransparent` | Button | 透明 |
| `btnLight` | Button | 浅色 (深色图标) |

---

### LiveEventDemoActivity

- **类名**: `com.hoyn.common.lib.ui.liveevent.LiveEventDemoActivity`
- **基类**: `BaseActivity<ActivityLiveEventDemoBinding, NoViewModel>`
- **布局**: `activity_live_event_demo.xml`
- **功能**: GlobalLiveEvent 示例，展示全局事件通信

**主要控件**:

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `btnSendNormal` | Button | 发送普通消息 |
| `btnSendSticky` | Button | 发送粘性消息 |
| `btnSendDelay` | Button | 延迟发送 |
| `btnToggleService` | Button | 启动/停止 Service |
| `btnSendToService` | Button | 发送消息给 Service |
| `btnSendBroadcastDemo` | Button | 广播测试 |
| `btnSubscribeNormal` | Button | 订阅普通消息 |
| `btnSubscribeSticky` | Button | 订阅粘性消息 |
| `btnSubscribeManual` | Button | 手动订阅 (Forever) |
| `btnClearLog` | Button | 清空日志 |
| `scrollView` | ScrollView | 日志滚动区域 |
| `tvLog` | TextView | 日志输出 |

---

## 列表项布局

### item_demo.xml

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `tvBadge` | TextView | 推荐标签 |
| `tvTitle` | TextView | 标题 |
| `tvDesc` | TextView | 描述 |

### item_post.xml

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `tvTitle` | TextView | 帖子标题 |
| `tvBody` | TextView | 帖子内容 |

## 库模块布局

### layout_toast.xml (common-ui)

| 控件 ID | 类型 | 功能 |
|---------|------|------|
| `toast_layout_root` | LinearLayout | Toast 根布局 |
| `tv_toast_content` | TextView | Toast 文本 |
