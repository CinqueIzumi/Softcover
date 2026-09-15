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
            api(project(":core:component"))
            implementation(project(":core:book"))

            implementation(libs.rhaydus.coreCommon)
            // `rememberIsOnline`'s injection seam names `NetworkAvailabilityProvider` in its own
            // signature, so a consumer sees the type without redeclaring the coordinate.
            api(libs.rhaydus.corePlatform)
            implementation(libs.rhaydus.designsystemCore)

            implementation(libs.koin.compose.multiplatform)

            // Voyager's `Screen` / `Navigator` / `Tab` are in the public surface of the navigation
            // contracts (AppNavigator, BookDetailPresenter), so consumers see them without redeclaring.
            api(libs.voyager.navigator)
            api(libs.voyager.tabNavigator)
        }
    }
}
