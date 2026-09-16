// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.jetbrains.compose.resources.ResourcesExtension
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
// INERT on the source-only `detekt` task. Per-variant tasks are deliberately excluded.
//
// `detektAndroidHostTest` covers the unit tests. It was previously left out, and the cost of that showed:
// by the time it was first run it had accumulated 3,308 findings. All but 46 came from a single stale glob —
// detekt 1.23.8's built-in "these rules don't apply to test code" exclude lists predate AGP 9's KMP source
// sets, so they have never heard of `androidHostTest` and fired `FunctionNaming` at 3,144 backticked test
// names. That is repaired in the shared baseline (see the test-excludes block in the foundation
// `config/detekt.yml`); the rest were fixed in the tests themselves. Keeping the task gated is what stops
// it silently refilling — an ungated task is indistinguishable from a passing one right up until you run it.
val typeResolvedDetektTasks = setOf(
    "detektAndroidMain",
    "detektJvmMain",
    "detektMain",
    "detektAndroidHostTest",
)

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

                // The unit tests. Spelled out for the same reason as the main compilations: detekt would
                // otherwise seed only the target's own source set, so a module that ever puts shared test
                // code in `commonTest` would have it silently skipped. Every module's tests live in
                // `androidHostTest` today; naming `commonTest` here means that stays true if one moves.
                "detektAndroidHostTest" -> setSource(
                    project.files(
                        "src/commonTest/kotlin",
                        "src/androidHostTest/kotlin",
                    ),
                )

                // `:desktopApp`'s own `detektMain`, and the aggregating `detektMain` that `:app`'s
                // per-variant tasks fan out from — the shared, variant-neutral `src/main` layout.
                "detektMain" -> setSource(project.files("src/main/java", "src/main/kotlin"))

                // `:app`'s per-variant tasks additionally read that variant's OWN source set, which is
                // where the build-type-specific code lives: the debug-routes bindings and (since the
                // component-library migration moved them out of `:core:designsystem`) the debug screens
                // themselves. Without this they would be silently unscanned — `:app` is the only module
                // with build types, so it is the only place this applies.
                //
                // Each variant gets only its own source set, never both: `src/debug` and `src/release`
                // each declare `nl.rhaydus.softcover.di.debugRoutesModule`, so putting them in one
                // scope would hand type resolution two conflicting declarations of the same symbol.
                "detektDebug" -> setSource(
                    project.files(
                        "src/main/java",
                        "src/main/kotlin",
                        "src/debug/java",
                        "src/debug/kotlin",
                    ),
                )

                "detektRelease" -> setSource(
                    project.files(
                        "src/main/java",
                        "src/main/kotlin",
                        "src/release/java",
                        "src/release/kotlin",
                    ),
                )
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
        dependsOn(":checkResourcePackaging")
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
// *allowed*; it does not constrain whether the target is re-exported (`api`) or kept private
// (`implementation`). An `api` edge republishes the target's whole public surface to every downstream
// consumer — which is exactly how `:core:designsystem` became a god-module. `implementation` is
// therefore the default for the modules below; every `api(project(<one of these>))` edge must be an
// explicit, reviewed entry in `allowedApiDataEdges` so a new one is a conscious decision, not a
// silent leak.
//
// The set is **not** just the data-area modules. It started that way, and the component-library
// migration walked straight into the gap: `:feature:book_detail` declared
// `api(project(":core:component"))` on a mistaken belief that it re-exported a component type, the
// two features doing the identical integration used `implementation`, and nothing failed — because
// `:core:component` was not in this set. The rule's own rationale applies verbatim to a library whose
// entire surface is components, so it is in the set now.
//
// Deliberately NOT in the set, so the omissions are decisions rather than oversights:
//
//  - `:core:domain` — a pure contract module with no dependencies of its own. Re-exporting it leaks
//    nothing a consumer could not already reach, and the migration tracker's § 3a settled that it may
//    be `api`-exposed freely. Putting it in the set would mean allowlisting ~15 legitimate edges.
//  - `:core:designsystem` — tokens. Once S4 finishes it has zero project dependencies (G2), so an
//    `api` edge to it republishes a leaf. Revisit only if it ever grows a dependency again.
//  - `:core:network`, `:core:database` — infra, and their `api` edges are load-bearing for the
//    Apollo/Room types that cross module boundaries by design.
val apiSignOffModules = setOf(
    ":core:book",
    ":core:lists",
    ":core:deadlines",
    ":core:personal",
    ":core:profile",
    ":core:identity",
    ":core:preferences",

    // Added by the component-library migration — see the note above.
    ":core:component",
    ":core:presentation",
    ":core:uibinding",
)

