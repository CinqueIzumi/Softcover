package nl.rhaydus.softcover.showcase.render

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RenderSlugTest {
    @Nested
    inner class ToSlug {
        @Test
        fun `splits camel case into kebab case`() {
            // ----- Arrange -----
            val label = "SheetScaffold"

            // ----- Act -----
            val result = label.toSlug(fallback = "fallback")

            // ----- Assert -----
            result shouldBe "sheet-scaffold"
        }

        @Test
        fun `collapses punctuation and spaces into single dashes and trims them`() {
            // ----- Arrange -----
            val label = "  Hello,   World!! "

            // ----- Act -----
            val result = label.toSlug(fallback = "fallback")

            // ----- Assert -----
            result shouldBe "hello-world"
        }

        @Test
        fun `returns the fallback for blank input`() {
            // ----- Arrange -----
            val label = "   "

            // ----- Act -----
            val result = label.toSlug(fallback = "fallback")

            // ----- Assert -----
            result shouldBe "fallback"
        }

        @Test
        fun `returns the fallback for input made only of symbols`() {
            // ----- Arrange -----
            val label = "!?-_"

            // ----- Act -----
            val result = label.toSlug(fallback = "fallback")

            // ----- Assert -----
            result shouldBe "fallback"
        }
    }

    @Nested
    inner class UniqueSlugs {
        @Test
        fun `suffixes repeated labels with increasing numbers`() {
            // ----- Arrange -----
            val labels = listOf("Card", "Card", "Card")

            // ----- Act -----
            val result = uniqueSlugs(labels = labels)

            // ----- Assert -----
            result shouldContainExactly listOf("card", "card-2", "card-3")
        }

        @Test
        fun `skips a suffixed slug that a literal label already claims`() {
            // ----- Arrange -----
            val labels = listOf("A", "A-2", "A")

            // ----- Act -----
            val result = uniqueSlugs(labels = labels)

            // ----- Assert -----
            result shouldContainExactly listOf("a", "a-2", "a-3")
        }

        @Test
        fun `falls back to the one-based index for blank labels`() {
            // ----- Arrange -----
            val labels = listOf("Card", " ", "")

            // ----- Act -----
            val result = uniqueSlugs(labels = labels)

            // ----- Assert -----
            result shouldContainExactly listOf("card", "fixture-2", "fixture-3")
        }

        @Test
        fun `treats labels that differ only in case or punctuation as duplicates`() {
            // ----- Arrange -----
            val labels = listOf("Sheet Scaffold", "SheetScaffold", "sheet_scaffold")

            // ----- Act -----
            val result = uniqueSlugs(labels = labels)

            // ----- Assert -----
            result shouldContainExactly listOf("sheet-scaffold", "sheet-scaffold-2", "sheet-scaffold-3")
        }

        @Test
        fun `returns an empty list for no labels`() {
            // ----- Arrange -----
            val labels = emptyList<String>()

            // ----- Act -----
            val result = uniqueSlugs(labels = labels)

            // ----- Assert -----
            result shouldContainExactly emptyList()
        }
    }
}
