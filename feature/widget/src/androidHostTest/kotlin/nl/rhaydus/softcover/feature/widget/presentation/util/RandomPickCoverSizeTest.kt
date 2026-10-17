package nl.rhaydus.softcover.feature.widget.presentation.util

import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RandomPickCoverSizeTest {
    @Nested
    inner class RandomPickCoverHeightDp {
        @Test
        fun `a widget shorter than its chrome gets no cover`() {
            // ----- Arrange -----
            val widgetHeightDp = 10

            // ----- Act -----
            val result = randomPickCoverHeightDp(widgetHeightDp = widgetHeightDp)

            // ----- Assert -----
            result shouldBe 0
        }
    }

    @Nested
    inner class MaxRandomPickCoverHeightDp {
        @Test
        fun `is at least the cover height of every widget height the launcher can give`() {
            // ----- Arrange -----
            val heights = 0..319

            // ----- Act -----
            val max = maxRandomPickCoverHeightDp()

            // ----- Assert -----
            heights.forEach { max shouldBeGreaterThanOrEqual randomPickCoverHeightDp(widgetHeightDp = it) }
        }
    }
}
