plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.compose.multiplatform.gradlePlugin)
    compileOnly(libs.apollo.gradlePlugin)
    compileOnly(libs.stability.analyzer.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)

    testImplementation(gradleTestKit())
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.kotest)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "softcover.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "softcover.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("androidRoom") {
            id = "softcover.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidApollo") {
            id = "softcover.android.apollo"
            implementationClass = "AndroidApolloConventionPlugin"
        }
        register("androidR8KeepContract") {
            id = "softcover.android.r8-keep-contract"
            implementationClass = "AndroidR8KeepContractConventionPlugin"
        }
        register("docBudgets") {
            id = "softcover.doc-budgets"
            implementationClass = "DocBudgetsConventionPlugin"
        }
    }
}
