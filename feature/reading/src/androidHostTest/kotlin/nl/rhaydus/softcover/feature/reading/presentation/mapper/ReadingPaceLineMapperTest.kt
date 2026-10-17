package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingPaceLineMapperTest {
    private fun forecast(
        avgPerReadingDay: Float = 35f,
        forecastReadingDays: Int = 5,
        unit: DeadlineUnit = DeadlineUnit.PAGES,
    ): ReadingPaceForecast = ReadingPaceForecast(
        avgPerReadingDay = avgPerReadingDay,
        remainingUnits = 100,
        forecastReadingDays = forecastReadingDays,
        unit = unit,
    )

    @Nested
    inner class ToPaceLine {
        @Test
        fun `joins the pace per day and the days to go for a page forecast`() {
            // ----- Arrange -----
            val forecast = forecast(
                avgPerReadingDay = 35f,
                forecastReadingDays = 5,
            )

            // ----- Act -----
            val line = forecast.toPaceLine()

            // ----- Assert -----
            line shouldBe "35 pages/day · about 5 days to go"
        }

        @Test
        fun `rounds a fractional pace to the nearest whole unit`() {
            // ----- Arrange -----
            val forecast = forecast(avgPerReadingDay = 34.6f)

            // ----- Act -----
            val line = forecast.toPaceLine()

            // ----- Assert -----
            line shouldBe "35 pages/day · about 5 days to go"
        }

        @Test
        fun `uses the singular for one page a day`() {
            // ----- Arrange -----
            val forecast = forecast(avgPerReadingDay = 1f)

            // ----- Act -----
            val line = forecast.toPaceLine()

            // ----- Assert -----
            line shouldBe "1 page/day · about 5 days to go"
        }

        @Test
        fun `uses the singular for one day to go`() {
            // ----- Arrange -----
            val forecast = forecast(forecastReadingDays = 1)

            // ----- Act -----
            val line = forecast.toPaceLine()

            // ----- Assert -----
            line shouldBe "35 pages/day · about 1 day to go"
        }

        @Test
        fun `shows the pace as a time for an audiobook forecast`() {
            // ----- Arrange -----
            val forecast = forecast(
                avgPerReadingDay = 3900f,
                forecastReadingDays = 3,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val line = forecast.toPaceLine()

            // ----- Assert -----
            line shouldBe "1h 5m/day · about 3 days to go"
        }
    }
}
