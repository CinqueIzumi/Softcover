package nl.rhaydus.softcover.core.personal.domain.model

import io.kotest.matchers.shouldBe
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
        fun `total negative — returns null`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(20)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 0,
                total = -10,
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
        fun `current exceeds total — book overshot, returns null`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(20)

            // ----- Act -----
            val result = ReadingPaceForecast.compute(
                dailyUnitsRead = dailyUnitsRead,
                current = 150,
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
        fun `all logged days are negative — excluded by the positive filter same as zero, returns null`() {
            // ----- Arrange -----
            val dailyUnitsRead = listOf(-5, -10)

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
        fun `unit PAGES — is carried onto the result without affecting the numeric forecast`() {
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
            result?.unit shouldBe DeadlineUnit.PAGES
            result?.avgPerReadingDay shouldBe 40f
            result?.remainingUnits shouldBe 90
            result?.forecastReadingDays shouldBe 3
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
}
