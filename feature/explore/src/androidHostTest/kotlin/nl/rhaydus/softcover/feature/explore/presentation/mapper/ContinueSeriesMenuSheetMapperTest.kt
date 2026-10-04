package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesBookAction
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
        userBookRead = null,
    )

    @Nested
    inner class ContinueSeriesMenuSheetFor {
        @Test
        fun `builds the sheet header from the book and its series`() {
            // ----- Arrange -----
            val input = book()

            // ----- Act -----
            val sheet = continueSeriesMenuSheetFor(book = input)

            // ----- Assert -----
            sheet.scaffold.header.section shouldBe SectionHeaderUiModel.Section(
                eyebrow = "Earthsea",
                headline = "The Tombs of Atuan",
            )
        }

        @Test
        fun `offers hiding the book then the series when the book belongs to one`() {
            // ----- Arrange -----
            val input = book()

            // ----- Act -----
            val options = continueSeriesMenuSheetFor(book = input).options

            // ----- Assert -----
            options shouldHaveSize 2
            options[0].dismissAction.shouldBeInstanceOf<OnDismissContinueSeriesBookAction>()
            options[1].dismissAction.shouldBeInstanceOf<OnDismissContinueSeriesAction>()
            options[1].label shouldBe "Hide everything from Earthsea"
        }

        @Test
        fun `offers only hiding the book when it has no series`() {
            // ----- Arrange -----
            val input = book(bookSeries = null)

            // ----- Act -----
            val options = continueSeriesMenuSheetFor(book = input).options

            // ----- Assert -----
            options shouldHaveSize 1
            options.single().dismissAction.shouldBeInstanceOf<OnDismissContinueSeriesBookAction>()
        }

        @Test
        fun `hides the book with its identity, series link and last series position`() {
            // ----- Arrange -----
            val input = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Ursula K. Le Guin",
                    ),
                ),
                positionsInSeries = listOf(
                    2.0,
                    3.0,
                ),
            )

            // ----- Act -----
            val action = continueSeriesMenuSheetFor(book = input).options[0].dismissAction
                .shouldBeInstanceOf<OnDismissContinueSeriesBookAction>()

            // ----- Assert -----
            action.book shouldBe DismissedSeriesBook(
                bookId = 3,
                title = "The Tombs of Atuan",
                coverUrl = "https://covers.test/3.jpg",
                authorText = "Ursula K. Le Guin",
                seriesName = "Earthsea",
                seriesId = 4,
                seriesPosition = 3.0,
            )
        }

        @Test
        fun `leaves the author text null when the book has no authors`() {
            // ----- Arrange -----
            val input = book(authors = emptyList())

            // ----- Act -----
            val action = continueSeriesMenuSheetFor(book = input).options[0].dismissAction
                .shouldBeInstanceOf<OnDismissContinueSeriesBookAction>()

            // ----- Assert -----
            action.book.authorText shouldBe null
        }

        @Test
        fun `hides the series with its name, cover, author text and book count`() {
            // ----- Arrange -----
            val input = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Ursula K. Le Guin",
                    ),
                ),
            )

            // ----- Act -----
            val action = continueSeriesMenuSheetFor(book = input).options[1].dismissAction
                .shouldBeInstanceOf<OnDismissContinueSeriesAction>()

            // ----- Assert -----
            action.seriesId shouldBe 4
            action.seriesName shouldBe "Earthsea"
            action.coverUrl shouldBe "https://covers.test/3.jpg"
            action.authorText shouldBe "Ursula K. Le Guin"
            action.bookCount shouldBe 6
        }
    }
}
