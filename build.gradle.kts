// Top-level build file where you can add configuration options common to all sub-projects/modules.
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.apollo) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.kover)
}

// Code coverage. Kover is applied to every shipped module (below) and aggregated into a single XML
// report at the root — `./gradlew koverXmlReport` → build/reports/kover/report.xml — which CI uploads
// to Codecov. :ktlint-rules is build tooling, not shipped code, so it is excluded from the report.
dependencies {
    subprojects
        .filter { it.path != ":ktlint-rules" }
        .forEach { kover(it) }
}

// The foundation's shared detekt baseline (F19) ships INSIDE the `nl.rhaydus:detekt-rules` jar as
// `config/detekt.yml`, so it has to be unpacked before detekt can point `config.setFrom(...)` at it.
// Under `foundation.local=true` the coordinate substitutes to the included build's jar — the same path
// the `ktlintRules` configuration takes.
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

// The production detekt tasks that carry TYPE RESOLUTION, by module shape: KMP libraries get one task per
// target, the Android application and the desktop JVM app get a single all-variants `detektMain`. Only
// these can run the foundation's `rhaydus:UnguardedFlowTerminalRead` rule (F1) — it is `@RequiresTypeResolution`,
// so it resolves `Flow.first()` apart from `Collection.first()` via the compile classpath, and is silently
// INERT on the source-only `detekt` task. Test-source and per-variant tasks are deliberately excluded.
val typeResolvedDetektTasks = setOf("detektAndroidMain", "detektJvmMain", "detektMain")

// Captured here because the generated `libs` accessor is scoped to this script's project and is not
// resolvable from inside the `subprojects { }` block below.
val rhaydusDetektRules = libs.rhaydus.detektRules

// Apply detekt uniformly to every Kotlin module (no baseline — gates from zero on the shared config).
// Wired centrally here, alongside the ktlint/styleCheck/checkModuleGraph gates, rather than per module.
subprojects {
    apply(plugin = "com.autonomousapps.dependency-analysis")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    // Coverage instrumentation for every shipped module; the merged report is wired at the root above.
    if (path != ":ktlint-rules") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
    }

    dependencies {
        // The custom `rhaydus` ruleset, discovered via its META-INF/services entry.
        add("detektPlugins", rhaydusDetektRules)
    }

    configure<DetektExtension> {
        buildUponDefaultConfig = true
        // Layered, later wins: the shared foundation baseline, then Softcover's own deltas on top.
        config.setFrom(
            rootProject.layout.buildDirectory.file("rhaydus-detekt/config/detekt.yml"),
            rootProject.files("config/detekt/detekt.yml"),
        )
        parallel = true
    }

    tasks.withType<Detekt>().configureEach {
        dependsOn(extractRhaydusDetektConfig)
        jvmTarget = "11"
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
        }
    }

    // Point each type-resolved task at the HAND-WRITTEN sources of the compilation it belongs to, keeping
    // the compilation's classpath (which is what type resolution actually needs) untouched. Two reasons this
    // has to be spelled out rather than left to detekt's defaults:
    //
    //  1. detekt seeds a KMP task with only that target's OWN source set, so a module whose code lives in
    //     `commonMain` would analyse nothing and silently report NO-SOURCE. `commonMain` is otherwise
    //     reachable only via `detektMetadataCommonMain`, which has no type resolution and therefore cannot
    //     run the crash-safety rule at all.
    //  2. The compilation's source set also carries GENERATED code — KSP/Room DAO impls, Apollo operations,
    //     Compose resource accessors. Analysing it produced ~2700 findings we neither own nor can fix.
    //
    // This must run in `afterEvaluate`: detekt seeds the KMP task sources from its own `afterEvaluate`, so a
    // plain `configureEach` here would be overwritten afterwards.
    //
    // `commonMain` is consequently analysed twice on a KMP module with a jvm target (once per compilation).
    // That is deliberate: a jvm-only file referencing a commonMain declaration needs commonMain in scope for
    // its receiver type to resolve, and an unresolved receiver makes the crash-safety rule silently miss.
    // Duplicate findings are visible only while a violation exists; the steady state is zero.
    afterEvaluate {
        tasks.withType<Detekt>().configureEach {
            when (name) {
                "detektAndroidMain" -> setSource(
                    project.files(
                        "src/commonMain/kotlin",
                        "src/mobileMain/kotlin",
                        "src/androidMain/kotlin",
                    ),
                )

                "detektJvmMain" -> setSource(project.files("src/commonMain/kotlin", "src/jvmMain/kotlin"))

                // `:desktopApp`'s own `detektMain`, and the per-variant tasks `:app`'s aggregating
                // `detektMain` fans out to. All three read the same non-KMP `src/main` layout.
                "detektMain", "detektDebug", "detektRelease" ->
                    setSource(project.files("src/main/java", "src/main/kotlin"))
            }
        }
    }

    // The source-only `detekt` task parses without a classpath, so the crash-safety rule is inert on it and
    // its remaining findings are a strict subset of what the type-resolved tasks report. Disabled so it
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
        dependsOn(":checkModuleGraph")
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

