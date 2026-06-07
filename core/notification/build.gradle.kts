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
        }

        androidMain.dependencies {
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
        }
    }
}
