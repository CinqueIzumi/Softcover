// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.jetbrains.compose.resources.ResourcesExtension

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
}

// Code coverage. Kover is applied to every shipped module (below) and aggregated into a single XML
// report at the root — `./gradlew koverXmlReport` → build/reports/kover/report.xml — which CI uploads
// to Codecov. :detekt-rules is build tooling, not shipped code, so it is excluded from the report.
dependencies {
    subprojects
        .filter { it.path != ":detekt-rules" }
        .forEach { kover(it) }
}

// The foundation's shared detekt baseline (F19) ships INSIDE the `nl.rhaydus:detekt-rules` jar as
// `config/detekt.yml`, so it has to be unpacked before detekt can point `config.setFrom(...)` at it.
val rhaydusDetektConfig by configurations.creating { isTransitive = false }

dependencies {
    add("rhaydusDetektConfig", libs.rhaydus.detektRules)
}

val extractRhaydusDetektConfig = tasks.register<Sync>("extractRhaydusDetektConfig") {
    group = "verification"
    description = "Unpacks the shared nl.rhaydus detekt baseline from the detekt-rules jar."

    // Derive the file tree from `configuration.elements`, not from `.singleFile` inside a bare
    // `provider { }`. The Provider that `elements` returns carries the task dependencies that BUILD the
    // jar, so editing the baseline in the included foundation rebuilds it and re-runs this Sync.
    // Resolving `.singleFile` eagerly severs that link and silently extracts a stale config — a gate
    // that quietly ignores upstream changes is worse than no gate.
    from(rhaydusDetektConfig.elements.map { jars -> jars.map { zipTree(it.asFile) } }) {
        include("config/detekt.yml")
    }
    into(layout.buildDirectory.dir("rhaydus-detekt"))
}

// The detekt tasks that carry TYPE RESOLUTION, by module shape: KMP libraries get one task per compilation
// (`detektMainAndroid`, `detektMainJvm`, and `detektHostTestAndroid` for the unit tests), the Android
// application and the minified test module get a `detektMain` lifecycle over their variants, and the desktop
// JVM app gets the `main` compilation's `detektMain`. Only these can run the foundation's
// `rhaydus:UnguardedFlowTerminalRead` rule, which resolves `Flow.first()` apart from `Collection.first()`
// through the Analysis API; the source-only `detekt` and `detekt<SourceSet>SourceSet` tasks never run it.
val typeResolvedDetektTasks = setOf(
    "detektMainAndroid",
    "detektMainJvm",
    "detektMain",
    "detektHostTestAndroid",
)

// Captured here because the generated `libs` accessor is scoped to this script's project and is not
// resolvable from inside the `subprojects { }` block below.
val rhaydusDetektRules = libs.rhaydus.detektRules

