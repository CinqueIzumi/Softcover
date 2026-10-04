package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HiddenSuggestionsBookCardMapperTest {
    private val cover = mockk<CoverUiModel>()

    private fun BookCardUiModel.row(): BookCardVariant.Row =
        variant.shouldBeInstanceOf<BookCardVariant.Row>()

    private fun hiddenBook(
        bookId: Int = 5,
        title: String? = "Piranesi",
        authorText: String? = null,
    ): DismissedSeriesBook = DismissedSeriesBook(
        bookId = bookId,
        title = title,
        coverUrl = null,
        authorText = authorText,
        seriesName = null,
        seriesId = null,
        seriesPosition = null,
    )

    private fun hiddenSeries(
        seriesId: Int = 8,
        seriesName: String? = "Earthsea",
        authorText: String? = null,
        bookCount: Int? = null,
    ): DismissedSeries = DismissedSeries(
        seriesId = seriesId,
        seriesName = seriesName,
        coverUrl = null,
        authorText = authorText,
        bookCount = bookCount,
    )

    private fun BookCardUiModel.captionParts(): List<String> = row().text.supportingLines.single()
        .map { it.shouldBeInstanceOf<BookCardLinePart.Text>().text }

    private fun BookCardUiModel.caption(): String {
        val line = row().text.supportingLines.single()

        return line.single().shouldBeInstanceOf<BookCardLinePart.Text>().text
    }

    @Nested
    inner class ToHiddenBookCard {
        @Test
        fun `builds a compact row keyed by book id with a cover lead and a Restore trailing`() {
            // ----- Arrange -----
            val input = hiddenBook(bookId = 5)

            // ----- Act -----
            val card = input.toHiddenBookCard(cover = cover)

            // ----- Assert -----
            card.key shouldBe BookCardKey.Book(bookId = 5)
            card.row().size shouldBe BookCardVariant.Row.Size.Compact
            card.row().lead shouldBe BookCardLead.Cover(cover = BookCardCover(model = cover))
            card.row().trailing shouldBe BookCardTrailing.Restore
        }

        @Test
        fun `uses the title and falls back to Hidden book when it is missing`() {
            // ----- Arrange -----
            val titledInput = hiddenBook(title = "Piranesi")
            val untitledInput = hiddenBook(title = null)

            // ----- Act -----
            val titled = titledInput.toHiddenBookCard(cover = cover)
            val untitled = untitledInput.toHiddenBookCard(cover = cover)

            // ----- Assert -----
            titled.row().text.title shouldBe "Piranesi"
            untitled.row().text.title shouldBe "Hidden book"
        }

        @Test
        fun `prefixes a non-blank author with By`() {
            // ----- Arrange -----
            val input = hiddenBook(authorText = "Susanna Clarke")

            // ----- Act -----
            val card = input.toHiddenBookCard(cover = cover)

            // ----- Assert -----
            card.row().text.byline shouldBe "By Susanna Clarke"
        }

        @Test
        fun `drops the byline for a null or blank author`() {
            // ----- Arrange -----
            val missingInput = hiddenBook(authorText = null)
            val blankInput = hiddenBook(authorText = "  ")

            // ----- Act -----
            val missing = missingInput.toHiddenBookCard(cover = cover)
            val blank = blankInput.toHiddenBookCard(cover = cover)

            // ----- Assert -----
            missing.row().text.byline.shouldBeNull()
            blank.row().text.byline.shouldBeNull()
        }
    }

    @Nested
    inner class ToHiddenSeriesCard {
        @Test
        fun `builds a compact row keyed by series id with a series stack lead and a Restore trailing`() {
            // ----- Arrange -----
            val input = hiddenSeries(seriesId = 8)

            // ----- Act -----
            val card = input.toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.key shouldBe BookCardKey.Series(seriesId = 8)
            card.row().size shouldBe BookCardVariant.Row.Size.Compact
            card.row().lead shouldBe BookCardLead.SeriesStack(front = cover)
            card.row().trailing shouldBe BookCardTrailing.Restore
        }

        @Test
        fun `uses the series name and falls back to Hidden series when it is missing`() {
            // ----- Arrange -----
            val namedInput = hiddenSeries(seriesName = "Earthsea")
            val unnamedInput = hiddenSeries(seriesName = null)

            // ----- Act -----
            val named = namedInput.toHiddenSeriesCard(cover = cover)
            val unnamed = unnamedInput.toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            named.row().text.title shouldBe "Earthsea"
            unnamed.row().text.title shouldBe "Hidden series"
        }

        @Test
        fun `emits the author and the book count as separate parts`() {
            // ----- Arrange -----
            val input = hiddenSeries(
                authorText = "Le Guin",
                bookCount = 3,
            )

            // ----- Act -----
            val card = input.toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.captionParts() shouldBe listOf("Le Guin", "3 books")
        }

        @Test
        fun `singularises the book count of one`() {
            // ----- Arrange -----
            val input = hiddenSeries(
                authorText = "Le Guin",
                bookCount = 1,
            )

            // ----- Act -----
            val card = input.toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.captionParts() shouldBe listOf("Le Guin", "1 book")
        }

        @Test
        fun `shows only the author when the count is missing`() {
            // ----- Arrange -----
            val input = hiddenSeries(
                authorText = "Le Guin",
                bookCount = null,
            )

            // ----- Act -----
            val card = input.toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.caption() shouldBe "Le Guin"
        }

        @Test
        fun `shows only the count when the author is missing`() {
            // ----- Arrange -----
            val input = hiddenSeries(
                authorText = null,
                bookCount = 4,
            )

            // ----- Act -----
            val card = input.toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.caption() shouldBe "4 books"
        }

        @Test
        fun `treats a blank author as missing`() {
            // ----- Arrange -----
            val withCountInput = hiddenSeries(
                authorText = " ",
                bookCount = 2,
            )
            val withoutCountInput = hiddenSeries(
                authorText = " ",
                bookCount = null,
            )

            // ----- Act -----
            val withCount = withCountInput.toHiddenSeriesCard(cover = cover)
            val withoutCount = withoutCountInput.toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            withCount.caption() shouldBe "2 books"
            withoutCount.caption() shouldBe "Series"
        }

        @Test
        fun `falls back to Series when neither author nor count is known`() {
            // ----- Arrange -----
            val input = hiddenSeries(
                authorText = null,
                bookCount = null,
            )

            // ----- Act -----
            val card = input.toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.caption() shouldBe "Series"
        }
    }
}
