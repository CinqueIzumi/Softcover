plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.reading"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:book"))
    implementation(project(":core:deadlines"))
    implementation(project(":core:library"))
    implementation(project(":core:preferences"))
    implementation(project(":core:profile"))
    implementation(project(":core:platform"))
    implementation(project(":core:designsystem"))

    implementation(libs.koin.compose)
    implementation(libs.coil)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
    implementation(libs.voyager.tabNavigator)
}
