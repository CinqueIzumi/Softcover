plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "nl.rhaydus.softcover"
    compileSdk {
        version = release(37)
    }

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
    kotlinOptions {
        jvmTarget = "11"
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

dependencies {
    // Orchestration tier (composes every feature + core module)
    implementation(project(":orchestration"))

    // Core modules the Application entry point + version provider touch directly
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:identity"))
    implementation(project(":core:connectivity"))
    implementation(project(":core:platform"))

    // DI
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Timber
    implementation(libs.timber)
}
