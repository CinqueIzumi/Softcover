package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages
import nl.rhaydus.softcover.core.profile.domain.model.PageCreditInputs
import nl.rhaydus.softcover.core.profile.domain.model.PagesRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PagesReadMapperTest {
    private val march = StatsRange.Bounded(
        start = LocalDate(
            year = 2026,
            month = 3,
            day = 1,
        ),
        endInclusive = LocalDate(
            year = 2026,
            month = 3,
            day = 31,
        ),
    )

    private val afterMarch = LocalDate(
        year = 2026,
        month = 6,
        day = 1,
    )

    private fun pagesRead(
        range: StatsRange,
        today: LocalDate = afterMarch,
        updates: List<ProgressUpdate> = emptyList(),
        readBooks: List<Book> = emptyList(),
        didNotFinishBooks: List<Book> = emptyList(),
    ): PagesRead = toPagesRead(
        inputs = PageCreditInputs(
            updates = updates,
            readBooks = readBooks,
            didNotFinishBooks = didNotFinishBooks,
        ),
        range = range,
        today = today,
        timeZone = TimeZone.UTC,
    )

    private fun stoppedAt(
        id: Int,
        stoppedAt: String?,
        page: Int,
    ): Book = dnfBook(
        id = id,
        stoppedAt = stoppedAt,
        pages = 500,
        read = unfinishedRead(
            id = id,
            currentPage = page,
        ),
    )

    @Nested
    inner class ToPagesRead {
        @Test
        fun `sums logged pages and both kinds of remainder dated in the range`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-03-02T10:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
                progressUpdate(
                    bookId = 9,
                    at = "2026-03-20T10:00:00Z",
                    pagesRead = 15,
                    pages = 15,
                ),
            )

            val finished = readBook(
                id = 1,
                finishedAt = "2026-03-10",
                pages = 250,
            )

            val stopped = stoppedAt(
                id = 2,
                stoppedAt = "2026-03-15T12:00:00.000000",
                page = 60,
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
                readBooks = listOf(finished),
                didNotFinishBooks = listOf(stopped),
            )

            // ----- Assert -----
            result.total shouldBe 100 + 150 + 15 + 60
        }

        @Test
        fun `leaves out credits dated outside the range`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 9,
                    at = "2026-02-28T10:00:00Z",
                    pagesRead = 40,
                    pages = 40,
                ),
                progressUpdate(
                    bookId = 9,
                    at = "2026-04-01T10:00:00Z",
                    pagesRead = 50,
                    pages = 90,
                ),
            )

            val finished = readBook(
                id = 1,
                finishedAt = "2026-04-10",
                pages = 250,
            )

            val stopped = stoppedAt(
                id = 2,
                stoppedAt = "2026-02-10T12:00:00.000000",
                page = 60,
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
                readBooks = listOf(finished),
                didNotFinishBooks = listOf(stopped),
            )

            // ----- Assert -----
            result.total shouldBe 0
        }

        @Test
        fun `equals the sum of the monthly pages for the same inputs`() {
            // ----- Arrange -----
            val range = StatsRange.Bounded(
                start = LocalDate(
                    year = 2026,
                    month = 1,
                    day = 1,
                ),
                endInclusive = LocalDate(
                    year = 2026,
                    month = 12,
                    day = 31,
                ),
            )

            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-01-05T10:00:00Z",
                    pagesRead = 70,
                    pages = 70,
                ),
                progressUpdate(
                    bookId = 9,
                    at = "2026-09-05T10:00:00Z",
                    pagesRead = 33,
                    pages = 33,
                ),
            )

            val finished = readBook(
                id = 1,
                finishedAt = "2026-05-10",
                pages = 250,
            )

            val stopped = stoppedAt(
                id = 2,
                stoppedAt = "2026-07-15T12:00:00.000000",
                page = 60,
            )

            // ----- Act -----
            val result = pagesRead(
                range = range,
                updates = updates,
                readBooks = listOf(finished),
                didNotFinishBooks = listOf(stopped),
            )

            val monthly = toMonthlyPages(
                updates = updates,
                readBooks = listOf(finished),
                didNotFinishBooks = listOf(stopped),
                range = range,
                timeZone = TimeZone.UTC,
            ) as MonthlyPages.Span

            // ----- Assert -----
            result.total shouldBe monthly.months.sumOf { it.count }
        }

        @Test
        fun `counts finished and stopped books by their finish and stop date in a bounded range`() {
            // ----- Arrange -----
            val readBooks = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-10",
                    pages = 100,
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-03-31",
                    pages = 100,
                ),
                readBook(
                    id = 3,
                    finishedAt = "2026-04-01",
                    pages = 100,
                ),
                readBook(id = 4),
            )

            val stoppedBooks = listOf(
                stoppedAt(
                    id = 5,
                    stoppedAt = "2026-03-01T12:00:00.000000",
                    page = 20,
                ),
                stoppedAt(
                    id = 6,
                    stoppedAt = "2026-02-28T12:00:00.000000",
                    page = 20,
                ),
                stoppedAt(
                    id = 7,
                    stoppedAt = null,
                    page = 20,
                ),
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                readBooks = readBooks,
                didNotFinishBooks = stoppedBooks,
            )

            // ----- Assert -----
            result.finishedBookCount shouldBe 2
            result.stoppedBookCount shouldBe 1
        }

        @Test
        fun `counts a finished book that was fully logged even though it adds no remainder`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-03-05T10:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
            )

            val finished = readBook(
                id = 1,
                finishedAt = "2026-03-10",
                pages = 100,
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
                readBooks = listOf(finished),
            )

            // ----- Assert -----
            result.finishedBookCount shouldBe 1
            result.total shouldBe 100
        }

        @Test
        fun `an unbounded range counts every read book and every stopped book, dated or not`() {
            // ----- Arrange -----
            val readBooks = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2020-03-10",
                    pages = 100,
                ),
                readBook(id = 2),
            )

            val stoppedBooks = listOf(
                stoppedAt(
                    id = 3,
                    stoppedAt = "2021-03-01T12:00:00.000000",
                    page = 20,
                ),
                stoppedAt(
                    id = 4,
                    stoppedAt = null,
                    page = 20,
                ),
            )

            // ----- Act -----
            val result = pagesRead(
                range = StatsRange.Unbounded,
                readBooks = readBooks,
                didNotFinishBooks = stoppedBooks,
            )

            // ----- Assert -----
            result.finishedBookCount shouldBe 2
            result.stoppedBookCount shouldBe 2
        }

        @Test
        fun `is true when pages of a book that neither finished nor stopped in the range are credited`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 9,
                    at = "2026-03-20T10:00:00Z",
                    pagesRead = 15,
                    pages = 15,
                ),
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
            )

            // ----- Assert -----
            result.hasInProgressPages shouldBe true
        }

        @Test
        fun `is true for logged pages of a book that finishes after the range`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-03-20T10:00:00Z",
                    pagesRead = 15,
                    pages = 15,
                ),
            )

            val finished = readBook(
                id = 1,
                finishedAt = "2026-04-10",
                pages = 100,
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
                readBooks = listOf(finished),
            )

            // ----- Assert -----
            result.hasInProgressPages shouldBe true
        }

        @Test
        fun `is false when every credited page belongs to a book that finished or stopped in the range`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-03-05T10:00:00Z",
                    pagesRead = 40,
                    pages = 40,
                ),
                progressUpdate(
                    bookId = 2,
                    at = "2026-03-06T10:00:00Z",
                    pagesRead = 10,
                    pages = 10,
                ),
            )

            val finished = readBook(
                id = 1,
                finishedAt = "2026-03-10",
                pages = 100,
            )

            val stopped = stoppedAt(
                id = 2,
                stoppedAt = "2026-03-15T12:00:00.000000",
                page = 30,
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
                readBooks = listOf(finished),
                didNotFinishBooks = listOf(stopped),
            )

            // ----- Assert -----
            result.hasInProgressPages shouldBe false
        }

        @Test
        fun `is false when the in-progress pages fall outside the range`() {
            // ----- Arrange -----
            val updates = listOf(
                progressUpdate(
                    bookId = 9,
                    at = "2026-02-20T10:00:00Z",
                    pagesRead = 15,
                    pages = 15,
                ),
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
            )

            // ----- Assert -----
            result.hasInProgressPages shouldBe false
        }

        private fun updatesOf(vararg pagesRead: Int): List<ProgressUpdate> = pagesRead.mapIndexed { index, pages ->
            progressUpdate(
                bookId = 9,
                at = "2026-03-${(index + 1).toString().padStart(
                    2,
                    '0',
                )}T10:00:00Z",
                pagesRead = pages,
                pages = pages,
            )
        }

        @Test
        fun `a bounded range averages over all its days`() {
            // ----- Arrange -----
            val updates = updatesOf(310)

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
            )

            // ----- Assert -----
            result.pagesPerDay shouldBe 10
        }

        @Test
        fun `a bounded range still running averages only over the days up to today`() {
            // ----- Arrange -----
            val updates = updatesOf(100)

            val today = LocalDate(
                year = 2026,
                month = 3,
                day = 10,
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                today = today,
                updates = updates,
            )

            // ----- Assert -----
            result.pagesPerDay shouldBe 10
        }

        @Test
        fun `a bounded range that has not started has no average`() {
            // ----- Arrange -----
            val updates = updatesOf(100)

            val today = LocalDate(
                year = 2026,
                month = 2,
                day = 20,
            )

            // ----- Act -----
            val result = pagesRead(
                range = march,
                today = today,
                updates = updates,
            )

            // ----- Assert -----
            result.pagesPerDay.shouldBeNull()
        }

        @Test
        fun `an unbounded range averages from the first credited day to today`() {
            // ----- Arrange -----
            val updates = updatesOf(
                60,
                40,
            )

            val today = LocalDate(
                year = 2026,
                month = 3,
                day = 10,
            )

            // ----- Act -----
            val result = pagesRead(
                range = StatsRange.Unbounded,
                today = today,
                updates = updates,
            )

            // ----- Assert -----
            result.pagesPerDay shouldBe 10
        }

        @Test
        fun `has no average when nothing was read`() {
            // ----- Arrange -----
            // ----- Act -----
            val bounded = pagesRead(range = march)
            val unbounded = pagesRead(range = StatsRange.Unbounded)

            // ----- Assert -----
            bounded.pagesPerDay.shouldBeNull()
            unbounded.pagesPerDay.shouldBeNull()
        }

        @Test
        fun `has no average when it rounds below one page a day`() {
            // ----- Arrange -----
            val updates = updatesOf(10)

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
            )

            // ----- Assert -----
            result.total shouldBe 10
            result.pagesPerDay.shouldBeNull()
        }

        @Test
        fun `rounds the average to the nearest page`() {
            // ----- Arrange -----
            val updates = updatesOf(46)

            // ----- Act -----
            val result = pagesRead(
                range = march,
                updates = updates,
            )

            // ----- Assert -----
            result.pagesPerDay shouldBe 1
        }
    }
}
