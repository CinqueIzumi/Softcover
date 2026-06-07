plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.connectivity"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:database"))
            api(project(":core:book"))
            api(project(":core:lists"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
        }
    }
}
