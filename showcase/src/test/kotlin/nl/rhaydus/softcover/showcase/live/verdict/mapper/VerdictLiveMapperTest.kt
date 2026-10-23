package nl.rhaydus.softcover.showcase.live.verdict.mapper

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.richtext.RichTextMark
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType
import nl.rhaydus.softcover.showcase.live.verdict.model.VerdictDemo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VerdictLiveMapperTest {
    @Nested
    inner class TogglingMark {
        @Test
        fun `does nothing on a collapsed selection`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.FINISHED).withReview(
                value = TextFieldValue(
                    text = "Hello world",
                    selection = TextRange(4),
                ),
            )

            // ----- Act -----
            val result = model.togglingMark(mark = RichTextMarkType.BOLD)

            // ----- Assert -----
            result shouldBe model
        }

        @Test
        fun `adds the mark over the selected range`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.FINISHED).withReview(
                value = TextFieldValue(
                    text = "Hello world",
                    selection = TextRange(
                        0,
                        5,
                    ),
                ),
            )

            // ----- Act -----
            val result = model.togglingMark(mark = RichTextMarkType.BOLD)

            // ----- Assert -----
            result.sheet.marks shouldContainExactly listOf(
                RichTextMark(
                    start = 0,
                    end = 5,
                    type = RichTextMarkType.BOLD,
                ),
            )
        }

        @Test
        fun `adds the mark over a backwards selection`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.FINISHED).withReview(
                value = TextFieldValue(
                    text = "Hello world",
                    selection = TextRange(
                        5,
                        0,
                    ),
                ),
            )

            // ----- Act -----
            val result = model.togglingMark(mark = RichTextMarkType.ITALIC)

            // ----- Assert -----
            result.sheet.marks shouldContainExactly listOf(
                RichTextMark(
                    start = 0,
                    end = 5,
                    type = RichTextMarkType.ITALIC,
                ),
            )
        }

        @Test
        fun `removes the mark when toggled twice over the same range`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.FINISHED).withReview(
                value = TextFieldValue(
                    text = "Hello world",
                    selection = TextRange(
                        0,
                        5,
                    ),
                ),
            )

            // ----- Act -----
            val result = model
                .togglingMark(mark = RichTextMarkType.BOLD)
                .togglingMark(mark = RichTextMarkType.BOLD)

            // ----- Assert -----
            result.sheet.marks.shouldBeEmpty()
        }

        @Test
        fun `keeps a different mark over the same range when toggling another type`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.FINISHED).withReview(
                value = TextFieldValue(
                    text = "Hello world",
                    selection = TextRange(
                        0,
                        5,
                    ),
                ),
            )

            // ----- Act -----
            val result = model
                .togglingMark(mark = RichTextMarkType.BOLD)
                .togglingMark(mark = RichTextMarkType.ITALIC)

            // ----- Assert -----
            result.sheet.marks.map { it.type } shouldContainExactly listOf(
                RichTextMarkType.BOLD,
                RichTextMarkType.ITALIC,
            )
        }

        @Test
        fun `marks the chip selected after adding the mark`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.FINISHED).withReview(
                value = TextFieldValue(
                    text = "Hello world",
                    selection = TextRange(
                        0,
                        5,
                    ),
                ),
            )

            // ----- Act -----
            val result = model.togglingMark(mark = RichTextMarkType.BOLD)

            // ----- Assert -----
            result.sheet.markChips[RichTextMarkType.BOLD].shouldNotBeNull().selected shouldBe true
            result.sheet.markChips[RichTextMarkType.ITALIC].shouldNotBeNull().selected shouldBe false
        }
    }

    @Nested
    inner class WithReview {
        @Test
        fun `keeps marks that still fit the new text`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.EDIT)
            val text = model.sheet.review.text

            // ----- Act -----
            val result = model.withReview(value = TextFieldValue(text = text))

            // ----- Assert -----
            result.sheet.marks shouldBe model.sheet.marks
        }

        @Test
        fun `clips a mark that runs past the end and drops one that starts beyond it`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.EDIT)

            // ----- Act -----
            val result = model.withReview(value = TextFieldValue(text = "Hello world"))

            // ----- Assert -----
            result.sheet.marks shouldContainExactly listOf(
                RichTextMark(
                    start = 2,
                    end = 11,
                    type = RichTextMarkType.ITALIC,
                ),
            )
        }

        @Test
        fun `drops a mark that is left empty by the new text`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.EDIT)

            // ----- Act -----
            val result = model.withReview(value = TextFieldValue(text = "Hi"))

            // ----- Assert -----
            result.sheet.marks.shouldBeEmpty()
        }

        @Test
        fun `disables the toolbar chips while the selection is collapsed`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.FINISHED)

            // ----- Act -----
            val result = model.withReview(
                value = TextFieldValue(
                    text = "Hello world",
                    selection = TextRange(3),
                ),
            )

            // ----- Assert -----
            result.sheet.markChips.values.map { it.interaction }.toSet() shouldBe setOf(ChipInteraction.Disabled)
        }

        @Test
        fun `enables the toolbar chips while text is selected`() {
            // ----- Arrange -----
            val model = verdictDemoModel(demo = VerdictDemo.FINISHED)

            // ----- Act -----
            val result = model.withReview(
                value = TextFieldValue(
                    text = "Hello world",
                    selection = TextRange(
                        0,
                        5,
                    ),
                ),
            )

            // ----- Assert -----
            result.sheet.markChips.values.map { it.interaction }.toSet() shouldBe setOf(ChipInteraction.Clickable)
        }
    }
}
