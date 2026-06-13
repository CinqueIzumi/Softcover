plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.orchestration"
    }

    sourceSets {
        commonMain.dependencies {
            // Core modules
            implementation(project(":core:domain"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:preferences"))
            implementation(project(":core:identity"))
            implementation(project(":core:book"))
            implementation(project(":core:lists"))
            implementation(project(":core:deadlines"))
            implementation(project(":core:personal"))
            implementation(project(":core:profile"))
            implementation(project(":core:library"))
            implementation(project(":core:connectivity"))
            implementation(project(":core:notification"))
            implementation(project(":core:designsystem"))

            // Feature modules (orchestration composes them all)
            implementation(project(":feature:book_detail"))
            implementation(project(":feature:explore"))
            implementation(project(":feature:library"))
            implementation(project(":feature:lists"))
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:reading"))
            implementation(project(":feature:scan"))
            implementation(project(":feature:session"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:app_update"))

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.transitions)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.splash)
        }
    }
}
