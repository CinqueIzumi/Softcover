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
    compileOnly(libs.apollo.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "softcover.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kmpLibrary") {
            id = "softcover.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "softcover.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidRoom") {
            id = "softcover.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidApollo") {
            id = "softcover.android.apollo"
            implementationClass = "AndroidApolloConventionPlugin"
        }
    }
}
