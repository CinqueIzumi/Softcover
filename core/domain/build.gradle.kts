plugins {
    id("softcover.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "nl.rhaydus.softcover.core.domain"
}

dependencies {
    api(libs.kotlinx.datetime)

    implementation(libs.kermit)
    implementation(libs.kotlinx.serialization.json)
}
