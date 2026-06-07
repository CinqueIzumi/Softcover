plugins {
    id("softcover.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.profile"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:identity"))
            implementation(project(":core:network"))

            implementation(libs.datastore.core)
            implementation(libs.datastore.core.okio)
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
