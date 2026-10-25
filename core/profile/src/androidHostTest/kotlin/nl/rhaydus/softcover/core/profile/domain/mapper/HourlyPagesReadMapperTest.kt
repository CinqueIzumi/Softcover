package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.StatsRange
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class HourlyPagesReadMapperTest {
    private val amsterdam = TimeZone.of("Europe/Amsterdam")

    private fun update(
        at: String,
        pagesRead: Int,
    ) = ProgressUpdate(
        bookId = 1,
        at = Instant.parse(at),
        pages = null,
        pagesRead = pagesRead,
    )

    private fun march(day: Int) = LocalDate(
        year = 2026,
        month = 3,
        day = day,
    )

    @Nested
    inner class Bucketing {
        @Test
        fun `always returns 24 entries`() {
            // ----- Arrange -----
            val updates = emptyList<ProgressUpdate>()

            // ----- Act -----
            val result = toHourlyPages(
                updates = updates,
                range = StatsRange.Unbounded,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.pages shouldBe List(24) { 0 }
        }

        @Test
        fun `sums pages read into the local hour`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    at = "2026-03-10T09:05:00Z",
                    pagesRead = 10,
                ),
                update(
                    at = "2026-03-11T09:55:00Z",
                    pagesRead = 15,
                ),
                update(
                    at = "2026-03-11T21:00:00Z",
                    pagesRead = 7,
                ),
            )

            // ----- Act -----
            val result = toHourlyPages(
                updates = updates,
                range = StatsRange.Unbounded,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.pages[9] shouldBe 25
            result.pages[21] shouldBe 7
            result.pages.sum() shouldBe 32
        }

        @Test
        fun `places the same instant in a different hour under another time zone`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    at = "2026-03-10T09:00:00Z",
                    pagesRead = 10,
                ),
            )

            // ----- Act -----
            val utc = toHourlyPages(
                updates = updates,
                range = StatsRange.Unbounded,
                timeZone = TimeZone.UTC,
            )
            val local = toHourlyPages(
                updates = updates,
                range = StatsRange.Unbounded,
                timeZone = amsterdam,
            )

            // ----- Assert -----
            utc.pages[9] shouldBe 10
            local.pages[10] shouldBe 10
            local.pages[9] shouldBe 0
        }

        @Test
        fun `ignores updates that read no pages`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    at = "2026-03-10T09:00:00Z",
                    pagesRead = 0,
                ),
            )

            // ----- Act -----
            val result = toHourlyPages(
                updates = updates,
                range = StatsRange.Unbounded,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.pages shouldBe List(24) { 0 }
        }
    }

    @Nested
    inner class Ranging {
        private val range = StatsRange.Bounded(
            start = march(10),
            endInclusive = march(12),
        )

        @Test
        fun `ignores updates outside a bounded range`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    at = "2026-03-09T12:00:00Z",
                    pagesRead = 5,
                ),
                update(
                    at = "2026-03-11T12:00:00Z",
                    pagesRead = 20,
                ),
                update(
                    at = "2026-03-13T12:00:00Z",
                    pagesRead = 9,
                ),
            )

            // ----- Act -----
            val result = toHourlyPages(
                updates = updates,
                range = range,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.pages[12] shouldBe 20
            result.pages.sum() shouldBe 20
        }

        @Test
        fun `keeps updates on the first and last day of the range`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    at = "2026-03-10T00:30:00Z",
                    pagesRead = 4,
                ),
                update(
                    at = "2026-03-12T23:30:00Z",
                    pagesRead = 6,
                ),
            )

            // ----- Act -----
            val result = toHourlyPages(
                updates = updates,
                range = range,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.pages[0] shouldBe 4
            result.pages[23] shouldBe 6
        }

        @Test
        fun `filters on the local date rather than the UTC date`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    at = "2026-03-09T23:30:00Z",
                    pagesRead = 8,
                ),
            )

            // ----- Act -----
            val utc = toHourlyPages(
                updates = updates,
                range = range,
                timeZone = TimeZone.UTC,
            )
            val local = toHourlyPages(
                updates = updates,
                range = range,
                timeZone = amsterdam,
            )

            // ----- Assert -----
            utc.pages.sum() shouldBe 0
            local.pages[0] shouldBe 8
        }

        @Test
        fun `drops an update that is in range in UTC but past the end locally`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    at = "2026-03-12T23:30:00Z",
                    pagesRead = 8,
                ),
            )

            // ----- Act -----
            val utc = toHourlyPages(
                updates = updates,
                range = range,
                timeZone = TimeZone.UTC,
            )
            val local = toHourlyPages(
                updates = updates,
                range = range,
                timeZone = amsterdam,
            )

            // ----- Assert -----
            utc.pages[23] shouldBe 8
            local.pages.sum() shouldBe 0
        }
    }
}
