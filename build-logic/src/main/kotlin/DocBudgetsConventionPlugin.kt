import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Registers `checkDocBudgets` on the applying project, wiring it to the repo's own
 * `docs/doc-budgets.txt` and `docs/working/ACTIVE.md` rather than requiring per-module configuration —
 * applied once, at the root, alongside `checkModuleGraph`.
 */
class DocBudgetsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        tasks.register<CheckDocBudgetsTask>("checkDocBudgets") {
            group = "verification"
            description = "Fails on any markdown file that crosses its budget in docs/doc-budgets.txt."

            budgetsFile.set(layout.projectDirectory.file("docs/doc-budgets.txt"))
            activeFile.set(layout.projectDirectory.file("docs/working/ACTIVE.md"))
            repoRoot.set(layout.projectDirectory)
            markdownFiles.from(
                fileTree(layout.projectDirectory) {
                    include("**/*.md")
                    exclude("**/build/**", "**/.gradle/**", "**/.git/**", "**/.idea/**")
                },
            )
        }
    }
}
