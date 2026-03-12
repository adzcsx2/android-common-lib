// 通用发布脚本 - 所有模块共用
// 使用方式: apply(from = rootProject.file("scripts/publish.gradle.kts"))
//
// 发布方式:
// 1. 本地发布: ./gradlew publishToMavenLocal
// 2. JitPack 发布: 推送代码到 Gitee，然后在 https://jitpack.io 输入 com.gitee.Hoyn:android-common-lib:版本号

apply(plugin = "maven-publish")

extensions.configure<PublishingExtension> {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components.findByName("release"))
            }

            groupId = "com.gitee.Hoyn"
            artifactId = project.name
            version = project.findProperty("libVersion")?.toString() ?: "1.0.0"

            pom {
                name.set(artifactId)
                description.set("Android Common Library - $artifactId module")
                url.set("https://gitee.com/Hoyn/android-common-lib")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("Hoyn")
                        name.set("Hoyn")
                    }
                }

                scm {
                    connection.set("scm:git:git@gitee.com:Hoyn/android-common-lib.git")
                    developerConnection.set("scm:git:git@gitee.com:Hoyn/android-common-lib.git")
                    url.set("https://gitee.com/Hoyn/android-common-lib")
                }
            }
        }
    }
}