// Allowlisted (source → data module) `api` edges: each genuinely renders/returns the data module's
// types in its own public surface. Adding a row is the deliberate sign-off the rule exists to force.
val allowedApiDataEdges = setOf(
    ":core:identity" to ":core:preferences",
    ":core:profile" to ":core:identity",
    ":feature:book_detail" to ":core:identity",
    ":feature:explore" to ":core:book",
    ":feature:explore" to ":core:identity",
    ":feature:explore" to ":core:lists",
    ":feature:explore" to ":core:preferences",
    ":feature:lists" to ":core:lists",
    ":feature:settings" to ":core:preferences",

    // `:core:uibinding` exists to map a domain model onto the UI type that renders it, so a consumer
    // needs to see *both sides* of a mapping without re-declaring them — that is the whole point of
    // the module, decided in the migration tracker's § 3a. `api` is the design, not a leak.
    ":core:uibinding" to ":core:component",

    // `ActiveSession` — `:core:presentation`'s cross-tier session model, consumed by `:feature:session`
    // through `ActiveSessionController` — carries a `CoverUiModel` per surface (S4-4). A consumer that
    // sees `ActiveSession` must see the type of its own properties, so this is the same "both sides of
    // a public type" shape as the `:core:uibinding` row above, not a leak of the whole library.
    ":core:presentation" to ":core:component",

    // `:feature:book_detail` re-exports presentation types through its own public surface (its
    // `BookDetailScreen` is constructed by orchestration with a `BookInitialCover`), settled in § 5e.
    // Contrast `:core:component`, which it depends on with `implementation`: nothing public in that
    // module names a component type, and an `api` edge there republished the whole library by mistake
    // — the mistake that widened this rule's scope in the first place.
    ":feature:book_detail" to ":core:presentation",
)

// Layering direction inside the UI stack (docs/working/component-library-migration.md § 5g). The
// tier rule lets any `:core:*` module depend on any other, so it has nothing to say about the one
// direction that matters here: `:core:component` -> `:core:designsystem`, `:core:uibinding` ->
// `:core:component`, and **never** the reverse.
//
// A reverse edge is a Gradle dependency cycle, so it does fail the build — but with a task-graph
// error that says nothing about why it is wrong. This exists for the error message: the direction
// rule is what forced S4's sub-commits to be re-cut consumer-first, and someone hitting it should be
// told that rather than left reading a cycle trace. It also catches the non-cyclic case:
// `:core:designsystem` -> `:core:presentation` is not a cycle, and is still forbidden — the whole
// point of S3's split was to establish that those two sit side by side rather than stacking.
val bannedReverseEdges = setOf(
    ":core:designsystem" to ":core:component",
    ":core:designsystem" to ":core:uibinding",
    ":core:designsystem" to ":core:presentation",
)

// Token-module isolation (docs/working/component-library-migration.md § 6 G2). `:core:designsystem` is
// tokens — theme, editorial typography, the icon/illustration catalogs, modifiers, shared-element
// scopes. A token is a value the whole app may read; it has nothing to ask of the app in return, so the
// module needs no project dependency at all, and the modules listed here must declare none.
//
// This is asserted rather than left to hold by accident. It held by accident once before and stopped:
// the module accumulated an `api` edge per component that wanted a domain type, which is how a token
// module became the god-module S3 and S4 spent six sub-commits unwinding. The last two edges
// (`:core:domain` and `kotlinx-datetime`, both for the `Deadline*` trio) went in S4-5b when those
// components moved to `:core:component`.
//
// Pairs with the detekt `ForbiddenImport` rule scoped to `**/core/designsystem/**` in
// `config/detekt/detekt.yml`. The two are NOT redundant, and § 6's G2 entry records how that was
// learned: S4-1 left fully-qualified `nl.rhaydus.softcover.core.domain.model.*` references in `Color.kt`
// and `LocalDarkTheme.kt`'s KDoc, and BOTH gates were blind to them — they are neither imports nor
// declared dependencies. A reviewer caught them. What the import rule does close is the case where a
// stray `import` outlives a dependency removal, which stays compilable for as long as some other module
// on the compile classpath still `api`-exposes the type.
val zeroProjectDependencyModules = setOf(
    ":core:designsystem",
)

// Component-library isolation (docs/working/component-library-migration.md §6 G1). The tier rule
// above lets any `:core:*` module depend on any other, which is too loose for the component library:
// `:core:component` renders UI from UI models and must never reach a domain model, a use case, DI, or
// navigation. That is the property that keeps it a *library* rather than a second god-module — the
// exact failure `:core:designsystem` already lived through — so it is a build failure, not a
// convention. A module listed here may depend on the named projects and nothing else.
val componentLibraryAllowedProjects = mapOf(
    ":core:component" to setOf(":core:designsystem"),
)

