import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Base convention for every `:core:*` Android library module: AGP + Kotlin, JDK 11, SDK levels,
 * and the cross-cutting runtime/test dependencies every module shares (coroutines, Koin,
 * JUnit5 + Kotest + MockK + Turbine). Logging (Kermit, via `AppLog`) comes transitively from
 * `:core:domain`.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.library")
            apply("org.jetbrains.kotlin.android")
        }

        extensions.configure<LibraryExtension> {
            compileSdk = 36

            defaultConfig {
                minSdk = 26
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }

            lint {
                warningsAsErrors = true
                abortOnError = true
                lintConfig = target.rootProject.file("lint.xml")
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        dependencies {
            add(
                "implementation",
                libs.library("kotlinx-coroutines-android"),
            )
            add(
                "implementation",
                libs.library("koin-android"),
            )

            add(
                "testImplementation",
                libs.library("junit-api"),
            )
            add(
                "testImplementation",
                libs.library("junit-params"),
            )
            add(
                "testRuntimeOnly",
                libs.library("junit-engine"),
            )
            add(
                "testImplementation",
                libs.library("mockk"),
            )
            add(
                "testImplementation",
                libs.library("kotest"),
            )
            add(
                "testImplementation",
                libs.library("coroutines-test"),
            )
            add(
                "testImplementation",
                libs.library("turbine"),
            )
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        Unit
    }
}
