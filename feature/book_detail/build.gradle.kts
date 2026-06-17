plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.book_detail"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:book"))
            implementation(project(":core:lists"))
            implementation(project(":core:deadlines"))
            implementation(project(":core:profile"))
            api(project(":core:identity"))
            implementation(project(":core:preferences"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            api(project(":core:designsystem"))

            implementation(libs.rhaydus.coreUi)
            implementation(libs.rhaydus.designsystemCore)
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.toad)

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.koin)
        }
    }
}
