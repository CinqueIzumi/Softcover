plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.progresssheet"
    }

    sourceSets {
        commonMain.dependencies {
            // The slice's public contract names `SheetSession`, `ProgressSheetUiModel`, `ProgressSheetEvent`
            // and `ProgressTimeDraft`, so a host sees all of them without re-declaring a dependency.
            api(project(":core:domain"))
            api(project(":core:component"))
            api(project(":core:uibinding"))
            api(libs.rhaydus.toad)
            api(libs.kotlinx.datetime)

            implementation(project(":core:preferences"))

            implementation(libs.rhaydus.coreCommon)
        }
    }
}
