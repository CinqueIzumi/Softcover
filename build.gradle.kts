// Top-level build file where you can add configuration options common to all sub-projects/modules.
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.apollo) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.module.graph)
    alias(libs.plugins.kover)
}

// Embeds an auto-generated Mermaid module-dependency graph into README.md under the "### Module graph"
// heading. Regenerate with `./gradlew createModuleGraph`. Tooling-only modules are excluded so the
// graph reflects the shipped app's tier DAG (:app → :orchestration → :feature:* → :core:*).
moduleGraphConfig {
    readmePath.set("$rootDir/README.md")
    heading.set("### Module graph")
    orientation.set(Orientation.LEFT_TO_RIGHT)
    theme.set(Theme.NEUTRAL)
    setStyleByModuleType.set(true)
    excludedModulesRegex.set(".*ktlint-rules.*")
}

// Code coverage. Kover is applied to every shipped module (below) and aggregated into a single XML
// report at the root — `./gradlew koverXmlReport` → build/reports/kover/report.xml — which CI uploads
// to Codecov. :ktlint-rules is build tooling, not shipped code, so it is excluded from the report.
dependencies {
    subprojects
        .filter { it.path != ":ktlint-rules" }
        .forEach { kover(it) }
}

// Apply detekt uniformly to every Kotlin module (no baseline — gates from zero on the shared config).
// Wired centrally here, alongside the ktlint/styleCheck/checkModuleGraph gates, rather than per module.
subprojects {
    apply(plugin = "com.autonomousapps.dependency-analysis")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    // Coverage instrumentation for every shipped module; the merged report is wired at the root above.
    // :ktlint-rules is build tooling, so it stays uninstrumented.
    if (path != ":ktlint-rules") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
    }

    configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        parallel = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "11"
        // Analyse production code only; test sources follow their own (looser) patterns.
        // `commonMain/kotlin` covers the KMP modules' shared production code (Android-only modules
        // have none, so the extra path is a harmless no-op for them).
        setSource(project.files("src/main/java", "src/main/kotlin", "src/commonMain/kotlin"))
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
        }
    }
}

// Wire the custom ktlint ruleset (:ktlint-rules) into the build so style is enforced for every
// developer with zero setup: `./gradlew ktlintCheck` (the gate, also run by `check`) and
// `./gradlew ktlintFormat` (autofix). The rules run via ktlint's rule-engine directly from the
// :ktlint-rules module, so there is no Spotless/plugin version coupling.
subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(":ktlint-rules:ktlintCheck")
        dependsOn(":checkModuleGraph")
    }
}

// Gate every KMP module's `check` on iOS compilation. The Android variant compiles common/androidMain
// for the JVM, where JVM-only APIs resolve fine (kotlin.jvm.* default imports, Dispatchers.IO,
// java.time) — so an Android-only build silently hides code that will not compile for iOS. Compiling
// all declared iOS targets here fails such leaks at `check` time (per KMP_MIGRATION.md §7's per-module
// Definition of Done) instead of at iOS bring-up in P6. Guarded to KMP modules; and to macOS hosts,
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
                    "org.jetbrains.compose.ui:ui-backhandler",
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
                )
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
                    }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Illegal module dependencies (see MODULE_STRUCTURE_GUIDELINES §2):\n" +
                    violations.distinct().sorted().joinToString("\n") { "  - $it" },
            )
        }

        logger.lifecycle("checkModuleGraph: $edges project dependencies validated, DAG intact.")
    }
}

// Runs the deterministic mechanical-style checks (scripts/style-check.sh) so CI / pre-commit can
// gate on them. By default the script checks the changed .kt files; pass files via -PstyleCheckFiles
// to scope it. Fails the build only on ERROR-tier findings (see the script header for the tiers).
tasks.register<Exec>("styleCheck") {
    group = "verification"
    description = "Runs scripts/style-check.sh over changed Kotlin files (mechanical style rules)."

    val script = "${rootProject.projectDir}/scripts/style-check.sh"
    val extraFiles = (project.findProperty("styleCheckFiles") as String?)
        ?.split(Regex("\\s+"))
        ?.filter { it.isNotBlank() }
        ?: emptyList()

    commandLine(listOf("bash", script) + extraFiles)
}