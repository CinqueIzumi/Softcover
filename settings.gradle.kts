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
include(":app")
include(":core:domain")
include(":core:database")
include(":core:network")
include(":core:platform")
include(":core:preferences")
include(":core:identity")
include(":core:book")
include(":core:lists")
include(":core:deadlines")
include(":core:personal")
include(":core:profile")
include(":core:library")
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
 