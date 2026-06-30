plugins {
    id("softcover.kmp.library")
    id("softcover.android.apollo")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.core.network"
    }

    sourceSets {
        commonMain.dependencies {
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
    }
}
