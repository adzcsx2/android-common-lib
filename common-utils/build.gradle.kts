plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "com.hoyn.common.utils"
    compileSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    api(project(":common-core"))
    api(libs.toaster)

    // Utils
    api(libs.utilcodex) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }

    // MMKV
    api(libs.mmkv)

    // Gson (for object serialization in MMKVUtils)
    api(libs.gson)

    // AndroidX Extensions
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)

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
