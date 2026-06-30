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

            api(libs.rhaydus.coreUi)
            api(libs.rhaydus.designsystemCore)
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.designsystemImage)

            implementation(libs.koin.compose.multiplatform)

            api(libs.voyager.navigator)
            implementation(libs.voyager.koin)
            api(libs.voyager.tabNavigator)

            api(libs.coil3)

            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.material.components)
        }
    }
}
