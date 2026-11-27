package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDay
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDayBook
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDayEnding
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ReadingDayMapperTest {
    private val day = LocalDate(
        year = 2026,
        month = 6,
        day = 10,
    )

    private fun update(
        bookId: Int,
        at: String,
        pagesRead: Int,
        pages: Int? = null,
    ) = ProgressUpdate(
        bookId = bookId,
        at = Instant.parse(at),
        pagesRead = pagesRead,
        pages = pages,
    )

    private fun readingDay(
        updates: List<ProgressUpdate>,
        books: List<Book> = emptyList(),
        didNotFinishBooks: List<Book> = emptyList(),
    ): ReadingDay = toReadingDay(
        updates = updates,
        readBooks = books,
        didNotFinishBooks = didNotFinishBooks,
        date = day,
        timeZone = TimeZone.UTC,
    )

    private fun ReadingDay.bookFor(bookId: Int): ReadingDayBook = books.firstOrNull { it.bookId == bookId }.shouldNotBeNull()

    @Nested
    inner class Positions {
        @Test
        fun `spans from before the first update to the last position of the day`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T10:00:00Z",
                    pagesRead = 50,
                    pages = 150,
                ),
                update(
                    bookId = 1,
                    at = "2026-06-10T14:00:00Z",
                    pagesRead = 30,
                    pages = 180,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.books shouldBe listOf(
                ReadingDayBook(
                    bookId = 1,
                    fromPage = 100,
                    toPage = 180,
                    updateCount = 2,
                    pages = 80,
                    ending = ReadingDayEnding.NONE,
                    firstUpdateAt = LocalTime(
                        hour = 10,
                        minute = 0,
                    ),
                    lastUpdateAt = LocalTime(
                        hour = 14,
                        minute = 0,
                    ),
                ),
            )
        }

        @Test
        fun `starts at page zero for the first ever update`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T10:00:00Z",
                    pagesRead = 40,
                    pages = 40,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.bookFor(1).fromPage shouldBe 0
        }

        @Test
        fun `derives the start of a re-read from the row, not the higher previous row`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-05-01T10:00:00Z",
                    pagesRead = 300,
                    pages = 300,
                ),
                update(
                    bookId = 1,
                    at = "2026-06-10T10:00:00Z",
                    pagesRead = 20,
                    pages = 70,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.bookFor(1).fromPage shouldBe 50
        }

        @Test
        fun `falls back to the last earlier position when the first update of the day has no page`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-01T10:00:00Z",
                    pagesRead = 120,
                    pages = 120,
                ),
                update(
                    bookId = 1,
                    at = "2026-06-10T10:00:00Z",
                    pagesRead = 30,
                    pages = null,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.bookFor(1).fromPage shouldBe 120
        }

        @Test
        fun `shows a remainder only finished book from its last position to its page count`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-01T10:00:00Z",
                    pagesRead = 250,
                    pages = 250,
                ),
            )

            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-06-10",
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = updates,
                books = books,
            )

            // ----- Assert -----
            result shouldBe ReadingDay(
                date = day,
                books = listOf(
                    ReadingDayBook(
                        bookId = 1,
                        fromPage = 250,
                        toPage = 300,
                        updateCount = 0,
                        pages = 50,
                        ending = ReadingDayEnding.FINISHED,
                        firstUpdateAt = null,
                        lastUpdateAt = null,
                    ),
                ),
            )
        }

        @Test
        fun `ends a finished book with updates that day at its page count`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T10:00:00Z",
                    pagesRead = 80,
                    pages = 280,
                ),
            )

            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-06-10",
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = updates,
                books = books,
            )

            // ----- Assert -----
            result.books shouldBe listOf(
                ReadingDayBook(
                    bookId = 1,
                    fromPage = 200,
                    toPage = 300,
                    updateCount = 1,
                    pages = 100,
                    ending = ReadingDayEnding.FINISHED,
                    firstUpdateAt = LocalTime(
                        hour = 10,
                        minute = 0,
                    ),
                    lastUpdateAt = LocalTime(
                        hour = 10,
                        minute = 0,
                    ),
                ),
            )
        }

        private val stoppedOnDay = "2026-06-10T12:00:00.000000"

        @Test
        fun `a stopped book ends at its stop page on the stop day`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 2,
                    at = "2026-06-01T10:00:00Z",
                    pagesRead = 80,
                    pages = 80,
                ),
            )

            val stopped = dnfBook(
                id = 2,
                stoppedAt = stoppedOnDay,
                pages = 300,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 120,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = updates,
                didNotFinishBooks = listOf(stopped),
            )

            // ----- Assert -----
            result shouldBe ReadingDay(
                date = day,
                books = listOf(
                    ReadingDayBook(
                        bookId = 2,
                        fromPage = 80,
                        toPage = 120,
                        updateCount = 0,
                        pages = 40,
                        ending = ReadingDayEnding.STOPPED,
                        firstUpdateAt = null,
                        lastUpdateAt = null,
                    ),
                ),
            )
        }

        @Test
        fun `a stopped book with updates that day still ends at its stop page`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 2,
                    at = "2026-06-10T09:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
            )

            val stopped = dnfBook(
                id = 2,
                stoppedAt = stoppedOnDay,
                pages = 300,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 150,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = updates,
                didNotFinishBooks = listOf(stopped),
            )

            // ----- Assert -----
            val book = result.bookFor(2)

            book.ending shouldBe ReadingDayEnding.STOPPED
            book.toPage shouldBe 150
            book.pages shouldBe 150
            book.updateCount shouldBe 1
        }

        @Test
        fun `a stopped book with no stop remainder still ends as stopped when it has an update that day`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 2,
                    at = "2026-06-10T09:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
            )

            val stopped = dnfBook(
                id = 2,
                stoppedAt = stoppedOnDay,
                pages = 300,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 100,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = updates,
                didNotFinishBooks = listOf(stopped),
            )

            // ----- Assert -----
            val book = result.bookFor(2)

            book.ending shouldBe ReadingDayEnding.STOPPED
            book.toPage shouldBe 100
            book.pages shouldBe 100
            book.updateCount shouldBe 1
        }

        @Test
        fun `a stopped book whose last read date is the stop day is not marked finished`() {
            // ----- Arrange -----
            val stopped = dnfBook(
                id = 2,
                stoppedAt = stoppedOnDay,
                pages = 300,
                lastReadDate = "2026-06-10",
                read = unfinishedRead(
                    id = 2,
                    currentPage = 120,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = emptyList(),
                didNotFinishBooks = listOf(stopped),
            )

            // ----- Assert -----
            result.bookFor(2).ending shouldBe ReadingDayEnding.STOPPED
        }

        @Test
        fun `a book with only logged updates has no ending`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 3,
                    at = "2026-06-10T09:00:00Z",
                    pagesRead = 20,
                    pages = 20,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.bookFor(3).ending shouldBe ReadingDayEnding.NONE
        }
    }

    @Nested
    inner class Counting {
        @Test
        fun `ignores seconds only updates in the update count`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T10:00:00Z",
                    pagesRead = 0,
                ),
                update(
                    bookId = 1,
                    at = "2026-06-10T11:00:00Z",
                    pagesRead = 25,
                    pages = 25,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.bookFor(1).updateCount shouldBe 1
        }

        @Test
        fun `is empty when nothing was read on the date`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-11T10:00:00Z",
                    pagesRead = 25,
                    pages = 25,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.books.shouldBeEmpty()
        }

        @Test
        fun `books pages add up to the daily total for the date`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T08:00:00Z",
                    pagesRead = 40,
                    pages = 240,
                ),
                update(
                    bookId = 2,
                    at = "2026-06-10T09:00:00Z",
                    pagesRead = 15,
                    pages = 15,
                ),
                update(
                    bookId = 3,
                    at = "2026-06-01T09:00:00Z",
                    pagesRead = 90,
                    pages = 90,
                ),
            )

            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-06-10",
                    pages = 300,
                ),
                readBook(
                    id = 3,
                    finishedAt = "2026-06-10",
                    pages = 120,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = updates,
                books = books,
            )

            val activity = toReadingActivity(
                updates = updates,
                readBooks = books,
                didNotFinishBooks = emptyList(),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.books.sumOf { it.pages } shouldBe activity.pagesByDay[day]
        }
    }

    @Nested
    inner class Ordering {
        @Test
        fun `puts the book with the most pages first`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T08:00:00Z",
                    pagesRead = 10,
                    pages = 10,
                ),
                update(
                    bookId = 2,
                    at = "2026-06-10T09:00:00Z",
                    pagesRead = 50,
                    pages = 50,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.books.map { it.bookId } shouldBe listOf(2, 1)
        }

        @Test
        fun `breaks a tie by the earliest update`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 3,
                    at = "2026-06-10T12:00:00Z",
                    pagesRead = 20,
                    pages = 20,
                ),
                update(
                    bookId = 4,
                    at = "2026-06-10T09:00:00Z",
                    pagesRead = 20,
                    pages = 20,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.books.map { it.bookId } shouldBe listOf(4, 3)
        }

        @Test
        fun `places a remainder only book after a tied book with updates`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 4,
                    at = "2026-06-10T09:00:00Z",
                    pagesRead = 20,
                    pages = 20,
                ),
                update(
                    bookId = 5,
                    at = "2026-06-01T09:00:00Z",
                    pagesRead = 80,
                    pages = 80,
                ),
            )

            val books = listOf(
                readBook(
                    id = 5,
                    finishedAt = "2026-06-10",
                    pages = 100,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = updates,
                books = books,
            )

            // ----- Assert -----
            result.books.map { it.bookId } shouldBe listOf(4, 5)
        }
    }

    @Nested
    inner class Times {
        @Test
        fun `spans each book from its earliest to its latest counted update`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T14:30:00Z",
                    pagesRead = 10,
                    pages = 10,
                ),
                update(
                    bookId = 2,
                    at = "2026-06-10T21:05:00Z",
                    pagesRead = 10,
                    pages = 20,
                ),
                update(
                    bookId = 2,
                    at = "2026-06-10T07:15:00Z",
                    pagesRead = 10,
                    pages = 10,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.bookFor(1).firstUpdateAt shouldBe LocalTime(
                hour = 14,
                minute = 30,
            )

            result.bookFor(1).lastUpdateAt shouldBe LocalTime(
                hour = 14,
                minute = 30,
            )

            result.bookFor(2).firstUpdateAt shouldBe LocalTime(
                hour = 7,
                minute = 15,
            )

            result.bookFor(2).lastUpdateAt shouldBe LocalTime(
                hour = 21,
                minute = 5,
            )
        }

        @Test
        fun `ignores a seconds only update when finding the latest time`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T07:15:00Z",
                    pagesRead = 10,
                    pages = 10,
                ),
                update(
                    bookId = 1,
                    at = "2026-06-10T23:00:00Z",
                    pagesRead = 0,
                ),
            )

            // ----- Act -----
            val result = readingDay(updates = updates)

            // ----- Assert -----
            result.bookFor(1).lastUpdateAt shouldBe LocalTime(
                hour = 7,
                minute = 15,
            )
        }

        @Test
        fun `has no times for a book that only holds a remainder`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-06-10",
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = readingDay(
                updates = emptyList(),
                books = books,
            )

            // ----- Assert -----
            result.bookFor(1).firstUpdateAt.shouldBeNull()
            result.bookFor(1).lastUpdateAt.shouldBeNull()
        }
    }
}
