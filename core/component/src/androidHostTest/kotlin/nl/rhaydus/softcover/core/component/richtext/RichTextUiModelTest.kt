package nl.rhaydus.softcover.core.component.richtext

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RichTextUiModelTest {
    private fun model(vararg paragraphs: List<String>) = RichTextUiModel(
        paragraphs = persistentListOf(
            *paragraphs.map { texts ->
                RichTextParagraph(runs = persistentListOf(*texts.map { RichTextRun(text = it) }.toTypedArray()))
            }.toTypedArray(),
        ),
    )

    @Nested
    inner class IsBlank {
        @Test
        fun `is true for no paragraphs`() {
            // ----- Arrange -----
            val input = RichTextUiModel.EMPTY

            // ----- Act -----
            val result = input.isBlank()

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true for a paragraph without runs`() {
            // ----- Arrange -----
            val input = model(emptyList())

            // ----- Act -----
            val result = input.isBlank()

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true when every run is empty or whitespace`() {
            // ----- Arrange -----
            val input = model(
                listOf("", "  "),
                listOf("\n\t"),
            )

            // ----- Act -----
            val result = input.isBlank()

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when a single run has text`() {
            // ----- Arrange -----
            val input = model(listOf("Hello"))

            // ----- Act -----
            val result = input.isBlank()

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when only a later paragraph has text`() {
            // ----- Arrange -----
            val input = model(
                listOf(" "),
                listOf("", "x"),
            )

            // ----- Act -----
            val result = input.isBlank()

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false for every preview`() {
            // ----- Arrange -----
            val previews = RichTextUiModel.previews.values

            // ----- Act -----
            val results = previews.map { it.isBlank() }

            // ----- Assert -----
            results.all { it.not() } shouldBe true
        }
    }
}
