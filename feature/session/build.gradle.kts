plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.session"
}

dependencies {
    api(project(":core:designsystem"))
    implementation(project(":core:platform"))

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
}
