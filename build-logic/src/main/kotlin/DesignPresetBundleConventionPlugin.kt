import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Registers `designPresetBundle` on the root project: the exporter's tokens and assets, the showcase
 * renders and the committed `design/project` content, assembled into `build/design-preset/project`.
 */
class DesignPresetBundleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        tasks.register<DesignPresetBundleTask>("designPresetBundle") {
            group = "design preset"
            description = "Assembles the Claude Design System project tree into build/design-preset/project."

            dependsOn(
                ":core:designsystem:exportDesignTokens",
                ":showcase:renderShowcase",
            )

            val handWritten = layout.projectDirectory.dir("design/project")
            handWrittenDir.set(handWritten)
            handWrittenFiles.from(fileTree(handWritten))
            tokensDir.fileProvider(
                provider {
                    project(":core:designsystem").tasks.getByName("exportDesignTokens").outputs.files.singleFile
                },
            )
            rendersDir.set(layout.buildDirectory.dir("design-preset/renders"))
            bundleDir.set(layout.buildDirectory.dir("design-preset/project"))
            maxFiles.set(MAX_FILES)
        }
    }

    private companion object {
        const val MAX_FILES = 1008
    }
}
