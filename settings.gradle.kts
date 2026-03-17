pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "common-lib"

// 模块声明
include(":app")
include(":common-core")
include(":common-utils")
include(":common-base")
include(":common-compose")
include(":common-network")
include(":common-image")
include(":common-ui")