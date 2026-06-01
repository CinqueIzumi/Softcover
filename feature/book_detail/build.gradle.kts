plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.book_detail"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:book"))
    implementation(project(":core:lists"))
    implementation(project(":core:deadlines"))
    implementation(project(":core:profile"))
    implementation(project(":core:identity"))
    implementation(project(":core:preferences"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))

    implementation(libs.koin.compose)
    implementation(libs.coil)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
}
