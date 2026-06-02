import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Applies the Apollo GraphQL plugin and exposes the runtime via `api()` so downstream modules (core
 * data sources and `:app` features) resolve `nl.rhaydus.softcover.graphql.*` transitively. Only
 * `:core:network` applies this — the single module that owns the schema and every operation/fragment.
 * The Apollo `service { }` configuration is network-specific and lives in `:core:network`'s build file.
 */
class AndroidApolloConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.apollographql.apollo")

        dependencies {
            add(
                "api",
                libs.library("apollo"),
            )
            add(
                "api",
                libs.library("apollo-normalized-cache"),
            )
        }
    }
}
