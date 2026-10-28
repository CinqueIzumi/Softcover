package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.profile.domain.model.RecentlyLovedBook
import nl.rhaydus.softcover.feature.profile.presentation.state.RecentlyLovedEntry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecentlyLovedCardMapperTest {
    private fun lovedBook(
        bookId: Int = 7,
        ratingStars: Double = 4.5,
        finishedOn: LocalDate = LocalDate(
            year = 2026,
            month = 5,
            day = 14,
        ),
    ): RecentlyLovedBook = RecentlyLovedBook(
        bookId = bookId,
        ratingStars = ratingStars,
        finishedOn = finishedOn,
    )

    private fun RecentlyLovedBook.toCard(libraryBook: Book = libraryBook(id = bookId)): BookCardUiModel =
        RecentlyLovedEntry(
            loved = this,
            libraryBook = libraryBook,
        ).toRecentlyLovedCard()

    private fun libraryBook(
        id: Int,
        title: String = "Dune",
        authorString: String = "Frank Herbert",
        editionUrl: String? = "https://example.com/picked.jpg",
        coverUrl: String = "https://example.com/default.jpg",
    ): Book {
        val edition = mockk<BookEdition> {
            every {
                this@mockk.id
            } returns 99
            every {
                url
            } returns editionUrl
            every {
                localImagePath
            } returns null
        }

        return mockk {
            every {
                this@mockk.id
            } returns id
            every {
                this@mockk.title
            } returns title
            every {
                this@mockk.authorString
            } returns authorString
            every {
                currentEdition
            } returns edition
            every {
                defaultEdition
            } returns null
            every {
                this@mockk.coverUrl
            } returns coverUrl
        }
    }

    private fun BookCardUiModel.column(): BookCardVariant.Column =
        variant.shouldBeInstanceOf<BookCardVariant.Column>()

    @Nested
    inner class ToRecentlyLovedCard {
        @Test
        fun `keys the card by the book id`() {
            // ----- Arrange -----
            val book = lovedBook(bookId = 42)

            // ----- Act -----
            val result = book.toCard()

            // ----- Assert -----
            result.key shouldBe BookCardKey.Book(bookId = 42)
        }

        @Test
        fun `puts the star rating first and the month and year of the finish second`() {
            // ----- Arrange -----
            val book = lovedBook(
                ratingStars = 4.5,
                finishedOn = LocalDate(
                    year = 2026,
                    month = 5,
                    day = 14,
                ),
            )

            // ----- Act -----
            val lines = book.toCard().column().text.supportingLines

            // ----- Assert -----
            lines shouldBe listOf(
                listOf(
                    BookCardLinePart.Rating(
                        label = "4.5",
                        contentDescription = "Rated 4.5 out of 5",
                    ),
                ),
                listOf(
                    BookCardLinePart.Text(text = "May 2026"),
                ),
            )
        }

        @Test
        fun `abbreviates the month and keeps the finish year on a year boundary`() {
            // ----- Arrange -----
            val book = lovedBook(
                finishedOn = LocalDate(
                    year = 2025,
                    month = 12,
                    day = 31,
                ),
            )

            // ----- Act -----
            val lines = book.toCard().column().text.supportingLines

            // ----- Assert -----
            lines.last() shouldBe listOf(BookCardLinePart.Text(text = "Dec 2025"))
        }

        @Test
        fun `formats a whole rating with one decimal`() {
            // ----- Arrange -----
            val book = lovedBook(ratingStars = 4.0)

            // ----- Act -----
            val lines = book.toCard().column().text.supportingLines

            // ----- Assert -----
            lines.first() shouldBe listOf(
                BookCardLinePart.Rating(
                    label = "4.0",
                    contentDescription = "Rated 4.0 out of 5",
                ),
            )
        }

        @Test
        fun `rounds a rating with more precision to one decimal`() {
            // ----- Arrange -----
            val book = lovedBook(ratingStars = 4.26)

            // ----- Act -----
            val lines = book.toCard().column().text.supportingLines

            // ----- Assert -----
            lines.first() shouldBe listOf(
                BookCardLinePart.Rating(
                    label = "4.3",
                    contentDescription = "Rated 4.3 out of 5",
                ),
            )
        }

        @Test
        fun `shows the library book's title and a By-prefixed byline`() {
            // ----- Arrange -----
            val book = lovedBook()
            val library = libraryBook(
                id = 7,
                title = "Dune",
                authorString = "Frank Herbert",
            )

            // ----- Act -----
            val text = book.toCard(libraryBook = library).column().text

            // ----- Assert -----
            text.title shouldBe "Dune"
            text.byline shouldBe "By Frank Herbert"
        }

        @Test
        fun `drops the byline when the library book's authors are blank`() {
            // ----- Arrange -----
            val book = lovedBook()
            val library = libraryBook(
                id = 7,
                authorString = "   ",
            )

            // ----- Act -----
            val text = book.toCard(libraryBook = library).column().text

            // ----- Assert -----
            text.byline.shouldBeNull()
        }

        @Test
        fun `drops the byline when the library book's authors are empty`() {
            // ----- Arrange -----
            val book = lovedBook()
            val library = libraryBook(
                id = 7,
                authorString = "",
            )

            // ----- Act -----
            val text = book.toCard(libraryBook = library).column().text

            // ----- Assert -----
            text.byline.shouldBeNull()
        }

        @Test
        fun `uses the library book's picked edition cover`() {
            // ----- Arrange -----
            val book = lovedBook(bookId = 7)
            val library = libraryBook(
                id = 7,
                editionUrl = "https://example.com/picked.jpg",
            )

            // ----- Act -----
            val cover = book.toCard(libraryBook = library).column().cover.shouldNotBeNull().model

            // ----- Assert -----
            cover.source.shouldNotBeNull().shouldBeInstanceOf<CoverSource.Remote>().url shouldBe
                "https://example.com/picked.jpg"
            cover.sharedTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Cover,
                bookId = 7,
            )
        }

        @Test
        fun `keeps the loved book's rating when a library book supplies the title and cover`() {
            // ----- Arrange -----
            val book = lovedBook(ratingStars = 4.5)
            val library = libraryBook(
                id = 7,
                title = "Dune",
            )

            // ----- Act -----
            val text = book.toCard(libraryBook = library).column().text

            // ----- Assert -----
            text.title shouldBe "Dune"
            text.supportingLines.first() shouldBe listOf(
                BookCardLinePart.Rating(
                    label = "4.5",
                    contentDescription = "Rated 4.5 out of 5",
                ),
            )
        }

        @Test
        fun `has no menu and no progress and presses on tap`() {
            // ----- Arrange -----
            val book = lovedBook()

            // ----- Act -----
            val result = book.toCard()

            // ----- Assert -----
            result.column().hasMenu shouldBe false
            result.decorations.progress.shouldBeNull()
            result.decorations.pressBehaviour shouldBe BookCardDecorations.Press.Tap
        }
    }

    @Nested
    inner class TransitionKeys {
        @Test
        fun `the title key is built from the same book id as the cover key`() {
            // ----- Arrange -----
            val book = lovedBook(bookId = 42)

            // ----- Act -----
            val column = book.toCard().column()

            // ----- Assert -----
            column.text.titleTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Title,
                bookId = 42,
            )
            column.cover.shouldNotBeNull().model.sharedTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Cover,
                bookId = 42,
            )
        }

        @Test
        fun `the rating part carries no transition key because it is the reader's own rating`() {
            // ----- Arrange -----
            val book = lovedBook(ratingStars = 4.5)

            // ----- Act -----
            val lines = book.toCard().column().text.supportingLines

            // ----- Assert -----
            val ratings = lines.flatten().filterIsInstance<BookCardLinePart.Rating>()
            ratings.map { it.transitionKey } shouldBe listOf(null)
        }

        @Test
        fun `different books get different title keys`() {
            // ----- Arrange -----
            val first = lovedBook(bookId = 1)
            val second = lovedBook(bookId = 2)

            // ----- Act -----
            val firstKey = first.toCard().column().text.titleTransitionKey
            val secondKey = second.toCard().column().text.titleTransitionKey

            // ----- Assert -----
            firstKey.shouldNotBeNull() shouldNotBe secondKey
        }
    }
}
