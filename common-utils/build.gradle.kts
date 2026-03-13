plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "com.hoyn.common.utils"
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
}

dependencies {
    api(project(":common-core"))

    // Utils
    api(libs.utilcodex)

    // MMKV
    api(libs.mmkv)

    // Gson (for object serialization in MMKVUtils)
    api(libs.gson)

    // AndroidX Extensions
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Window Manager (foldable/multi-window support)
    implementation(libs.androidx.window)

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
