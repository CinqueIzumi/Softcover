plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.personal"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:database"))
            implementation(project(":core:network"))
            implementation(project(":core:identity"))

            implementation(libs.rhaydus.coreCommon)
        }
    }
}
