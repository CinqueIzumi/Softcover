import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Applies the Apollo GraphQL plugin and exposes the runtime via the `commonMain` `api` configuration
 * so downstream modules (core data sources and `:app` features) resolve
 * `nl.rhaydus.softcover.graphql.*` transitively across every KMP target. Only `:core:network` applies
 * this — the single module that owns the schema and every operation/fragment. The Apollo `service { }`
 * configuration is network-specific and lives in `:core:network`'s build file.
 *
 * `:core:network` is a Kotlin Multiplatform module, so dependencies attach to the source-set-scoped
 * `commonMainApi` configuration rather than the plain `api` configuration the Android/JVM library
 * plugin would expose. The Apollo runtime and normalized cache are both multiplatform artifacts.
 */
class AndroidApolloConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.apollographql.apollo")

        dependencies {
            add(
                "commonMainApi",
                libs.library("apollo"),
            )
            add(
                "commonMainApi",
                libs.library("apollo-normalized-cache"),
            )
        }
    }
}
