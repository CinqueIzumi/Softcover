import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Layers Jetpack Compose on top of [AndroidLibraryConventionPlugin]. Applied by the modules that own
 * UI (`:core:designsystem`) or Compose-based platform helpers (`:core:platform`).
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            val bom = libs.library("androidx-compose-bom").get()

            add(
                "implementation",
                platform(bom),
            )
            add(
                "androidTestImplementation",
                platform(bom),
            )

            add(
                "implementation",
                libs.library("androidx-compose-ui"),
            )
            add(
                "implementation",
                libs.library("androidx-compose-ui-graphics"),
            )
            add(
                "implementation",
                libs.library("androidx-compose-ui-tooling-preview"),
            )
            add(
                "implementation",
                libs.library("androidx-compose-material3"),
            )
            add(
                "implementation",
                libs.library("androidx-activity-compose"),
            )

            add(
                "debugImplementation",
                libs.library("androidx-compose-ui-tooling"),
            )
            add(
                "debugImplementation",
                libs.library("androidx-compose-ui-test-manifest"),
            )
        }
    }
}
