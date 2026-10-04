package nl.rhaydus.softcover.core.component.chart

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChartToneTest {
    @Nested
    inner class ForRank {
        @Test
        fun `maps ranks zero to four onto Lead through Fifth in order`() {
            // ----- Arrange -----
            val ranks = 0..4

            // ----- Act -----
            val tones = ranks.map { ChartTone.forRank(it) }

            // ----- Assert -----
            tones shouldBe listOf(
                ChartTone.Lead,
                ChartTone.Second,
                ChartTone.Third,
                ChartTone.Fourth,
                ChartTone.Fifth,
            )
        }

        @Test
        fun `keeps Fifth for a rank past the last`() {
            // ----- Arrange -----
            val rank = 12

            // ----- Act -----
            val tone = ChartTone.forRank(rank)

            // ----- Assert -----
            tone shouldBe ChartTone.Fifth
        }

        @Test
        fun `keeps Lead for a negative rank`() {
            // ----- Arrange -----
            val rank = -3

            // ----- Act -----
            val tone = ChartTone.forRank(rank)

            // ----- Assert -----
            tone shouldBe ChartTone.Lead
        }

        @Test
        fun `never returns Unknown for any rank`() {
            // ----- Arrange -----
            val ranks = -2..20

            // ----- Act -----
            val tones = ranks.map { ChartTone.forRank(it) }

            // ----- Assert -----
            tones.contains(ChartTone.Unknown) shouldBe false
        }
    }
}
