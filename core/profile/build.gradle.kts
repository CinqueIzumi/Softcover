plugins {
    id("softcover.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "nl.rhaydus.softcover.core.profile"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:identity"))

    implementation(libs.dataStore)
    implementation(libs.kotlinx.serialization.json)
}
