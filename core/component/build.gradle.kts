plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

// Compose Multiplatform resource accessor: keep `Res` internal to this module (a consumer reaches a
// component's copy through the component, never through CMP's resource runtime directly). The library
// owns the copy that belongs to a component rather than to a feature — the offline banner and the
// offline screen say the same thing on every surface that shows them.
compose.resources {
    publicResClass = false
    packageOfResClass = "nl.rhaydus.softcover.core.component.generated.resources"
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.component"

        // The KMP Android library plugin keeps Android resources off by default, and Compose
        // Multiplatform resources ship as Android *assets* — so without this the `composeResources`
        // block below compiles and generates its accessors, but nothing is packaged into the APK and
        // the first read throws `MissingResourceException` at runtime. That is exactly what happened:
        // S4-5a gave this module its own copy without the flag, and the offline banner crashed the app
        // on launch the first time it was run on a device with no network. `:core:designsystem` carries
        // the same line for the same reason.
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.collections.immutable)

            // Tokens only — theme, icon catalog, illustrations, modifiers, shared-element scopes. This
            // is the ONE project dependency the library is allowed (`componentLibraryAllowedProjects`
            // in the root build); everything else it needs arrives in a UI model or an event lambda.
            //
            // `api` rather than `implementation` because the Appearance picker tiles' UI models name a
            // token in their own public surface (`ThemePreviewTileUiModel.palette: SpinePalette`), so a
            // consumer holding one needs the type. The root build deliberately keeps
            // `:core:designsystem` out of `apiSignOffModules` for exactly this reason: once G2 lands it
            // is a leaf, so re-exporting it republishes nothing a consumer could not already reach.
            api(project(":core:designsystem"))

            // Foundation UI kit. `core-common` and `designsystem-core` arrive through
            // `:core:designsystem`'s `api` edges; these it holds on `implementation`, so they are
            // declared here: the editorial section header (the verdict sheet's own chrome) and the
            // shimmer image the share cards load covers through.
            //
            // `core-common` supplies `currentLocalDateTime()` in the progress sheet's date picker. It
            // is declared here as of S4-5b: it used to arrive through `:core:designsystem`'s
            // `api(core-common)` edge, which that module dropped when the `Deadline*` trio took its
            // last user away.
            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.designsystemImage)

            // `rememberCoverImageRequest` (cover/) returns `coil3.request.ImageRequest` as part of its
            // public signature, so this is an `api` edge rather than `implementation` — a consumer
            // holding the request needs the type on its own compile classpath.
            api(libs.coil3)

            // `UpdateProgressBottomSheet` (progress/) uses `LocalDateTime`/`TimeZone`. It used to get
            // this transitively through `:core:designsystem`'s own `api(libs.kotlinx.datetime)`;
            // S4-5b tightens that edge to `implementation` (the badge/ family it was covering for
            // moved here without needing the type on its own public surface), so this module now
            // declares its actual dependency directly instead of relying on someone else's `api`.
            implementation(libs.kotlinx.datetime)
        }
    }
}
