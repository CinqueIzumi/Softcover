package nl.rhaydus.softcover.core.uibinding.verdict

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.richtext.RichTextMark
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.richtext.editorBufferToDocument
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VerdictDraftReducerTest {
    private fun freshModel(): VerdictSheetUiModel = PreviewData.baseBook.toVerdictSheetUiModel(
        context = VerdictSheetContext.EDIT,
        review = null,
        cover = null,
    )

    private fun VerdictSheetUiModel.typed(
        text: String,
        selection: TextRange,
        marks: List<RichTextMark> = emptyList(),
    ): VerdictSheetUiModel = copy(
        review = TextFieldValue(
            text = text,
            selection = selection,
        ),
        marks = persistentListOf(*marks.toTypedArray()),
    ).withToolbarState()

    @Nested
    inner class RatingAndSpoilers {
        @Test
        fun `RatingChanged replaces the rating`() {
            // ----- Arrange -----
            val model = freshModel()

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.RatingChanged(rating = 4.5))

            // ----- Assert -----
            result.rating shouldBe 4.5
        }

        @Test
        fun `RatingChanged with null clears the rating`() {
            // ----- Arrange -----
            val model = freshModel().reduce(VerdictDraftEvent.RatingChanged(rating = 3.0))

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.RatingChanged(rating = null))

            // ----- Assert -----
            result.rating shouldBe null
        }

        @Test
        fun `SpoilersToggled sets checked and keeps the rest of the toggle row`() {
            // ----- Arrange -----
            val model = freshModel()

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.SpoilersToggled(checked = true))

            // ----- Assert -----
            result.spoilers shouldBe model.spoilers.copy(checked = true)
        }

        @Test
        fun `SpoilersToggled to false unchecks a checked toggle`() {
            // ----- Arrange -----
            val model = freshModel().reduce(VerdictDraftEvent.SpoilersToggled(checked = true))

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.SpoilersToggled(checked = false))

            // ----- Assert -----
            result.spoilers.checked shouldBe false
        }
    }

    @Nested
    inner class ReviewEdits {
        @Test
        fun `typing before a mark shifts the mark right by the inserted length`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "hello world",
                selection = TextRange(0),
                marks = listOf(
                    RichTextMark(
                        start = 6,
                        end = 11,
                        type = RichTextMarkType.BOLD,
                    ),
                ),
            )
            val edited = TextFieldValue(
                text = "XXhello world",
                selection = TextRange(2),
            )

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.ReviewEdited(value = edited))

            // ----- Assert -----
            result.review shouldBe edited
            result.marks shouldBe persistentListOf(
                RichTextMark(
                    start = 8,
                    end = 13,
                    type = RichTextMarkType.BOLD,
                ),
            )
        }

        @Test
        fun `deleting before a mark shifts the mark left`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "hello world",
                selection = TextRange(0),
                marks = listOf(
                    RichTextMark(
                        start = 6,
                        end = 11,
                        type = RichTextMarkType.ITALIC,
                    ),
                ),
            )
            val edited = TextFieldValue(
                text = "llo world",
                selection = TextRange(0),
            )

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.ReviewEdited(value = edited))

            // ----- Assert -----
            result.marks shouldBe persistentListOf(
                RichTextMark(
                    start = 4,
                    end = 9,
                    type = RichTextMarkType.ITALIC,
                ),
            )
        }

        @Test
        fun `a selection-only change leaves the marks untouched`() {
            // ----- Arrange -----
            val marks = listOf(
                RichTextMark(
                    start = 0,
                    end = 5,
                    type = RichTextMarkType.BOLD,
                ),
            )
            val model = freshModel().typed(
                text = "hello world",
                selection = TextRange(0),
                marks = marks,
            )
            val moved = TextFieldValue(
                text = "hello world",
                selection = TextRange(
                    start = 1,
                    end = 3,
                ),
            )

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.ReviewEdited(value = moved))

            // ----- Assert -----
            result.marks shouldBe persistentListOf(*marks.toTypedArray())
            result.review shouldBe moved
        }

        @Test
        fun `a ranged selection enables the toolbar chips and a collapsed one disables them`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "hello",
                selection = TextRange(0),
            )
            val ranged = TextFieldValue(
                text = "hello",
                selection = TextRange(
                    start = 0,
                    end = 3,
                ),
            )
            val collapsed = TextFieldValue(
                text = "hello",
                selection = TextRange(2),
            )

            // ----- Act -----
            val withRange = model.reduce(VerdictDraftEvent.ReviewEdited(value = ranged))
            val withoutRange = withRange.reduce(VerdictDraftEvent.ReviewEdited(value = collapsed))

            // ----- Assert -----
            withRange.markChips.values.all { it.interaction == ChipInteraction.Clickable } shouldBe true
            withoutRange.markChips.values.all { it.interaction == ChipInteraction.Disabled } shouldBe true
        }

        @Test
        fun `a chip is selected when the selection lies inside its mark`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "hello world",
                selection = TextRange(0),
                marks = listOf(
                    RichTextMark(
                        start = 0,
                        end = 5,
                        type = RichTextMarkType.BOLD,
                    ),
                ),
            )
            val inside = TextFieldValue(
                text = "hello world",
                selection = TextRange(
                    start = 1,
                    end = 4,
                ),
            )

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.ReviewEdited(value = inside))

            // ----- Assert -----
            result.markChips.getValue(RichTextMarkType.BOLD).selected shouldBe true
            result.markChips.getValue(RichTextMarkType.ITALIC).selected shouldBe false
        }
    }

    @Nested
    inner class MarkToggling {
        @Test
        fun `toggling over a ranged selection adds the mark and selects its chip`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "hello world",
                selection = TextRange(
                    start = 0,
                    end = 5,
                ),
            )

            // ----- Act -----
            val result = model.reduce(VerdictDraftEvent.MarkToggled(mark = RichTextMarkType.BOLD))

            // ----- Assert -----
            result.marks shouldBe persistentListOf(
                RichTextMark(
                    start = 0,
                    end = 5,
                    type = RichTextMarkType.BOLD,
                ),
            )
            result.markChips.getValue(RichTextMarkType.BOLD).selected shouldBe true
        }

        @Test
        fun `toggling twice removes the mark again`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "hello world",
                selection = TextRange(
                    start = 0,
                    end = 5,
                ),
            )

            // ----- Act -----
            val result = model
                .reduce(VerdictDraftEvent.MarkToggled(mark = RichTextMarkType.BOLD))
                .reduce(VerdictDraftEvent.MarkToggled(mark = RichTextMarkType.BOLD))

            // ----- Assert -----
            result.marks shouldBe persistentListOf()
            result.markChips.getValue(RichTextMarkType.BOLD).selected shouldBe false
        }
    }

    @Nested
    inner class CarryingDraftOf {
        @Test
        fun `a null previous returns the model unchanged`() {
            // ----- Arrange -----
            val model = freshModel()

            // ----- Act -----
            val result = model.carryingDraftOf(previous = null)

            // ----- Assert -----
            result shouldBe model
        }

        @Test
        fun `the previous draft wins while the rebuilt scaffold is kept`() {
            // ----- Arrange -----
            val previous = freshModel()
                .typed(
                    text = "my take",
                    selection = TextRange(
                        start = 0,
                        end = 2,
                    ),
                    marks = listOf(
                        RichTextMark(
                            start = 0,
                            end = 2,
                            type = RichTextMarkType.SPOILER,
                        ),
                    ),
                )
                .reduce(VerdictDraftEvent.RatingChanged(rating = 2.5))
                .reduce(VerdictDraftEvent.SpoilersToggled(checked = true))
            val rebuilt = PreviewData.baseBook.toVerdictSheetUiModel(
                context = VerdictSheetContext.FINISHED,
                review = null,
                cover = null,
            )

            // ----- Act -----
            val result = rebuilt.carryingDraftOf(previous = previous)

            // ----- Assert -----
            result.scaffold shouldBe rebuilt.scaffold
            result.rating shouldBe 2.5
            result.review shouldBe previous.review
            result.marks shouldBe previous.marks
            result.spoilers shouldBe rebuilt.spoilers.copy(checked = true)
            result.markChips.getValue(RichTextMarkType.SPOILER).selected shouldBe true
        }
    }

    @Nested
    inner class DraftReview {
        @Test
        fun `draftReview turns the text and marks into the persisted document`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "hello world",
                selection = TextRange(0),
                marks = listOf(
                    RichTextMark(
                        start = 0,
                        end = 5,
                        type = RichTextMarkType.BOLD,
                    ),
                ),
            )

            // ----- Act -----
            val result = model.draftReview()

            // ----- Assert -----
            result shouldBe editorBufferToDocument(
                text = "hello world",
                marks = model.marks,
            )
        }

        @Test
        fun `an empty draft yields the empty rich text`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "",
                selection = TextRange(0),
            )

            // ----- Act -----
            val result = model.draftReview()

            // ----- Assert -----
            result shouldBe RichTextUiModel.EMPTY
        }

        @Test
        fun `a whitespace-only draft yields the empty rich text`() {
            // ----- Arrange -----
            val model = freshModel().typed(
                text = "  ",
                selection = TextRange(2),
            )

            // ----- Act -----
            val result = model.draftReview()

            // ----- Assert -----
            result shouldBe RichTextUiModel.EMPTY
        }
    }
}
