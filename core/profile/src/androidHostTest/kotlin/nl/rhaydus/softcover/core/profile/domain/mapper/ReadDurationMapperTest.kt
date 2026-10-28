package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.DatePrecision
import nl.rhaydus.softcover.core.domain.model.StatsRange
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadDurationMapperTest {
    private val range = StatsRange.Bounded(
        start = LocalDate.parse("2026-01-01"),
        endInclusive = LocalDate.parse("2026-12-31"),
    )

    @Nested
    inner class ToReadDurations {
        @Test
        fun `a day-precise read with a start and finish becomes a duration`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 7,
                    reads = listOf(
                        datedRead(
                            id = 70,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-05",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            val actual = result.map {
                Triple(
                    it.bookId,
                    it.readId,
                    it.days,
                )
            }
            val expected = listOf(
                Triple(
                    7,
                    70,
                    5,
                ),
            )
            actual shouldBe expected
        }

        @Test
        fun `reads without a start, with a month or year or unknown precision, or finishing before the start are dropped`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = null,
                            finishedAt = "2026-03-05",
                        ),
                        datedRead(
                            id = 2,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-05",
                            precision = DatePrecision.MONTH,
                        ),
                        datedRead(
                            id = 3,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-05",
                            precision = DatePrecision.YEAR,
                        ),
                        datedRead(
                            id = 4,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-05",
                            precision = null,
                        ),
                        datedRead(
                            id = 5,
                            startedAt = "2026-03-06",
                            finishedAt = "2026-03-05",
                        ),
                        datedRead(
                            id = 6,
                            startedAt = "2026-03-01",
                            finishedAt = null,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `a read finishing on its start day is kept`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-03-05",
                            finishedAt = "2026-03-05",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            result.map { it.days } shouldBe listOf(1)
        }

        @Test
        fun `every reread of a book counts as its own duration`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2025-03-01",
                            finishedAt = "2025-03-02",
                        ),
                        datedRead(
                            id = 2,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-04",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            result.map { it.readId to it.days } shouldBe listOf(1 to 2, 2 to 4)
        }

        @Test
        fun `a bounded range keeps only reads whose own finish date is inside it, boundary days included`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2025-12-20",
                            finishedAt = "2025-12-31",
                        ),
                        datedRead(
                            id = 2,
                            startedAt = "2025-12-30",
                            finishedAt = "2026-01-01",
                        ),
                        datedRead(
                            id = 3,
                            startedAt = "2026-06-01",
                            finishedAt = "2026-06-02",
                        ),
                        datedRead(
                            id = 4,
                            startedAt = "2026-12-30",
                            finishedAt = "2026-12-31",
                        ),
                        datedRead(
                            id = 5,
                            startedAt = "2026-12-31",
                            finishedAt = "2027-01-01",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(range)

            // ----- Assert -----
            result.map { it.readId } shouldBe listOf(2, 3, 4)
        }

        @Test
        fun `a book with no user book contributes nothing`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1).copy(userBook = null))

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `pages come from the read's edition pages first`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    pages = 300,
                    editions = listOf(
                        edition(
                            id = 5,
                            pages = 200,
                        ),
                    ),
                    editionId = 5,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-02",
                            editionPages = 100,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            result.map { it.pages } shouldBe listOf(100)
        }

        @Test
        fun `a non-positive edition pages falls back to the reader's edition`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    pages = 300,
                    editions = listOf(
                        edition(
                            id = 5,
                            pages = 200,
                        ),
                    ),
                    editionId = 5,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-02",
                            editionPages = 0,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            result.map { it.pages } shouldBe listOf(200)
        }

        @Test
        fun `without any edition pages the book's pages are used`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    pages = 300,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-02",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            result.map { it.pages } shouldBe listOf(300)
        }

        @Test
        fun `zero or negative book pages leave the length unknown`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    pages = 0,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-02",
                        ),
                    ),
                ),
                readBook(
                    id = 2,
                    pages = -4,
                    reads = listOf(
                        datedRead(
                            id = 2,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-02",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.toReadDurations(StatsRange.Unbounded)

            // ----- Assert -----
            result.map { it.pages } shouldBe listOf(null, null)
        }
    }

    @Nested
    inner class CountExcludedReads {
        @Test
        fun `counts in-range reads that fail a rule`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = null,
                            finishedAt = "2026-03-05",
                        ),
                        datedRead(
                            id = 2,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-05",
                            precision = DatePrecision.MONTH,
                        ),
                        datedRead(
                            id = 3,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-05",
                            precision = null,
                        ),
                        datedRead(
                            id = 4,
                            startedAt = "2026-03-06",
                            finishedAt = "2026-03-05",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.countExcludedReads(range)

            // ----- Assert -----
            result shouldBe 4
        }

        @Test
        fun `does not count counted reads, out-of-range reads or reads with no finish`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-03-01",
                            finishedAt = "2026-03-05",
                        ),
                        datedRead(
                            id = 2,
                            startedAt = null,
                            finishedAt = "2025-03-05",
                        ),
                        datedRead(
                            id = 3,
                            startedAt = "2026-03-01",
                            finishedAt = null,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.countExcludedReads(range)

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `an unbounded range counts failing reads from any year`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = null,
                            finishedAt = "2019-03-05",
                        ),
                        datedRead(
                            id = 2,
                            startedAt = null,
                            finishedAt = "2026-03-05",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.countExcludedReads(StatsRange.Unbounded)

            // ----- Assert -----
            result shouldBe 2
        }
    }
}
