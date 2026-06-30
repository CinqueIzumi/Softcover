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
 * the Android target, two iOS targets (`iosArm64` + `iosSimulatorArm64`; `iosX64` is omitted — Compose
 * Multiplatform no longer publishes it, and the Intel iOS simulator is obsolete on Apple-silicon
 * Macs), and the JVM desktop target, matches the Android-only plugin's SDK/JDK levels and lint config,
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

            // Desktop (JVM) target — the final platform in the migration. Declared centrally here, so
            // every `:core:*` / `:feature:*` module gets a `jvmMain` source set and a `compileKotlinJvm`
            // task without per-module wiring (mirrors how the iOS targets are declared once above).
            jvm()

            // expect/actual classes are still flagged Beta by the compiler; the project uses them for
            // platform-token seams (e.g. core:notification's appearance handles), so opt in once here
            // rather than per module. Harmless for modules that don't declare any.
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }

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

            // Shared mobile (Android + iOS, excluding desktop/JVM) source set. KMP provides no
            // Android+iOS-only set by default. Desktop-specific UI is branched into `jvmMain`; the
            // Android and iOS layouts stay identical and must live in exactly one place — `mobileMain`.
            // Both `androidMain` and `iosMain` depend on it, while `jvmMain` stays on `commonMain`
            // alone, so an `expect` in `commonMain` is satisfied by one `actual` in `mobileMain`
            // (covering Android + iOS) plus one in `jvmMain` (desktop). Declared centrally here so every
            // `:core:*` / `:feature:*` module gets the seam without per-module wiring. Calling the
            // template explicitly keeps it applied (manual `dependsOn` edges otherwise disable the
            // auto-application) and guarantees `androidMain`/`iosMain` exist before we re-parent them.
            applyDefaultHierarchyTemplate()

            val mobileMain = sourceSets.maybeCreate("mobileMain")
            mobileMain.dependsOn(sourceSets.getByName("commonMain"))
            sourceSets.getByName("androidMain").dependsOn(mobileMain)
            // Re-parents the template's `iosMain` onto `mobileMain` (additive to its existing edge to
            // commonMain). Today only `iosArm64`/`iosSimulatorArm64` exist, so `appleMain` is empty and
            // nothing is lost. If a non-iOS Apple target (macOS/watchOS) is ever added, wire it with an
            // explicit `<target>Main.dependsOn(mobileMain)` — it will NOT inherit the seam via appleMain.
            sourceSets.getByName("iosMain").dependsOn(mobileMain)

            // Test mirror. `androidHostTest` always exists (withHostTestBuilder above); `iosTest` is
            // materialised lazily, so it stays defensive.
            val mobileTest = sourceSets.maybeCreate("mobileTest")
            mobileTest.dependsOn(sourceSets.getByName("commonTest"))
            sourceSets.getByName("androidHostTest").dependsOn(mobileTest)
            sourceSets.findByName("iosTest")?.dependsOn(mobileTest)
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
