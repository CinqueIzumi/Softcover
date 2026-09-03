plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.component"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.collections.immutable)

            // Tokens only — theme, icon catalog, illustrations, modifiers, shared-element scopes. This
            // is the ONE project dependency the library is allowed (`componentLibraryAllowedProjects`
            // in the root build); everything else it needs arrives in a UI model or an event lambda.
            implementation(project(":core:designsystem"))

            // Foundation UI kit. `core-common`, `designsystem-core` and coil arrive through
            // `:core:designsystem`'s `api` edges; these two it holds on `implementation`, so they are
            // declared here: the editorial section header (the verdict sheet's own chrome) and the
            // shimmer image the share cards load covers through.
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.designsystemImage)
        }
    }
}
