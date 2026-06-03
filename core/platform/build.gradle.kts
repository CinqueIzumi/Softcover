plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.core.platform"
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.work.runtime.ktx)
}
