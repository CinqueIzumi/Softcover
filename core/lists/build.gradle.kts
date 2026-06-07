plugins {
    id("softcover.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.lists"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            implementation(project(":core:book"))
        }
    }
}
