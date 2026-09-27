plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

// `Res` stays internal: a consumer reaches a component's copy through the component, never through
// CMP's resource runtime directly.
compose.resources {
    publicResClass = false
    packageOfResClass = "nl.rhaydus.softcover.core.component.generated.resources"
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.component"

        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.collections.immutable)
            api(project(":core:designsystem"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.designsystemImage)

            api(libs.coil3)
            api(libs.kotlinx.datetime)
        }
    }
}
