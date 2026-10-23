package nl.rhaydus.softcover.core.designsystem.preset

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ContentCheckTest {
    @TempDir
    lateinit var designDir: File

    private val tokens = setOf("space-md", "on-surface", "surface-container")

    private fun brandBook(text: String) = designDir.resolve("README.md").writeText(text)

    private fun bundleCss(text: String) {
        val file = designDir.resolve("components/bundle.css")
        file.parentFile.mkdirs()
        file.writeText(text)
    }

    @Nested
    inner class Problems {
        @Test
        fun `passes when neither file exists`() {
            // ----- Arrange -----
            // designDir is empty.

            // ----- Act -----
            val result = ContentCheck.problems(
                designDir,
                tokens,
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `passes when every reference is a known token`() {
            // ----- Arrange -----
            brandBook("Pad with `space-md` on `on-surface`.")
            bundleCss(".a { padding: var(--space-md); color: var(--on-surface); }")

            // ----- Act -----
            val result = ContentCheck.problems(
                designDir,
                tokens,
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `fails a mistyped token in the brand book`() {
            // ----- Arrange -----
            brandBook("ok\nPad with `space-mdd`.")

            // ----- Act -----
            val result = ContentCheck.problems(
                designDir,
                tokens,
            )

            // ----- Assert -----
            result shouldHaveSize 1
            result.single() shouldContain "README.md:2"
            result.single() shouldContain "space-mdd"
        }

        @Test
        fun `ignores a backticked code word that is not token-like`() {
            // ----- Arrange -----
            brandBook("Call `build-thing` or `Modifier`.")

            // ----- Act -----
            val result = ContentCheck.problems(
                designDir,
                tokens,
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `fails an unknown css variable`() {
            // ----- Arrange -----
            bundleCss(".a { margin: var(--x); }")

            // ----- Act -----
            val result = ContentCheck.problems(
                designDir,
                tokens,
            )

            // ----- Assert -----
            result shouldHaveSize 1
            result.single() shouldContain "bundle.css:1"
            result.single() shouldContain "x"
        }

        @Test
        fun `fails a raw hex colour in the stylesheet`() {
            // ----- Arrange -----
            bundleCss(".a { color: #fff; }")

            // ----- Act -----
            val result = ContentCheck.problems(
                designDir,
                tokens,
            )

            // ----- Assert -----
            result.map { it.contains("raw colour #fff") } shouldBe listOf(true)
        }
    }
}