// Enforces the module-tier DAG from MODULE_STRUCTURE_GUIDELINES §2 so the split graph cannot
// silently regress: a module may depend only on a lower tier, and a leaf feature may never depend on
// a sibling feature. Replaces the manual `grep` import audits with a build-time gate (wired into
// `check` above). Tiers are derived from the module path.
val allowedTargetTiers = mapOf(
    "core" to setOf("core"),
    "feature" to setOf("core"),
    "orchestration" to setOf("feature", "core"),
    "app" to setOf("orchestration", "core"),
)

fun tierOf(path: String): String? = when {
    path.startsWith(":core:") -> "core"
    path.startsWith(":feature:") -> "feature"
    path == ":orchestration" -> "orchestration"
    path == ":app" || path == ":desktopApp" -> "app"
    else -> null
}

// api-visibility rule (MODULE_STRUCTURE_GUIDELINES §10). The tier check above proves an edge is
// *allowed*; it does not constrain whether a data-area module is re-exported (`api`) or kept private
// (`implementation`). An `api` edge to a data module transitively republishes that whole feature's
// data/use-case layer to every downstream consumer — which is exactly how `:core:designsystem` became
// a god-module. `implementation` is therefore the default for these; every `api(project(<data>))` edge
// must be an explicit, reviewed entry below so a new one is a conscious decision, not a silent leak.
val dataAreaModules = setOf(
    ":core:book",
    ":core:lists",
    ":core:deadlines",
    ":core:personal",
    ":core:profile",
    ":core:identity",
    ":core:preferences",
)

// Allowlisted (source → data module) `api` edges: each genuinely renders/returns the data module's
// types in its own public surface. Adding a row is the deliberate sign-off the rule exists to force.
val allowedApiDataEdges = setOf(
    ":core:designsystem" to ":core:book",
    ":core:identity" to ":core:preferences",
    ":core:profile" to ":core:identity",
    ":feature:book_detail" to ":core:identity",
    ":feature:explore" to ":core:book",
    ":feature:explore" to ":core:identity",
    ":feature:lists" to ":core:lists",
    ":feature:settings" to ":core:preferences",
)

// Mirrors settings.gradle.kts: true when developing against the local foundation checkout (the
// nl.rhaydus:* coordinates are substituted by an includeBuild of ../rhaydus-foundation).
val foundationLocal = Properties().apply {
    val localProperties = rootDir.resolve("local.properties")
    if (localProperties.exists()) localProperties.inputStream().use { load(it) }
}.getProperty("foundation.local").toBoolean()

