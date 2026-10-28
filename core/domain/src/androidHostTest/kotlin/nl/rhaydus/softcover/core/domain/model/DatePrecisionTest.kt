package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DatePrecisionTest {
    @Nested
    inner class FromCode {
        @Test
        fun `codes 1 to 3 map to day, month and year`() {
            // ----- Arrange -----
            val codes = listOf(1, 2, 3)

            // ----- Act -----
            val result = codes.map { DatePrecision.fromCode(code = it) }

            // ----- Assert -----
            result shouldBe listOf(DatePrecision.DAY, DatePrecision.MONTH, DatePrecision.YEAR)
        }

        @Test
        fun `codes outside 1 to 3 and null map to no precision`() {
            // ----- Arrange -----
            val codes = listOf(0, 4, -1, null)

            // ----- Act -----
            val result = codes.map { DatePrecision.fromCode(code = it) }

            // ----- Assert -----
            result shouldBe listOf(null, null, null, null)
        }

        @Test
        fun `every precision round trips through its code`() {
            // ----- Arrange -----
            val precisions = DatePrecision.entries

            // ----- Act -----
            val result = precisions.map { DatePrecision.fromCode(code = it.code) }

            // ----- Assert -----
            result shouldBe precisions
        }
    }
}
