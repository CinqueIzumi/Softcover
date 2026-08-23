plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.component"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.collections.immutable)
        }
    }
}