// dependency-analysis (buildHealth) configuration. Gates on the high-value categories — genuinely
// unused dependencies and wrong api/implementation exposure (MODULE_STRUCTURE_GUIDELINES §10) — while
// staying out of the way of the convention-plugin design: the uniform runtime + test bundle provided
// by AndroidLibraryConventionPlugin (coroutines, koin, JUnit5/Kotest/MockK/Turbine) is
// intentionally declared centrally, not per module, so it is excluded from the "unused" check. The
// "declare transitive dependencies directly" advice is BOM/convention-managed completeness noise and
// is treated as informational (ignored), not a gate.
dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                severity("fail")
                exclude(
                    // Uniform runtime + test bundle provided by AndroidLibraryConventionPlugin.
                    "io.insert-koin:koin-android",
                    "org.jetbrains.kotlinx:kotlinx-coroutines-android",
                    // Desktop's Main dispatcher — supplied via ServiceLoader (no compile reference), the
                    // desktop counterpart of coroutines-android; wired into :desktopApp's runtime classpath.
                    "org.jetbrains.kotlinx:kotlinx-coroutines-swing",
                    "org.junit.jupiter:junit-jupiter-api",
                    "org.junit.jupiter:junit-jupiter-params",
                    "io.kotest:kotest-assertions-core",
                    "io.mockk:mockk",
                    "app.cash.turbine:turbine",
                    // Provided uniformly by the Compose / Room convention plugins (not per-module deps).
                    "androidx.activity:activity-compose",
                    "androidx.compose.ui:ui",
                    "androidx.compose.ui:ui-tooling-preview",
                    "androidx.compose.ui:ui-tooling",
                    "androidx.compose.material3:material3",
                    "androidx.compose.ui:ui-graphics",
                    // KMP Compose Multiplatform artifacts provided uniformly by KmpComposeConventionPlugin.
                    "org.jetbrains.compose.runtime:runtime",
                    "org.jetbrains.compose.foundation:foundation",
                    "org.jetbrains.compose.animation:animation",
                    "org.jetbrains.compose.ui:ui",
                    "org.jetbrains.androidx.navigationevent:navigationevent-compose",
                    "org.jetbrains.compose.components:components-ui-tooling-preview",
                    "org.jetbrains.compose.material3:material3",
                    // Compose Multiplatform auto-injects a `jvmDev` source set (Compose Hot Reload) plus the
                    // OS-specific desktop runtime for every module that has a `jvm()` target; :desktopApp's
                    // compose.desktop.currentOs resolves to the same OS-specific coordinate. None are referenced
                    // in code (the desktop UI types resolve transitively), so DA flags them — exclude every host
                    // variant centrally, the same way the convention-plugin Compose artifacts above are excluded.
                    "org.jetbrains.compose.desktop:desktop-jvm-macos-arm64",
                    "org.jetbrains.compose.desktop:desktop-jvm-macos-x64",
                    "org.jetbrains.compose.desktop:desktop-jvm-linux-x64",
                    "org.jetbrains.compose.desktop:desktop-jvm-linux-arm64",
                    "org.jetbrains.compose.desktop:desktop-jvm-windows-x64",
                    "org.jetbrains.compose.hot-reload:hot-reload-runtime-api",
                    // KMP-variant bundle deps from KmpLibraryConventionPlugin's commonMain set — provided
                    // uniformly, so never a per-module "unused" finding (mirrors the onIncorrectConfiguration list).
                    "io.insert-koin:koin-core",
                    "org.jetbrains.kotlinx:kotlinx-coroutines-core",
                    // False positives: genuinely used via mechanisms DA can't see without type resolution.
                    "io.insert-koin:koin-androidx-compose", // koinInject(...)
                    "cafe.adriel.voyager:voyager-koin", // ScreenModel / screenModelScope (ToadScreenModel)
                    "org.jetbrains.kotlinx:kotlinx-serialization-json", // @Serializable / Json
                    "androidx.work:work-runtime-ktx", // CoroutineWorker
                    "androidx.camera:camera-camera2", // CameraX runtime backend, loaded via ServiceLoader (no compile ref)
                    "com.google.mlkit:barcode-scanning", // MLKit barcode model + API used by the scanner; DA mis-resolves to a transitive
                    "com.google.android.material:material", // Material Components Theme.Material3.* XML themes (resource-only, no Kotlin ref)
                )

                // With foundation.local=true the nl.rhaydus:* coordinates are substituted by an includeBuild
                // of ../rhaydus-foundation; dependency-analysis cannot resolve the composite-build ABI and
                // false-flags every foundation dependency as unused. Exclude them in local mode only — against
                // the published artifacts (CI / normal builds) DA resolves them and the gate stays effective.
                if (foundationLocal) {
                    exclude(
                        "nl.rhaydus:core-common",
                        "nl.rhaydus:core-platform",
                        "nl.rhaydus:toad",
                        "nl.rhaydus:designsystem-core",
                        "nl.rhaydus:designsystem-editorial",
                        "nl.rhaydus:designsystem-image",
                        "nl.rhaydus:offline-sync",
                    )
                }
            }

            onIncorrectConfiguration {
                severity("fail")
                exclude(
                    // api/impl of convention-plugin-provided deps is managed centrally, not per module.
                    "androidx.compose.material3:material3",
                    "androidx.compose.ui:ui",
                    "androidx.compose.ui:ui-graphics",
                    // KMP-variant bundle deps from KmpLibraryConventionPlugin's commonMain set — same
                    // central-provisioning rationale as the Android `-android` variants above.
                    "io.insert-koin:koin-core",
                    "org.jetbrains.kotlinx:kotlinx-coroutines-core",
                    // Intentional public exposure: designsystem returns a Coil ImageRequest (§10).
                    "io.coil-kt.coil3:coil-compose",
                )
            }

            onUsedTransitiveDependencies {
                severity("ignore")
            }

            // Compile-vs-runtime classpath splitting is a micro-optimisation entangled with the
            // convention bundle (it wants coroutines-android demoted to runtimeOnly in every module);
            // not a correctness/structure concern, so it does not gate.
            onRuntimeOnly {
                severity("ignore")
            }

            onRedundantPlugins {
                severity("ignore")
            }
        }
    }
}

