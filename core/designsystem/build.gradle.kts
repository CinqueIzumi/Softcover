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
            // NO `project(...)` dependency belongs here, and `checkModuleGraph` fails the build if one
            // appears (migration tracker § 6, G2). This module is tokens — theme, editorial typography,
            // the icon/illustration catalogs, modifiers, the bottom-chrome padding seam,
            // shared-element scopes — and a token has nothing
            // to ask of the rest of the app. The source-level half of the same gate is a detekt
            // `ForbiddenImport` scoped to `**/core/designsystem/**` in `config/detekt/detekt.yml`.
            //
            // Three dependencies left with the `Deadline*` trio in S4-5b, and all three had to go in the
            // same commit that emptied them because `onUnusedDependencies` is `severity("fail")`:
            // `:core:domain` (the trio's domain types), `kotlinx-datetime` and `core-common`
            // (`secondsToHm` for the pace line, `currentLocalDate` for the release-date formatters).
            // `:core:component` and `:core:uibinding` declare the latter two themselves now, rather than
            // inheriting them through the `api` edges that used to sit here.
            api(libs.rhaydus.designsystemCore)

            implementation(libs.rhaydus.designsystemEditorial)
        }

        androidMain.dependencies {
            // Provides the Material Components `Theme.Material3.*` XML themes the Android manifest/resources
            // reference. Used only from XML (not Kotlin), so dependency-analysis can't see it — excluded from
            // the buildHealth unused-check in the root build.
            implementation(libs.material.components)
        }
    }
}
