plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.feature.book_detail"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:book"))
    implementation(project(":core:lists"))
    implementation(project(":core:deadlines"))
    implementation(project(":core:profile"))
    api(project(":core:identity"))
    implementation(project(":core:preferences"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    api(project(":core:designsystem"))

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
}