// Apply detekt uniformly to every Kotlin module (no baseline - gates from zero on the shared config).
// Wired centrally here, alongside the ktlint/styleCheck gates, rather than per module.
subprojects {
    apply(plugin = "dev.detekt")

    // Coverage instrumentation for every shipped module; the merged report is wired at the root above.
    if (path != ":detekt-rules") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
    }

    dependencies {
        // The custom `rhaydus` ruleset, discovered via its META-INF/services entry.
        add("detektPlugins", rhaydusDetektRules)

        // Softcover's own rules; the module cannot analyse itself with the jar it produces.
        if (path != ":detekt-rules") {
            add("detektPlugins", project(":detekt-rules"))
        }
    }

    configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        // Layered, later wins: the shared foundation baseline, then Softcover's own deltas on top.
        config.setFrom(
            listOfNotNull(
                rootProject.layout.buildDirectory.file("rhaydus-detekt/config/detekt.yml"),
                rootProject.file("config/detekt/detekt.yml"),
                rootProject.file("config/detekt/softcover-rules.yml").takeIf { path != ":detekt-rules" },
            ),
        )
        parallel.set(true)
    }

    tasks.withType<Detekt>().configureEach {
        dependsOn(extractRhaydusDetektConfig)
        reports {
            html.required.set(true)
            checkstyle.required.set(true)
            markdown.required.set(false)
            sarif.required.set(false)
        }
    }

    // Point each KMP compilation task at the HAND-WRITTEN sources it belongs to, keeping the compilation's
    // classpath (which is what type resolution needs) untouched. The compilation's source sets also carry
    // GENERATED code - KSP/Room DAO impls, Apollo operations, Compose resource accessors - which we neither
    // own nor can fix. `commonMain` is analysed once per compilation: a platform file referencing a
    // `commonMain` declaration needs it in scope for the receiver type to resolve. Duplicate findings are
    // visible only while a violation exists; the steady state is zero.
    afterEvaluate {
        tasks.withType<Detekt>().configureEach {
            when (name) {
                "detektMainAndroid" -> setSource(
                    project.files(
                        "src/commonMain/kotlin",
                        "src/mobileMain/kotlin",
                        "src/androidMain/kotlin",
                    ),
                )

                "detektMainJvm" -> setSource(project.files("src/commonMain/kotlin", "src/jvmMain/kotlin"))

                "detektHostTestAndroid" -> setSource(
                    project.files(
                        "src/commonTest/kotlin",
                        "src/androidHostTest/kotlin",
                    ),
                )
            }
        }
    }

    // A lifecycle `detektMain` that fans out to nothing would pass green with nothing scanned, so a
    // detekt/AGP change that stops registering the variant tasks must fail here instead.
    tasks.matching { it.name == "detektMain" && it !is Detekt }.configureEach {
        doFirst {
            val variantTasks = taskDependencies.getDependencies(this).filterIsInstance<Detekt>()
            check(variantTasks.isNotEmpty()) {
                "No type-resolved detekt<Variant> task is registered for $path; the detekt gate would pass vacuously."
            }
        }
    }

    // The source-only `detekt` task parses without a classpath, so every type-resolved rule is inert on it
    // and its remaining findings are a strict subset of what the type-resolved tasks report. Disabled so it
    // neither double-reports nor lends a false sense of coverage.
    tasks.matching { it.name == "detekt" }.configureEach {
        enabled = false
    }

    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(tasks.matching { it.name in typeResolvedDetektTasks })
    }
}

// Wire the custom ktlint ruleset into the build so style is enforced for every developer with zero
// setup: `./gradlew ktlintCheck` (the gate, also run by `check`) and `./gradlew ktlintFormat` (autofix).
// The rules run via ktlint's rule-engine directly from the published `nl.rhaydus:ktlint-rules` jar
// (resolved on the `ktlintRules` configuration), so there is no Spotless/plugin version coupling.
// `-Pktlint.root=<dir>` scopes the scan (used for testing a rule on a throwaway dir); defaults to the repo.
val ktlintRules by configurations.creating

dependencies {
    add("ktlintRules", libs.rhaydus.ktlintRules)
}

val ktlintScanRoot = (project.findProperty("ktlint.root") as String?) ?: rootDir.absolutePath

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Auto-wraps multi-arg calls/declarations across the repo (custom ktlint ruleset)."
    classpath = ktlintRules
    mainClass.set("nl.rhaydus.ktlint.MainKt")
    args("format", ktlintScanRoot)
}

tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Fails the build on custom-ruleset violations (multi-arg one-per-line wrapping)."
    classpath = ktlintRules
    mainClass.set("nl.rhaydus.ktlint.MainKt")
    args("check", ktlintScanRoot)
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(rootProject.tasks.named("ktlintCheck"))
        dependsOn(":checkResourcePackaging")
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
                    // Added to every Compose module by the stability analyzer plugin.
                    "com.github.skydoves:compose-stability-runtime",
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

