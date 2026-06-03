plugins {
    id("softcover.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)

            implementation(libs.kermit)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
