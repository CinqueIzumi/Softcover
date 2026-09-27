import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

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
        configRelativePaths: List<String> = emptyList(),
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
        task.configFiles.setFrom(configRelativePaths.map { repoRoot.resolve(it) })
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

        @Test
        fun `flags a permanent doc that cites a plan directory`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "docs/reference/foo.md",
                "# Foo\n\nSee docs/working/275-foo/step-1.md for details.\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    listOf("docs/reference/foo.md"),
                )
            }
            exception.message shouldContain "cites a plan directory"
        }

        @Test
        fun `flags a permanent doc that cites a plan step`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "docs/reference/foo.md",
                "# Foo\n\nThis implements S4-5b.\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    listOf("docs/reference/foo.md"),
                )
            }
            exception.message shouldContain "cites a plan step"
        }

        @Test
        fun `passes a permanent doc with a step-like token that is not a step id`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "docs/reference/foo.md",
                "# Foo\n\nThis references S3 elsewhere.\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    "",
                    listOf("docs/reference/foo.md"),
                )
            }
        }

        @Test
        fun `passes a doc under docs-working that cites a plan directory`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "docs/working/notes.md",
                "See docs/working/275-foo/step-1.md for details.\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    "",
                    listOf("docs/working/notes.md"),
                )
            }
        }

        @Test
        fun `excludes vendored rhaydus docs from citation checks`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "docs/rhaydus/0.3.1/architecture.md",
                "This follows D3 in docs/working/275-foo/step-1.md.\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    "",
                    listOf("docs/rhaydus/0.3.1/architecture.md"),
                )
            }
        }

        @Test
        fun `passes a permanent doc that references the top-level ACTIVE tracker`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "docs/reference/foo.md",
                "# Foo\n\nSee docs/working/ACTIVE.md for state.\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    "",
                    listOf("docs/reference/foo.md"),
                )
            }
        }
    }

    @Nested
    inner class ConfigFiles {
        @Test
        fun `flags a yml comment citing a plan directory`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/detekt/detekt.yml",
                "rule:\n  active: true  # see docs/working/275-foo/notes.md\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/detekt/detekt.yml"),
                )
            }
            exception.message shouldContain "comment cites docs/working"
        }

        @Test
        fun `flags a yml comment citing a decision number`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/detekt/detekt.yml",
                "rule:\n  active: true  # per D17\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/detekt/detekt.yml"),
                )
            }
            exception.message shouldContain "comment cites a decision number"
        }

        @Test
        fun `flags a yml comment citing a step id`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/detekt/detekt.yml",
                "rule:\n  active: true  # implements S4-1\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/detekt/detekt.yml"),
                )
            }
            exception.message shouldContain "comment cites a plan step"
        }

        @Test
        fun `flags a kts line comment citing a step id`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val x = 1 // implements S4-1\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
            exception.message shouldContain "comment cites a plan step"
        }

        @Test
        fun `flags a kts block comment citing a decision number`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val x = 1 /* per D17 */\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
            exception.message shouldContain "comment cites a decision number"
        }

        @Test
        fun `passes a kts string literal containing a decision-number-like token in a url`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val doc = \"https://example.com/D17\"\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            // The token sits inside a string literal with no trailing comment on the line, so
            // nothing on it is a plan citation to flag.
            assertDoesNotThrow {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
        }

        @Test
        fun `passes a kts string literal containing a step-id-like token`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val note = \"see S4-1 for context\"\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
        }

        @Test
        fun `flags a kts line with a string literal followed by a real trailing comment`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val label = \"https://example.com/x\" // implements S4-1\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            // The string literal itself is inert; the real "//" after it is what should trip the check.
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
            exception.message shouldContain "comment cites a plan step"
        }

        @Test
        fun `passes a kts string with an escaped quote that does not close the literal early`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val s = \"a \\\" // D17\"\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            // The escaped quote must not end the string literal early, or the text after it would
            // be misread as a real trailing comment.
            assertDoesNotThrow {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
        }

        @Test
        fun `flags a kts string that closes before a real trailing comment despite an escaped quote`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val s = \"a \\\"\" // D17\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            // The escaped quote keeps the string open, but the very next unescaped quote closes it,
            // leaving the trailing comment for real this time.
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
            exception.message shouldContain "comment cites a decision number"
        }

        @Test
        fun `passes a yml hash inside a double-quoted value`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/detekt/detekt.yml",
                "key: \"a # D17\"\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/detekt/detekt.yml"),
                )
            }
        }

        @Test
        fun `passes a yml hash inside a single-quoted value with a doubled escaped quote`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/detekt/detekt.yml",
                "key: 'it''s # D17'\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/detekt/detekt.yml"),
                )
            }
        }

        @Test
        fun `passes a yml hash with no whitespace before it`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/detekt/detekt.yml",
                "x: a#D17\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            // A "#" only opens a YAML comment at line start or after a space or tab.
            assertDoesNotThrow {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/detekt/detekt.yml"),
                )
            }
        }

        @Test
        fun `flags a yml hash after a closed double-quoted value`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/detekt/detekt.yml",
                "x: \"a\" # D17\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/detekt/detekt.yml"),
                )
            }
            exception.message shouldContain "comment cites a decision number"
        }

        @Test
        fun `flags a kts trailing comment after a char literal holding a double quote`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val c = '\"' // S4-1\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            // Without char-literal tracking, the quote inside '"' would be misread as a string
            // opener that swallows the rest of the line, including the real "//" comment.
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
            exception.message shouldContain "comment cites a plan step"
        }

        @Test
        fun `flags a kts trailing comment after a char literal holding an escaped quote`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "build.gradle.kts",
                "val c = '\\'' // D17\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            val exception = shouldThrow<GradleException> {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("build.gradle.kts"),
                )
            }
            exception.message shouldContain "comment cites a decision number"
        }

        @Test
        fun `passes a config file with an extension the scan does not recognise`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/notes.txt",
                "Plan D17 lives here\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            assertDoesNotThrow {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/notes.txt"),
                )
            }
        }

        @Test
        fun `passes a config kt file even with a real trailing comment, since kt is not scanned`() {
            // ----- Arrange -----
            initRepoOnMain()
            write(
                "config/tool/Extra.kt",
                "val x = 1 // implements S4-1\n",
            )
            commitAll("baseline")

            // ----- Act & Assert -----
            // Only .yml, .yaml and .kts are dispatched to a comment extractor; a .kt file under
            // config/ is part of the configFiles input set but falls through unscanned.
            assertDoesNotThrow {
                runCheck(
                    "",
                    emptyList(),
                    configRelativePaths = listOf("config/tool/Extra.kt"),
                )
            }
        }
    }
}
