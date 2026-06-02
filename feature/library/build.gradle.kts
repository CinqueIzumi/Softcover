plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.library"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:book"))
    implementation(project(":core:lists"))
    implementation(project(":core:deadlines"))
    implementation(project(":core:library"))
    implementation(project(":core:preferences"))
    implementation(project(":core:designsystem"))

    implementation(libs.koin.compose)
    implementation(libs.reorderable)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
    api(libs.voyager.tabNavigator)
}
