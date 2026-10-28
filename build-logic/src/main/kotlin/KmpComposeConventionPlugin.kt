import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Layers the foundation's `rhaydus.kmp.compose` onto a module that already applies
 * `rhaydus.kmp.library`, then adds the Compose stability gate ([applyComposeStabilityGate]).
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("rhaydus.kmp.compose")
        applyComposeStabilityGate()
    }
}
