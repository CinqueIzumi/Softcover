plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.scan"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:book"))
    implementation(project(":core:platform"))
    implementation(project(":core:designsystem"))

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
}
