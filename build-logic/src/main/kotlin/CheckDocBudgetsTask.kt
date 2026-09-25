import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

private fun parseLimitToken(token: String): Pair<Long, BudgetUnit> = when {
    token.endsWith("KB") -> (token.removeSuffix("KB").toLong() * 1024) to BudgetUnit.BYTES
    token.endsWith("B") -> token.removeSuffix("B").toLong() to BudgetUnit.BYTES
    token.endsWith("L") -> token.removeSuffix("L").toLong() to BudgetUnit.LINES
    else -> throw GradleException("Unrecognised doc-budgets.txt unit in token '$token'")
}

private fun globToRegex(glob: String): Regex {
    val pattern = StringBuilder("^")
    var i = 0
    while (i < glob.length) {
        when {
            glob.startsWith(
                "**",
                i,
            ) -> {
                pattern.append(".*")
                i += 2
            }

            glob[i] == '*' -> {
                pattern.append("[^/]*")
                i += 1
            }

            glob[i] in ".^$+?()[]{}|\\" -> {
                pattern.append('\\').append(glob[i])
                i += 1
            }

            else -> {
                pattern.append(glob[i])
                i += 1
            }
        }
    }
    pattern.append("$")
    return Regex(pattern.toString())
}

private fun lineCountOf(text: String): Long = when {
    text.isEmpty() -> 0
    text.endsWith("\n") -> text.count { it == '\n' }.toLong()
    else -> text.count { it == '\n' }.toLong() + 1
}

private fun extractNowSection(text: String): String {
    val lines = text.split("\n")
    val start = lines.indexOfFirst { it.trimEnd() == "## Now" }
    if (start == -1) return ""
    val section = mutableListOf<String>()
    for (index in start until lines.size) {
        if (index > start && lines[index].startsWith("## ")) break
        section += lines[index]
    }
    return section.joinToString("\n")
}

private fun runGit(
    workingDir: File,
    vararg args: String,
): ByteArray? {
    val process = ProcessBuilder(listOf("git") + args)
        .directory(workingDir)
        .start()
    val output = process.inputStream.use { it.readBytes() }
    process.errorStream.use { it.readBytes() }
    return if (process.waitFor() == 0) output else null
}

private fun resolveMergeBase(root: File): String =
    runGit(
        root,
        "merge-base",
        "HEAD",
        "origin/main",
    )?.toString(Charsets.UTF_8)?.trim()
        ?: runGit(
            root,
            "merge-base",
            "HEAD",
            "main",
        )?.toString(Charsets.UTF_8)?.trim()
        ?: throw GradleException(
            "checkDocBudgets: cannot resolve a git merge-base against origin/main or main — " +
                "the checkout needs full history (fetch-depth: 0).",
        )

private fun contentAtRef(
    root: File,
    ref: String,
    relativePath: String,
): ByteArray? =
    runGit(
        root,
        "show",
        "$ref:$relativePath",
    )

/**
 * Gates `docs/doc-budgets.txt` in CI: a file already over its budget still passes as long as it has not
 * grown, in the budget's own unit, since the merge-base with `origin/main` (falling back to local `main`
 * if the remote branch is unavailable). A file that is over budget and did not exist at that merge-base
 * always fails — there is no ratchet to compare against. Resolving no merge-base at all (a shallow
 * checkout) is a hard failure rather than a silent pass, since `check` must run with `fetch-depth: 0`.
 */
abstract class CheckDocBudgetsTask : DefaultTask() {
    @get:InputFile
    abstract val budgetsFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val markdownFiles: ConfigurableFileCollection

    @get:InputFile
    @get:Optional
    abstract val activeFile: RegularFileProperty

    @get:Internal
    abstract val repoRoot: DirectoryProperty

    @TaskAction
    fun check() {
        val root = repoRoot.get().asFile
        val (rules, nowLimit) = parseBudgets(budgetsFile.get().asFile.readText())
        val mergeBase = resolveMergeBase(root)
        val violations = mutableListOf<String>()

        markdownFiles.files.forEach { file ->
            val relativePath = file.relativeTo(root).invariantSeparatorsPath
            val rule = rules.firstOrNull { globToRegex(it.glob).matches(relativePath) } ?: return@forEach
            val currentBytes = file.readBytes()
            val currentSize = sizeOf(
                currentBytes,
                rule.unit,
            )
            if (currentSize <= rule.limit) return@forEach

            val baseBytes = contentAtRef(
                root,
                mergeBase,
                relativePath,
            )
            if (baseBytes == null) {
                violations += "$relativePath: new file over budget (${rule.glob} ${rule.token}) — " +
                    "$currentSize${rule.unit.suffix} (limit ${rule.limit}${rule.unit.suffix})"
                return@forEach
            }

            val baseSize = sizeOf(
                baseBytes,
                rule.unit,
            )
            if (currentSize > baseSize) {
                violations += "$relativePath: over budget and grew since merge-base (${rule.glob} ${rule.token}) — " +
                    "$baseSize${rule.unit.suffix} → $currentSize${rule.unit.suffix} (limit ${rule.limit}${rule.unit.suffix})"
            }
        }

        if (nowLimit != null) {
            activeFile.orNull?.asFile
                ?.takeIf { it.exists() }
                ?.readLines()
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.forEach { relativePath ->
                    val file = root.resolve(relativePath)
                    if (file.exists().not()) return@forEach

                    val currentNow = lineCountOf(extractNowSection(file.readText()))
                    if (currentNow <= nowLimit) return@forEach

                    val baseBytes = contentAtRef(
                        root,
                        mergeBase,
                        relativePath,
                    )
                    val baseNow = baseBytes?.let { lineCountOf(extractNowSection(it.toString(Charsets.UTF_8))) } ?: 0
                    if (baseBytes == null || currentNow > baseNow) {
                        violations += "$relativePath ## Now section: over budget (## Now ${nowLimit}L) — " +
                            "${baseNow}L → ${currentNow}L"
                    }
                }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Doc budgets exceeded (docs/doc-budgets.txt; see .claude/rules/docs.md):\n" +
                    violations.sorted().joinToString("\n") { "  - $it" },
            )
        }

        logger.lifecycle("checkDocBudgets: ${markdownFiles.files.size} markdown files within budget.")
    }

    private fun sizeOf(
        bytes: ByteArray,
        unit: BudgetUnit,
    ): Long = when (unit) {
        BudgetUnit.BYTES -> bytes.size.toLong()
        BudgetUnit.LINES -> lineCountOf(bytes.toString(Charsets.UTF_8))
    }

    private fun parseBudgets(text: String): Pair<List<BudgetRule>, Long?> {
        val rules = mutableListOf<BudgetRule>()
        var nowLimit: Long? = null

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trimEnd()
            when {
                line.isBlank() -> Unit
                line.startsWith("## Now") -> {
                    val token = line.trim().split(Regex("\\s+")).last()
                    nowLimit = parseLimitToken(token).first
                }

                line.startsWith("#") -> Unit
                else -> {
                    val fields = line.trim().split(Regex("\\s+"))
                    val (limit, unit) = parseLimitToken(fields.last())
                    rules += BudgetRule(
                        fields.first(),
                        fields.last(),
                        limit,
                        unit,
                    )
                }
            }
        }

        return rules to nowLimit
    }
}
