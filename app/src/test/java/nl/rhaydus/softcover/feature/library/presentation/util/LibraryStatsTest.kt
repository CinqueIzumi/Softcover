package nl.rhaydus.softcover.feature.library.presentation.util

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.domain.model.enum.JournalEventType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryStatsTest {

    // region Fixtures

    private fun buildEdition(
        id: Int = 1,
        pages: Int? = null,
        owned: Boolean = false,
    ) = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        pages = pages,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = 2020,
        releaseDate = null,
        format = "paperback",
        owned = owned,
    )

    private fun buildUserBook(journals: List<ReadingJournal> = emptyList()) = UserBook(
        id = 1,
        status = BookStatus.Read,
        dateAdded = "2024-01-01",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = null,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = journals,
    )

    private fun buildBook(
        id: Int = 1,
        currentEditionPages: Int? = null,
        defaultEditionPages: Int? = null,
        userBook: UserBook? = null,
    ): Book {
        val ownedEdition = if (currentEditionPages != null) {
            buildEdition(
                id = 100,
                pages = currentEditionPages,
                owned = true,
            )
        } else {
            null
        }

        val defaultEdition = if (defaultEditionPages != null) {
            buildEdition(
                id = 200,
                pages = defaultEditionPages,
            )
        } else {
            null
        }

        return Book(
            id = id,
            canonicalId = null,
            title = "Test Book",
            editions = listOfNotNull(ownedEdition),
            defaultEdition = defaultEdition,
            rating = 0.0,
            description = "",
            releaseYear = 2020,
            releaseDate = null,
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
    }

    private fun buildFinishedJournal(updatedAt: String) = ReadingJournal(
        updatedAt = updatedAt,
        event = JournalEventType.StatusFinished.eventName,
    )

    // endregion

    @Nested
    inner class TotalPages {

        @Test
        fun `returns 0 for empty list`() {
            // ----- Arrange -----
            val books = emptyList<Book>()

            // ----- Act -----
            val result = books.totalPages()

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `sums currentEdition pages when available`() {
            // ----- Arrange -----
            val books = listOf(
                buildBook(currentEditionPages = 300),
                buildBook(currentEditionPages = 200),
            )

            // ----- Act -----
            val result = books.totalPages()

            // ----- Assert -----
            result shouldBe 500
        }

        @Test
        fun `falls back to defaultEdition pages when currentEdition has no pages`() {
            // ----- Arrange -----
            val books = listOf(
                buildBook(
                    currentEditionPages = null,
                    defaultEditionPages = 400,
                ),
            )

            // ----- Act -----
            val result = books.totalPages()

            // ----- Assert -----
            result shouldBe 400
        }

        @Test
        fun `contributes 0 when neither edition has pages`() {
            // ----- Arrange -----
            val books = listOf(
                buildBook(
                    currentEditionPages = null,
                    defaultEditionPages = null,
                ),
                buildBook(currentEditionPages = 100),
            )

            // ----- Act -----
            val result = books.totalPages()

            // ----- Assert -----
            result shouldBe 100
        }
    }

    @Nested
    inner class FormatBookCount {

        @Test
        fun `returns "1 title" for count of 1`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = formatBookCount(count = 1)

            // ----- Assert -----
            result shouldBe "1 title"
        }

        @Test
        fun `returns "N titles" for count of 0`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = formatBookCount(count = 0)

            // ----- Assert -----
            result shouldBe "0 titles"
        }

        @Test
        fun `returns "N titles" for count greater than 1`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = formatBookCount(count = 42)

            // ----- Assert -----
            result shouldBe "42 titles"
        }
    }

    @Nested
    inner class FormatPageCount {

        @Test
        fun `returns null for 0 pages`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = formatPageCount(pages = 0)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `formats small page count without thousands separator`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = formatPageCount(pages = 300)

            // ----- Assert -----
            result shouldBe "300 pages"
        }

        @Test
        fun `formats large page count with thousands separator`() {
            // ----- Arrange -----
            val expected = "%,d pages".format(1500)

            // ----- Act -----
            val result = formatPageCount(pages = 1500)

            // ----- Assert -----
            result shouldBe expected
        }

        @Test
        fun `formats very large page count with thousands separator`() {
            // ----- Arrange -----
            val expected = "%,d pages".format(12345)

            // ----- Act -----
            val result = formatPageCount(pages = 12345)

            // ----- Assert -----
            result shouldBe expected
        }
    }

    @Nested
    inner class FinishedYear {

        @Test
        fun `returns null when userBook is null`() {
            // ----- Arrange -----
            val book = buildBook(userBook = null)

            // ----- Act -----
            val result = book.finishedYear()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when no StatusFinished journal exists`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(journals = emptyList()))

            // ----- Act -----
            val result = book.finishedYear()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns year from most recent StatusFinished journal`() {
            // ----- Arrange -----
            val journals = listOf(buildFinishedJournal("2022-05-10T12:00:00"))
            val book = buildBook(userBook = buildUserBook(journals = journals))

            // ----- Act -----
            val result = book.finishedYear()

            // ----- Assert -----
            result shouldBe 2022
        }

        @Test
        fun `returns year from latest journal when multiple finished journals exist`() {
            // ----- Arrange -----
            val journals = listOf(
                buildFinishedJournal("2021-01-01T00:00:00"),
                buildFinishedJournal("2023-12-31T23:59:59"),
            )

            val book = buildBook(userBook = buildUserBook(journals = journals))

            // ----- Act -----
            val result = book.finishedYear()

            // ----- Assert -----
            result shouldBe 2023
        }

        @Test
        fun `returns null for malformed updatedAt date`() {
            // ----- Arrange -----
            val journals = listOf(
                ReadingJournal(
                    updatedAt = "not-a-date",
                    event = JournalEventType.StatusFinished.eventName,
                ),
            )

            val book = buildBook(userBook = buildUserBook(journals = journals))

            // ----- Act -----
            val result = book.finishedYear()

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class AvailableFinishedYears {

        @Test
        fun `returns empty list when no books have finished year`() {
            // ----- Arrange -----
            val books = listOf(buildBook(userBook = null))

            // ----- Act -----
            val result = books.availableFinishedYears()

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `returns distinct years in descending order`() {
            // ----- Arrange -----
            val books = listOf(
                buildBook(
                    id = 1,
                    userBook = buildUserBook(
                        journals = listOf(buildFinishedJournal("2021-06-01T00:00:00")),
                    ),
                ),
                buildBook(
                    id = 2,
                    userBook = buildUserBook(
                        journals = listOf(buildFinishedJournal("2023-01-01T00:00:00")),
                    ),
                ),
                buildBook(
                    id = 3,
                    userBook = buildUserBook(
                        journals = listOf(buildFinishedJournal("2021-12-31T00:00:00")),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.availableFinishedYears()

            // ----- Assert -----
            result shouldBe listOf(2023, 2021)
        }
    }
}
