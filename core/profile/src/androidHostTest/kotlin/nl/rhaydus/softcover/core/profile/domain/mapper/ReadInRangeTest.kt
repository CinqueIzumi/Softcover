package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.domain.model.StatsRange
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadInRangeTest {
    private val march = StatsRange.Bounded(
        start = LocalDate(
            2026,
            3,
            1,
        ),
        endInclusive = LocalDate(
            2026,
            3,
            31,
        ),
    )

    @Nested
    inner class Unbounded {
        @Test
        fun `keeps every book once, dated or not`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2020-01-01",
                ),
                readBook(id = 2),
                readBook(id = 2),
            )

            // ----- Act -----
            val result = books.readInRange(
                StatsRange.Unbounded,
                emptyList(),
                TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1, 2)
        }
    }

    @Nested
    inner class Bounded {
        @Test
        fun `keeps a book finished inside the range and drops one finished outside`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-10",
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-04-10",
                ),
            )

            // ----- Act -----
            val result = books.readInRange(
                march,
                emptyList(),
                TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `counts both ends of the range`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-01",
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-03-31",
                ),
                readBook(
                    id = 3,
                    finishedAt = "2026-02-28",
                ),
            )

            // ----- Act -----
            val result = books.readInRange(
                march,
                emptyList(),
                TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1, 2)
        }

        @Test
        fun `keeps a did not finish book stopped inside the range`() {
            // ----- Arrange -----
            val books = listOf(
                dnfBook(
                    id = 1,
                    stoppedAt = "2026-03-12T12:00:00.000000",
                ),
            )

            // ----- Act -----
            val result = books.readInRange(
                march,
                emptyList(),
                TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `drops a did not finish book stopped outside the range`() {
            // ----- Arrange -----
            val books = listOf(
                dnfBook(
                    id = 1,
                    stoppedAt = "2026-05-12T12:00:00.000000",
                ),
            )

            // ----- Act -----
            val result = books.readInRange(
                march,
                emptyList(),
                TimeZone.UTC,
            )

            // ----- Assert -----
            result.shouldBe(emptyList())
        }

        @Test
        fun `keeps a book started inside the range though it is not finished`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-03-05",
                            finishedAt = null,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = books.readInRange(
                march,
                emptyList(),
                TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `keeps an undated book with a progress update inside the range`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1), readBook(id = 2))

            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-03-15T10:00:00Z",
                    pagesRead = 10,
                ),
            )

            // ----- Act -----
            val result = books.readInRange(
                march,
                updates,
                TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `reads a progress update's day in the given time zone`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1))

            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-03-31T23:30:00Z",
                    pagesRead = 10,
                ),
            )

            val april = StatsRange.Bounded(
                LocalDate(
                    2026,
                    4,
                    1,
                ),
                LocalDate(
                    2026,
                    4,
                    30,
                ),
            )

            // ----- Act -----
            val result = books.readInRange(
                april,
                updates,
                TimeZone.of("Europe/Amsterdam"),
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `drops a book with no user book`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-10",
                ).copy(userBook = null),
            )

            // ----- Act -----
            val result = books.readInRange(
                march,
                emptyList(),
                TimeZone.UTC,
            )

            // ----- Assert -----
            result.shouldBe(emptyList())
        }
    }
}
