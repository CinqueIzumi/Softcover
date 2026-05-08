package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookTest {

    // ----- Fixtures -----

    private fun buildEdition(
        id: Int,
        owned: Boolean = false,
    ): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = 2020,
        format = "paperback",
        owned = owned,
    )

    private fun buildBook(
        editions: List<BookEdition>,
        defaultEdition: BookEdition? = null,
        userBook: UserBook? = null,
    ): Book = Book(
        id = 1,
        canonicalId = null,
        title = "Test Book",
        editions = editions,
        defaultEdition = defaultEdition,
        rating = 4.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = userBook,
        userBookRead = null,
    )

    private fun buildUserBook(editionId: Int?): UserBook = mockk<UserBook>().also {
        every { it.status } returns BookStatus.Reading
        every { it.editionId } returns editionId
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
        fun `returns merged edition from editions list when userBook is null and defaultEdition id matches`() {
            // ----- Arrange -----
            val editionInList = buildEdition(id = 10, owned = true)
            val staleDefaultEdition = buildEdition(id = 10, owned = false)
            val book = buildBook(
                editions = listOf(editionInList),
                defaultEdition = staleDefaultEdition,
                userBook = null,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result?.owned?.shouldBeTrue()
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
        fun `returns merged edition from editions list when userBook editionId does not match and defaultEdition id matches`() {
            // ----- Arrange -----
            val editionInList = buildEdition(id = 10, owned = true)
            val staleDefaultEdition = buildEdition(id = 10, owned = false)
            val userBook = buildUserBook(editionId = 999)
            val book = buildBook(
                editions = listOf(editionInList),
                defaultEdition = staleDefaultEdition,
                userBook = userBook,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result?.owned?.shouldBeTrue()
        }

        @Test
        fun `returns owned edition when userBook is null and no edition id matches defaultEdition id`() {
            // ----- Arrange -----
            val ownedEdition = buildEdition(id = 10, owned = true)
            val defaultEdition = buildEdition(id = 99)
            val book = buildBook(
                editions = listOf(ownedEdition),
                defaultEdition = defaultEdition,
                userBook = null,
            )

            // ----- Act -----
            val result = book.currentEdition

            // ----- Assert -----
            result shouldBe ownedEdition
        }

        @Test
        fun `returns first owned edition when userBook is null and multiple editions are owned`() {
            // ----- Arrange -----
            val firstOwnedEdition = buildEdition(id = 10, owned = true)
            val secondOwnedEdition = buildEdition(id = 20, owned = true)
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
            val ownedEdition = buildEdition(id = 10, owned = true)
            val defaultEditionMatchingEdition = buildEdition(id = 20, owned = false)
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
            val userSelectedEdition = buildEdition(id = 10, owned = false)
            val ownedEdition = buildEdition(id = 20, owned = true)
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

        @Test
        fun `falls through to defaultEdition when userBook is null and no edition is owned and no edition id matches defaultEdition id`() {
            // ----- Arrange -----
            val editionInList = buildEdition(id = 10, owned = false)
            val defaultEdition = buildEdition(id = 99, owned = false)
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
    }

    // ----- status -----

    @Nested
    inner class Status {

        @Test
        fun `returns None when userBook is null`() {
            // ----- Arrange -----
            val book = buildBook(editions = listOf(buildEdition(id = 1)), userBook = null)

            // ----- Act -----
            val result = book.status

            // ----- Assert -----
            result shouldBe BookStatus.None
        }

        @Test
        fun `returns status from userBook when userBook is not null`() {
            // ----- Arrange -----
            val userBook = buildUserBook(editionId = null).also {
                every { it.status } returns BookStatus.Read
            }
            val book = buildBook(editions = listOf(buildEdition(id = 1)), userBook = userBook)

            // ----- Act -----
            val result = book.status

            // ----- Assert -----
            result shouldBe BookStatus.Read
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
            val series = BookSeries(id = 1, name = "The Stormlight Archive", amountOfBooks = 5)
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
            val series = BookSeries(id = 1, name = "The Stormlight Archive", amountOfBooks = 5)
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                bookSeries = series,
                positionsInSeries = listOf(2.0),
            )

            // ----- Act -----
            val result = book.seriesText

            // ----- Assert -----
            result shouldBe "#2 of 5 in The Stormlight Archive"
        }

        @Test
        fun `returns position text with positionsInSeries of 1`() {
            // ----- Arrange -----
            val series = BookSeries(id = 1, name = "Mistborn", amountOfBooks = 3)
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                bookSeries = series,
                positionsInSeries = listOf(1.0),
            )

            // ----- Act -----
            val result = book.seriesText

            // ----- Assert -----
            result shouldBe "#1 of 3 in Mistborn"
        }

        @Test
        fun `returns fractional position in seriesText when positionsInSeries is 1-5`() {
            // ----- Arrange -----
            val series = BookSeries(id = 1, name = "Wheel of Time", amountOfBooks = 14)
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                bookSeries = series,
                positionsInSeries = listOf(1.5),
            )

            // ----- Act -----
            val result = book.seriesText

            // ----- Assert -----
            result shouldBe "#1.5 of 14 in Wheel of Time"
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
        fun `returns whole number string for position 5-0`() {
            // ----- Arrange -----
            val book = buildBook(editions = listOf(buildEdition(id = 1))).copy(
                positionsInSeries = listOf(5.0),
            )

            // ----- Act -----
            val result = book.positionInSeriesDisplay

            // ----- Assert -----
            result shouldBe "5"
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
}
