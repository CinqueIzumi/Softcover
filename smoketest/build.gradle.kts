import java.util.Properties

plugins {
    alias(libs.plugins.android.test)
}

// Passed only as a runtime instrumentation argument, never through a resource, BuildConfig or log, so the
// token stays out of every build output.
val hardcoverSmokeToken: String? = System.getenv("HARDCOVER_SMOKE_TOKEN")?.takeIf { it.isNotBlank() }
    ?: Properties().apply {
        val localProperties = rootDir.resolve("local.properties")
        if (localProperties.exists()) localProperties.inputStream().use { stream -> load(stream) }
    }.getProperty("hardcover.apiToken")?.takeIf { it.isNotBlank() }

android {
    namespace = "nl.rhaydus.softcover.smoketest"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        hardcoverSmokeToken?.let { testInstrumentationRunnerArguments["hardcoverToken"] = it }
    }

    buildTypes {
        create("minified") {
            matchingFallbacks += "release"
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            proguardFiles("src/main/keepRules/smoketest.keep")
        }
    }

    targetProjectPath = ":app"

    // The test APK runs in its own process and drives the app from outside. Instrumenting inside the
    // app process would load the runner into R8-stripped code, which is exactly what must not be required.
    experimentalProperties["android.experimental.self-instrumenting"] = true

    testOptions {
        managedDevices {
            localDevices {
                create("pixelApi36") {
                    device = "Pixel 6"
                    apiLevel = 36
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.junit)
}
