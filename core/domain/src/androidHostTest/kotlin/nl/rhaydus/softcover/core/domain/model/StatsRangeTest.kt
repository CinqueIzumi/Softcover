package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StatsRangeTest {
    private val bounded = StatsRange.Bounded(
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

    @Nested
    inner class Contains {
        @Test
        fun `Unbounded contains any date`() {
            // ----- Arrange -----
            val date = LocalDate(
                year = 1999,
                month = 12,
                day = 31,
            )

            // ----- Act -----
            val result = date in StatsRange.Unbounded

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `Bounded contains its start`() {
            // ----- Arrange -----
            val date = bounded.start

            // ----- Act -----
            val result = date in bounded

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `Bounded contains its endInclusive`() {
            // ----- Arrange -----
            val date = bounded.endInclusive

            // ----- Act -----
            val result = date in bounded

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `Bounded excludes the day before start`() {
            // ----- Arrange -----
            val date = LocalDate(
                year = 2026,
                month = 2,
                day = 28,
            )

            // ----- Act -----
            val result = date in bounded

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `Bounded excludes the day after end`() {
            // ----- Arrange -----
            val date = LocalDate(
                year = 2026,
                month = 4,
                day = 1,
            )

            // ----- Act -----
            val result = date in bounded

            // ----- Assert -----
            result shouldBe false
        }
    }
}
