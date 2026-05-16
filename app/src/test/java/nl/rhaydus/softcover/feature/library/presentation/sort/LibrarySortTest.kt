package nl.rhaydus.softcover.feature.library.presentation.sort

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.domain.model.enum.JournalEventType
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineUnit
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class LibrarySortTest {

    // region Fixtures

    private fun buildEdition(
        id: Int = 1,
        pages: Int? = null,
        authors: List<Author> = emptyList(),
        title: String? = null,
    ) = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = title,
        url = null,
        localImagePath = null,
        isbn10 = null,
        pages = pages,
        audioSeconds = null,
        authors = authors,
        releaseYear = 2020,
        releaseDate = null,
        format = "paperback",
        owned = false,
    )

    private fun buildUserBook(
        dateAdded: String = "2024-01-01",
        rating: Double? = null,
        journals: List<ReadingJournal> = emptyList(),
    ) = UserBook(
        id = 1,
        status = BookStatus.Reading,
        dateAdded = dateAdded,
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = null,
        rating = rating,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = journals,
    )

    private fun buildUserBookRead(
        progress: Float = 0f,
    ) = UserBookRead(
        id = 1,
        currentPage = null,
        currentSeconds = null,
        progress = progress,
        startedAt = null,
        finishedAt = null,
    )

    private fun buildBook(
        id: Int = 1,
        title: String = "A Book",
        authors: List<Author> = emptyList(),
        rating: Double = 0.0,
        userBook: UserBook? = null,
        userBookRead: UserBookRead? = null,
        currentEditionPages: Int? = null,
        defaultEditionPages: Int? = null,
    ): Book {
        val currentEdition = if (currentEditionPages != null) {
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
            title = title,
            editions = listOfNotNull(currentEdition),
            defaultEdition = defaultEdition,
            rating = rating,
            description = "",
            releaseYear = 2020,
            releaseDate = null,
            coverUrl = "",
            authors = authors,
            usersCount = 0,
            ratingsCount = 0,
            bookSeries = null,
            positionsInSeries = emptyList(),
            isCompilation = false,
            userBook = userBook,
            userBookRead = userBookRead,
        )
    }

    private fun buildEdition(
        id: Int = 1,
        pages: Int? = null,
        authors: List<Author> = emptyList(),
        title: String? = null,
        owned: Boolean = false,
    ) = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = title,
        url = null,
        localImagePath = null,
        isbn10 = null,
        pages = pages,
        audioSeconds = null,
        authors = authors,
        releaseYear = 2020,
        releaseDate = null,
        format = "paperback",
        owned = owned,
    )

    private fun buildFinishedJournal(updatedAt: String) = ReadingJournal(
        updatedAt = updatedAt,
        event = JournalEventType.StatusFinished.eventName,
    )

    private fun buildDeadline(
        bookId: Int,
        deadlineDate: LocalDate,
        initialPerDay: Float = 10f,
        unit: DeadlineUnit = DeadlineUnit.PAGES,
    ) = BookDeadline(
        bookId = bookId,
        deadlineDate = deadlineDate,
        setAt = LocalDate.now(),
        initialPerDay = initialPerDay,
        unit = unit,
    )

    // endregion

    @Nested
    inner class ApplySort {

        @Nested
        inner class DateAdded {

            @Test
            fun `sorts by dateAdded descending`() {
                // ----- Arrange -----
                val older = buildBook(
                    id = 1,
                    userBook = buildUserBook(dateAdded = "2023-01-01"),
                )
                val newer = buildBook(
                    id = 2,
                    userBook = buildUserBook(dateAdded = "2024-06-01"),
                )
                val books = listOf(older, newer)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.DATE_ADDED)

                // ----- Assert -----
                result[0].id shouldBe 2
                result[1].id shouldBe 1
            }

            @Test
            fun `sorts book with no userBook last (LocalDate MIN)`() {
                // ----- Arrange -----
                val withDate = buildBook(
                    id = 1,
                    userBook = buildUserBook(dateAdded = "2024-01-01"),
                )
                val noUserBook = buildBook(
                    id = 2,
                    userBook = null,
                )
                val books = listOf(noUserBook, withDate)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.DATE_ADDED)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }
        }

        @Nested
        inner class DateFinished {

            @Test
            fun `sorts by most-recent StatusFinished journal descending`() {
                // ----- Arrange -----
                val earlier = buildBook(
                    id = 1,
                    userBook = buildUserBook(
                        journals = listOf(buildFinishedJournal("2023-03-01T00:00:00")),
                    ),
                )

                val later = buildBook(
                    id = 2,
                    userBook = buildUserBook(
                        journals = listOf(buildFinishedJournal("2024-07-15T00:00:00")),
                    ),
                )

                val books = listOf(earlier, later)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.DATE_FINISHED)

                // ----- Assert -----
                result[0].id shouldBe 2
                result[1].id shouldBe 1
            }

            @Test
            fun `sorts book with no finished journal last`() {
                // ----- Arrange -----
                val withFinish = buildBook(
                    id = 1,
                    userBook = buildUserBook(
                        journals = listOf(buildFinishedJournal("2024-01-01T00:00:00")),
                    ),
                )

                val noFinish = buildBook(
                    id = 2,
                    userBook = buildUserBook(),
                )

                val books = listOf(noFinish, withFinish)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.DATE_FINISHED)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }
        }

        @Nested
        inner class Title {

            @Test
            fun `sorts by title case-insensitively ascending`() {
                // ----- Arrange -----
                val books = listOf(
                    buildBook(
                        id = 1,
                        title = "Zebra",
                    ),
                    buildBook(
                        id = 2,
                        title = "apple",
                    ),
                    buildBook(
                        id = 3,
                        title = "Mango",
                    ),
                )

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.TITLE)

                // ----- Assert -----
                result[0].title shouldBe "apple"
                result[1].title shouldBe "Mango"
                result[2].title shouldBe "Zebra"
            }
        }

        @Nested
        inner class Author {

            @Test
            fun `sorts by first author name ascending case-insensitively`() {
                // ----- Arrange -----
                val books = listOf(
                    buildBook(
                        id = 1,
                        authors = listOf(Author(id = 1, name = "Tolkien")),
                    ),
                    buildBook(
                        id = 2,
                        authors = listOf(Author(id = 2, name = "Adams")),
                    ),
                    buildBook(
                        id = 3,
                        authors = listOf(Author(id = 3, name = "le Guin")),
                    ),
                )

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.AUTHOR)

                // ----- Assert -----
                result[0].authors[0].name shouldBe "Adams"
                result[1].authors[0].name shouldBe "le Guin"
                result[2].authors[0].name shouldBe "Tolkien"
            }

            @Test
            fun `sorts book with no authors first (empty string)`() {
                // ----- Arrange -----
                val noAuthor = buildBook(
                    id = 1,
                    authors = emptyList(),
                )
                val withAuthor = buildBook(
                    id = 2,
                    authors = listOf(Author(id = 1, name = "Asimov")),
                )

                val books = listOf(withAuthor, noAuthor)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.AUTHOR)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }
        }

        @Nested
        inner class Rating {

            @Test
            fun `sorts by userBook rating descending when available`() {
                // ----- Arrange -----
                val highRated = buildBook(
                    id = 1,
                    rating = 3.0,
                    userBook = buildUserBook(rating = 5.0),
                )

                val lowRated = buildBook(
                    id = 2,
                    rating = 4.5,
                    userBook = buildUserBook(rating = 2.0),
                )

                val books = listOf(lowRated, highRated)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.RATING)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }

            @Test
            fun `falls back to book rating when userBook rating is null`() {
                // ----- Arrange -----
                val higherBook = buildBook(
                    id = 1,
                    rating = 4.8,
                    userBook = buildUserBook(rating = null),
                )

                val lowerBook = buildBook(
                    id = 2,
                    rating = 3.2,
                    userBook = buildUserBook(rating = null),
                )

                val books = listOf(lowerBook, higherBook)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.RATING)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }
        }

        @Nested
        inner class Progress {

            @Test
            fun `sorts by progress fraction descending`() {
                // ----- Arrange -----
                val moreDone = buildBook(
                    id = 1,
                    userBookRead = buildUserBookRead(progress = 0.8f),
                )
                val lessDone = buildBook(
                    id = 2,
                    userBookRead = buildUserBookRead(progress = 0.2f),
                )
                val books = listOf(lessDone, moreDone)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.PROGRESS)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }

            @Test
            fun `book with no userBookRead gets progress 0`() {
                // ----- Arrange -----
                val withProgress = buildBook(
                    id = 1,
                    userBookRead = buildUserBookRead(progress = 0.5f),
                )
                val noProgress = buildBook(
                    id = 2,
                    userBookRead = null,
                )
                val books = listOf(noProgress, withProgress)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.PROGRESS)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }
        }

        @Nested
        inner class PageCount {

            @Test
            fun `sorts by pageCount descending using currentEdition pages`() {
                // ----- Arrange -----
                val thicker = buildBook(
                    id = 1,
                    currentEditionPages = 800,
                )
                val thinner = buildBook(
                    id = 2,
                    currentEditionPages = 200,
                )
                val books = listOf(thinner, thicker)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.PAGE_COUNT)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }

            @Test
            fun `falls back to defaultEdition pages when currentEdition has none`() {
                // ----- Arrange -----
                val withDefault = buildBook(
                    id = 1,
                    currentEditionPages = null,
                    defaultEditionPages = 500,
                )

                val noPages = buildBook(
                    id = 2,
                    currentEditionPages = null,
                    defaultEditionPages = null,
                )

                val books = listOf(noPages, withDefault)

                // ----- Act -----
                val result = books.applySort(mode = LibrarySortMode.PAGE_COUNT)

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }
        }

        @Nested
        inner class DeadlineUrgency {

            @Test
            fun `sorts by deadline date ascending — soonest first`() {
                // ----- Arrange -----
                val today = LocalDate.now()
                val nearDeadline = buildDeadline(
                    bookId = 1,
                    deadlineDate = today.plusDays(3),
                )
                val farDeadline = buildDeadline(
                    bookId = 2,
                    deadlineDate = today.plusDays(30),
                )

                val books = listOf(
                    buildBook(id = 1),
                    buildBook(id = 2),
                )

                val deadlines = mapOf(1 to nearDeadline, 2 to farDeadline)

                // ----- Act -----
                val result = books.applySort(
                    mode = LibrarySortMode.DEADLINE_URGENCY,
                    deadlines = deadlines,
                )

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }

            @Test
            fun `books without deadlines sort last (Long MAX_VALUE urgency)`() {
                // ----- Arrange -----
                val today = LocalDate.now()
                val withDeadline = buildBook(id = 1)
                val noDeadline = buildBook(id = 2)

                val deadline = buildDeadline(
                    bookId = 1,
                    deadlineDate = today.plusDays(10),
                )
                val deadlines = mapOf(1 to deadline)

                val books = listOf(noDeadline, withDeadline)

                // ----- Act -----
                val result = books.applySort(
                    mode = LibrarySortMode.DEADLINE_URGENCY,
                    deadlines = deadlines,
                )

                // ----- Assert -----
                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }
        }
    }

    @Nested
    inner class ApplyEditionSort {

        private fun buildEditionWithTitle(title: String?) = buildEdition(
            title = title,
            pages = 100,
        )

        @Nested
        inner class Title {

            @Test
            fun `sorts editions by title ascending case-insensitively`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        title = "Zebra Edition",
                    ),
                    buildEdition(
                        id = 2,
                        title = "alpha Edition",
                    ),
                    buildEdition(
                        id = 3,
                        title = "Middle Edition",
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.TITLE)

                // ----- Assert -----
                result[0].title shouldBe "alpha Edition"
                result[1].title shouldBe "Middle Edition"
                result[2].title shouldBe "Zebra Edition"
            }

            @Test
            fun `null title sorts before non-null`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        title = "Some Title",
                    ),
                    buildEdition(
                        id = 2,
                        title = null,
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.TITLE)

                // ----- Assert -----
                result[0].id shouldBe 2
                result[1].id shouldBe 1
            }
        }

        @Nested
        inner class Author {

            @Test
            fun `sorts editions by first author name ascending`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        authors = listOf(Author(id = 1, name = "Tolkien")),
                    ),
                    buildEdition(
                        id = 2,
                        authors = listOf(Author(id = 2, name = "Adams")),
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.AUTHOR)

                // ----- Assert -----
                result[0].id shouldBe 2
                result[1].id shouldBe 1
            }
        }

        @Nested
        inner class PageCount {

            @Test
            fun `sorts editions by page count descending`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        pages = 200,
                    ),
                    buildEdition(
                        id = 2,
                        pages = 600,
                    ),
                    buildEdition(
                        id = 3,
                        pages = null,
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.PAGE_COUNT)

                // ----- Assert -----
                result[0].id shouldBe 2
                result[1].id shouldBe 1
                result[2].id shouldBe 3
            }
        }

        @Nested
        inner class UnchangedModes {

            @Test
            fun `DATE_ADDED returns list unchanged`() {
                // ----- Arrange -----
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.DATE_ADDED)

                // ----- Assert -----
                result shouldBe editions
            }

            @Test
            fun `DATE_FINISHED returns list unchanged`() {
                // ----- Arrange -----
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.DATE_FINISHED)

                // ----- Assert -----
                result shouldBe editions
            }

            @Test
            fun `RATING returns list unchanged`() {
                // ----- Arrange -----
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.RATING)

                // ----- Assert -----
                result shouldBe editions
            }

            @Test
            fun `PROGRESS returns list unchanged`() {
                // ----- Arrange -----
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.PROGRESS)

                // ----- Assert -----
                result shouldBe editions
            }

            @Test
            fun `DEADLINE_URGENCY returns list unchanged`() {
                // ----- Arrange -----
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.DEADLINE_URGENCY)

                // ----- Assert -----
                result shouldBe editions
            }
        }
    }
}
