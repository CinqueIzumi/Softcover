import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "nl.rhaydus.softcover.showcase"
    compileSdk = 37

    defaultConfig {
        applicationId = "nl.rhaydus.softcover.showcase"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    lint {
        warningsAsErrors = true
        abortOnError = true
        lintConfig = rootProject.file("lint.xml")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:component"))

    implementation(libs.rhaydus.toad)
    implementation(libs.rhaydus.coreCommon)

    implementation(libs.koin.android)
    implementation(libs.koin.compose.multiplatform)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.compose.material3.expressive)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest)
    testImplementation(libs.coroutines.test)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.androidx.compose.ui.test.junit4)
}

val renderRequested = providers.gradleProperty("showcaseRender").isPresent
val renderOutputDir = rootProject.layout.buildDirectory.dir("design-preset/renders")

tasks.withType<Test> {
    useJUnitPlatform()
    val renderRunner = "nl.rhaydus.softcover.showcase.render.*"
    if (renderRequested) {
        filter.includeTestsMatching(renderRunner)
        systemProperty("roborazzi.test.record", "true")
        systemProperty("showcase.render.dir", renderOutputDir.get().asFile.absolutePath)
        outputs.upToDateWhen { false }
    } else {
        filter.excludeTestsMatching(renderRunner)
    }
}
