plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.widget"

        androidResources.enable = true
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:preferences"))
            implementation(project(":core:identity"))
            implementation(project(":core:book"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:uibinding"))
            implementation(project(":core:presentation"))

            implementation(libs.rhaydus.coreCommon)

            implementation(libs.koin.android)

            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)
        }
    }
}
