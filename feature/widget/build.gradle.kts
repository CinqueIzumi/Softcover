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
            implementation(project(":core:designsystem"))

            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)
        }
    }
}
