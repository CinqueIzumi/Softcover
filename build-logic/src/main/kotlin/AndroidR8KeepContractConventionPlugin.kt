import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

/**
 * Registers `checkR8KeepContract` on an application, one task per minified variant. It is not wired into
 * `check`: CI calls it explicitly. `:smoketest`'s keep rules are excluded because they apply to the test
 * APK, not to the app's R8 run.
 */
class AndroidR8KeepContractConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        var registered = 0
        val aggregate = tasks.register("checkR8KeepContract") {
            group = "verification"
            description = "Fails when R8 renames or drops a class that is reached by name."

            doFirst {
                check(registered > 0) { "No variant runs R8, so the keep contract has nothing to check." }
            }
        }

        extensions.getByType<ApplicationAndroidComponentsExtension>().onVariants { variant ->
            if (variant.isMinifyEnabled.not()) return@onVariants

            registered++

            val capitalized = variant.name.replaceFirstChar { it.uppercase() }
            val mapping = variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)

            val task = tasks.register<CheckR8KeepContractTask>("checkR8KeepContract$capitalized") {
                group = "verification"
                description = "Checks the ${variant.name} R8 output against the keep contract."

                mappingFile.set(mapping)
                seedsFile.set(layout.file(mapping.map { it.asFile.resolveSibling("seeds.txt") }))
                configurationFile.set(layout.file(mapping.map { it.asFile.resolveSibling("configuration.txt") }))
                manifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
                namespace.set(variant.namespace)
                keepFiles.from(
                    rootProject.fileTree(rootProject.layout.projectDirectory) {
                        include("**/src/*/keepRules/*.keep")
                        exclude(
                            "**/build/**",
                            "smoketest/**",
                        )
                    },
                )
                report.set(layout.buildDirectory.file("reports/r8-keep-contract/${variant.name}.txt"))
            }

            variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                .use(task)
                .toGet(
                    ScopedArtifact.CLASSES,
                    CheckR8KeepContractTask::classJars,
                    CheckR8KeepContractTask::classDirectories,
                )

            aggregate.configure { dependsOn(task) }
        }
    }
}
