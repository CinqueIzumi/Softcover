import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // The build-type debug-routes binding (app/src/debug) implements the @Composable DebugRoutesContent
    // seam, so the application shell needs the Compose compiler to transform that override correctly.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "nl.rhaydus.softcover"
    compileSdk = 37

    defaultConfig {
        applicationId = "nl.rhaydus.softcover"
        minSdk = 26
        targetSdk = 37
        versionCode = 29
        versionName = "2.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
    lint {
        warningsAsErrors = true
        abortOnError = true
        lintConfig = rootProject.file("lint.xml")
    }
}

// AGP 9 ships built-in Kotlin and removes the `android.kotlinOptions { }` block; Kotlin compiler
// options now live in the top-level `kotlin { }` DSL.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // Orchestration tier (composes every feature + core module)
    implementation(project(":orchestration"))

    // Core modules the Application entry point + version provider touch directly
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:notification"))

    // DI
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Image loading — the Application registers a network fetcher on the singleton loader (the default
    // loader ships none) via SingletonImageLoader.Factory. coil-core comes transitively from the
    // network artifact; coil-compose is not used here (no Compose UI in :app).
    implementation(libs.coil3.network.okhttp)
}
