pluginManagement {
    repositories {
        maven { url = uri("https://mirrors.huaweicloud.com/repository/maven") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://mirrors.huaweicloud.com/repository/maven") }
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
include(":common-all")
include(":common-base")
include(":common-compose")
include(":common-network")
include(":common-image")
include(":common-ui")