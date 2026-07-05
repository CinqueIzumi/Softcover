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

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        androidHostTest.dependencies {
            // The production code uses `androidx.sqlite:sqlite-bundled` (the KMP artifact), which
            // ships Android native libraries. The JVM host-test runner needs the JVM variant
            // instead — it carries macOS/Linux/Windows native dylibs loaded at test startup.
            runtimeOnly(libs.androidx.sqlite.bundled.jvm)
        }
    }
}
