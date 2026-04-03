plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    id("kotlin-parcelize")
    jacoco
}

android {
    namespace = "com.hoyn.common.lib"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hoyn.camera"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}

// ===== APK build info: print APK path and build time after assemble =====
tasks.configureEach {
    if (name.startsWith("package") && (name.endsWith("Debug") || name.endsWith("Release"))) {
        val variantName = if (name.endsWith("Debug")) "debug" else "release"
        val buildDir = layout.buildDirectory.get().asFile
        var startTime: Long = 0L
        doFirst {
            startTime = System.currentTimeMillis()
        }
        doLast {
            val elapsed = System.currentTimeMillis() - startTime
            val apkDir = File(buildDir, "outputs/apk/$variantName")
            val apkFile = apkDir.listFiles()
                ?.firstOrNull { it.extension == "apk" && it.isFile }
            if (apkFile != null) {
                val elapsedSeconds = elapsed / 1000.0
                logger.lifecycle("========================================")
                logger.lifecycle("APK Build Info")
                logger.lifecycle("----------------------------------------")
                logger.lifecycle("Variant : $variantName")
                logger.lifecycle("APK Path: ${apkFile.absolutePath}")
                logger.lifecycle("APK Size: ${"%.2f".format(apkFile.length() / (1024.0 * 1024.0))} MB")
                logger.lifecycle("Package Time: ${"%.1f".format(elapsedSeconds)}s")
                logger.lifecycle("========================================")
            }
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // 依赖所有模块用于演示
    implementation("com.github.adzcsx2.android-common-lib:common-all:1.2.9")

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.video)
    implementation(libs.camerax.view)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.arch.core.testing)
}

// 全局 opt-in Material3 实验性 API
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}
