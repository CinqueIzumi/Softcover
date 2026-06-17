plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.library"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:book"))
            api(project(":core:lists"))
            api(project(":core:preferences"))
            api(project(":core:identity"))

            implementation(libs.rhaydus.coreUi)
        }
    }
}
