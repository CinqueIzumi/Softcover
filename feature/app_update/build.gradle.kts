plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.feature.app_update"
}

dependencies {
    api(project(":core:domain"))

    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
}
