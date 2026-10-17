plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

// The Roadmap and What's new screens render documents published from the repo root (`ROADMAP.md`,
// `CHANGELOG.md`), fetched live so an edit reaches users with no app release. This bundles both as they
// stood at build time, as the offline / first-launch fallback — copied from the single source of truth at
// the repo root rather than kept as second committed copies, so the in-app fallback can never drift from
// the published documents.
val bundlePublishedDocuments = tasks.register<Sync>("bundlePublishedDocuments") {
    group = "build"
    description = "Copies the repo's ROADMAP.md and CHANGELOG.md into this module's Compose resources as offline fallbacks."

    from(rootProject.layout.projectDirectory.file("ROADMAP.md")) {
        rename { "roadmap.md" }
    }
    from(rootProject.layout.projectDirectory.file("CHANGELOG.md")) {
        rename { "changelog.md" }
    }
    into(layout.buildDirectory.dir("generated/publishedDocuments/files"))
}

// Compose Multiplatform resource accessor: keep `Res` internal to this module (its only resources are the
// bundled published documents above, which never leave the data layer).
compose.resources {
    publicResClass = false
    packageOfResClass = "nl.rhaydus.softcover.feature.settings.generated.resources"

    // Wired through the task provider, not the bare directory: the Provider `map` returns carries the
    // producing task, so the resource-packaging tasks depend on the Sync and pick up an edited
    // published document instead of packaging a stale (or missing) copy.
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = bundlePublishedDocuments.map { layout.buildDirectory.dir("generated/publishedDocuments").get() },
    )
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.settings"

        // Required for the bundled published documents to reach the APK at all. The KMP Android library
        // plugin keeps Android resources off by default and Compose Multiplatform resources ship as
        // Android *assets*, so without this the `compose.resources` block still generates its `Res`
        // accessor and still compiles — the failure is entirely at runtime, as a
        // `MissingResourceException` from `PublishedDocumentBundledDataSource`. That made it invisible: the
        // bundled copy is only read before the first live fetch lands, so a machine with a warm cache
        // never touches it.
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:book"))
            implementation(project(":core:component"))
            implementation(project(":core:database"))
            implementation(project(":core:lists"))
            implementation(project(":core:network"))
            api(project(":core:preferences"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:presentation"))
            implementation(project(":core:uibinding"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.designsystemCore)
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.toad)

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.koin)
            api(libs.voyager.tabNavigator)
        }
    }
}
