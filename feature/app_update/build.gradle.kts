plugins {
    id("softcover.kmp.library")
    alias(libs.plugins.kotlin.serialization)
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

        jvmMain.dependencies {
            // Parses the GitHub Releases API response for the desktop self-updater.
            implementation(libs.kotlinx.serialization.json)

            // AppDispatchers, for running the update check/download off the main thread.
            implementation(libs.rhaydus.coreUi)
        }

        // The desktop updater lives in jvmMain, so its tests need the JUnit5 + MockK stack the
        // convention otherwise only wires for androidHostTest.
        jvmTest.dependencies {
            implementation(libs.junit.api)
            implementation(libs.junit.params)
            runtimeOnly(libs.junit.engine)
            runtimeOnly(libs.junit.platform.launcher)
            implementation(libs.mockk)
        }
    }
}
