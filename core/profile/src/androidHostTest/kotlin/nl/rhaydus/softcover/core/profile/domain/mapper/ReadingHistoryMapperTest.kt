package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingHistoryMapperTest {
    private val today = LocalDate(
        2026,
        10,
        23,
    )

    private fun historyOf(
        readBooks: List<Book>,
        didNotFinishBooks: List<Book> = emptyList(),
        updates: List<ProgressUpdate> = emptyList(),
    ): ReadingHistory = toReadingHistory(
        updates = updates,
        readBooks = readBooks,
        didNotFinishBooks = didNotFinishBooks,
        today = today,
        timeZone = TimeZone.UTC,
    )

    @Nested
    inner class Years {
        @Test
        fun `zero-fills the years between the first finish year and the current year`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2023-05-01",
                ),
                readBook(
                    id = 2,
                    finishedAt = "2025-02-10",
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.booksByYear shouldBe listOf(
                YearCount(
                    year = 2023,
                    count = 1,
                ),
                YearCount(
                    year = 2024,
                    count = 0,
                ),
                YearCount(
                    year = 2025,
                    count = 1,
                ),
                YearCount(
                    year = 2026,
                    count = 0,
                ),
            )
        }

        @Test
        fun `is not capped at eight years`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2010-05-01",
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.booksByYear.size shouldBe 17
            history.booksByYear.first().year shouldBe 2010
            history.booksByYear.last().year shouldBe 2026
        }

        @Test
        fun `extends past the current year when a book finished later`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2027-01-02",
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.booksByYear.map { it.year } shouldBe listOf(2027)
            history.pagesByYear.map { it.year } shouldBe listOf(2027)
        }

        @Test
        fun `lists the years read distinct and newest first`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2024-01-01",
                ),
                readBook(
                    id = 2,
                    finishedAt = "2025-06-01",
                ),
                readBook(
                    id = 3,
                    finishedAt = "2024-09-01",
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.yearsRead shouldBe listOf(2025, 2024)
            history.trackedYears shouldBe 2
        }

        @Test
        fun `sums pages per year using the page fallback`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2025-01-01",
                    pages = 100,
                ),
                readBook(
                    id = 2,
                    finishedAt = "2025-06-01",
                    pages = 100,
                    editions = listOf(
                        edition(
                            id = 5,
                            pages = 250,
                        ),
                    ),
                    editionId = 5,
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.pagesByYear.first { it.year == 2025 }.count shouldBe 350
        }

        @Test
        fun `a re-read counts once, in its latest finish year`() {
            // ----- Arrange -----
            val reread = readBook(
                id = 1,
                reads = listOf(
                    finishedRead(
                        id = 1,
                        finishedAt = "2024-03-01",
                    ),
                    finishedRead(
                        id = 2,
                        finishedAt = "2025-03-01",
                    ),
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = listOf(reread))

            // ----- Assert -----
            history.yearsRead shouldBe listOf(2025)
            history.booksByYear.sumOf { it.count } shouldBe 1
        }

        @Test
        fun `a year with only stopped book pages extends the span and counts no books`() {
            // ----- Arrange -----
            val finished = readBook(
                id = 1,
                finishedAt = "2025-05-01",
                pages = 100,
            )

            val stopped = dnfBook(
                id = 2,
                stoppedAt = "2023-04-10T12:00:00.000000",
                pages = 400,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 60,
                ),
            )

            // ----- Act -----
            val history = historyOf(
                readBooks = listOf(finished),
                didNotFinishBooks = listOf(stopped),
            )

            // ----- Assert -----
            history.pagesByYear shouldBe listOf(
                YearCount(
                    year = 2023,
                    count = 60,
                ),
                YearCount(
                    year = 2024,
                    count = 0,
                ),
                YearCount(
                    year = 2025,
                    count = 100,
                ),
                YearCount(
                    year = 2026,
                    count = 0,
                ),
            )

            history.booksByYear.first { it.year == 2023 }.count shouldBe 0
            history.yearsRead shouldBe listOf(2025)
            history.trackedYears shouldBe 1
        }

        @Test
        fun `logged pages of an unfinished book extend the span to the year they were read`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 7,
                    at = "2022-08-01T10:00:00Z",
                    pagesRead = 30,
                    pages = 30,
                ),
            )

            // ----- Act -----
            val history = historyOf(
                readBooks = emptyList(),
                updates = updates,
            )

            // ----- Assert -----
            history.pagesByYear.first() shouldBe YearCount(
                year = 2022,
                count = 30,
            )

            history.pagesByYear.sumOf { it.count } shouldBe 30
            history.booksByYear.sumOf { it.count } shouldBe 0
            history.yearsRead shouldBe emptyList()
        }

        @Test
        fun `pages per year are the attributed credits of logged updates and remainders`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2025-02-01T10:00:00Z",
                    pagesRead = 40,
                    pages = 40,
                ),
            )

            val finished = readBook(
                id = 1,
                finishedAt = "2026-01-05",
                pages = 100,
            )

            // ----- Act -----
            val history = historyOf(
                readBooks = listOf(finished),
                updates = updates,
            )

            // ----- Assert -----
            history.pagesByYear shouldBe listOf(
                YearCount(
                    year = 2025,
                    count = 40,
                ),
                YearCount(
                    year = 2026,
                    count = 60,
                ),
            )
        }

        @Test
        fun `undated books yield no years`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1))

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.booksByYear shouldBe emptyList()
            history.pagesByYear shouldBe emptyList()
            history.yearsRead shouldBe emptyList()
            history.trackedYears shouldBe 0
        }
    }

    @Nested
    inner class PagesByMonth {
        @Test
        fun `covers all twelve months of the current year and ignores other years`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-05",
                    pages = 100,
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-03-20",
                    pages = 50,
                ),
                readBook(
                    id = 3,
                    finishedAt = "2025-03-05",
                    pages = 999,
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.pagesByMonth.map { it.month } shouldBe (1..12).toList()
            history.pagesByMonth.all { it.year == 2026 } shouldBe true
            history.pagesByMonth.first { it.month == 3 }.count shouldBe 150
            history.pagesByMonth.sumOf { it.count } shouldBe 150
        }

        @Test
        fun `counts stopped and unfinished book pages in the month they were credited`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 9,
                    at = "2026-05-02T10:00:00Z",
                    pagesRead = 25,
                    pages = 25,
                ),
            )

            val stopped = dnfBook(
                id = 2,
                stoppedAt = "2026-07-15T12:00:00.000000",
                pages = 400,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 90,
                ),
            )

            // ----- Act -----
            val history = historyOf(
                readBooks = emptyList(),
                didNotFinishBooks = listOf(stopped),
                updates = updates,
            )

            // ----- Assert -----
            history.pagesByMonth.filter { it.count > 0 } shouldBe listOf(
                MonthCount(
                    year = 2026,
                    month = 5,
                    count = 25,
                ),
                MonthCount(
                    year = 2026,
                    month = 7,
                    count = 90,
                ),
            )
        }

        @Test
        fun `monthly pages of the current year add up to its yearly pages`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-01-02T10:00:00Z",
                    pagesRead = 30,
                    pages = 30,
                ),
            )

            val finished = readBook(
                id = 1,
                finishedAt = "2026-02-03",
                pages = 100,
            )

            val stopped = dnfBook(
                id = 2,
                stoppedAt = "2026-04-10T12:00:00.000000",
                pages = 300,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 50,
                ),
            )

            // ----- Act -----
            val history = historyOf(
                readBooks = listOf(finished),
                didNotFinishBooks = listOf(stopped),
                updates = updates,
            )

            // ----- Assert -----
            history.pagesByMonth.sumOf { it.count } shouldBe 150
            history.pagesByMonth.sumOf { it.count } shouldBe history.pagesByYear.first { it.year == 2026 }.count
        }
    }

    @Nested
    inner class RecentlyLoved {
        @Test
        fun `keeps the latest three books rated four or higher, newest first`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-01",
                    rating = 5.0,
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-02-01",
                    rating = 4.0,
                ),
                readBook(
                    id = 3,
                    finishedAt = "2026-03-01",
                    rating = 3.5,
                ),
                readBook(
                    id = 4,
                    finishedAt = "2026-04-01",
                    rating = 4.5,
                ),
                readBook(
                    id = 5,
                    finishedAt = "2026-05-01",
                    rating = 4.0,
                ),
                readBook(
                    id = 6,
                    finishedAt = "2026-06-01",
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.recentlyLoved.map { it.bookId } shouldBe listOf(5, 4, 2)

            history.recentlyLoved.map { it.finishedOn } shouldBe listOf(
                LocalDate(
                    2026,
                    5,
                    1,
                ),
                LocalDate(
                    2026,
                    4,
                    1,
                ),
                LocalDate(
                    2026,
                    2,
                    1,
                ),
            )
        }

        @Test
        fun `lists fewer than three when fewer qualify`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-01",
                    rating = 3.5,
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-02-01",
                    rating = 4.0,
                ),
            )

            // ----- Act -----
            val history = historyOf(readBooks = books)

            // ----- Assert -----
            history.recentlyLoved.map { it.bookId } shouldBe listOf(2)
        }
    }
}
