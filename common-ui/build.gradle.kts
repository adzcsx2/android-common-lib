plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "com.hoyn.common.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api(project(":common-core"))
    api(project(":common-utils"))

    // AndroidX UI
    api(libs.androidx.appcompat)
    api(libs.androidx.recyclerview)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.lifecycle.viewmodel)

    // Material
    api(libs.material)

    // SmartRefreshLayout
    api(libs.smart.refresh)
    api(libs.smart.refresh.header)

    // BaseRecyclerViewAdapterHelper
    api(libs.base.recycler.view.adapter.helper)

    // Navigation
    api(libs.navigation.fragment)
    api(libs.navigation.ui)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// 应用发布配置
apply(from = rootProject.file("scripts/publish.gradle.kts"))
