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
        versionCode = 33
        versionName = "3.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing is configured only when a keystore is supplied via the environment (CI decodes
    // one from repository secrets). A local build without these variables falls back to an unsigned
    // release — exactly as before — so the signing wiring never blocks a developer build.
    val releaseKeystoreFile = System.getenv("ANDROID_KEYSTORE_FILE")?.let(::file)

    signingConfigs {
        if (releaseKeystoreFile != null) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Null when no keystore is supplied (local builds) → release stays unsigned, as before.
            signingConfig = signingConfigs.findByName("release")
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

    // The logging facade the Application entry point installs (AppLog.install) now lives in the
    // foundation core-common module.
    implementation(libs.rhaydus.coreCommon)

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