tasks.register("checkModuleGraph") {
    group = "verification"
    description = "Fails on any module dependency that breaks the tier DAG (MODULE_STRUCTURE_GUIDELINES §2)."

    doLast {
        val violations = mutableListOf<String>()
        var edges = 0

        subprojects.forEach { module ->
            val fromTier = tierOf(module.path) ?: return@forEach
            val allowed = allowedTargetTiers.getValue(fromTier)

            module.configurations.forEach { configuration ->
                configuration.dependencies
                    .filterIsInstance<ProjectDependency>()
                    .forEach { dependency ->
                        if (dependency.path == module.path) return@forEach

                        val targetTier = tierOf(dependency.path) ?: return@forEach
                        edges++

                        if (targetTier !in allowed) {
                            violations += "${module.path} → ${dependency.path}  " +
                                "($fromTier may depend only on $allowed)"
                        }

                        // api-visibility rule: a declared `api` edge to a data-area module must be
                        // allowlisted above. Match every declarable api bucket — plain `api`
                        // (android-only modules) and the KMP/variant `<sourceSet>Api` configs
                        // (`commonMainApi`, `androidMainApi`, `debugApi`, …) — but never the test ones,
                        // where an api edge can't leak into a production consumer's graph.
                        val isApiEdge = configuration.name.endsWith("api", ignoreCase = true) &&
                            configuration.name.contains("test", ignoreCase = true).not()

                        if (
                            isApiEdge &&
                            dependency.path in dataAreaModules &&
                            (module.path to dependency.path) !in allowedApiDataEdges
                        ) {
                            violations += "${module.path} → api(${dependency.path})  " +
                                "(data modules must be implementation-depended unless allowlisted — " +
                                "MODULE_STRUCTURE_GUIDELINES §10)"
                        }
                    }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Illegal module dependencies (see MODULE_STRUCTURE_GUIDELINES §2 tiers / §10 " +
                    "api-visibility):\n" +
                    violations.distinct().sorted().joinToString("\n") { "  - $it" },
            )
        }

        logger.lifecycle("checkModuleGraph: $edges project dependencies validated, DAG intact.")
    }
}

// The per-change style gate: type-resolved detekt across every module — the shared nl.rhaydus baseline
// plus the custom `rhaydus` ruleset, gating from zero with no baseline file.
//
// detekt's own tasks are wired into the heavy `check` lifecycle, but the gate developers actually run
// per change is `styleCheck` — so without this, findings would only surface in a rarely-run full build
// and silently accumulate. It runs the TYPE-RESOLVED tasks, which is what makes the crash-safety rule
// (`rhaydus:UnguardedFlowTerminalRead`) fire at all; the cost is that it compiles Android + JVM rather
// than merely parsing source. That cost is the point: an inert gate is not a gate.
//
// (This task used to also shell out to `scripts/style-check.sh`. All six of that script's recipes are now
// blocking rules — five in nl.rhaydus:ktlint-rules via `ktlintCheck`, the sixth the detekt rule above — so
// the script was retired. See docs/working/foundation-upstream-candidates.md F1/F7/F22.)
tasks.register("styleCheck") {
    group = "verification"
    description = "Runs type-resolved detekt (shared baseline + rhaydus crash-safety ruleset) across all modules."

    dependsOn(subprojects.map { sp -> sp.tasks.matching { it.name in typeResolvedDetektTasks } })
}
