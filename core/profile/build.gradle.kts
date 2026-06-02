plugins {
    id("softcover.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "nl.rhaydus.softcover.core.profile"
}

dependencies {
    api(project(":core:domain"))
    api(project(":core:identity"))
    implementation(project(":core:network"))

    implementation(libs.dataStore)
    implementation(libs.kotlinx.serialization.json)
}
