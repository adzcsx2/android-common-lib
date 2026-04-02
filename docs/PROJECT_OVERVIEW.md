# 项目概览

## 基本信息

| 项目 | 说明 |
|------|------|
| 项目名称 | Android Common Library |
| Maven 坐标 | `com.github.adzcsx2.android-common-lib` |
| 当前版本 | 1.2.4 |
| 发布方式 | JitPack |
| 许可证 | Apache License 2.0 |
| 语言 | Kotlin (纯 Kotlin，禁止新增 Java 业务代码) |

## SDK 版本

| 项目 | 版本 |
|------|------|
| compileSdk | 34 |
| minSdk | 24 |
| targetSdk | 34 |
| Kotlin | 2.3.10 |
| AGP | 9.1.0 |
| JVM Target | 11 |
| Gradle | 9.x |

## 技术栈

| 分类 | 技术 |
|------|------|
| UI 框架 | Android View + Jetpack Compose |
| 架构模式 | MVVM |
| 依赖注入 | Koin |
| 网络请求 | Retrofit 2.11.0 + OkHttp 4.12.0 |
| 图片加载 | Glide 4.16.0 |
| 异步处理 | Kotlin Coroutines 1.10.1 |
| 状态管理 | StateFlow + UIState |
| 本地存储 | MMKV 2.3.0 / Room 2.8.4 |
| 导航 | Navigation 2.8.9 |
| 生命周期 | Lifecycle 2.8.7 |

## 模块架构

```
                    common-core (核心抽象层)
                           |
         +-----------------+-----------------+
         |                 |                 |
   common-utils       common-base      common-compose
         |                 |                 |
         +--------+--------+                 |
                  |                          |
         +--------+--------+                 |
         |                 |                 |
   common-network     common-image          |
         |                 |                 |
         +--------+--------+                 |
                  |                          |
             common-ui <---------------------+
                  |
              common-all
                  |
                app
```

## 模块清单

| 模块 | 包名 | 说明 | 依赖 |
|------|------|------|------|
| common-core | `com.hoyn.common.core` | 核心基础 (UIState/IBaseResponse/Message/ThrowableBean) | Lifecycle, Coroutines |
| common-base | `com.hoyn.common.base` | 架构基础 (BaseActivity/BaseFragment/BaseViewModel/事件机制) | common-core, common-utils |
| common-compose | `com.hoyn.common.compose` | Compose 组件 (主题/基础Activity&Fragment/扩展) | common-core, common-base |
| common-utils | `com.hoyn.common.utils` | Context 扩展、协程扩展、Logger、MMKV | common-core |
| common-network | `com.hoyn.common.network` | Retrofit + OkHttp 封装 | common-core, common-utils |
| common-image | `com.hoyn.common.image` | Glide 图片加载封装 | common-core, common-utils |
| common-ui | `com.hoyn.common.ui` | Toast/状态栏/权限/View 扩展 | common-base |
| common-all | - | 聚合模块，一次性引入所有库模块 | 所有 common-* 模块 |

## 示例应用

| 模块 | ApplicationId | 说明 |
|------|---------------|------|
| app | `com.hoyn.common.lib` | 主示例应用，展示库的所有功能 |

## 构建配置

### 构建类型

| 类型 | Minify | 说明 |
|------|--------|------|
| debug | false | 开发调试 |
| release | false | 发布构建 |

### 构建特性

- View Binding: 启用
- Compose: 启用 (app)
- BuildConfig: 启用
- Room Schema: 启用

## 质量门禁

```bash
./gradlew qualityCheck              # lint + debug 单元测试 (全模块)
./gradlew libraryPublishDryRun      # 验证所有库模块可发布到 Maven Local
```
