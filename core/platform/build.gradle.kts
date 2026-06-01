plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.core.platform"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime.ktx)
}
