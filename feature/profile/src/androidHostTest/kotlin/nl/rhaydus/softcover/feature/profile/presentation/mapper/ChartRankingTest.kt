package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ChartTone
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChartRankingTest {
    @Nested
    inner class RankedColumnTones {
        @Test
        fun `ranks the biggest lead, the next two second, other non-zero third and zero fifth`() {
            // ----- Arrange -----
            val values = listOf(5, 9, 8, 3, 1, 0, 2)

            // ----- Act -----
            val result = rankedColumnTones(values)

            // ----- Assert -----
            result shouldBe listOf(
                ChartTone.Second,
                ChartTone.Lead,
                ChartTone.Second,
                ChartTone.Third,
                ChartTone.Third,
                ChartTone.Fifth,
                ChartTone.Third,
            )
        }

        @Test
        fun `gives the earliest column the higher tone on a tie`() {
            // ----- Arrange -----
            val values = listOf(4, 4, 4, 4)

            // ----- Act -----
            val result = rankedColumnTones(values)

            // ----- Assert -----
            result shouldBe listOf(
                ChartTone.Lead,
                ChartTone.Second,
                ChartTone.Second,
                ChartTone.Third,
            )
        }

        @Test
        fun `marks every column fifth when all are zero`() {
            // ----- Arrange -----
            val values = listOf(0, 0, 0)

            // ----- Act -----
            val result = rankedColumnTones(values)

            // ----- Assert -----
            result shouldBe listOf(ChartTone.Fifth, ChartTone.Fifth, ChartTone.Fifth)
        }

        @Test
        fun `yields no tones for no values`() {
            // ----- Arrange -----
            val values = emptyList<Int>()

            // ----- Act -----
            val result = rankedColumnTones(values)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class PeakIndexLookup {
        @Test
        fun `finds the index of the biggest value`() {
            // ----- Arrange -----
            val values = listOf(1, 7, 3)

            // ----- Act -----
            val result = peakIndex(values)

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `picks the earliest index on a tie`() {
            // ----- Arrange -----
            val values = listOf(2, 6, 6, 1)

            // ----- Act -----
            val result = peakIndex(values)

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `is null when every value is zero`() {
            // ----- Arrange -----
            val values = listOf(0, 0, 0)

            // ----- Act -----
            val result = peakIndex(values)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null for no values`() {
            // ----- Arrange -----
            val values = emptyList<Int>()

            // ----- Act -----
            val result = peakIndex(values)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
