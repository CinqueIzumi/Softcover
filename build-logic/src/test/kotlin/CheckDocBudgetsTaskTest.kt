import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import java.io.File
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class CheckDocBudgetsTaskTest {
    private lateinit var repoRoot: File

    @BeforeEach
    fun setUp() {
        repoRoot = createTempDir(prefix = "check-doc-budgets-")
    }

    @AfterEach
    fun tearDown() {
        repoRoot.deleteRecursively()
    }

    private fun git(vararg args: String) {
        val process = ProcessBuilder(listOf("git") + args)
            .directory(repoRoot)
            .redirectErrorStream(true)
            .start()
        process.inputStream.readBytes()
        check(process.waitFor() == 0) { "git ${args.joinToString(" ")} failed in $repoRoot" }
    }

    private fun initRepoOnMain() {
        git(
            "init",
            "-b",
            "main",
        )
        git(
            "config",
            "user.email",
            "test@example.com",
        )
        git(
            "config",
            "user.name",
            "Test",
        )
    }

    private fun commitAll(message: String) {
        git(
            "add",
            "-A",
        )
        git(
            "commit",
            "-m",
            message,
        )
    }

    private fun write(
        relativePath: String,
        content: String,
    ): File =
        repoRoot.resolve(relativePath).apply {
            parentFile.mkdirs()
            writeText(content)
        }

    private fun runCheck(
        budgets: String,
        markdownRelativePaths: List<String>,
        activeContent: String? = null,
    ) {
        val budgetsFile = write(
            "docs/doc-budgets.txt",
            budgets,
        )
        val project = ProjectBuilder.builder().withProjectDir(repoRoot).build()
        val task = project.tasks.create(
            "checkDocBudgets",
            CheckDocBudgetsTask::class.java,
        )

        task.budgetsFile.set(budgetsFile)
        task.repoRoot.set(repoRoot)
        task.markdownFiles.setFrom(markdownRelativePaths.map { repoRoot.resolve(it) })
        if (activeContent != null) {
            task.activeFile.set(
                write(
                    "docs/working/ACTIVE.md",
                    activeContent,
                ),
            )
        }

        task.check()
    }

    @Nested
    inner class Check {
        @Test
        fun `matches a double-star glob but not a single-star glob for a nested path`() {
            // ----- Arrange -----
            initRepoOnMain()
            val budgets = """
                docs/reference/*.md             100B
                docs/reference/**/*.md          20B
            """.trimIndent()
            write(
                ".gitkeep",
                "",
            )
            commitAll("baseline")
            write(
                "docs/reference/sub/deep.md",
                "x".repeat(25),
            )

            // ----- Act & Assert -----
            // 25 bytes is under the single-star rule's 100B limit but over the double-star rule's
            // 20B limit; if the wrong (single-star) rule matched this would pass instead of failing.
            val exception = shouldThrow<GradleException> {
                runCheck(
                    budgets,
                    listOf("docs/reference/sub/deep.md"),
                )
            }
            exception.message shouldContain "docs/reference/sub/deep.md"
        }

        @Test
        fun `applies the first matching rule when several globs match`() {
            // ----- Arrange -----
            initRepoOnMain()
            val budgets = """
                fixture/special/exact.md        8B
                fixture/special/*.md            1000B
            """.trimIndent()
            write(
                ".gitkeep",
                "",
            )
            commitAll("baseline")
            write(
                "fixture/special/exact.md",
                "1234567890",
            )

            // ----- Act & Assert -----
            // 10 bytes exceeds the first rule's 8B limit but is well within the second rule's
            // 1000B limit; a violation proves the first (more specific) rule won.
            val exception = shouldThrow<GradleException> {
                runCheck(
                    budgets,
                    listOf("fixture/special/exact.md"),
                )
            }
            exception.message shouldContain "fixture/special/exact.md"
        }

        @Test
        fun `parses KB and L tokens into bytes and lines`() {
            // ----- Arrange -----
            initRepoOnMain()
            val budgets = """
                fixture/kb.md                    1KB
                fixture/lines.md                 3L
            """.trimIndent()
            write(
                "fixture/kb.md",
                "a".repeat(1024),
            )
            write(
                "fixture/lines.md",
                "one\ntwo\nthree\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            // 1024 bytes is exactly the 1KB limit (not over it) and 3 lines is exactly the 3L
            // limit, so neither file should be flagged.
            assertDoesNotThrow {
                runCheck(
                    budgets,
                    listOf("fixture/kb.md", "fixture/lines.md"),
                )
            }
        }

        @Test
        fun `flags a new file that is over budget with no merge-base history`() {
            // ----- Arrange -----
            initRepoOnMain()
            val budgets = "fixture/new.md                  10B"
            write(
                ".gitkeep",
                "",
            )
            commitAll("baseline")
            write(
                "fixture/new.md",
                "12345678901",
            )

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    budgets,
                    listOf("fixture/new.md"),
                )
            }
            exception.message shouldContain "new file over budget"
        }

        @Test
        fun `passes a file that is over budget but unchanged since the merge-base`() {
            // ----- Arrange -----
            initRepoOnMain()
            val budgets = "fixture/tiny.md                 10B"
            write(
                "fixture/tiny.md",
                "12345678901234",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    budgets,
                    listOf("fixture/tiny.md"),
                )
            }
        }

        @Test
        fun `fails a file that is over budget and has grown since the merge-base`() {
            // ----- Arrange -----
            initRepoOnMain()
            val budgets = "fixture/tiny.md                 10B"
            write(
                "fixture/tiny.md",
                "12345678901234",
            )
            commitAll("baseline")
            write(
                "fixture/tiny.md",
                "1234567890123456789",
            )

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    budgets,
                    listOf("fixture/tiny.md"),
                )
            }
            exception.message shouldContain "grew since merge-base"
        }

        @Test
        fun `fails when no merge-base can be resolved against origin-main or main`() {
            // ----- Arrange -----
            git(
                "init",
                "-b",
                "trunk",
            )
            git(
                "config",
                "user.email",
                "test@example.com",
            )
            git(
                "config",
                "user.name",
                "Test",
            )
            val budgets = "fixture/tiny.md                 10B"
            write(
                "fixture/tiny.md",
                "x",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    budgets,
                    listOf("fixture/tiny.md"),
                )
            }
            exception.message shouldContain "cannot resolve a git merge-base"
        }

        @Test
        fun `fails when the Now section grows past its budget since the merge-base`() {
            // ----- Arrange -----
            initRepoOnMain()
            val budgets = "## Now                           3L"
            val oldNow = "# Tracker\n\n## Now\n- a\n\n## Later\n- z\n"
            write(
                "docs/working/tracker.md",
                oldNow,
            )
            commitAll("baseline")
            val grownNow = "# Tracker\n\n## Now\n- a\n- b\n- c\n\n## Later\n- z\n"
            write(
                "docs/working/tracker.md",
                grownNow,
            )

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    budgets,
                    emptyList(),
                    activeContent = "docs/working/tracker.md\n",
                )
            }
            exception.message shouldContain "docs/working/tracker.md ## Now section"
        }

        @Test
        fun `passes when the Now section is over budget but unchanged since the merge-base`() {
            // ----- Arrange -----
            initRepoOnMain()
            val budgets = "## Now                           3L"
            val oldNow = "# Tracker\n\n## Now\n- a\n- b\n- c\n- d\n\n## Later\n- z\n"
            write(
                "docs/working/tracker.md",
                oldNow,
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    budgets,
                    emptyList(),
                    activeContent = "docs/working/tracker.md\n",
                )
            }
        }
    }
}
