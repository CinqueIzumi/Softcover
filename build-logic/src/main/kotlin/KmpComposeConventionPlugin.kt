import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Layers Compose Multiplatform onto [KmpLibraryConventionPlugin] for KMP modules that own UI
 * (e.g. `:core:designsystem`, `:feature:library`) — the KMP sibling of [AndroidComposeConventionPlugin].
 *
 * Applies the JetBrains Compose Gradle plugin (`org.jetbrains.compose`) plus the standalone Kotlin
 * Compose compiler plugin, and wires the **multiplatform** `compose.*` artifacts into `commonMain`
 * so the widgets compile for every target (on Android these resolve to the AndroidX Compose
 * libraries, so existing `androidx.compose.*` imports keep working). Android-only Compose extras —
 * `activity-compose` and the preview tooling — stay in `androidMain`.
 *
 * Per-module concerns (Coil/Voyager/Koin-compose, the Android-only google-fonts/CameraX/MLKit deps,
 * `androidResources.enable`) live in the module build file.
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.plugin.compose")
            apply("org.jetbrains.compose")
        }

        val compose = extensions.getByType<ComposeExtension>().dependencies

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.ui)
                // BackHandler ships in a standalone CMP artifact; the Android variant of compose.ui
                // (AndroidX `compose.ui`) doesn't carry it, so wire it explicitly for every target.
                implementation(libs.library("compose-ui-backhandler"))
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                // CMP's stable material3 strips the M3-expressive APIs the design system uses, so
                // pin the alpha material3 artifact explicitly (see the catalog note).
                implementation(libs.library("compose-material3-expressive"))
            }
            sourceSets.getByName("androidMain").dependencies {
                implementation(libs.library("androidx-activity-compose"))
                implementation(libs.library("androidx-compose-ui-tooling-preview"))
                implementation(libs.library("androidx-compose-ui-tooling"))
            }
        }
    }
}
