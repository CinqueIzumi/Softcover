plugins {
    id("softcover.kmp.library")
    id("softcover.kmp.compose")
}

kotlin {
    androidLibrary {
        namespace = "nl.rhaydus.softcover.orchestration"
    }

    // :orchestration aggregates the whole module graph and hosts App(), so it is the module that
    // ships the iOS app framework the Xcode project embeds. The iOS targets themselves are declared
    // by the softcover.kmp.library convention plugin; re-declaring them here is idempotent and only
    // attaches the framework binary. Only this module exports a framework — keeping binaries.framework
    // out of the convention plugin avoids emitting one for every library module. The whole graph is
    // statically linked in, so no explicit `export(...)` is needed; Swift only calls MainViewController
    // and doInitKoinIos, both defined in this module's iosMain.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "OrchestrationKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core modules
            implementation(project(":core:domain"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:preferences"))
            implementation(project(":core:identity"))
            implementation(project(":core:book"))
            implementation(project(":core:lists"))
            implementation(project(":core:deadlines"))
            implementation(project(":core:personal"))
            implementation(project(":core:profile"))
            implementation(project(":core:library"))
            implementation(project(":core:connectivity"))
            implementation(project(":core:notification"))
            implementation(project(":core:designsystem"))

            // Feature modules (orchestration composes them all)
            implementation(project(":feature:book_detail"))
            implementation(project(":feature:explore"))
            implementation(project(":feature:library"))
            implementation(project(":feature:lists"))
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:reading"))
            implementation(project(":feature:scan"))
            implementation(project(":feature:session"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:app_update"))

            implementation(libs.koin.compose.multiplatform)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.transitions)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.splash)
        }

        iosMain.dependencies {
            // Coil's network image loading on iOS rides Ktor's Darwin engine (OkHttp is JVM/Android-
            // only). Confined to iosMain so Android keeps its OkHttp fetcher untouched.
            implementation(libs.coil3.network.ktor)
            implementation(libs.ktor.client.darwin)
        }

        jvmMain.dependencies {
            // Desktop reuses Coil's OkHttp network fetcher (the same engine Android uses); DesktopApp
            // installs it on the singleton loader. Confined to jvmMain so the other targets are untouched.
            implementation(libs.coil3.network.okhttp)
        }
    }
}
