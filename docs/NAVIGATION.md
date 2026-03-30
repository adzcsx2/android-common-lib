# 导航文档

## 概述

本文档描述项目中 Activity 和 Fragment 之间的导航关系。

---

## 导航总览

```
MainActivity (LAUNCHER)
    |
    +-- startActivity<ComposeDemoActivity>
    +-- startActivity<ToastDemoActivity>
    +-- startActivity<NetworkDemoActivity>
    +-- startActivity<FragmentDemoActivity>
    |       |
    |       +-- FragmentDemoFragment (replace R.id.fragmentContainer)
    |
    +-- startActivity<MmkvDemoActivity>
    +-- startActivity<LogDemoActivity>
    +-- startActivity<StatusBarDemoActivity>
    +-- startActivity<LiveEventDemoActivity>
            |
            +-- startService<LiveEventDemoService>
            +-- sendBroadcast -> LiveEventDemoReceiver
```

---

## 详细导航关系

### MainActivity

**入口**: LAUNCHER Activity

**跳转方式**: 使用 `startActivity<T>()` 扩展函数

| 目标 | 触发方式 |
|------|----------|
| ComposeDemoActivity | 列表点击 "Compose Demo" |
| ToastDemoActivity | 列表点击 "Toast Demo" |
| NetworkDemoActivity | 列表点击 "Network Demo" |
| FragmentDemoActivity | 列表点击 "Fragment Demo" |
| MmkvDemoActivity | 列表点击 "MMKV Demo" |
| LogDemoActivity | 列表点击 "Log Demo" |
| StatusBarDemoActivity | 列表点击 "Status Bar Demo" |
| LiveEventDemoActivity | 列表点击 "LiveEvent Demo" |

---

### FragmentDemoActivity -> FragmentDemoFragment

**容器**: `R.id.fragmentContainer` (FragmentContainerView)

**加载方式**: `supportFragmentManager.beginTransaction().replace()`

**参数传递**: 通过 Intent extras 传递 `initialSessionId` 和 `initialCount`，再由 Fragment 的 `newInstance()` 工厂方法转为 Fragment arguments

```
Intent extras:
  EXTRA_INITIAL_SESSION_ID -> Fragment arguments: FragmentDemoViewModel.KEY_SESSION_ID
  EXTRA_INITIAL_COUNT -> Fragment arguments: FragmentDemoViewModel.KEY_COUNT
```

---

### LiveEventDemoActivity -> LiveEventDemoService

**通信方式**: GlobalLiveEvent 全局事件总线

**流程**:
1. Activity 调用 `startService(Intent(this, LiveEventDemoService::class.java))`
2. Service 在 `onCreate()` 中订阅 GlobalLiveEvent
3. Activity 通过 `GlobalLiveEvent.sendMessage(CODE_SERVICE_REQUEST, msg)` 发送消息
4. Service 收到后通过 `GlobalLiveEvent.sendMessage(CODE_SERVICE_RESPONSE, ...)` 回复
5. Activity 通过预先注册的 componentObserver 接收回复

---

### LiveEventDemoActivity -> LiveEventDemoReceiver

**通信方式**: 广播 + GlobalLiveEvent

**流程**:
1. Activity 发送广播 `ACTION_LIVE_EVENT_DEMO_BROADCAST`
2. Receiver 收到广播后转发到 GlobalLiveEvent
3. Activity 通过 componentObserver 接收

---

## 返回栈

所有 Demo 页面均通过 `finish()` 返回上一级，不使用 Navigation Component 图。

---

## 跳转工具

项目提供 `startActivity<T>()` Context 扩展函数用于页面跳转，位于 `common-base` 模块。
