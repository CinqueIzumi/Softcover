plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.lists"
}

dependencies {
    api(project(":core:domain"))
    api(project(":core:lists"))
    implementation(project(":core:designsystem"))

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
}
