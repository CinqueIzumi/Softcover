package nl.rhaydus.softcover.feature.widget.presentation.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingCoverSizeTest {
    @Nested
    inner class TrendingCoverHeightDp {
        @Test
        fun `a short widget gets the height left after padding and header`() {
            // ----- Arrange -----
            val widgetHeightDp = 120
            val widgetWidthDp = 400

            // ----- Act -----
            val result = trendingCoverHeightDp(
                widgetHeightDp = widgetHeightDp,
                widgetWidthDp = widgetWidthDp,
                showsPageRow = false,
            )

            // ----- Assert -----
            result shouldBe 84
        }

        @Test
        fun `a tall narrow widget caps the cover so a 2 to 3 cover fits the cell width`() {
            // ----- Arrange -----
            val widgetHeightDp = 400
            val widgetWidthDp = 200

            // ----- Act -----
            val result = trendingCoverHeightDp(
                widgetHeightDp = widgetHeightDp,
                widgetWidthDp = widgetWidthDp,
                showsPageRow = false,
            )

            // ----- Assert -----
            result shouldBe 132
        }

        @Test
        fun `the cover height never goes negative`() {
            // ----- Arrange -----
            val widgetHeightDp = 0
            val widgetWidthDp = 400

            // ----- Act -----
            val result = trendingCoverHeightDp(
                widgetHeightDp = widgetHeightDp,
                widgetWidthDp = widgetWidthDp,
                showsPageRow = true,
            )

            // ----- Assert -----
            result shouldBe 0
        }
    }
}
