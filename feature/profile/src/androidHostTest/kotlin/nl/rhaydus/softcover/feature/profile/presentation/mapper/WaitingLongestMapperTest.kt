package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.LongestWaiting
import nl.rhaydus.softcover.core.profile.domain.model.WaitingBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WaitingLongestMapperTest {
    private fun waiting(
        id: Int,
        since: LocalDate = LocalDate(
            year = 2023,
            month = 3,
            day = 14,
        ),
    ): WaitingBook {
        val book = mockk<Book> {
            every {
                this@mockk.id
            } returns id

            every {
                title
            } returns "Book $id"

            every {
                authorString
            } returns "Author $id"

            every {
                currentEdition
            } returns null

            every {
                defaultEdition
            } returns null

            every {
                coverUrl
            } returns "https://example.com/$id.jpg"
        }

        return WaitingBook(
            book = book,
            since = since,
        )
    }

    private fun BookCardUiModel.eyebrow(): String? =
        variant.shouldBeInstanceOf<BookCardVariant.Row>().text.eyebrow?.text

    @Nested
    inner class WaitingLongestUiModelMapping {
        @Test
        fun `says what would show here when neither book exists`() {
            // ----- Arrange -----
            val longest = LongestWaiting()

            // ----- Act -----
            val result = waitingLongestUiModel(longest)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.cards.cards.shouldBeEmpty()
            result.caption shouldBe "Books on your Want to Read shelf, and owned books you haven't started, show up here."
        }

        @Test
        fun `uses the singular caption for a single book`() {
            // ----- Arrange -----
            val longest = LongestWaiting(onShelf = waiting(id = 2))

            // ----- Act -----
            val result = waitingLongestUiModel(longest)

            // ----- Assert -----
            result.caption shouldBe "A book that has waited a while for you."
        }

        @Test
        fun `uses the plural caption for two books`() {
            // ----- Arrange -----
            val longest = LongestWaiting(
                onList = waiting(id = 1),
                onShelf = waiting(id = 2),
            )

            // ----- Act -----
            val result = waitingLongestUiModel(longest)

            // ----- Assert -----
            result.caption shouldBe "Two books that have waited a while for you."
        }

        @Test
        fun `puts the book on the list before the book on the shelf`() {
            // ----- Arrange -----
            val longest = LongestWaiting(
                onList = waiting(id = 1),
                onShelf = waiting(id = 2),
            )

            // ----- Act -----
            val result = waitingLongestUiModel(longest)

            // ----- Assert -----
            result.cards.cards.map { it.key } shouldBe listOf(
                BookCardKey.Book(bookId = 1),
                BookCardKey.Book(bookId = 2),
            )

            result.cards.cards[0].eyebrow() shouldBe "On your list since March 2023"
            result.cards.cards[1].eyebrow() shouldBe "On your shelf since March 2023"
        }

        @Test
        fun `keeps only the shelf row when no book waits on the list`() {
            // ----- Arrange -----
            val longest = LongestWaiting(onShelf = waiting(id = 2))

            // ----- Act -----
            val result = waitingLongestUiModel(longest)

            // ----- Assert -----
            result.cards.cards.map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 2))
            result.cards.cards.single().eyebrow() shouldBe "On your shelf since March 2023"
        }

        @Test
        fun `shows the month and year the wait began, whatever the day`() {
            // ----- Arrange -----
            val longest = LongestWaiting(
                onList = waiting(
                    id = 1,
                    since = LocalDate(
                        year = 2024,
                        month = 1,
                        day = 1,
                    ),
                ),
                onShelf = waiting(
                    id = 2,
                    since = LocalDate(
                        year = 2021,
                        month = 12,
                        day = 31,
                    ),
                ),
            )

            // ----- Act -----
            val result = waitingLongestUiModel(longest)

            // ----- Assert -----
            result.cards.cards[0].eyebrow() shouldBe "On your list since January 2024"
            result.cards.cards[1].eyebrow() shouldBe "On your shelf since December 2021"
        }

        @Test
        fun `yields two loading stand-ins with no payload while the pair loads`() {
            // ----- Arrange -----
            val longest: LongestWaiting? = null

            // ----- Act -----
            val result = waitingLongestUiModel(longest)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.cards.cards shouldHaveSize 2
            result.cards.cards.all { it.isLoading } shouldBe true
            result.cards[result.cards.cards.first().key].shouldBeNull()
        }
    }
}
