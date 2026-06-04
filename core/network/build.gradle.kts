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
    }
}
