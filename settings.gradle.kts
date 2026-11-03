pluginManagement {
    includeBuild("build-logic")
    includeBuild("foundation/build-logic") { name = "foundation-build-logic" }
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Softcover"

includeBuild("foundation")

include(":app")
include(":showcase")
include(":smoketest")
include(":desktopApp")
include(":konsist")
include(":apollo-schema-corrections")
include(":orchestration")
include(":core:domain")
include(":core:database")
include(":core:network")
include(":core:notification")
include(":core:preferences")
include(":core:identity")
include(":core:book")
include(":core:lists")
include(":core:corpus")
include(":core:deadlines")
include(":core:personal")
include(":core:profile")
include(":core:connectivity")
include(":core:designsystem")
include(":core:component")
include(":core:uibinding")
include(":core:presentation")
include(":core:progresssheet")
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
include(":feature:widget")
 