plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.settings"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:lists"))
    implementation(project(":core:library"))
    api(project(":core:preferences"))
    implementation(project(":core:designsystem"))

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
    api(libs.voyager.tabNavigator)
}
