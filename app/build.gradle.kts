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
        versionCode = 36
        versionName = "3.1.2"

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

    // The DebugRoutesContent seam, bound per build type below. Needed in every variant: the release
    // binding implements the same interface with a no-op body.
    implementation(project(":core:presentation"))

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
    // network artifact; coil-compose is not used here (the only Compose UI in :app is debug-only).
    implementation(libs.coil3.network.okhttp)

    // Debug-only tooling: the motion / share-card / routes debug screens (`src/debug/`). They live in
    // the application shell rather than in a library module because `:app` is the only module with
    // build types — the KMP Android library plugin produces a single variant, so an implementation in
    // `:core:designsystem` shipped in release builds even though its *binding* was stripped. On
    // `debugImplementation`, none of this reaches the release binary. `:app` cannot depend on
    // `:feature:settings` (the tier rule allows app -> orchestration/core only), which is why the
    // screens are here and not beside the Settings surface that reveals them.
    //
    // `compose-material3-expressive` rather than the AndroidX material3 from the Compose BOM: these
    // screens render `:core:designsystem` composables (SoftcoverTopBar, AnimatedStatNumber) that are
    // compiled against the same CMP artifact, and the M3-expressive APIs one of them uses
    // (LinearWavyProgressIndicator) are exactly what CMP's stable material3 strips. One material3 on
    // the classpath, at one version.
    //
    // Only these three are declared. `designsystem-core` and `core-common` are already on :app's
    // compile classpath in every variant, through `:core:designsystem`'s `api` edges; `designsystem-
    // editorial` is not, because that module holds it on `implementation`. `voyager-navigator` is
    // declared rather than taken from `:core:presentation`'s `api` edge, so the debug screens do not
    // silently depend on another module's choice to re-export it.
    debugImplementation(libs.compose.material3.expressive)
    debugImplementation(libs.voyager.navigator)
    debugImplementation(libs.rhaydus.designsystemEditorial)
}
