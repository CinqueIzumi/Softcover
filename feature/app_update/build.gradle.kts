plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.feature.app_update"
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
}
