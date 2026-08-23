plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.lists"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:lists"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:presentation"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.designsystemCore)
            implementation(libs.rhaydus.toad)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.koin)
        }
    }
}
