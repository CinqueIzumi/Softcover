plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.lists"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:lists"))
    implementation(project(":core:designsystem"))

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
}
