plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "nl.rhaydus.softcover.feature.explore"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:book"))
    implementation(project(":core:identity"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dataStore)

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
    implementation(libs.voyager.tabNavigator)
}
