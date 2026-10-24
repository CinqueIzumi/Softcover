package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StatsScopeResolveTest {
    private val rollingThirtyDays = StatsScope.Rolling(StatsWindow.LAST_30_DAYS)
    private val rollingSixMonths = StatsScope.Rolling(StatsWindow.LAST_6_MONTHS)
    private val rollingTwelveMonths = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)

    @Nested
    inner class Last30Days {
        @Test
        fun `spans 30 days including today`() {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                10,
                23,
            )

            // ----- Act -----
            val range = rollingThirtyDays.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2026,
                    9,
                    24,
                ),
                endInclusive = today,
            )
        }

        @Test
        fun `crosses a short February into the previous month`() {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                3,
                5,
            )

            // ----- Act -----
            val range = rollingThirtyDays.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2026,
                    2,
                    4,
                ),
                endInclusive = today,
            )
        }
    }

    @Nested
    inner class Last6Months {
        @Test
        fun `starts the day after the same date six months back`() {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                10,
                15,
            )

            // ----- Act -----
            val range = rollingSixMonths.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2026,
                    4,
                    16,
                ),
                endInclusive = today,
            )
        }

        @Test
        fun `clamps a missing February date to the month end`() {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                8,
                31,
            )

            // ----- Act -----
            val range = rollingSixMonths.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2026,
                    3,
                    1,
                ),
                endInclusive = today,
            )
        }

        @Test
        fun `clamps to the 30th when the target month is shorter`() {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                3,
                31,
            )

            // ----- Act -----
            val range = rollingSixMonths.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2025,
                    10,
                    1,
                ),
                endInclusive = today,
            )
        }

        @Test
        fun `handles a leap day as today`() {
            // ----- Arrange -----
            val today = LocalDate(
                2028,
                2,
                29,
            )

            // ----- Act -----
            val range = rollingSixMonths.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2027,
                    8,
                    30,
                ),
                endInclusive = today,
            )
        }
    }

    @Nested
    inner class Last12Months {
        @Test
        fun `starts the day after the same date a year back`() {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                10,
                15,
            )

            // ----- Act -----
            val range = rollingTwelveMonths.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2025,
                    10,
                    16,
                ),
                endInclusive = today,
            )
        }

        @Test
        fun `clamps a leap day to the end of February in a common year`() {
            // ----- Arrange -----
            val today = LocalDate(
                2028,
                2,
                29,
            )

            // ----- Act -----
            val range = rollingTwelveMonths.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2027,
                    3,
                    1,
                ),
                endInclusive = today,
            )
        }

        @Test
        fun `starts on 1 March when today is the last day of a common February`() {
            // ----- Arrange -----
            val today = LocalDate(
                2027,
                2,
                28,
            )

            // ----- Act -----
            val range = rollingTwelveMonths.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2026,
                    3,
                    1,
                ),
                endInclusive = today,
            )
        }
    }

    @Nested
    inner class CalendarYear {
        @Test
        fun `covers 1 January through 31 December regardless of today`() {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                10,
                23,
            )

            // ----- Act -----
            val range = StatsScope.CalendarYear(2024).resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Bounded(
                start = LocalDate(
                    2024,
                    1,
                    1,
                ),
                endInclusive = LocalDate(
                    2024,
                    12,
                    31,
                ),
            )
        }
    }

    @Nested
    inner class AllTime {
        @Test
        fun `is unbounded`() {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                10,
                23,
            )

            // ----- Act -----
            val range = StatsScope.AllTime.resolve(today)

            // ----- Assert -----
            range shouldBe StatsRange.Unbounded
        }
    }
}
