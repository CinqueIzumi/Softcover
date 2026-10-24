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
            implementation(project(":core:lists"))
            implementation(project(":core:personal"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:component"))
            implementation(project(":core:uibinding"))
            implementation(project(":core:presentation"))
            implementation(project(":core:progresssheet"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.designsystemCore)
            implementation(libs.rhaydus.toad)

            implementation(libs.koin.android)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.koin)

            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.work.runtime.ktx)

            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)
        }
    }
}
