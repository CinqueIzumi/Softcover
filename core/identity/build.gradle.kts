plugins {
    id("rhaydus.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.identity"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            api(project(":core:preferences"))

            implementation(libs.rhaydus.coreCommon)
        }
    }
}
