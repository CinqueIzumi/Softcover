package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
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

    private fun BookCardUiModel.caption(): String {
        val line = row().text.supportingLines.single()

        return line.parts.single().shouldBeInstanceOf<BookCardLinePart.Text>().text
    }

    @Nested
    inner class ToHiddenBookCard {
        @Test
        fun `builds a compact row keyed by book id with a cover lead and a Restore trailing`() {
            // ----- Act -----
            val card = hiddenBook(bookId = 5).toHiddenBookCard(cover = cover)

            // ----- Assert -----
            card.key shouldBe BookCardKey.Book(bookId = 5)
            card.row().size shouldBe BookCardVariant.Row.Size.Compact
            card.row().lead shouldBe BookCardLead.Cover(model = cover)
            card.row().trailing shouldBe BookCardTrailing.Restore(label = "Restore")
        }

        @Test
        fun `has no lead when there is no cover`() {
            // ----- Act -----
            val card = hiddenBook().toHiddenBookCard(cover = null)

            // ----- Assert -----
            card.row().lead.shouldBeNull()
        }

        @Test
        fun `uses the title and falls back to Hidden book when it is missing`() {
            // ----- Act -----
            val titled = hiddenBook(title = "Piranesi").toHiddenBookCard(cover = cover)
            val untitled = hiddenBook(title = null).toHiddenBookCard(cover = cover)

            // ----- Assert -----
            titled.row().text.title shouldBe "Piranesi"
            untitled.row().text.title shouldBe "Hidden book"
        }

        @Test
        fun `prefixes a non-blank author with By`() {
            // ----- Act -----
            val card = hiddenBook(authorText = "Susanna Clarke").toHiddenBookCard(cover = cover)

            // ----- Assert -----
            card.row().text.byline shouldBe "By Susanna Clarke"
        }

        @Test
        fun `drops the byline for a null or blank author`() {
            // ----- Act -----
            val missing = hiddenBook(authorText = null).toHiddenBookCard(cover = cover)
            val blank = hiddenBook(authorText = "  ").toHiddenBookCard(cover = cover)

            // ----- Assert -----
            missing.row().text.byline.shouldBeNull()
            blank.row().text.byline.shouldBeNull()
        }
    }

    @Nested
    inner class ToHiddenSeriesCard {
        @Test
        fun `builds a compact row keyed by series id with a series stack lead and a Restore trailing`() {
            // ----- Act -----
            val card = hiddenSeries(seriesId = 8).toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.key shouldBe BookCardKey.Series(seriesId = 8)
            card.row().size shouldBe BookCardVariant.Row.Size.Compact
            card.row().lead shouldBe BookCardLead.SeriesStack(front = cover)
            card.row().trailing shouldBe BookCardTrailing.Restore(label = "Restore")
        }

        @Test
        fun `keeps the series stack lead when there is no cover`() {
            // ----- Act -----
            val card = hiddenSeries().toHiddenSeriesCard(cover = null)

            // ----- Assert -----
            card.row().lead shouldBe BookCardLead.SeriesStack(front = null)
        }

        @Test
        fun `uses the series name and falls back to Hidden series when it is missing`() {
            // ----- Act -----
            val named = hiddenSeries(seriesName = "Earthsea").toHiddenSeriesCard(cover = cover)
            val unnamed = hiddenSeries(seriesName = null).toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            named.row().text.title shouldBe "Earthsea"
            unnamed.row().text.title shouldBe "Hidden series"
        }

        @Test
        fun `joins the author and the book count in the caption`() {
            // ----- Act -----
            val card = hiddenSeries(
                authorText = "Le Guin",
                bookCount = 3,
            ).toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.caption() shouldBe "Le Guin · 3 books"
        }

        @Test
        fun `singularises the book count of one`() {
            // ----- Act -----
            val card = hiddenSeries(
                authorText = "Le Guin",
                bookCount = 1,
            ).toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.caption() shouldBe "Le Guin · 1 book"
        }

        @Test
        fun `shows only the author when the count is missing`() {
            // ----- Act -----
            val card = hiddenSeries(
                authorText = "Le Guin",
                bookCount = null,
            ).toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.caption() shouldBe "Le Guin"
        }

        @Test
        fun `shows only the count when the author is missing`() {
            // ----- Act -----
            val card = hiddenSeries(
                authorText = null,
                bookCount = 4,
            ).toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.caption() shouldBe "4 books"
        }

        @Test
        fun `treats a blank author as missing`() {
            // ----- Act -----
            val withCount = hiddenSeries(
                authorText = " ",
                bookCount = 2,
            ).toHiddenSeriesCard(cover = cover)
            val withoutCount = hiddenSeries(
                authorText = " ",
                bookCount = null,
            ).toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            withCount.caption() shouldBe "2 books"
            withoutCount.caption() shouldBe "Series"
        }

        @Test
        fun `falls back to Series when neither author nor count is known`() {
            // ----- Act -----
            val card = hiddenSeries(
                authorText = null,
                bookCount = null,
            ).toHiddenSeriesCard(cover = cover)

            // ----- Assert -----
            card.caption() shouldBe "Series"
        }
    }
}
