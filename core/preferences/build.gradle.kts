plugins {
    id("softcover.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "nl.rhaydus.softcover.core.preferences"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:network"))

    implementation(libs.dataStore)
    implementation(libs.kotlinx.serialization.json)
}
