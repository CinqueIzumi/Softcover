plugins {
    id("softcover.android.library")
    id("softcover.android.compose")
}

android {
    namespace = "nl.rhaydus.softcover.core.designsystem"
}

dependencies {
    api(project(":core:domain"))
    api(project(":core:book"))
    api(project(":core:library"))
    api(project(":core:profile"))
    api(project(":core:identity"))
    api(project(":core:personal"))
    api(project(":core:preferences"))
    implementation(project(":core:platform"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.material.components)

    implementation(libs.koin.compose)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
    api(libs.voyager.tabNavigator)

    api(libs.coil)

    // Shared BarcodeScanner composable
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)
}