// Compose Multiplatform resources must actually reach the APK. A module that declares
// `compose.resources` gets its generated `Res` accessor and compiles perfectly whether or not the
// resources are ever packaged — **the failure is entirely at runtime**, as a `MissingResourceException`
// on the first read. On Android the deciding switch is `androidResources.enable`, which the KMP Android
// library plugin leaves OFF by default (CMP resources ship as Android *assets*, which that flag gates).
//
// This is a gate rather than a convention because the convention already failed twice, silently, and
// was found by accident rather than by any check:
//
//  - `:core:component` — the offline banner's copy. It crashed the app on launch the first time
//    it was run on a device with no network, three commits after it landed.
//  - `:feature:settings` — the bundled `ROADMAP.md` fallback, read only before the first live fetch, so
//    a machine with a warm cache never touches it. It had never worked on Android.
//
// Both are the same one-line omission, and neither `compileDebugKotlin`, `ktlintCheck`, detekt,
// `projectHealth` nor `checkModuleGraph` could see it — nothing in the source or the dependency graph
// is wrong.
tasks.register("checkResourcePackaging") {
    group = "verification"
    description = "Fails when a module declares Compose Multiplatform resources but does not enable " +
        "Android resources, which silently omits them from the APK."

    doLast {
        val violations = mutableListOf<String>()
        var checked = 0

        subprojects.forEach { module ->
            // `packageOfResClass` is the discriminator, not the presence of the Compose plugin (which
            // every UI module applies) nor a `composeResources/` directory on disk (which the
            // `customDirectory` case does not have). Declaring a resource package IS the act of saying
            // "this module owns resources"; the plugin leaves it empty for everyone else.
            val compose = module.extensions.findByName("compose") as? ExtensionAware ?: return@forEach
            val resources = compose.extensions.findByType(ResourcesExtension::class.java) ?: return@forEach

            if (resources.packageOfResClass.isEmpty()) return@forEach

            val kotlin = module.extensions.findByName("kotlin") as? ExtensionAware ?: return@forEach
            val androidLibrary = kotlin.extensions
                .findByName("androidLibrary") as? KotlinMultiplatformAndroidLibraryExtension
                ?: return@forEach

            checked++

            if (androidLibrary.androidResources.enable.not()) {
                violations += "${module.path}  (declares Compose resources but leaves " +
                    "`androidResources.enable` off, so nothing is packaged into the APK and the " +
                    "first read throws MissingResourceException at runtime — add " +
                    "`androidResources.enable = true` to its `androidLibrary` block)"
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Compose resources declared but not packaged:\n" +
                    violations.sorted().joinToString("\n") { "  - $it" },
            )
        }

        logger.lifecycle(
            "checkResourcePackaging: $checked module(s) with Compose resources, all packaged.",
        )
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

// The per-change style gate: `ktlintCheck` plus type-resolved detekt across every module — the shared
// nl.rhaydus baseline plus the custom `rhaydus` ruleset, gating from zero with no baseline file.
//
// detekt's own tasks are wired into the heavy `check` lifecycle, but the gate developers actually run
// per change is `styleCheck` — so without this, findings would only surface in a rarely-run full build
// and silently accumulate. It runs the TYPE-RESOLVED tasks, which is what makes the crash-safety rule
// (`rhaydus:UnguardedFlowTerminalRead`) fire at all; the cost is that it compiles Android + JVM rather
// than merely parsing source. That cost is the point: an inert gate is not a gate.
tasks.register("styleCheck") {
    group = "verification"
    description = "Runs ktlintCheck, type-resolved detekt (shared baseline + rhaydus crash-safety ruleset) and the Compose stability check across all modules."

    dependsOn(tasks.named("ktlintCheck"))
    dependsOn(subprojects.map { sp -> sp.tasks.matching { it.name in typeResolvedDetektTasks } })
    dependsOn(subprojects.map { sp -> sp.tasks.matching { it.name == "androidMainStabilityCheck" } })
}

