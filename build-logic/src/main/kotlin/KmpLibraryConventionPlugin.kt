import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Base convention for `:core:*` / `:feature:*` modules migrated to Kotlin Multiplatform — the KMP
 * sibling of [AndroidLibraryConventionPlugin]. Applies the modern single-Android-target KMP library
 * plugin (`com.android.kotlin.multiplatform.library`) plus the Kotlin Multiplatform plugin, declares
 * the Android + three iOS targets, matches the Android-only plugin's SDK/JDK levels and lint config,
 * and wires the shared dependencies as their KMP (non-`-android`) variants.
 *
 * Test stack mirrors the Android plugin but split by source set: the multiplatform tools
 * (Kotest, Turbine, coroutines-test) go in `commonTest`; the JVM-only tools (JUnit5, MockK) go in
 * the Android host-test set. Logging (Kermit, via `AppLog`) is not injected here — it comes
 * transitively from `:core:domain`, which owns the facade and declares Kermit itself.
 *
 * Per-module concerns stay in the module build file: `namespace` (required by `androidLibrary`),
 * any extra dependencies, and — for UI modules — the Compose Multiplatform layer.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.kotlin.multiplatform.library")
            apply("org.jetbrains.kotlin.multiplatform")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>(
                "androidLibrary",
            ) {
                compileSdk = 36
                minSdk = 26

                withHostTestBuilder { }

                lint {
                    warningsAsErrors = true
                    abortOnError = true
                    lintConfig = target.rootProject.file("lint.xml")
                }
            }

            iosArm64()
            iosSimulatorArm64()
            iosX64()

            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.library("kotlinx-coroutines-core"))
                implementation(libs.library("koin-core"))
            }
            sourceSets.getByName("commonTest").dependencies {
                implementation(libs.library("kotest"))
                implementation(libs.library("coroutines-test"))
                implementation(libs.library("turbine"))
            }
            sourceSets.getByName("androidHostTest").dependencies {
                implementation(libs.library("junit-api"))
                implementation(libs.library("junit-params"))
                runtimeOnly(libs.library("junit-engine"))
                // Gradle 9 no longer provides the JUnit Platform launcher automatically.
                runtimeOnly(libs.library("junit-platform-launcher"))
                implementation(libs.library("mockk"))
            }
        }

        // JVM target for the Android (and any host) Kotlin compilations — matches the Android-only
        // plugin's JDK 11. Native compile tasks have no jvmTarget, so scoping to KotlinCompile is safe.
        tasks.withType<KotlinCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        Unit
    }
}
