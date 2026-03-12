plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.hoyn.common.lib"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hoyn.common.lib"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        buildConfig = true
    }
}

dependencies {
    // 依赖所有模块用于演示
    implementation(project(":common-core"))
    implementation(project(":common-utils"))
    implementation(project(":common-log"))
    implementation(project(":common-network"))
    implementation(project(":common-image"))
    implementation(project(":common-ui"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
