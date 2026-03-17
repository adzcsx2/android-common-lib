import org.gradle.language.base.plugins.LifecycleBasePlugin

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
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
