package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesDismissalKind
import nl.rhaydus.softcover.feature.explore.presentation.state.continueSeriesMenuSheetScaffoldFor
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ContinueSeriesMenuSheetMapperTest {
    private val series = BookSeries(
        id = 4,
        name = "Earthsea",
        amountOfBooks = 6,
    )

    private fun book(
        bookSeries: BookSeries? = series,
        authors: List<Author> = emptyList(),
        positionsInSeries: List<Double> = listOf(2.0),
    ): Book = Book(
        id = 3,
        title = "The Tombs of Atuan",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 1971,
        coverUrl = "https://covers.test/3.jpg",
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = bookSeries,
        positionsInSeries = positionsInSeries,
        isCompilation = false,
        userBook = null,
    )

    @Nested
    inner class ContinueSeriesMenuSheetFor {
        @Test
        fun `offers hiding the book then the series when the book belongs to one`() {
            // ----- Arrange -----
            val input = book()

            // ----- Act -----
            val options = continueSeriesMenuSheetFor(book = input).options

            // ----- Assert -----
            options shouldHaveSize 2
            options[0].key shouldBe ContinueSeriesDismissalKind.THIS_BOOK
            options[1].key shouldBe ContinueSeriesDismissalKind.WHOLE_SERIES
            options[1].label shouldBe "Hide everything from Earthsea"
        }

        @Test
        fun `describes every option it offers`() {
            // ----- Arrange -----
            val input = book()

            // ----- Act -----
            val options = continueSeriesMenuSheetFor(book = input).options

            // ----- Assert -----
            options.forEach { it.description.shouldNotBeBlank() }
        }

        @Test
        fun `offers only hiding the book when it has no series`() {
            // ----- Arrange -----
            val input = book(bookSeries = null)

            // ----- Act -----
            val options = continueSeriesMenuSheetFor(book = input).options

            // ----- Assert -----
            options shouldHaveSize 1
            options.single().key shouldBe ContinueSeriesDismissalKind.THIS_BOOK
            options.single().description.shouldNotBeBlank()
        }

        @Test
        fun `carries the book id and the scaffold built for the book`() {
            // ----- Arrange -----
            val input = book()

            // ----- Act -----
            val sheet = continueSeriesMenuSheetFor(book = input)

            // ----- Assert -----
            sheet.bookId shouldBe 3
            sheet.scaffold shouldBe continueSeriesMenuSheetScaffoldFor(book = input)
        }
    }
}
