package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

class BookTest {
    // ----- Fixtures -----

    private fun buildEdition(
        id: Int,
        owned: Boolean = false,
        releaseDate: LocalDate? = null,
        title: String? = null,
    ): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = title,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = 2020,
        releaseDate = releaseDate,
        format = "paperback",
        owned = owned,
    )

    private fun buildBook(
        editions: List<BookEdition>,
        defaultEdition: BookEdition? = null,
        userBook: UserBook? = null,
        releaseDate: LocalDate? = null,
    ): Book = Book(
        id = 1,
        canonicalId = null,
        title = "Test Book",
        editions = editions,
        defaultEdition = defaultEdition,
        rating = 4.0,
        description = "",
        releaseYear = 2020,
        releaseDate = releaseDate,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = userBook,
    )

    private fun buildUserBook(editionId: Int?): UserBook = mockk<UserBook>().also {
        every {
            it.status
        } returns BookStatus.Reading
        every {
            it.editionId
        } returns editionId
    }

    // ----- displayTitle -----

    @Nested
    inner class DisplayTitle {
        @Test
        fun `uses the title of the user's own edition`() {
            // ----- Arrange -----
            val book = buildBook(
                editions = listOf(
                    buildEdition(
                        id = 1,
                        title = "Other Title",
                    ),
                    buildEdition(
                        id = 2,
                        title = "My Edition",
                    ),
                ),
                userBook = buildUserBook(editionId = 2),
            )

            // ----- Act -----
            val result = book.displayTitle

            // ----- Assert -----
            result shouldBe "My Edition"
        }

        @Test
        fun `falls back to the book title when the user's edition title is blank`() {
            // ----- Arrange -----
            val book = buildBook(
                editions = listOf(buildEdition(
                    id = 2,
                    title = "   ",
                ),),
                userBook = buildUserBook(editionId = 2),
            )

            // ----- Act -----
            val result = book.displayTitle

            // ----- Assert -----
            result shouldBe "Test Book"
        }

        @Test
        fun `falls back to the book title when the user's edition has no title`() {
            // ----- Arrange -----
            val book = buildBook(
                editions = listOf(buildEdition(
                    id = 2,
                    title = null,
                ),),
                userBook = buildUserBook(editionId = 2),
            )

            // ----- Act -----
            val result = book.displayTitle

            // ----- Assert -----
            result shouldBe "Test Book"
        }

        @Test
        fun `ignores owned and default editions when there is no user book`() {
            // ----- Arrange -----
            val owned = buildEdition(
                id = 1,
                owned = true,
                title = "Owned Title",
            )
            val default = buildEdition(
                id = 2,
                title = "Default Title",
            )
            val book = buildBook(
                editions = listOf(owned, default),
                defaultEdition = default,
            )

            // ----- Act -----
            val result = book.displayTitle

            // ----- Assert -----
            result shouldBe "Test Book"
        }
    }

    // ----- currentEdition -----

    @Nested
    inner class CurrentEdition {
        @Test
        fun `returns edition from editions list when userBook editionId matches`() {
            // ----- Arrange -----
            val matchingEdition = buildEdition(id = 10)
            val otherEdition = buildEdition(id = 20)
            val userBook = buildUserBook(editionId = 10)
            val book = buildBook(
                editions = listOf(matchingEdition, otherEdition),
                userBook = userBook,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result shouldBe matchingEdition
        }

        @Test
        fun `returns defaultEdition itself when userBook is null and no edition in list shares its id`() {
            // ----- Arrange -----
            val editionInList = buildEdition(id = 10)
            val defaultEdition = buildEdition(id = 99)
            val book = buildBook(
                editions = listOf(editionInList),
                defaultEdition = defaultEdition,
                userBook = null,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result shouldBe defaultEdition
        }

        @Test
        fun `returns first edition when userBook is null and defaultEdition is null`() {
            // ----- Arrange -----
            val firstEdition = buildEdition(id = 10)
            val secondEdition = buildEdition(id = 20)
            val book = buildBook(
                editions = listOf(firstEdition, secondEdition),
                defaultEdition = null,
                userBook = null,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result shouldBe firstEdition
        }

        @Test
        fun `returns first owned edition when userBook is null and multiple editions are owned`() {
            // ----- Arrange -----
            val firstOwnedEdition = buildEdition(
                id = 10,
                owned = true,
            )
            val secondOwnedEdition = buildEdition(
                id = 20,
                owned = true,
            )
            val book = buildBook(
                editions = listOf(firstOwnedEdition, secondOwnedEdition),
                userBook = null,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result shouldBe firstOwnedEdition
        }

        @Test
        fun `returns owned edition over defaultEdition-by-id match when userBook is null`() {
            // ----- Arrange -----
            val ownedEdition = buildEdition(
                id = 10,
                owned = true,
            )
            val defaultEditionMatchingEdition = buildEdition(
                id = 20,
                owned = false,
            )
            val defaultEdition = buildEdition(id = 20)
            val book = buildBook(
                editions = listOf(ownedEdition, defaultEditionMatchingEdition),
                defaultEdition = defaultEdition,
                userBook = null,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result shouldBe ownedEdition
        }

        @Test
        fun `returns userBook matched edition over owned edition when userBook editionId matches a non-owned edition`() {
            // ----- Arrange -----
            val userSelectedEdition = buildEdition(
                id = 10,
                owned = false,
            )
            val ownedEdition = buildEdition(
                id = 20,
                owned = true,
            )
            val userBook = buildUserBook(editionId = 10)
            val book = buildBook(
                editions = listOf(userSelectedEdition, ownedEdition),
                userBook = userBook,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result shouldBe userSelectedEdition
        }
    }

    // ----- status -----

    @Nested
    inner class Status {
        @Test
        fun `returns None when userBook is null`() {
            // ----- Arrange -----
            val book = buildBook(
                editions = listOf(buildEdition(id = 1)),
                userBook = null,
            )

            // ----- Act -----
            val result = book.status

            // ----- Assert -----
            result shouldBe BookStatus.None
        }
    }

    // ----- isPaused -----

    @Nested
    inner class IsPaused {
        @Test
        fun `is true when the user book is paused`() {
            // ----- Arrange -----
            val userBook = buildUserBook(editionId = null)
            every {
                userBook.status
            } returns BookStatus.Paused
            val book = buildBook(
                editions = listOf(buildEdition(id = 1)),
                userBook = userBook,
            )

            // ----- Act -----
            val result = book.isPaused

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when the user book is reading`() {
            // ----- Arrange -----
            val book = buildBook(
                editions = listOf(buildEdition(id = 1)),
                userBook = buildUserBook(editionId = null),
            )

            // ----- Act -----
            val result = book.isPaused

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when there is no user book`() {
            // ----- Arrange -----
            val book = buildBook(
                editions = listOf(buildEdition(id = 1)),
                userBook = null,
            )

            // ----- Act -----
            val result = book.isPaused

            // ----- Assert -----
            result shouldBe false
        }
    }

    // ----- seriesText -----

    @Nested
    inner class SeriesText {
        @Test
        fun `returns null when bookSeries is null`() {
            // ----- Arrange -----
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                bookSeries = null,
                positionsInSeries = emptyList(),
            )

            // ----- Act -----
            val result = book.seriesText

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns series name when positionsInSeries is empty`() {
            // ----- Arrange -----
            val series = BookSeries(
                id = 1,
                name = "The Stormlight Archive",
                amountOfBooks = 5,
            )
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                bookSeries = series,
                positionsInSeries = emptyList(),
            )

            // ----- Act -----
            val result = book.seriesText

            // ----- Assert -----
            result shouldBe "The Stormlight Archive"
        }

        @Test
        fun `returns position and series name when positionsInSeries is set`() {
            // ----- Arrange -----
            val series = BookSeries(
                id = 1,
                name = "The Stormlight Archive",
                amountOfBooks = 5,
            )
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                bookSeries = series,
                positionsInSeries = listOf(2.0),
            )

            // ----- Act -----
            val result = book.seriesText

            // ----- Assert -----
            result shouldBe "#2 of 5 in The Stormlight Archive"
        }
    }

    // ----- positionInSeriesDisplay -----

    @Nested
    inner class PositionInSeriesDisplay {
        @Test
        fun `returns null when positionsInSeries is empty`() {
            // ----- Arrange -----
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                positionsInSeries = emptyList(),
            )

            // ----- Act -----
            val result = book.positionInSeriesDisplay

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns whole number string for position 1-0`() {
            // ----- Arrange -----
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                positionsInSeries = listOf(1.0),
            )

            // ----- Act -----
            val result = book.positionInSeriesDisplay

            // ----- Assert -----
            result shouldBe "1"
        }

        @Test
        fun `returns fractional string for position 1-5`() {
            // ----- Arrange -----
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                positionsInSeries = listOf(1.5),
            )

            // ----- Act -----
            val result = book.positionInSeriesDisplay

            // ----- Assert -----
            result shouldBe "1.5"
        }

        @Test
        fun `returns first-last range string for multi-position compilation`() {
            // ----- Arrange -----
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                positionsInSeries = listOf(1.0, 2.0, 3.0),
            )

            // ----- Act -----
            val result = book.positionInSeriesDisplay

            // ----- Assert -----
            result shouldBe "1-3"
        }
    }

    // ----- isUnreleased -----

    @Nested
    inner class ReleaseInfo {
        @Test
        fun `isUnreleased is true for a future book releaseDate`() {
            // ----- Arrange -----
            val futureDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(
                7,
                DateTimeUnit.DAY,
            )
            val book = buildBook(
                editions = listOf(buildEdition(id = 1)),
                releaseDate = futureDate,
            )

            // ----- Act -----
            val result = book.isUnreleased

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `isUnreleased is false for a past book releaseDate`() {
            // ----- Arrange -----
            val pastDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(
                7,
                DateTimeUnit.DAY,
            )
            val book = buildBook(
                editions = listOf(buildEdition(id = 1)),
                releaseDate = pastDate,
            )

            // ----- Act -----
            val result = book.isUnreleased

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `isUnreleased is false for today's book releaseDate`() {
            // ----- Arrange -----
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val book = buildBook(
                editions = listOf(buildEdition(id = 1)),
                releaseDate = today,
            )

            // ----- Act -----
            val result = book.isUnreleased

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `isUnreleased is false when book releaseDate is null`() {
            // ----- Arrange -----
            val book = buildBook(
                editions = listOf(buildEdition(id = 1)),
                releaseDate = null,
            )

            // ----- Act -----
            val result = book.isUnreleased

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `isUnreleased ignores a future edition releaseDate when the book was released`() {
            // ----- Arrange -----
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val futureDate = today.plus(
                7,
                DateTimeUnit.DAY,
            )
            val currentEdition = buildEdition(
                id = 10,
                owned = true,
                releaseDate = futureDate,
            )
            val defaultEdition = buildEdition(
                id = 20,
                releaseDate = futureDate,
            )
            val book = buildBook(
                editions = listOf(currentEdition),
                defaultEdition = defaultEdition,
                releaseDate = today.minus(
                    7,
                    DateTimeUnit.DAY,
                ),
            )

            // ----- Act -----
            val result = book.isUnreleased

            // ----- Assert -----
            result shouldBe false
        }
    }
}
