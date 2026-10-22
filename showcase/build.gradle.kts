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
    testImplementation(libs.androidx.compose.ui.test)
}

val renderOutputDir = rootProject.layout.buildDirectory.dir("design-preset/renders")
val renderRunner = "nl.rhaydus.softcover.showcase.render.ShowcaseRenderRunner"

// The render runner generates the design preset's images and is not a test: every ordinary Test task
// excludes it, and only `renderShowcase` includes it.
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    if (name != "renderShowcase") filter.excludeTestsMatching(renderRunner)
}

tasks.register<Test>("renderShowcase") {
    group = "design preset"
    description = "Draws every gallery fixture to PNG under build/design-preset/renders."

    val debugUnitTest = tasks.named<Test>("testDebugUnitTest").get()
    testClassesDirs = debugUnitTest.testClassesDirs
    classpath = debugUnitTest.classpath
    filter.includeTestsMatching(renderRunner)
    systemProperty("roborazzi.test.record", "true")
    systemProperty("showcase.render.dir", renderOutputDir.get().asFile.absolutePath)
    outputs.dir(renderOutputDir)
    val renderDir = renderOutputDir
    doFirst { renderDir.get().asFile.deleteRecursively() }
}
