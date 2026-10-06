package nl.rhaydus.softcover.core.uibinding.verdict

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VerdictSheetMapperTest {
    private val book = PreviewData.baseBook
    private val review = RichTextUiModel.previews.getValue("Plain prose")
    private val cover = CoverUiModel(
        source = null,
        coverlessTitle = book.title,
        variant = CoverVariant.Jacket,
    )

    @Nested
    inner class ContextCopy {
        @Test
        fun `FINISHED writes the finished prompt copy naming the book`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.FINISHED
            val noReview: RichTextUiModel? = null
            val noCover: CoverUiModel? = null

            // ----- Act -----
            val result = book.toVerdictSheetUiModel(
                context,
                review = noReview,
                cover = noCover,
            )

            // ----- Assert -----
            result.scaffold.header.section.headlineText shouldBe "You finished ${book.title}."
            result.scaffold.header.section.description.shouldNotBeNull()
            result.scaffold.dismissLabel shouldBe "Not now"
            result.scaffold.footer?.primary?.label shouldBe "Save"
        }

        @Test
        fun `EDIT writes the edit copy naming the book with no description`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.EDIT
            val noReview: RichTextUiModel? = null
            val noCover: CoverUiModel? = null

            // ----- Act -----
            val result = book.toVerdictSheetUiModel(
                context,
                review = noReview,
                cover = noCover,
            )

            // ----- Assert -----
            result.scaffold.header.section.headlineText shouldBe "Your take on ${book.title}."
            result.scaffold.header.section.description.shouldBeNull()
            result.scaffold.dismissLabel shouldBe "Cancel"
            result.scaffold.footer?.primary?.label shouldBe "Save changes"
        }
    }

    @Nested
    inner class DeleteSecondary {
        @Test
        fun `EDIT with a non-blank review offers Delete`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.EDIT
            val noCover: CoverUiModel? = null

            // ----- Act -----
            val result = book.toVerdictSheetUiModel(
                context,
                review = review,
                cover = noCover,
            )

            // ----- Assert -----
            result.scaffold.footer?.secondary.shouldNotBeNull()
        }

        @Test
        fun `FINISHED never offers Delete even with a review`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.FINISHED
            val noCover: CoverUiModel? = null

            // ----- Act -----
            val result = book.toVerdictSheetUiModel(
                context,
                review = review,
                cover = noCover,
            )

            // ----- Assert -----
            result.scaffold.footer?.secondary.shouldBeNull()
        }

        @Test
        fun `EDIT with a null review offers no Delete`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.EDIT
            val noReview: RichTextUiModel? = null
            val noCover: CoverUiModel? = null

            // ----- Act -----
            val result = book.toVerdictSheetUiModel(
                context,
                review = noReview,
                cover = noCover,
            )

            // ----- Assert -----
            result.scaffold.footer?.secondary.shouldBeNull()
        }

        @Test
        fun `EDIT with a blank review offers no Delete`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.EDIT
            val review = RichTextUiModel(paragraphs = persistentListOf())
            val cover = null

            // ----- Act -----
            val result = book.toVerdictSheetUiModel(
                context = context,
                review = review,
                cover = cover,
            )

            // ----- Assert -----
            result.scaffold.footer?.secondary.shouldBeNull()
        }
    }

    @Nested
    inner class InitialVerdict {
        @Test
        fun `positive rating seeds initialRating`() {
            // ----- Arrange -----
            val rated = book.copy(userBook = book.userBook?.copy(rating = 4.5))

            // ----- Act -----
            val result = rated.toVerdictSheetUiModel(
                VerdictSheetContext.EDIT,
                review = null,
                cover = null,
            )

            // ----- Assert -----
            result.initialRating shouldBe 4.5
        }

        @Test
        fun `zero rating is treated as unrated`() {
            // ----- Arrange -----
            val zero = book.copy(userBook = book.userBook?.copy(rating = 0.0))

            // ----- Act -----
            val result = zero.toVerdictSheetUiModel(
                VerdictSheetContext.EDIT,
                review = null,
                cover = null,
            )

            // ----- Assert -----
            result.initialRating.shouldBeNull()
        }

        @Test
        fun `null rating and missing userBook leave initialRating null`() {
            // ----- Arrange -----
            val noUserBook = book.copy(userBook = null)

            // ----- Act -----
            val result = noUserBook.toVerdictSheetUiModel(
                VerdictSheetContext.EDIT,
                review = null,
                cover = null,
            )

            // ----- Assert -----
            result.initialRating.shouldBeNull()
            result.spoilers.checked shouldBe false
        }

        @Test
        fun `spoilers flag is carried from the user book`() {
            // ----- Arrange -----
            val spoilers = book.copy(userBook = book.userBook?.copy(reviewHasSpoilers = true))

            // ----- Act -----
            val result = spoilers.toVerdictSheetUiModel(
                VerdictSheetContext.EDIT,
                review = null,
                cover = null,
            )

            // ----- Assert -----
            result.spoilers.checked shouldBe true
        }

        @Test
        fun `null review seeds an empty review and a given review is passed through`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.EDIT
            val noReview: RichTextUiModel? = null

            // ----- Act -----
            val empty = book.toVerdictSheetUiModel(
                context,
                review = noReview,
                cover = null,
            )
            val given = book.toVerdictSheetUiModel(
                context,
                review,
                cover = null,
            )

            // ----- Assert -----
            empty.initialReview shouldBe RichTextUiModel.EMPTY
            given.initialReview shouldBe review
        }
    }

    @Nested
    inner class Trailing {
        @Test
        fun `cover becomes the header jacket`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.FINISHED
            val noReview: RichTextUiModel? = null

            // ----- Act -----
            val result = book.toVerdictSheetUiModel(
                context,
                review = noReview,
                cover = cover,
            )

            // ----- Assert -----
            result.scaffold.header.trailing shouldBe SheetHeaderTrailing.Jacket(cover = cover)
        }

        @Test
        fun `null cover leaves the header without trailing`() {
            // ----- Arrange -----
            val context = VerdictSheetContext.FINISHED
            val noReview: RichTextUiModel? = null
            val noCover: CoverUiModel? = null

            // ----- Act -----
            val result = book.toVerdictSheetUiModel(
                context,
                review = noReview,
                cover = noCover,
            )

            // ----- Assert -----
            result.scaffold.header.trailing.shouldBeNull()
        }
    }
}
