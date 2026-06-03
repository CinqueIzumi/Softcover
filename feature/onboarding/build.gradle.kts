plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.onboarding"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:identity"))
    implementation(project(":core:designsystem"))

    implementation(libs.koin.compose)

    implementation(libs.voyager.koin)
}
