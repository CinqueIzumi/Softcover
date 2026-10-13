package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecentlyLovedCardMapperTest {
    private fun lovedBook(
        bookId: Int = 7,
        coverUrl: String = "https://example.com/dune.jpg",
        title: String = "Dune",
        author: String = "Frank Herbert",
        ratingStars: Double = 4.5,
        monthLabel: String = "May 2026",
    ): LovedBook = LovedBook(
        bookId = bookId,
        coverUrl = coverUrl,
        title = title,
        author = author,
        ratingStars = ratingStars,
        monthLabel = monthLabel,
    )

    private fun BookCardUiModel.column(): BookCardVariant.Column =
        variant.shouldBeInstanceOf<BookCardVariant.Column>()

    @Nested
    inner class ToRecentlyLovedCard {
        @Test
        fun `keys the card by the book id`() {
            // ----- Arrange -----
            val book = lovedBook(bookId = 42)

            // ----- Act -----
            val result = book.toRecentlyLovedCard()

            // ----- Assert -----
            result.key shouldBe BookCardKey.Book(bookId = 42)
        }

        @Test
        fun `puts the star rating first and the month label second`() {
            // ----- Arrange -----
            val book = lovedBook(
                ratingStars = 4.5,
                monthLabel = "May 2026",
            )

            // ----- Act -----
            val lines = book.toRecentlyLovedCard().column().text.supportingLines

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
        fun `formats a whole rating with one decimal`() {
            // ----- Arrange -----
            val book = lovedBook(ratingStars = 4.0)

            // ----- Act -----
            val lines = book.toRecentlyLovedCard().column().text.supportingLines

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
            val lines = book.toRecentlyLovedCard().column().text.supportingLines

            // ----- Assert -----
            lines.first() shouldBe listOf(
                BookCardLinePart.Rating(
                    label = "4.3",
                    contentDescription = "Rated 4.3 out of 5",
                ),
            )
        }

        @Test
        fun `shows the title and a By-prefixed byline`() {
            // ----- Arrange -----
            val book = lovedBook(
                title = "Dune",
                author = "Frank Herbert",
            )

            // ----- Act -----
            val text = book.toRecentlyLovedCard().column().text

            // ----- Assert -----
            text.title shouldBe "Dune"
            text.byline shouldBe "By Frank Herbert"
        }

        @Test
        fun `drops the byline when the author is blank`() {
            // ----- Arrange -----
            val book = lovedBook(author = "   ")

            // ----- Act -----
            val text = book.toRecentlyLovedCard().column().text

            // ----- Assert -----
            text.byline.shouldBeNull()
        }

        @Test
        fun `drops the byline when the author is empty`() {
            // ----- Arrange -----
            val book = lovedBook(author = "")

            // ----- Act -----
            val text = book.toRecentlyLovedCard().column().text

            // ----- Assert -----
            text.byline.shouldBeNull()
        }

        @Test
        fun `builds a remote cover source from a non-blank cover url`() {
            // ----- Arrange -----
            val book = lovedBook(coverUrl = "https://example.com/dune.jpg")

            // ----- Act -----
            val source = book.toRecentlyLovedCard().column().cover.shouldNotBeNull().model.source

            // ----- Assert -----
            source.shouldNotBeNull().shouldBeInstanceOf<CoverSource.Remote>().url shouldBe "https://example.com/dune.jpg"
        }

        @Test
        fun `gives a null cover source and keeps the title as the coverless title when the cover url is blank`() {
            // ----- Arrange -----
            val book = lovedBook(
                coverUrl = "  ",
                title = "Dune",
            )

            // ----- Act -----
            val cover = book.toRecentlyLovedCard().column().cover.shouldNotBeNull().model

            // ----- Assert -----
            cover.source.shouldBeNull()
            cover.coverlessTitle shouldBe "Dune"
        }

        @Test
        fun `has no menu and no progress and presses on tap`() {
            // ----- Arrange -----
            val book = lovedBook()

            // ----- Act -----
            val result = book.toRecentlyLovedCard()

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
            val column = book.toRecentlyLovedCard().column()

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
            val lines = book.toRecentlyLovedCard().column().text.supportingLines

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
            val firstKey = first.toRecentlyLovedCard().column().text.titleTransitionKey
            val secondKey = second.toRecentlyLovedCard().column().text.titleTransitionKey

            // ----- Assert -----
            firstKey.shouldNotBeNull() shouldNotBe secondKey
        }
    }
}
