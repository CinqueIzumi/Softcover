plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.identity"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            api(project(":core:preferences"))
        }
    }
}
