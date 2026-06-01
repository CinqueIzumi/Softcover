plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.core.designsystem"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:book"))
    implementation(project(":core:library"))
    implementation(project(":core:profile"))
    implementation(project(":core:identity"))
    implementation(project(":core:personal"))
    implementation(project(":core:preferences"))
    implementation(project(":core:deadlines"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
    implementation(libs.voyager.tabNavigator)
    implementation(libs.voyager.transitions)

    implementation(libs.coil)

    // Shared BarcodeScanner composable
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)
}
