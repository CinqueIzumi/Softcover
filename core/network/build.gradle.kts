import java.util.Properties

plugins {
    id("softcover.kmp.library")
    id("softcover.android.apollo")
}

// Bearer token for the `downloadApolloSchema` introspection call below. Sourced from the environment
// first (CI / a shell export keeps it out of the repo), falling back to `local.properties`, which is
// git-ignored — the same local-override channel `foundation.local` uses.
val hardcoverApiToken: String = run {
    System.getenv("HARDCOVER_API_TOKEN")?.let { return@run it }

    val properties = Properties()
    val localProperties = rootDir.resolve("local.properties")
    if (localProperties.exists()) localProperties.inputStream().use { stream -> properties.load(stream) }

    properties.getProperty("hardcover.apiToken").orEmpty()
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.network"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.rhaydus.corePlatform)
            implementation(project(":core:domain"))
        }
    }
}

apollo {
    service("service") {
        packageName.set("nl.rhaydus.softcover")
        addTypename.set("always")

        schemaFiles.from(
            "src/commonMain/graphql/schema.graphqls",
            "src/commonMain/graphql/extra.graphqls",
        )

        mapScalar("numeric", "kotlin.Double")
        mapScalar("float8", "kotlin.Double")
        mapScalar("date", "kotlin.String")
        mapScalar("timestamp", "kotlin.String")
        mapScalar("timestamptz", "kotlin.String")
        mapScalar("smallint", "kotlin.Int")
        mapScalar("bigint", "kotlin.Long")

        codegenModels.set("responseBased")
        generateMethods.set(listOf("dataClass"))

        // Generates `nl.rhaydus.softcover.cache.Cache` (typePolicies / fieldPolicies) consumed by the
        // separately-versioned normalized-cache library's key generator and field-policy resolver. The
        // plugin appends `.cache` to this package argument, so the base is the service package.
        plugin("com.apollographql.cache:normalized-cache-apollo-compiler-plugin:${libs.versions.apolloCache.get()}") {
            pluginArgument("com.apollographql.cache.packageName", "nl.rhaydus.softcover")
        }

        // Refreshes `schema.graphqls` from the live API via `./gradlew :core:network:downloadApolloSchema`.
        // The Hardcover endpoint requires an authenticated bearer token, so the token is read from the
        // environment or from `local.properties` (`hardcover.apiToken`) — never committed. Without a token
        // the block still configures cleanly; only the download task fails, so a normal build is unaffected.
        introspection {
            endpointUrl.set("https://api.hardcover.app/v1/graphql")
            headers.put("Authorization", "Bearer $hardcoverApiToken")
            schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        }
    }
}
