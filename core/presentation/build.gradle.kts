plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:book"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.designsystemCore)

            implementation(libs.koin.compose.multiplatform)

            // Voyager's `Screen` / `Navigator` / `Tab` are in the public surface of the navigation
            // contracts (AppNavigator, BookDetailPresenter), so consumers see them without redeclaring.
            api(libs.voyager.navigator)
            api(libs.voyager.tabNavigator)
        }
    }
}
