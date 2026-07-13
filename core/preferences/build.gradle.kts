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

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.corePlatform)

            implementation(libs.datastore.core)
            implementation(libs.datastore.core.okio)
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        jvmMain.dependencies {
            // The app constructs the namespaced KSafe and injects it into the foundation's
            // JvmSecureStorage (OS secret store + software fallback).
            implementation(libs.ksafe)
        }
    }

}
