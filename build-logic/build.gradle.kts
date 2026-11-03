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
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidRoom") {
            id = "softcover.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidApollo") {
            id = "softcover.android.apollo"
            implementationClass = "AndroidApolloConventionPlugin"
        }
        register("designPresetBundle") {
            id = "softcover.design-preset-bundle"
            implementationClass = "DesignPresetBundleConventionPlugin"
        }
    }
}
