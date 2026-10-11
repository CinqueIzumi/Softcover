import com.skydoves.compose.stability.gradle.StabilityAnalyzerExtension
import com.skydoves.compose.stability.gradle.StabilityCheckTask
import com.skydoves.compose.stability.gradle.StabilityDumpTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

internal fun Project.applyComposeStabilityGate() {
    pluginManager.apply("com.github.skydoves.compose.stability.analyzer")

    extensions.configure<StabilityAnalyzerExtension> {
        stabilityValidation {
            includeTests.set(false)
            failOnStabilityChange.set(true)
        }
    }

    // The analyzer reads `stability/compile<Variant>Kotlin/`, but the KMP Android target compiles as
    // `compileAndroidMain`, so its per-variant tasks would find no input and silently dump nothing. The
    // analyzer assigns that input when it registers the tasks from its variant callback, so the override
    // waits for `afterEvaluate` to land after it.
    val androidMainInfo = layout.buildDirectory.file("stability/compileAndroidMain/stability-info.json")
    val dumps = tasks.withType<StabilityDumpTask>().matching { it.name.startsWith("androidMain") }
    val checks = tasks.withType<StabilityCheckTask>().matching { it.name.startsWith("androidMain") }
    afterEvaluate {
        dumps.configureEach { stabilityInputFiles.setFrom(androidMainInfo) }
        checks.configureEach { stabilityInputFiles.setFrom(androidMainInfo) }
    }
    dumps.configureEach { dependsOn("compileAndroidMain") }
    checks.configureEach { dependsOn("compileAndroidMain") }
}
