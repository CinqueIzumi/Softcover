package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.AuthorRanking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TopAuthorsMapperTest {
    private fun leadBook(
        id: Int = 7,
        coverUrl: String = "https://example.com/lead.jpg",
    ): Book {
        val edition = mockk<BookEdition> {
            every {
                this@mockk.id
            } returns 99
            every {
                url
            } returns coverUrl
            every {
                localImagePath
            } returns null
        }

        return mockk {
            every {
                this@mockk.id
            } returns id
            every {
                title
            } returns "A Wizard of Earthsea"
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

    private fun ranking(
        name: String = "Ursula K. Le Guin",
        booksRead: Int = 4,
        averageRating: Double? = 4.5,
        leadBook: Book = leadBook(),
    ): AuthorRanking = AuthorRanking(
        author = Author(
            id = 1,
            name = name,
        ),
        booksRead = booksRead,
        averageRating = averageRating,
        leadBook = leadBook,
    )

    private fun BookCardUiModel.row(): BookCardVariant.Row = variant.shouldBeInstanceOf<BookCardVariant.Row>()

    @Nested
    inner class TopAuthorRows {
        @Test
        fun `a null ranking gives masked loading rows with distinct keys`() {
            // ----- Arrange -----
            val rankings: List<AuthorRanking>? = null

            // ----- Act -----
            val rows = topAuthorRows(rankings)

            // ----- Assert -----
            rows.all { it.isLoading } shouldBe true
            rows.map { it.key }.toSet() shouldHaveSize rows.size
        }

        @Test
        fun `titles the row with the author name and keys it by the lead book`() {
            // ----- Arrange -----
            val rankings = listOf(
                ranking(
                    name = "N. K. Jemisin",
                    leadBook = leadBook(id = 21),
                ),
            )

            // ----- Act -----
            val row = topAuthorRows(rankings).single()

            // ----- Assert -----
            row.row().text.title shouldBe "N. K. Jemisin"
            row.key shouldBe BookCardKey.Book(bookId = 21)
            row.isLoading shouldBe false
        }

        @Test
        fun `leads with the cover of the lead book`() {
            // ----- Arrange -----
            val rankings = listOf(ranking(leadBook = leadBook(coverUrl = "https://example.com/lead.jpg")))

            // ----- Act -----
            val lead = topAuthorRows(rankings).single().row().lead

            // ----- Assert -----
            val cover = lead.shouldBeInstanceOf<BookCardLead.Cover>().cover.model
            cover.source.shouldNotBeNull().shouldBeInstanceOf<CoverSource.Remote>().url shouldBe
                "https://example.com/lead.jpg"
        }

        @Test
        fun `says one book in the singular`() {
            // ----- Arrange -----
            val rankings = listOf(ranking(booksRead = 1))

            // ----- Act -----
            val lines = topAuthorRows(rankings).single().row().text.supportingLines

            // ----- Assert -----
            lines shouldBe listOf(listOf(BookCardLinePart.Text(text = "1 book")))
        }

        @Test
        fun `says the count of books in the plural`() {
            // ----- Arrange -----
            val rankings = listOf(ranking(booksRead = 4))

            // ----- Act -----
            val lines = topAuthorRows(rankings).single().row().text.supportingLines

            // ----- Assert -----
            lines shouldBe listOf(listOf(BookCardLinePart.Text(text = "4 books")))
        }

        @Test
        fun `formats the average rating to one decimal`() {
            // ----- Arrange -----
            val rankings = listOf(ranking(averageRating = 4.55555))

            // ----- Act -----
            val trailing = topAuthorRows(rankings).single().row().trailing

            // ----- Assert -----
            trailing shouldBe BookCardTrailing.Rating(
                label = "4.6",
                contentDescription = "Your average rating, 4.6",
            )
        }

        @Test
        fun `an unrated author has no trailing`() {
            // ----- Arrange -----
            val rankings = listOf(ranking(averageRating = null))

            // ----- Act -----
            val trailing = topAuthorRows(rankings).single().row().trailing

            // ----- Assert -----
            trailing.shouldBeNull()
        }
    }

    @Nested
    inner class TopAuthorsCaption {
        private val designCaption = "Your five most-read authors, with your average rating of their books."

        @Test
        fun `loading shows the design caption`() {
            // ----- Arrange -----
            val topAuthors: List<AuthorRanking>? = null

            // ----- Act -----
            val result = topAuthorsCaption(
                topAuthors = topAuthors,
                booksRead = 0,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result shouldBe designCaption
        }

        @Test
        fun `rankings show the design caption`() {
            // ----- Arrange -----
            val topAuthors = listOf(ranking())

            // ----- Act -----
            val result = topAuthorsCaption(
                topAuthors = topAuthors,
                booksRead = 4,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result shouldBe designCaption
        }

        @Test
        fun `no books in a bounded scope names the scope`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2024)

            // ----- Act -----
            val result = topAuthorsCaption(
                topAuthors = emptyList(),
                booksRead = 0,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "No finished books in 2024."
        }

        @Test
        fun `no books all time invites finishing a few`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = topAuthorsCaption(
                topAuthors = emptyList(),
                booksRead = 0,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "Finish a few books and the authors you read most show up here."
        }

        @Test
        fun `books without any ranking say none lists an author`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2024)

            // ----- Act -----
            val result = topAuthorsCaption(
                topAuthors = emptyList(),
                booksRead = 3,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "None of these books lists an author."
        }
    }

    @Nested
    inner class HasTopAuthors {
        @Test
        fun `is false while loading`() {
            // ----- Arrange -----
            val topAuthors: List<AuthorRanking>? = null

            // ----- Act -----
            val result = hasTopAuthors(topAuthors)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false for an empty ranking`() {
            // ----- Arrange -----
            val topAuthors = emptyList<AuthorRanking>()

            // ----- Act -----
            val result = hasTopAuthors(topAuthors)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true for a non-empty ranking`() {
            // ----- Arrange -----
            val topAuthors = listOf(ranking())

            // ----- Act -----
            val result = hasTopAuthors(topAuthors)

            // ----- Assert -----
            result shouldBe true
        }
    }
}
