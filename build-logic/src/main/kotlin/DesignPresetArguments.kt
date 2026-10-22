import java.io.File
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.process.CommandLineArgumentProvider

/**
 * The exporter's command line. The paths are `@Internal`: their absolute form must not reach the task's
 * cache key, and the files they point at are declared as inputs and outputs on the task itself.
 */
class DesignPresetArguments(
    @get:Input val mode: String,
    @get:Internal val moduleDir: File,
    @get:Internal val outDir: File,
    @get:Internal val designDir: File,
) : CommandLineArgumentProvider {
    override fun asArguments(): Iterable<String> = listOf(
        mode,
        "--module-dir=${moduleDir.absolutePath}",
        "--out=${outDir.absolutePath}",
        "--design-dir=${designDir.absolutePath}",
    )
}
