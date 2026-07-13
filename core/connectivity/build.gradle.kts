plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.connectivity"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:database"))
            implementation(project(":core:book"))
            implementation(project(":core:lists"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.corePlatform)
            implementation(libs.rhaydus.offlineSync)
        }

        androidMain.dependencies {
            // `androidContext()` for the foundation's AndroidNetworkAvailabilityProvider.
            implementation(libs.koin.android)
        }
    }
}
