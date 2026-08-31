plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.uibinding"
    }

    sourceSets {
        commonMain.dependencies {
            // `api` on both sides of a mapping (migration tracker § 3a): a feature depending on this
            // module sees the domain type it maps from and the design-system/UI type it maps to,
            // without re-declaring either. No `allowedApiDataEdges` row is needed — `:core:domain` is
            // a contract module, not a data-area one.
            api(project(":core:domain"))
            api(project(":core:designsystem"))
        }
    }
}
