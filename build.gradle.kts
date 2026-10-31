// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.apollo) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.stability.analyzer) apply false
    alias(libs.plugins.dependency.analysis) apply false
    alias(libs.plugins.kover)
    id("rhaydus.doc-budgets")
    id("rhaydus.module-graph")
    id("rhaydus.dependency-analysis")
    id("rhaydus.style")
    id("rhaydus.resource-packaging")
}

// Code coverage. Kover is applied to every shipped module (below) and aggregated into a single XML
// report at the root — `./gradlew koverXmlReport` → build/reports/kover/report.xml — which CI uploads
// to Codecov.
dependencies {
    subprojects.forEach { kover(it) }
}

rhaydusStyle {
    detektRules = libs.rhaydus.detektRules
    ktlintRules = libs.rhaydus.ktlintRules
    detektConfigFiles = listOf(file("config/detekt/detekt.yml"))
    extraStyleTasks = setOf("androidMainStabilityCheck")
}

// Coverage instrumentation for every shipped module; the merged report is wired at the root above.
subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(":checkPresentationFileSize")
        dependsOn(":checkDocBudgets")
    }
}

// Gate every KMP module's `check` on iOS compilation. The Android variant compiles common/androidMain
// for the JVM, where JVM-only APIs resolve fine (kotlin.jvm.* default imports, Dispatchers.IO,
// java.time) — so an Android-only build silently hides code that will not compile for iOS. Compiling
// all declared iOS targets here fails such leaks at `check` time instead of only at iOS link time.
// Guarded to KMP modules; and to macOS hosts,
// since Kotlin/Native iOS compilation is unavailable elsewhere (an iOS CI must use a macOS runner).
subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        if (System.getProperty("os.name").startsWith("Mac")) {
            tasks.matching { it.name == "check" }.configureEach {
                dependsOn(
                    "compileKotlinIosArm64",
                    "compileKotlinIosSimulatorArm64",
                )
            }
        }
    }
}

// Gate every KMP module's `check` on JVM (desktop) compilation too. Unlike the iOS gate this runs on
// every host (Kotlin/JVM compilation is available everywhere), so a missing or incorrect `jvmMain`
// actual fails at `check` time rather than only when the desktop app is assembled.
subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        tasks.matching { it.name == "check" }.configureEach {
            dependsOn("compileKotlinJvm")
        }
    }
}

// Module-graph gate (`checkModuleGraph`, wired into every `check` by `rhaydus.module-graph`).
moduleGraph {
    tierOf = { path ->
        when {
            path.startsWith(":core:") -> "core"
            path.startsWith(":feature:") -> "feature"
            path == ":orchestration" -> "orchestration"
            path == ":app" || path == ":desktopApp" -> "app"
            else -> null
        }
    }

    allowedTargetTiers = mapOf(
        "core" to setOf("core"),
        "feature" to setOf("core"),
        "orchestration" to setOf("feature", "core"),
        "app" to setOf("orchestration", "core"),
    )

    // An `api` edge republishes the target's whole public surface, which is how `:core:designsystem`
    // became a god-module. Every `api` edge to a module here must be allowlisted below.
    //
    // Deliberately not in the set:
    //  - `:core:domain`: a dependency-free contract module, `api`-exposed freely.
    //  - `:core:designsystem`: tokens with no project dependency, so an `api` edge republishes a leaf.
    //  - `:core:network`, `:core:database`: infra whose `api` edges carry the Apollo/Room types that
    //    cross module boundaries by design.
    dataAreaModules = setOf(
        ":core:book",
        ":core:lists",
        ":core:deadlines",
        ":core:personal",
        ":core:profile",
        ":core:identity",
        ":core:preferences",
        ":core:component",
        ":core:presentation",
        ":core:uibinding",
    )

    allowedApiDataEdges = setOf(
        ":core:identity" to ":core:preferences",
        ":core:profile" to ":core:identity",
        ":feature:book_detail" to ":core:component",
        ":feature:book_detail" to ":core:identity",
        ":feature:book_detail" to ":core:personal",
        ":feature:explore" to ":core:book",
        ":feature:explore" to ":core:component",
        ":feature:explore" to ":core:identity",
        ":feature:explore" to ":core:lists",
        ":feature:explore" to ":core:preferences",
        ":feature:lists" to ":core:lists",
        ":feature:onboarding" to ":core:component",
        ":feature:reading" to ":core:component",
        ":feature:settings" to ":core:preferences",

        // `:core:uibinding` maps a domain model onto the UI type that renders it, so a consumer needs
        // both sides of the mapping.
        ":core:uibinding" to ":core:component",

        // `ActiveSession` carries a `CoverUiModel` per surface, so a consumer must see that type too.
        ":core:presentation" to ":core:component",

        // `BookDetailScreen` is constructed by orchestration with a `BookInitialCover`.
        ":feature:book_detail" to ":core:presentation",
    )

    // The UI stack layers `:core:component` -> `:core:designsystem` and `:core:uibinding` ->
    // `:core:component`, never the reverse. The tier rule cannot see this, and a cycle fails Gradle
    // with an unhelpful message. `designsystem` -> `presentation` is no cycle and is forbidden too.
    bannedEdges = setOf(
        ":core:designsystem" to ":core:component",
        ":core:designsystem" to ":core:uibinding",
        ":core:designsystem" to ":core:presentation",
    )

    // `:core:designsystem` is tokens and declares no project dependency. The detekt `ForbiddenImport`
    // rule in `config/detekt/detekt.yml` is complementary: it catches a stray import that outlives a
    // dependency removal, which this declared-dependency gate cannot see.
    // `:core:component` renders from UI models and may reach nothing but the tokens.
    allowedProjects = mapOf(
        ":core:designsystem" to emptySet(),
        ":core:component" to setOf(":core:designsystem"),
    )

    // DI, navigation and the network client are how a component library stops being renderable in
    // isolation. The convention-provided `koin-core` is skipped, so declaring it explicitly passes;
    // the `ForbiddenImport` rule scoped to `:core:component` in `config/detekt/detekt.yml` closes
    // that gap at the import.
    bannedExternalGroups = mapOf(
        ":core:component" to setOf(
            "io.insert-koin",
            "cafe.adriel.voyager",
            "com.apollographql.apollo",
        ),
    )

    ignoredExternalCoordinates = setOf(
        "io.insert-koin:koin-core",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core",
    )
}

