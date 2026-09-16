package nl.rhaydus.softcover.core.uibinding.date

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.common.currentLocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DateFormatsTest {
    @Nested
    inner class FormatCompactDateTest {
        @Test
        fun `a date in the current year has no year suffix`() {
            // ----- Arrange -----
            val now = currentLocalDate()
            val date = LocalDate(
                year = now.year,
                monthNumber = 9,
                dayOfMonth = 2,
            )

            // ----- Act -----
            val result = date.formatCompactDate()

            // ----- Assert -----
            result shouldBe "Sep 2"
        }

        @Test
        fun `a date in a different year appends the year`() {
            // ----- Arrange -----
            val now = currentLocalDate()
            val otherYear = if (now.year == 2019) 2020 else 2019
            val date = LocalDate(
                year = otherYear,
                monthNumber = 9,
                dayOfMonth = 2,
            )

            // ----- Act -----
            val result = date.formatCompactDate()

            // ----- Assert -----
            result shouldBe "Sep 2, $otherYear"
        }
    }

    @Nested
    inner class FormatLongDateTest {
        @Test
        fun `formats the full month name, unpadded day and year`() {
            // ----- Arrange -----
            val date = LocalDate(
                year = 2026,
                monthNumber = 9,
                dayOfMonth = 2,
            )

            // ----- Act -----
            val result = date.formatLongDate()

            // ----- Assert -----
            result shouldBe "September 2, 2026"
        }
    }
}
