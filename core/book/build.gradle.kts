plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.book"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:database"))
            api(project(":core:network"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.okio)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
