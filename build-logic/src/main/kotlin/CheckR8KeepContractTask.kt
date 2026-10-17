import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails when R8's output for a variant breaks what the platform and libraries reach by name.
 */
@CacheableTask
abstract class CheckR8KeepContractTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mappingFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val seedsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val configurationFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val manifestFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val keepFiles: ConfigurableFileCollection

    @get:Classpath
    abstract val classJars: ListProperty<RegularFile>

    @get:Classpath
    abstract val classDirectories: ListProperty<Directory>

    @get:Input
    abstract val namespace: Property<String>

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun check() {
        val classes = ClassFileReader.readAll(
            jars = classJars.get().map { it.asFile },
            directories = classDirectories.get().map { it.asFile },
        )

        val violations = R8KeepContract(
            classes,
            namespace.get(),
        ).violations(
            manifest = manifestFile.get().asFile,
            mapping = mappingFile.get().asFile,
            seeds = seedsFile.get().asFile,
            configuration = configurationFile.get().asFile,
            keepFiles = keepFiles.files,
        )

        report.get().asFile.writeText(
            if (violations.isEmpty()) "R8 keep contract holds" else violations.joinToString("\n"),
        )

        if (violations.isNotEmpty()) {
            throw GradleException(
                "The R8 keep contract is broken (${violations.size}):\n" +
                    violations.joinToString("\n") { " - $it" },
            )
        }
    }
}
