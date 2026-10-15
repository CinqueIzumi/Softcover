package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
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
    ): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = null,
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

    // ----- effectiveReleaseDate / isUnreleased -----

    @Nested
    inner class ReleaseInfo {
        @Test
        fun `effectiveReleaseDate prefers currentEdition release date when present`() {
            // ----- Arrange -----
            val editionDate = LocalDate(
                2025,
                1,
                1,
            )
            val defaultDate = LocalDate(
                2020,
                6,
                1,
            )
            val bookDate = LocalDate(
                2015,
                3,
                15,
            )

            val currentEdition = buildEdition(
                id = 10,
                owned = true,
                releaseDate = editionDate,
            )
            val defaultEdition = buildEdition(
                id = 20,
                releaseDate = defaultDate,
            )
            val book = buildBook(
                editions = listOf(currentEdition),
                defaultEdition = defaultEdition,
                releaseDate = bookDate,
            )

            // ----- Act -----
            val result = book.effectiveReleaseDate

            // ----- Assert -----
            result shouldBe editionDate
        }

        @Test
        fun `effectiveReleaseDate falls back to defaultEdition release date when currentEdition has none`() {
            // ----- Arrange -----
            val defaultDate = LocalDate(
                2020,
                6,
                1,
            )
            val bookDate = LocalDate(
                2015,
                3,
                15,
            )

            val currentEdition = buildEdition(
                id = 10,
                owned = true,
                releaseDate = null,
            )
            val defaultEdition = buildEdition(
                id = 20,
                releaseDate = defaultDate,
            )
            val book = buildBook(
                editions = listOf(currentEdition),
                defaultEdition = defaultEdition,
                releaseDate = bookDate,
            )

            // ----- Act -----
            val result = book.effectiveReleaseDate

            // ----- Assert -----
            result shouldBe defaultDate
        }

        @Test
        fun `effectiveReleaseDate falls back to book releaseDate when neither edition supplies one`() {
            // ----- Arrange -----
            val bookDate = LocalDate(
                2015,
                3,
                15,
            )

            val currentEdition = buildEdition(
                id = 10,
                owned = true,
                releaseDate = null,
            )
            val defaultEdition = buildEdition(
                id = 20,
                releaseDate = null,
            )
            val book = buildBook(
                editions = listOf(currentEdition),
                defaultEdition = defaultEdition,
                releaseDate = bookDate,
            )

            // ----- Act -----
            val result = book.effectiveReleaseDate

            // ----- Assert -----
            result shouldBe bookDate
        }

        @Test
        fun `effectiveReleaseDate is null when no source supplies one`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 10,
                releaseDate = null,
            )
            val book = buildBook(
                editions = listOf(edition),
                releaseDate = null,
            )

            // ----- Act -----
            val result = book.effectiveReleaseDate

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `isUnreleased is true for a future effectiveReleaseDate`() {
            // ----- Arrange -----
            val futureDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(
                7,
                DateTimeUnit.DAY,
            )

            val edition = buildEdition(
                id = 10,
                releaseDate = futureDate,
            )
            val book = buildBook(editions = listOf(edition))

            // ----- Act -----
            val result = book.isUnreleased

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `isUnreleased is false for today's effectiveReleaseDate`() {
            // ----- Arrange -----
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            val edition = buildEdition(
                id = 10,
                releaseDate = today,
            )
            val book = buildBook(editions = listOf(edition))

            // ----- Act -----
            val result = book.isUnreleased

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `isUnreleased is false when effectiveReleaseDate is null`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 10,
                releaseDate = null,
            )
            val book = buildBook(
                editions = listOf(edition),
                releaseDate = null,
            )

            // ----- Act -----
            val result = book.isUnreleased

            // ----- Assert -----
            result shouldBe false
        }
    }
}
