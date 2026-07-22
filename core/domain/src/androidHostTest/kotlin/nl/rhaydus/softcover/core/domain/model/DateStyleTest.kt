package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DateStyleTest {
    // ----- format -----

    @Nested
    inner class Format {
        private val date = LocalDate(
            2026,
            7,
            16,
        )

        @Test
        fun `formats date as day-slash-month-slash-year for DAY_MONTH_YEAR`() {
            // ----- Act -----
            val result = DateStyle.DAY_MONTH_YEAR.format(date)

            // ----- Assert -----
            result shouldBe "16/07/2026"
        }

        @Test
        fun `formats date as month-slash-day-slash-year for MONTH_DAY_YEAR`() {
            // ----- Act -----
            val result = DateStyle.MONTH_DAY_YEAR.format(date)

            // ----- Assert -----
            result shouldBe "07/16/2026"
        }

        @Test
        fun `formats date as year-slash-month-slash-day for YEAR_MONTH_DAY`() {
            // ----- Act -----
            val result = DateStyle.YEAR_MONTH_DAY.format(date)

            // ----- Assert -----
            result shouldBe "2026/07/16"
        }
    }

    // ----- label -----

    @Nested
    inner class Label {
        @Test
        fun `DAY_MONTH_YEAR label is Day, month, year`() {
            // ----- Assert -----
            DateStyle.DAY_MONTH_YEAR.label shouldBe "Day, month, year"
        }

        @Test
        fun `MONTH_DAY_YEAR label is Month, day, year`() {
            // ----- Assert -----
            DateStyle.MONTH_DAY_YEAR.label shouldBe "Month, day, year"
        }

        @Test
        fun `YEAR_MONTH_DAY label is Year, month, day`() {
            // ----- Assert -----
            DateStyle.YEAR_MONTH_DAY.label shouldBe "Year, month, day"
        }
    }
}
