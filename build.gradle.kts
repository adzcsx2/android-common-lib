import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

val ciSkipTests = (findProperty("ciSkipTests") as? String)?.toBooleanStrictOrNull() ?: false

// JitPack injects -Pgroup and -Pversion via Gradle -P flags.
// Version is NOT inherited by subprojects in Gradle; group inherits but not reliably.
// Propagate both to ALL projects so AGP auto-created publications get correct coordinates.
allprojects {
    val jitpackGroup = (findProperty("group") as? String)?.takeIf { it.isNotEmpty() }
    val jitpackVersion = (findProperty("version") as? String)?.takeIf { it.isNotEmpty() }
    jitpackGroup?.let { group = it }
    jitpackVersion?.let { version = it }
}

allprojects {
    configurations.configureEach {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }

    if (ciSkipTests) {
        tasks.configureEach {
            if (name.contains("test", ignoreCase = true)) {
                enabled = false
            }
        }
    }
}

subprojects {
    pluginManager.withPlugin("maven-publish") {
        extensions.configure(PublishingExtension::class.java) {
            publications {
                if (findByName("release") == null && plugins.hasPlugin("com.android.library")) {
                    create("release", MavenPublication::class.java) {
                        groupId = project.group.toString()
                        artifactId = project.name
                        version = project.version.toString()
                        afterEvaluate {
                            from(components.findByName("release"))
                        }
                    }
                }
            }
        }
    }
}

val androidModules = listOf(
    "app",
    "common-all",
    "common-base",
    "common-compose",
    "common-core",
    "common-image",
    "common-network",
    "common-ui",
    "common-utils"
)

val publishableLibraryModules = androidModules.filterNot { it == "app" }

tasks.register("qualityCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs lint and debug unit tests for all Android modules."
    dependsOn(androidModules.flatMap { moduleName ->
        listOf(
            ":$moduleName:lint",
            ":$moduleName:testDebugUnitTest"
        )
    })
}

tasks.register("libraryPublishDryRun") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Publishes all library modules to Maven Local to validate publication artifacts and metadata."
    dependsOn(publishableLibraryModules.map { moduleName -> ":$moduleName:publishToMavenLocal" })
}
