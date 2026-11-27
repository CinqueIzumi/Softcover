package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingListStatsMapperTest {
    private val june = StatsRange.Bounded(
        start = LocalDate(
            year = 2026,
            month = 6,
            day = 1,
        ),
        endInclusive = LocalDate(
            year = 2026,
            month = 6,
            day = 30,
        ),
    )

    private fun listed(
        id: Int,
        added: String,
        reads: List<UserBookRead> = emptyList(),
    ): Book {
        val base = readBook(
            id = id,
            reads = reads,
        )

        return base.copy(
            userBook = base.userBook?.copy(
                status = BookStatus.WantToRead,
                dateAdded = added,
            ),
        )
    }

    private fun startedOn(
        id: Int,
        date: String,
    ): UserBookRead = datedRead(
        id = id,
        startedAt = date,
        finishedAt = null,
    )

    @Nested
    inner class ToReadingListStats {
        @Test
        fun `an unbounded range counts every book that waited`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2025-01-01",
                ),
                listed(
                    id = 2,
                    added = "2026-06-10",
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            result.addedCount shouldBe 2
        }

        @Test
        fun `a bounded range counts only the books added inside it`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-05-31",
                ),
                listed(
                    id = 2,
                    added = "2026-06-01",
                ),
                listed(
                    id = 3,
                    added = "2026-06-30",
                ),
                listed(
                    id = 4,
                    added = "2026-07-01",
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(june)

            // ----- Assert -----
            result.addedCount shouldBe 2
        }

        @Test
        fun `a book with a read logged in the past did not wait`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-10",
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-01-02",
                            finishedAt = "2026-01-20",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            listOf(result.addedCount, result.waitedCount, result.waitDays) shouldBe listOf(0, 0, null)
        }

        @Test
        fun `a first read with no start that finished before the book was added did not wait`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-10",
                    reads = listOf(
                        finishedRead(
                            id = 1,
                            finishedAt = "2026-02-01",
                        ),
                        startedOn(
                            id = 2,
                            date = "2026-06-20",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            listOf(result.addedCount, result.waitedCount) shouldBe listOf(0, 0)
        }

        @Test
        fun `a read started the day the book was added did not wait`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-10",
                    reads = listOf(
                        startedOn(
                            id = 1,
                            date = "2026-06-10",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            listOf(result.addedCount, result.waitedCount) shouldBe listOf(0, 0)
        }

        @Test
        fun `a book started the day after it was added waits one day`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-10",
                    reads = listOf(
                        startedOn(
                            id = 1,
                            date = "2026-06-11",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            listOf(result.waitDays, result.waitedCount) shouldBe listOf(1, 1)
        }

        @Test
        fun `the wait runs to the earliest started read`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-01",
                    reads = listOf(
                        startedOn(
                            id = 1,
                            date = "2026-06-20",
                        ),
                        startedOn(
                            id = 2,
                            date = "2026-06-05",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            result.waitDays shouldBe 4
        }

        @Test
        fun `a book whose first start falls outside the range is added but not sampled`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-10",
                    reads = listOf(
                        startedOn(
                            id = 1,
                            date = "2026-08-01",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(june)

            // ----- Assert -----
            listOf(result.addedCount, result.waitedCount, result.waitDays) shouldBe listOf(1, 0, null)
        }

        @Test
        fun `a book never started is added but not sampled`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-10",
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            listOf(result.addedCount, result.waitedCount, result.waitDays) shouldBe listOf(1, 0, null)
        }

        @Test
        fun `an odd sample takes the middle wait`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-01",
                    reads = listOf(
                        startedOn(
                            id = 1,
                            date = "2026-06-03",
                        ),
                    ),
                ),
                listed(
                    id = 2,
                    added = "2026-06-01",
                    reads = listOf(
                        startedOn(
                            id = 2,
                            date = "2026-06-08",
                        ),
                    ),
                ),
                listed(
                    id = 3,
                    added = "2026-06-01",
                    reads = listOf(
                        startedOn(
                            id = 3,
                            date = "2026-06-21",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            listOf(result.waitDays, result.waitedCount) shouldBe listOf(7, 3)
        }

        @Test
        fun `an even sample averages the middle two waits`() {
            // ----- Arrange -----
            val books = listOf(
                listed(
                    id = 1,
                    added = "2026-06-01",
                    reads = listOf(
                        startedOn(
                            id = 1,
                            date = "2026-06-03",
                        ),
                    ),
                ),
                listed(
                    id = 2,
                    added = "2026-06-01",
                    reads = listOf(
                        startedOn(
                            id = 2,
                            date = "2026-06-07",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            listOf(result.waitDays, result.waitedCount) shouldBe listOf(4, 2)
        }

        @Test
        fun `a book without a user book or a parseable added date is skipped`() {
            // ----- Arrange -----
            val noUserBook = listed(
                id = 1,
                added = "2026-06-10",
            ).copy(userBook = null)

            val badDate = listed(
                id = 2,
                added = "not a date",
            )

            // ----- Act -----
            val result = listOf(noUserBook, badDate).toReadingListStats(StatsRange.Unbounded)

            // ----- Assert -----
            result.addedCount shouldBe 0
        }
    }
}
