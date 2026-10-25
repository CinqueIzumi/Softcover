package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.domain.model.resolve
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class MonthlyPagesReadMapperTest {
    private val year2026 = StatsScope.CalendarYear(2026).resolve(
        LocalDate(
            year = 2026,
            month = 10,
            day = 23,
        ),
    )

    private fun update(
        bookId: Int,
        at: String,
        pagesRead: Int,
        pages: Int? = null,
    ) = ProgressUpdate(
        bookId = bookId,
        at = Instant.parse(at),
        pages = pages,
        pagesRead = pagesRead,
    )

    private fun spanCounts(
        result: MonthlyPages,
    ): List<Triple<Int, Int, Int>> = (result as MonthlyPages.Span).months.map {
        Triple(
            it.year,
            it.month,
            it.count,
        )
    }

    private fun monthsTouched(
        scope: StatsScope,
        today: LocalDate,
    ): List<Pair<Int, Int>> {
        val result = toMonthlyPages(
            updates = emptyList(),
            readBooks = emptyList(),
            range = scope.resolve(today),
            timeZone = TimeZone.UTC,
        )

        return (result as MonthlyPages.Span).months.map { it.year to it.month }
    }

    @Nested
    inner class Counting {
        @Test
        fun `sums each update into the month it was logged`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 200,
                    pages = 200,
                ),
                update(
                    bookId = 1,
                    at = "2026-04-10T10:00:00Z",
                    pagesRead = 200,
                    pages = 400,
                ),
            )
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-04-12",
                    pages = 400,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = updates,
                readBooks = books,
                range = year2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            val months = spanCounts(result)
            months[2] shouldBe Triple(
                2026,
                3,
                200,
            )
            months[3] shouldBe Triple(
                2026,
                4,
                200,
            )
        }

        @Test
        fun `counts updates on a book that is not finished`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 7,
                    at = "2026-05-02T10:00:00Z",
                    pagesRead = 55,
                    pages = 55,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = updates,
                readBooks = emptyList(),
                range = year2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            spanCounts(result)[4] shouldBe Triple(
                2026,
                5,
                55,
            )
        }

        @Test
        fun `adds the unlogged remainder of a finished book on its finish date`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
            )
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-04-20",
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = updates,
                readBooks = books,
                range = year2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            val months = spanCounts(result)
            months[2] shouldBe Triple(
                2026,
                3,
                100,
            )
            months[3] shouldBe Triple(
                2026,
                4,
                200,
            )
        }

        @Test
        fun `measures the remainder against the reader's own edition first`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 300,
                    pages = 300,
                ),
            )
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-04-20",
                    pages = 300,
                    editions = listOf(
                        edition(
                            id = 5,
                            pages = 400,
                        ),
                    ),
                    editionId = 5,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = updates,
                readBooks = books,
                range = year2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            spanCounts(result)[3] shouldBe Triple(
                2026,
                4,
                100,
            )
        }

        @Test
        fun `puts every page of a finished book with no updates on its finish date`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-06-15",
                    pages = 320,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = emptyList(),
                readBooks = books,
                range = year2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            spanCounts(result)[5] shouldBe Triple(
                2026,
                6,
                320,
            )
        }

        @Test
        fun `leaves out updates and finishes outside the range`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2025-12-31T10:00:00Z",
                    pagesRead = 90,
                    pages = 90,
                ),
            )
            val books = listOf(
                readBook(
                    id = 2,
                    finishedAt = "2025-11-05",
                    pages = 250,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = updates,
                readBooks = books,
                range = year2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            spanCounts(result).map { it.third } shouldBe List(12) { 0 }
        }

        @Test
        fun `buckets by the local date in the given time zone`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-31T23:30:00Z",
                    pagesRead = 40,
                    pages = 40,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = updates,
                readBooks = emptyList(),
                range = year2026,
                timeZone = TimeZone.of("Europe/Amsterdam"),
            )

            // ----- Assert -----
            val months = spanCounts(result)
            months[2] shouldBe Triple(
                2026,
                3,
                0,
            )
            months[3] shouldBe Triple(
                2026,
                4,
                40,
            )
        }
    }

    @Nested
    inner class Ranges {
        @Test
        fun `a calendar year spans January to December`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2025)
            val today = LocalDate(
                year = 2026,
                month = 10,
                day = 23,
            )

            // ----- Act -----
            val result = monthsTouched(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe (1..12).map { 2025 to it }
        }

        @Test
        fun `a twelve-month window includes the partial first month for thirteen columns`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)
            val today = LocalDate(
                year = 2026,
                month = 10,
                day = 23,
            )

            // ----- Act -----
            val result = monthsTouched(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result.size shouldBe 13
            result.first() shouldBe (2025 to 10)
            result.last() shouldBe (2026 to 10)
        }

        @Test
        fun `a twelve-month window ending on a month's last day spans twelve months`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)
            val today = LocalDate(
                year = 2026,
                month = 10,
                day = 31,
            )

            // ----- Act -----
            val result = monthsTouched(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result.size shouldBe 12
            result.first() shouldBe (2025 to 11)
        }

        @Test
        fun `thirty days on the first of March spans January to March`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS)
            val today = LocalDate(
                year = 2026,
                month = 3,
                day = 1,
            )

            // ----- Act -----
            val result = monthsTouched(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe listOf(
                2026 to 1,
                2026 to 2,
                2026 to 3,
            )
        }
    }

    @Nested
    inner class AllTime {
        @Test
        fun `sums each calendar month across years`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2024-03-10T10:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
                update(
                    bookId = 2,
                    at = "2026-03-12T10:00:00Z",
                    pagesRead = 50,
                    pages = 50,
                ),
                update(
                    bookId = 2,
                    at = "2025-12-01T10:00:00Z",
                    pagesRead = 30,
                    pages = 80,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = updates,
                readBooks = emptyList(),
                range = StatsRange.Unbounded,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            val expected = MonthlyPages.AcrossYears(
                pages = List(12) {
                    when (it) {
                        2 -> 150
                        11 -> 30
                        else -> 0
                    }
                },
            )
            result shouldBe expected
        }

        @Test
        fun `counts the pages of a finished book with no updates in its finish month`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2019-08-09",
                    pages = 210,
                ),
            )

            // ----- Act -----
            val result = toMonthlyPages(
                updates = emptyList(),
                readBooks = books,
                range = StatsRange.Unbounded,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            (result as MonthlyPages.AcrossYears).pages[7] shouldBe 210
        }
    }
}
