plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.settings"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:lists"))
            api(project(":core:preferences"))
            implementation(project(":core:designsystem"))

            implementation(libs.rhaydus.coreUi)
            implementation(libs.rhaydus.designsystemCore)
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.toad)

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.koin)
            api(libs.voyager.tabNavigator)
        }
    }
}
