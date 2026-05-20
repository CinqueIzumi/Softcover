plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.apollo)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "nl.rhaydus.softcover"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "nl.rhaydus.softcover"
        minSdk = 26
        targetSdk = 36
        versionCode = 27
        versionName = "2.2.1"

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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Apollo (graph ql communication)
    implementation(libs.apollo)
    implementation(libs.apollo.normalized.cache)

    // Datastore
    implementation(libs.dataStore)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // DI
    implementation(libs.koin.core)
    implementation(libs.koin.compose)

    // Navigation
    implementation(libs.voyager.koin)
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.tabNavigator)
    implementation(libs.voyager.transitions)

    // Image loading
    implementation(libs.coil)

    // Timber
    implementation(libs.timber)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Material Components for Android - used for XML themes (Theme.Material3.*)
    implementation(libs.material.components)

    // Splash screen
    implementation(libs.androidx.splash)

    // Google Play in-app updates
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)

    // WorkManager for scheduled notifications
    implementation(libs.androidx.work.runtime.ktx)

    // Unit test dependencies
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.engine)
    testImplementation(libs.junit.api)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

apollo {
    service("service") {
        packageName.set("nl.rhaydus.softcover")
        addTypename.set("always")

        schemaFiles.from("src/main/graphql/schema.graphqls", "src/main/graphql/extra.graphqls")

        mapScalar("numeric", "kotlin.Double")
        mapScalar("float8", "kotlin.Double")
        mapScalar("date", "kotlin.String")
        mapScalar("timestamp", "kotlin.String")
        mapScalar("timestamptz", "kotlin.String")
        mapScalar("smallint", "kotlin.Int")

        codegenModels.set("responseBased")
        generateMethods.set(listOf("dataClass"))
    }
}