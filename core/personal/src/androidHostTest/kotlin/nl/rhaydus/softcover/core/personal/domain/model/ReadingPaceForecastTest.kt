package nl.rhaydus.softcover.core.personal.domain.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingPaceForecastTest {
    @Nested
    inner class Compute {
        @Test
        fun `canonical example — zero days excluded from average, avgPerReadingDay is the mean of positive days`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(50, 0, 30)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 20,
                total = 200,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result?.avgPerReadingDay shouldBe 40f
            result?.remainingUnits shouldBe 180
            result?.forecastReadingDays shouldBe 5
        }

        @Test
        fun `ceil rounding — 90 remaining at 40 per day forecasts 3 days not 2`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(30, 50)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 10,
                total = 100,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result?.avgPerReadingDay shouldBe 40f
            result?.remainingUnits shouldBe 90
            // ceil(90 / 40) = ceil(2.25) = 3
            result?.forecastReadingDays shouldBe 3
        }

        @Test
        fun `exact division — remaining divides evenly by average with no rounding needed`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(20)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 50,
                total = 150,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result?.avgPerReadingDay shouldBe 20f
            result?.remainingUnits shouldBe 100
            result?.forecastReadingDays shouldBe 5
        }

        @Test
        fun `total zero — returns null`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(20)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 0,
                total = 0,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `current equals total — book already finished, returns null`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(20)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 100,
                total = 100,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `all logged days are zero — no positive reading day, returns null`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(0, 0, 0)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 0,
                total = 100,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `empty dailyUnitsRead list — returns null`() {
            // ----- Arrange -----
            val dailyUnitsRead = emptyList<Int>()

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 0,
                total = 100,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `single-day history — produces a valid result from one logged day`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(25)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 50,
                total = 100,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result?.avgPerReadingDay shouldBe 25f
            result?.remainingUnits shouldBe 50
            result?.forecastReadingDays shouldBe 2
        }

        @Test
        fun `unit SECONDS — is carried onto the result without affecting the numeric forecast`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(30, 50)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 10,
                total = 100,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Assert -----
            result?.unit shouldBe DeadlineUnit.SECONDS
            result?.avgPerReadingDay shouldBe 40f
            result?.remainingUnits shouldBe 90
            result?.forecastReadingDays shouldBe 3
        }
    }

    private fun entry(
        day: Int,
        pages: Int? = null,
        seconds: Int? = null,
        month: Int = 1,
    ) = ReadingJournalEntry(
        date = LocalDate(
            2026,
            month,
            day,
        ),
        pages = pages,
        seconds = seconds,
    )

    @Nested
    inner class ComputeFromHistory {
        @Test
        fun `entries before since are ignored and the first in-range day diffs from zero`() {
            // ----- Arrange -----
            val history = listOf(
                entry(
                    day = 1,
                    pages = 100,
                ),
                entry(
                    day = 5,
                    pages = 20,
                ),
                entry(
                    day = 6,
                    pages = 60,
                ),
            )

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                history = history,
                unit = DeadlineUnit.PAGES,
                current = 60,
                total = 160,
                since = LocalDate(
                    2026,
                    1,
                    5,
                ),
            )

            // ----- Assert -----
            val forecast = result.shouldNotBeNull()
            forecast.avgPerReadingDay shouldBe 30f
            forecast.forecastReadingDays shouldBe 4
        }

        @Test
        fun `a null since keeps every entry`() {
            // ----- Arrange -----
            val history = listOf(
                entry(
                    day = 1,
                    pages = 100,
                ),
                entry(
                    day = 5,
                    pages = 120,
                ),
            )

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                history = history,
                unit = DeadlineUnit.PAGES,
                current = 120,
                total = 220,
                since = null,
            )

            // ----- Assert -----
            result.shouldNotBeNull().avgPerReadingDay shouldBe 60f
        }

        @Test
        fun `the last position logged on a date wins`() {
            // ----- Arrange -----
            val history = listOf(
                entry(
                    day = 1,
                    pages = 10,
                ),
                entry(
                    day = 1,
                    pages = 40,
                ),
                entry(
                    day = 2,
                    pages = 50,
                ),
            )

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                history = history,
                unit = DeadlineUnit.PAGES,
                current = 50,
                total = 100,
                since = null,
            )

            // ----- Assert -----
            result.shouldNotBeNull().avgPerReadingDay shouldBe 25f
        }

        @Test
        fun `a backward correction clamps to zero and is excluded from the average`() {
            // ----- Arrange -----
            val history = listOf(
                entry(
                    day = 1,
                    pages = 50,
                ),
                entry(
                    day = 2,
                    pages = 30,
                ),
                entry(
                    day = 3,
                    pages = 50,
                ),
            )

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                history = history,
                unit = DeadlineUnit.PAGES,
                current = 50,
                total = 150,
                since = null,
            )

            // ----- Assert -----
            result.shouldNotBeNull().avgPerReadingDay shouldBe 35f
        }

        @Test
        fun `the seconds unit reads seconds and ignores pages`() {
            // ----- Arrange -----
            val history = listOf(
                entry(
                    day = 1,
                    pages = 5,
                    seconds = 600,
                ),
                entry(
                    day = 2,
                    pages = 9,
                    seconds = 1800,
                ),
            )

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                history = history,
                unit = DeadlineUnit.SECONDS,
                current = 1800,
                total = 3000,
                since = null,
            )

            // ----- Assert -----
            val forecast = result.shouldNotBeNull()
            forecast.avgPerReadingDay shouldBe 900f
            forecast.forecastReadingDays shouldBe 2
        }

        @Test
        fun `entries without a position for the unit yield null`() {
            // ----- Arrange -----
            val history = listOf(entry(
                day = 1,
                pages = 10,
            ),)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                history = history,
                unit = DeadlineUnit.SECONDS,
                current = 0,
                total = 100,
                since = null,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `a since after every entry yields null`() {
            // ----- Arrange -----
            val history = listOf(entry(
                day = 1,
                pages = 10,
            ),)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                history = history,
                unit = DeadlineUnit.PAGES,
                current = 10,
                total = 100,
                since = LocalDate(
                    2026,
                    2,
                    1,
                ),
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
