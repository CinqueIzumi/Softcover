plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.settings"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:lists"))
    implementation(project(":core:library"))
    implementation(project(":core:preferences"))
    implementation(project(":core:designsystem"))

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
    implementation(libs.voyager.tabNavigator)
}
