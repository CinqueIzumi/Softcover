plugins {
    id("softcover.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.preferences"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:network"))

            implementation(libs.rhaydus.coreUi)

            implementation(libs.datastore.core)
            implementation(libs.datastore.core.okio)
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        jvmMain.dependencies {
            // Backs the desktop SecureApiKeyStorage actual (OS secret store + software fallback).
            implementation(libs.ksafe)
        }
    }

}
