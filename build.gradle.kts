// Top-level build file where you can add configuration options common to all sub-projects/modules.
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.apollo) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt) apply false
}

// Apply detekt uniformly to every Kotlin module (no baseline — gates from zero on the shared config).
// Wired centrally here, alongside the ktlint/styleCheck/checkModuleGraph gates, rather than per module.
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        parallel = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "11"
        // Analyse production code only; test sources follow their own (looser) patterns.
        setSource(project.files("src/main/java", "src/main/kotlin"))
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
    path == ":app" -> "app"
    else -> null
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