plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

// Compose Multiplatform resource accessor: keep `Res` internal to this module (consumers reach
// drawables through the `SoftcoverIcon` catalog, never CMP's resource runtime directly).
compose.resources {
    publicResClass = false
    packageOfResClass = "nl.rhaydus.softcover.core.designsystem.generated.resources"
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.designsystem"

        // The KMP Android library plugin keeps Android resources off by default; this module ships
        // the shared drawables/strings/themes, so opt them in.
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:book"))
            api(project(":core:library"))
            api(project(":core:profile"))
            api(project(":core:identity"))
            api(project(":core:personal"))
            api(project(":core:preferences"))

            api(libs.rhaydus.coreUi)
            api(libs.rhaydus.designsystemCore)

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.androidx.lifecycle.runtime.compose)
            api(libs.androidx.lifecycle.viewmodel)

            api(libs.voyager.navigator)
            implementation(libs.voyager.koin)
            api(libs.voyager.tabNavigator)

            api(libs.coil3)

            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.material.components)

            // Shared BarcodeScanner composable
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.mlkit.barcode.scanning)
        }
    }
}
