// 通用发布脚本 - 所有模块共用
// 使用方式: apply(from = rootProject.file("scripts/publish.gradle.kts"))
//
// 发布方式:
// 1. 本地发布: ./gradlew publishToMavenLocal
// 2. JitPack 发布: 推送代码到 GitHub，然后在 https://jitpack.io 输入 com.github.adzcsx2:android-common-lib:版本号

apply(plugin = "maven-publish")

extensions.configure<PublishingExtension> {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components.findByName("release"))
            }

            groupId = "com.github.adzcsx2"
            artifactId = project.name
            version = project.findProperty("libVersion")?.toString() ?: "1.0.0"

            pom {
                name.set(artifactId)
                description.set("Android Common Library - $artifactId module")
                url.set("https://github.com/adzcsx2/android-common-lib")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("adzcsx2")
                        name.set("adzcsx2")
                    }
                }

                scm {
                    connection.set("scm:git:git@github.com:adzcsx2/android-common-lib.git")
                    developerConnection.set("scm:git:git@github.com:adzcsx2/android-common-lib.git")
                    url.set("https://github.com/adzcsx2/android-common-lib")
                }
            }
        }
    }
}
