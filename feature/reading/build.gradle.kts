plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.reading"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:book"))
            implementation(project(":core:deadlines"))
            implementation(project(":core:library"))
            implementation(project(":core:preferences"))
            implementation(project(":core:profile"))
            implementation(project(":core:notification"))
            implementation(project(":core:designsystem"))

            implementation(libs.rhaydus.coreUi)
            implementation(libs.rhaydus.toad)

            implementation(libs.koin.compose.multiplatform)
            implementation(libs.coil3)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.koin)
            api(libs.voyager.tabNavigator)
        }
    }
}
