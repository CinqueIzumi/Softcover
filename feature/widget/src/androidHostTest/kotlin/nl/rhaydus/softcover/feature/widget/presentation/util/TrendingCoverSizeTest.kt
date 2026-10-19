package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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

    @Nested
    inner class TrendingCoverHeightDpOverSizes {
        @Test
        fun `the largest cover height over the sizes wins`() {
            // ----- Arrange -----
            val sizes = listOf(
                DpSize(
                    width = 400.dp,
                    height = 120.dp,
                ),
                DpSize(
                    width = 400.dp,
                    height = 200.dp,
                ),
            )

            // ----- Act -----
            val result = trendingCoverHeightDp(sizes)

            // ----- Assert -----
            result shouldBe trendingCoverHeightDp(
                widgetHeightDp = 200,
                widgetWidthDp = 400,
                showsPageRow = false,
            )
        }

        @Test
        fun `an empty list of sizes gives a height of one`() {
            // ----- Arrange -----
            val sizes = emptyList<DpSize>()

            // ----- Act -----
            val result = trendingCoverHeightDp(sizes)

            // ----- Assert -----
            result shouldBe 1
        }
    }
}
