plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.session"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:designsystem"))

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.voyager.navigator)
        }

        androidMain.dependencies {
            implementation(project(":core:notification"))

            implementation(libs.koin.android)
        }
    }
}
