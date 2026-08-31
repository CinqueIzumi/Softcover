plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

// The Roadmap screen renders the repo's public `ROADMAP.md`, fetched live so a milestone edit reaches
// users with no app release. This bundles the file as it stood at build time, as the offline /
// first-launch fallback — copied from the single source of truth at the repo root rather than kept as a
// second committed copy, so the in-app fallback can never drift from the published roadmap.
val bundleRoadmapResource = tasks.register<Sync>("bundleRoadmapResource") {
    group = "build"
    description = "Copies the repo's ROADMAP.md into this module's Compose resources as the offline fallback."

    from(rootProject.layout.projectDirectory.file("ROADMAP.md")) {
        rename { "roadmap.md" }
    }
    into(layout.buildDirectory.dir("generated/roadmapResource/files"))
}

// Compose Multiplatform resource accessor: keep `Res` internal to this module (its only resource is the
// bundled roadmap above, which never leaves the Roadmap data layer).
compose.resources {
    publicResClass = false
    packageOfResClass = "nl.rhaydus.softcover.feature.settings.generated.resources"

    // Wired through the task provider, not the bare directory: the Provider `map` returns carries the
    // producing task, so the resource-packaging tasks depend on the Sync and pick up an edited
    // ROADMAP.md instead of packaging a stale (or missing) copy.
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = bundleRoadmapResource.map { layout.buildDirectory.dir("generated/roadmapResource").get() },
    )
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.settings"
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
