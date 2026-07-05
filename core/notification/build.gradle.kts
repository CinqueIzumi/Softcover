plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.notification"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))

            implementation(libs.rhaydus.coreCommon)
        }

        androidMain.dependencies {
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.koin.android)
        }
    }
}
