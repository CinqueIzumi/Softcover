plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.scan"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:book"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:presentation"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.designsystemCore)
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.toad)

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.koin)
        }

        androidMain.dependencies {
            // CameraX + bundled ML Kit pipeline backing the Android BarcodeScanner actual.
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.mlkit.barcode.scanning)

            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}