// External-coordinate ban for the same modules. DI, navigation, and the network client are the three
// ways a component library stops being renderable in isolation.
//
// Note on Koin, and on the limit of this check: `KmpLibraryConventionPlugin` injects
// `io.insert-koin:koin-core` into EVERY KMP module's commonMain, so a blanket group ban would fail on
// a dependency the module never declared. The convention-provided coordinates are therefore skipped
// (the same set dependency-analysis already treats as uniformly provided), and what remains caught
// here is every Koin artifact a module must opt into to do DI from a composable — `koin-compose`,
// `koin-android`, `koin-androidx-compose`.
//
// That leaves a REAL residual hole this check cannot close: `koin-core` on its own is enough for
// `KoinComponent` / `GlobalContext.get()` service-locator DI, and because the coordinate is skipped,
// declaring it explicitly passes. Verified empirically, not assumed. A declared-coordinate gate is
// structurally blind to this — usage is the thing that matters, not the declaration — so it is closed
// at the import instead, by the `ForbiddenImport` rule scoped to `:core:component` in
// `config/detekt/detekt.yml`. The two gates are complementary: this one keeps the dependency graph
// honest, that one keeps the source honest.
val componentLibraryBannedGroups = setOf(
    "io.insert-koin",
    "cafe.adriel.voyager",
    "com.apollographql.apollo",
)

// Injected uniformly by the convention plugins rather than declared per module, so they are not a
// signal about what a module chose to depend on. Mirrors the dependency-analysis exclude list above.
val conventionProvidedCoordinates = setOf(
    "io.insert-koin:koin-core",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core",
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
    description = "Fails on any module dependency that breaks the tier DAG " +
        "(MODULE_STRUCTURE_GUIDELINES §2), re-exports a data module (§10), or breaches " +
        "component-library isolation."

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
                            dependency.path in apiSignOffModules &&
                            (module.path to dependency.path) !in allowedApiDataEdges
                        ) {
                            violations += "${module.path} → api(${dependency.path})  " +
                                "(must be implementation-depended unless allowlisted in " +
                                "allowedApiDataEdges — MODULE_STRUCTURE_GUIDELINES §10)"
                        }

                        if (module.path in zeroProjectDependencyModules) {
                            violations += "${module.path} → ${dependency.path}  " +
                                "(tokens only — this module must declare NO project dependency at " +
                                "all. See docs/working/component-library-migration.md § 6 G2)"
                        }

                        if ((module.path to dependency.path) in bannedReverseEdges) {
                            violations += "${module.path} → ${dependency.path}  " +
                                "(wrong direction — the UI stack layers component → designsystem and " +
                                "uibinding → component; a stayer may never depend on a mover. See " +
                                "docs/working/component-library-migration.md § 5g)"
                        }

                        // Component-library isolation: an explicit per-module allowlist, tighter than
                        // the tier rule.
                        val allowedProjects = componentLibraryAllowedProjects[module.path]

                        if (allowedProjects != null && dependency.path !in allowedProjects) {
                            violations += "${module.path} → ${dependency.path}  " +
                                "(component library may depend only on $allowedProjects — " +
                                "no domain, data, DI or navigation)"
                        }
                    }

                // Deliberately NOT restricted to non-test configurations, unlike the
                // api-visibility check above. That one skips test configs because a test-only `api`
                // edge cannot leak into a consumer's graph, which is a statement about leakage. This
                // one is a statement about the module itself: the component library renders from UI
                // models and needs no DI, navigation, or network client to be exercised — in tests
                // least of all. Do not "fix" the asymmetry by adding a test exclusion.
                if (module.path in componentLibraryAllowedProjects) {
                    configuration.dependencies
                        .filterIsInstance<ExternalModuleDependency>()
                        .forEach { dependency ->
                            val coordinate = "${dependency.group}:${dependency.name}"

                            if (coordinate in conventionProvidedCoordinates) return@forEach

                            if (dependency.group in componentLibraryBannedGroups) {
                                violations += "${module.path} → $coordinate  " +
                                    "(component library may not depend on " +
                                    "${dependency.group} — DI, navigation and networking are banned)"
                            }
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

// Compose Multiplatform resources must actually reach the APK. A module that declares
// `compose.resources` gets its generated `Res` accessor and compiles perfectly whether or not the
// resources are ever packaged — **the failure is entirely at runtime**, as a `MissingResourceException`
// on the first read. On Android the deciding switch is `androidResources.enable`, which the KMP Android
// library plugin leaves OFF by default (CMP resources ship as Android *assets*, which that flag gates).
//
// This is a gate rather than a convention because the convention already failed twice, silently, and
// was found by accident rather than by any check:
//
//  - `:core:component` (S4-5a) — the offline banner's copy. It crashed the app on launch the first time
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

