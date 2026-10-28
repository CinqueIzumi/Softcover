package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ReadingActivityMapperTest {
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

    private fun day(iso: String) = LocalDate.parse(iso)

    private fun activityOf(
        updates: List<ProgressUpdate>,
        readBooks: List<Book> = emptyList(),
        timeZone: TimeZone = TimeZone.UTC,
    ) = toReadingActivity(
        updates = updates,
        readBooks = readBooks,
        timeZone = timeZone,
    )

    @Nested
    inner class PagesByDay {
        @Test
        fun `keeps days from every year`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2025-12-31T10:00:00Z",
                    pagesRead = 10,
                    pages = 10,
                ),
                update(
                    bookId = 1,
                    at = "2026-01-01T10:00:00Z",
                    pagesRead = 20,
                    pages = 30,
                ),
            )

            // ----- Act -----
            val result = activityOf(updates = updates)

            // ----- Assert -----
            result.pagesByDay shouldBe mapOf(
                day("2025-12-31") to 10,
                day("2026-01-01") to 20,
            )
        }

        @Test
        fun `leaves out days without pages`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 0,
                ),
            )

            // ----- Act -----
            val result = activityOf(updates = updates)

            // ----- Assert -----
            result.pagesByDay.keys.shouldBeEmpty()
            result.streaks.shouldBeEmpty()
        }

        @Test
        fun `sums updates and the remainder of a day`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T08:00:00Z",
                    pagesRead = 40,
                    pages = 40,
                ),
                update(
                    bookId = 1,
                    at = "2026-03-10T20:00:00Z",
                    pagesRead = 60,
                    pages = 100,
                ),
                update(
                    bookId = 2,
                    at = "2026-03-10T09:00:00Z",
                    pagesRead = 15,
                    pages = 15,
                ),
            )
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-10",
                    pages = 130,
                ),
            )

            // ----- Act -----
            val result = activityOf(
                updates = updates,
                readBooks = books,
            )

            // ----- Assert -----
            result.pagesByDay[day("2026-03-10")] shouldBe 145
        }

        @Test
        fun `buckets an update into the local day of the time zone`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T23:30:00Z",
                    pagesRead = 12,
                ),
            )

            // ----- Act -----
            val result = activityOf(
                updates = updates,
                timeZone = TimeZone.of("Europe/Amsterdam"),
            )

            // ----- Assert -----
            result.pagesByDay.keys shouldBe setOf(day("2026-03-11"))
        }
    }

    @Nested
    inner class Streaks {
        @Test
        fun `merges consecutive days across a year boundary into one run`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2025-12-31T10:00:00Z",
                    pagesRead = 5,
                ),
                update(
                    bookId = 1,
                    at = "2026-01-01T10:00:00Z",
                    pagesRead = 5,
                ),
            )

            // ----- Act -----
            val result = activityOf(updates = updates)

            // ----- Assert -----
            result.streaks shouldBe listOf(
                ReadingStreak(
                    start = day("2025-12-31"),
                    end = day("2026-01-01"),
                ),
            )
        }

        @Test
        fun `a gap of a day starts a separate run, oldest first`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-05-10T10:00:00Z",
                    pagesRead = 5,
                ),
                update(
                    bookId = 1,
                    at = "2026-05-03T10:00:00Z",
                    pagesRead = 5,
                ),
                update(
                    bookId = 1,
                    at = "2026-05-04T10:00:00Z",
                    pagesRead = 5,
                ),
            )

            // ----- Act -----
            val result = activityOf(updates = updates)

            // ----- Assert -----
            result.streaks shouldBe listOf(
                ReadingStreak(
                    start = day("2026-05-03"),
                    end = day("2026-05-04"),
                ),
                ReadingStreak(
                    start = day("2026-05-10"),
                    end = day("2026-05-10"),
                ),
            )
        }

        @Test
        fun `a zero-page day breaks a run`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-05-03T10:00:00Z",
                    pagesRead = 5,
                ),
                update(
                    bookId = 1,
                    at = "2026-05-04T10:00:00Z",
                    pagesRead = 0,
                ),
                update(
                    bookId = 1,
                    at = "2026-05-05T10:00:00Z",
                    pagesRead = 5,
                ),
            )

            // ----- Act -----
            val result = activityOf(updates = updates)

            // ----- Assert -----
            result.streaks.size shouldBe 2
        }
    }
}
