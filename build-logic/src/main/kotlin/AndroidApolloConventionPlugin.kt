import com.apollographql.apollo.gradle.api.ApolloExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Apollo GraphQL codegen, applied only by `:core:network` — the single module that owns the schema
 * and every operation/fragment. Generated types are exposed via `api()` so downstream modules (core
 * data sources and `:app` features) resolve `nl.rhaydus.softcover.graphql.*` transitively.
 */
class AndroidApolloConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.apollographql.apollo")

        dependencies {
            add("api", libs.library("apollo"))
            add("implementation", libs.library("apollo-normalized-cache"))
        }

        extensions.configure<ApolloExtension> {
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
                mapScalar("bigint", "kotlin.Long")

                codegenModels.set("responseBased")
                generateMethods.set(listOf("dataClass"))
            }
        }
    }
}
