plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.app_update"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.play.app.update)
            implementation(libs.play.app.update.ktx)
        }
    }
}
