import java.util.Properties

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Softcover"

// Foundation inner-loop switch (see docs/rhaydus/0.3.1/CAPABILITIES.md "How a project consumes it"):
// set `foundation.local=true` in local.properties to substitute the published nl.rhaydus coordinates
// for a sibling `../rhaydus-foundation` checkout via an included build — no version bumps, instant
// cross-repo edits. Off by default; the build resolves the published artifacts from mavenCentral.
val foundationLocal = Properties().apply {
    val localProperties = rootDir.resolve("local.properties")
    if (localProperties.exists()) localProperties.inputStream().use { load(it) }
}.getProperty("foundation.local").toBoolean()
if (foundationLocal) {
    includeBuild("../rhaydus-foundation")
}

include(":app")
include(":desktopApp")
include(":orchestration")
include(":core:domain")
include(":core:database")
include(":core:network")
include(":core:notification")
include(":core:preferences")
include(":core:identity")
include(":core:book")
include(":core:lists")
include(":core:deadlines")
include(":core:personal")
include(":core:profile")
include(":core:connectivity")
include(":core:designsystem")
include(":feature:lists")
include(":feature:profile")
include(":feature:onboarding")
include(":feature:explore")
include(":feature:library")
include(":feature:book_detail")
include(":feature:reading")
include(":feature:session")
include(":feature:scan")
include(":feature:settings")
include(":feature:app_update")
 