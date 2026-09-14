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

            // Foundation UI kit. `core-common` and `designsystem-core` arrive through
            // `:core:designsystem`'s `api` edges; these it holds on `implementation`, so they are
            // declared here: the editorial section header (the verdict sheet's own chrome) and the
            // shimmer image the share cards load covers through.
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.designsystemImage)

            // `rememberCoverImageRequest` (cover/) returns `coil3.request.ImageRequest` as part of its
            // public signature, so this is an `api` edge rather than `implementation` — a consumer
            // holding the request needs the type on its own compile classpath.
            api(libs.coil3)
        }
    }
}
