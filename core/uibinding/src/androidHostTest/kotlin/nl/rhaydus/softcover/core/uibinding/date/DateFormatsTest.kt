package nl.rhaydus.softcover.core.uibinding.date

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DateFormatsTest {
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
