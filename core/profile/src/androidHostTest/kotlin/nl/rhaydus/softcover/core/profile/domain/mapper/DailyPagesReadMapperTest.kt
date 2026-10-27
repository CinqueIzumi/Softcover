package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.resolve
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class DailyPagesReadMapperTest {
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

    private fun date(
        month: Int,
        day: Int,
        year: Int = 2026,
    ) = LocalDate(
        year = year,
        month = month,
        day = day,
    )

    @Nested
    inner class Folding {
        @Test
        fun `keeps only the requested year`() {
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
            val result = toDailyPages(
                updates = updates,
                readBooks = emptyList(),
                year = 2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            val newYearsDay = date(
                month = 1,
                day = 1,
            )

            result.pages shouldBe mapOf(newYearsDay to 20)
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
            val result = toDailyPages(
                updates = updates,
                readBooks = emptyList(),
                year = 2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.pages.keys.shouldBeEmpty()
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
            val result = toDailyPages(
                updates = updates,
                readBooks = books,
                year = 2026,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            val finishDay = date(
                month = 3,
                day = 10,
            )

            result.pages[finishDay] shouldBe 145
        }
    }

    @Nested
    inner class Consistency {
        @Test
        fun `month sums of the year equal the monthly columns`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-01-05T10:00:00Z",
                    pagesRead = 50,
                    pages = 50,
                ),
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 70,
                    pages = 120,
                ),
                update(
                    bookId = 2,
                    at = "2026-03-20T10:00:00Z",
                    pagesRead = 30,
                    pages = 30,
                ),
                update(
                    bookId = 2,
                    at = "2026-11-02T10:00:00Z",
                    pagesRead = 10,
                    pages = 40,
                ),
            )
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-04-01",
                    pages = 200,
                ),
            )
            val lastDayOfYear = date(
                month = 12,
                day = 31,
            )
            val range = StatsScope.CalendarYear(2026).resolve(lastDayOfYear)

            // ----- Act -----
            val daily = toDailyPages(
                updates = updates,
                readBooks = books,
                year = 2026,
                timeZone = TimeZone.UTC,
            )
            val monthly = toMonthlyPages(
                updates = updates,
                readBooks = books,
                range = range,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            val dailyByMonth = (1..12).map { month ->
                daily.pages.filterKeys { it.month.ordinal + 1 == month }.values.sum()
            }
            dailyByMonth shouldBe (monthly as MonthlyPages.Span).months.map { it.count }
        }
    }
}
