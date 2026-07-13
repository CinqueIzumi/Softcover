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

            implementation(libs.rhaydus.coreCommon)

            // `api`: the connectivity contracts below expose foundation supertypes
            // (WriteQueue, OfflineWriteDrainer) on their own public surface.
            api(libs.rhaydus.offlineSync)

            implementation(libs.kotlinx.serialization.json)
        }
    }
}