// Softcover-specific dependency-analysis exclusions, on top of the shared `rhaydus.dependency-analysis`
// policy.
dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                exclude(
                    // Desktop's Main dispatcher, supplied via ServiceLoader (no compile reference).
                    "org.jetbrains.kotlinx:kotlinx-coroutines-swing",
                    // The `:smoketest` device stack: reached by the instrumentation harness, not by a
                    // compile reference.
                    "androidx.test:runner",
                    "androidx.test:core",
                    "androidx.test.ext:junit",
                    "androidx.test.uiautomator:uiautomator",
                    "junit:junit",
                    // Provided uniformly by the Compose / Room convention plugins.
                    "androidx.compose.material3:material3",
                    "androidx.compose.ui:ui-graphics",
                    // False positives: genuinely used via mechanisms DA can't see without type resolution.
                    "io.insert-koin:koin-androidx-compose", // koinInject(...)
                    "cafe.adriel.voyager:voyager-koin", // ScreenModel / screenModelScope (ToadScreenModel)
                    "org.jetbrains.kotlinx:kotlinx-serialization-json", // @Serializable / Json
                    "androidx.work:work-runtime-ktx", // CoroutineWorker
                    // CameraX runtime backend, loaded via ServiceLoader (no compile ref).
                    "androidx.camera:camera-camera2",
                    // Used by the scanner; DA mis-resolves it to a transitive.
                    "com.google.mlkit:barcode-scanning",
                    // Theme.Material3.* XML themes (resource-only, no Kotlin ref).
                    "com.google.android.material:material",
                    // The included foundation build has no published root/-android/-jvm split, which DA needs
                    // to credit platform usage to a commonMain declaration, so every foundation dependency
                    // reads as unused.
                    "nl.rhaydus:core-common",
                    "nl.rhaydus:core-platform",
                    "nl.rhaydus:offline-sync",
                    "nl.rhaydus:toad",
                    "nl.rhaydus:designsystem-core",
                    "nl.rhaydus:designsystem-editorial",
                    "nl.rhaydus:designsystem-image",
                )
            }

            onIncorrectConfiguration {
                // Intentional public exposure: designsystem returns a Coil ImageRequest.
                exclude("io.coil-kt.coil3:coil-compose")
            }
        }
    }
}

// No baseline and no exemption list: the oversized files were split before this gate landed, so any
// offender is a regression. Rationale in docs/reference/module-structure.md § Build wiring conventions.
val presentationFileLineLimit = 600

tasks.register("checkPresentationFileSize") {
    group = "verification"
    description = "Fails when a presentation or :core:component main source file exceeds " +
        "$presentationFileLineLimit lines."

    doLast {
        val violations = mutableListOf<String>()
        var checked = 0

        subprojects.forEach { module ->
            val isComponentLibrary = module.path == ":core:component"
            val sourceSets = module.projectDir.resolve("src")
                .listFiles { dir -> dir.isDirectory && dir.name.endsWith("Main") }
                .orEmpty()

            sourceSets.forEach { sourceSet ->
                sourceSet.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .filter { file ->
                        isComponentLibrary ||
                            "presentation" in file.relativeTo(sourceSet).invariantSeparatorsPath.split("/")
                    }
                    .forEach { file ->
                        checked++
                        val lines = file.useLines { it.count() }
                        if (lines > presentationFileLineLimit) {
                            violations += "${file.relativeTo(rootDir).invariantSeparatorsPath}  ($lines lines)"
                        }
                    }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Presentation files over $presentationFileLineLimit lines (split them one section per " +
                    "file, see docs/reference/module-structure.md § Build wiring conventions):\n" +
                    violations.sorted().joinToString("\n") { "  - $it" },
            )
        }

        logger.lifecycle(
            "checkPresentationFileSize: $checked file(s) checked, all within $presentationFileLineLimit lines.",
        )
    }
}
