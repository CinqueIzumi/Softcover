package nl.rhaydus.softcover.core.component.bookcard

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookCardTagStripFitTest {
    @Nested
    inner class AllChipsFit {
        @Test
        fun `returns every chip when they fit exactly with gaps between them`() {
            // ----- Arrange -----
            val chipWidths = listOf(10, 10, 10)
            val overflowWidths = listOf(8, 8)

            // ----- Act -----
            val result = fittingChipCount(
                chipWidths = chipWidths,
                overflowWidths = overflowWidths,
                gap = 5,
                available = 40,
            )

            // ----- Assert -----
            result shouldBe 3
        }

        @Test
        fun `returns zero for empty input`() {
            // ----- Arrange -----
            val chipWidths = emptyList<Int>()

            // ----- Act -----
            val result = fittingChipCount(
                chipWidths = chipWidths,
                overflowWidths = emptyList(),
                gap = 5,
                available = 0,
            )

            // ----- Assert -----
            result shouldBe 0
        }
    }

    @Nested
    inner class Overflowing {
        @Test
        fun `counts a gap between every placed chip and before the overflow chip`() {
            // ----- Arrange -----
            val chipWidths = listOf(10, 10, 10)
            val overflowWidths = listOf(8, 8)

            // ----- Act -----
            val result = fittingChipCount(
                chipWidths = chipWidths,
                overflowWidths = overflowWidths,
                gap = 5,
                available = 39,
            )

            // ----- Assert -----
            result shouldBe 2
        }

        @Test
        fun `drops another chip when the overflow chip width does not fit`() {
            // ----- Arrange -----
            val chipWidths = listOf(10, 10, 10)
            val overflowWidths = listOf(12, 20)

            // ----- Act -----
            val result = fittingChipCount(
                chipWidths = chipWidths,
                overflowWidths = overflowWidths,
                gap = 5,
                available = 39,
            )

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `uses the overflow width indexed by hidden count minus one`() {
            // ----- Arrange -----
            val chipWidths = listOf(10, 10, 10)
            val overflowWidths = listOf(5, 100)

            // ----- Act -----
            val result = fittingChipCount(
                chipWidths = chipWidths,
                overflowWidths = overflowWidths,
                gap = 5,
                available = 39,
            )

            // ----- Assert -----
            result shouldBe 2
        }

        @Test
        fun `returns zero when not even one chip fits beside the overflow chip`() {
            // ----- Arrange -----
            val chipWidths = listOf(50, 50)
            val overflowWidths = listOf(30)

            // ----- Act -----
            val result = fittingChipCount(
                chipWidths = chipWidths,
                overflowWidths = overflowWidths,
                gap = 5,
                available = 60,
            )

            // ----- Assert -----
            result shouldBe 0
        }
    }
}
