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
            // `secondsToHm` (the deadline pace line) and `currentLocalDate` (the release-date
            // formatters), both inherited from `:core:designsystem` until S4-5b moved their callers here
            // and that module dropped its own `core-common` edge.
            implementation(libs.rhaydus.coreCommon)

            api(project(":core:domain"))
            api(project(":core:component"))
            api(project(":core:designsystem"))
            // `LocalDate` is the public receiver of `toUnreleasedBadgeUiModel` and of both
            // `DateFormats` formatters — declare it rather than relying on the transitive edge
            // through `:core:domain`, since this module names the type in its own public surface.
            api(libs.kotlinx.datetime)
        }
    }
}
