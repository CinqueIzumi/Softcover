plugins {
    id("softcover.android.library")
    id("softcover.android.room")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "nl.rhaydus.softcover.core.database"
}

dependencies {
    api(project(":core:domain"))

    implementation(libs.kotlinx.serialization.json)
}
