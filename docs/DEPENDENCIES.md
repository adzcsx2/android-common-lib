# 依赖文档

## 概述

本文档记录项目的所有依赖库及其版本。版本统一在 `gradle/libs.versions.toml` 中管理。

---

## Version Catalog (libs.versions.toml)

### 插件版本

| 插件 | 版本 |
|------|------|
| AGP (android-application / android-library) | 9.1.0 |
| Kotlin | 2.3.10 |
| KSP | 2.3.6 |
| Compose Compiler | 2.3.10 |
| Room | 2.8.4 |

### 库版本

| 分类 | 库名 | 版本 |
|------|------|------|
| AndroidX Core | core-ktx | 1.13.1 |
| AndroidX Core | appcompat | 1.7.1 |
| AndroidX Core | recyclerview | 1.3.2 |
| AndroidX Core | activity-ktx | 1.8.2 |
| AndroidX Core | fragment-ktx | 1.8.7 |
| Lifecycle | runtime / viewmodel / savedstate | 2.8.7 |
| Navigation | fragment / ui | 2.8.9 |
| Material | material | 1.13.0 |
| Network | Retrofit | 2.11.0 |
| Network | OkHttp | 4.12.0 |
| Network | Gson | 2.11.0 |
| Database | Room | 2.8.4 |
| Image | Glide | 4.16.0 |
| Image | Coil Compose | 2.7.0 |
| Coroutines | kotlinx-coroutines | 1.10.1 |
| DI | Koin | 3.5.6 |
| Storage | MMKV | 2.3.0 |
| UI | SmartRefreshLayout | 2.0.6 |
| UI | BaseRecyclerViewAdapterHelper | 3.0.14 |
| Compose | Compose BOM | 2024.04.01 |
| Testing | JUnit | 4.13.2 |
| Testing | Mockito Core | 5.5.0 |
| Testing | Robolectric | 4.11.1 |

---

## 模块依赖关系

### app 模块

```kotlin
// 库模块
implementation(project(":common-core"))
implementation(project(":common-base"))
implementation(project(":common-compose"))
implementation(project(":common-utils"))
implementation(project(":common-network"))
implementation(project(":common-image"))
implementation(project(":common-ui"))

// AndroidX
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.appcompat)
implementation(libs.material)

// Navigation
implementation(libs.navigation.fragment)
implementation(libs.navigation.ui)

// Room
implementation(libs.room.runtime)
ksp(libs.room.compiler)

// Koin
implementation(libs.koin.core)
implementation(libs.koin.android)
```

### 模块间禁止依赖

| 模块 | 禁止依赖 |
|------|----------|
| common-core | 任何其他 common-* 模块 |
| common-utils | common-base, common-ui, common-network, common-image |
| common-base | common-ui, common-network, common-image |
| common-network / common-image | common-ui |
