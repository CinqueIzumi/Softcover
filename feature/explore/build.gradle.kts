plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.feature.explore"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:book"))
            api(project(":core:identity"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            implementation(project(":core:designsystem"))
            api(project(":core:preferences"))
            api(project(":core:lists"))

            implementation(libs.rhaydus.coreCommon)
            implementation(libs.rhaydus.designsystemCore)
            implementation(libs.rhaydus.designsystemEditorial)
            implementation(libs.rhaydus.toad)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.datastore.core)
            implementation(libs.datastore.core.okio)
            implementation(libs.okio)

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.koin)
            api(libs.voyager.tabNavigator)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
