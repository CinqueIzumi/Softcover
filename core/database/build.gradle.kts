plugins {
    id("softcover.kmp.library")
    id("softcover.android.room")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.database"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))

            implementation(libs.rhaydus.coreUi)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
