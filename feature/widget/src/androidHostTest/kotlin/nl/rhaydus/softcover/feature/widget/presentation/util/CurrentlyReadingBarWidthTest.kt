package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentlyReadingBarWidthTest {
    @Nested
    inner class ProgressBarWidthDp {
        @Test
        fun `a widget too narrow for the row keeps a bar as wide as it is tall`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 40.dp,
                height = 110.dp,
            )

            // ----- Act -----
            val bar = progressBarWidthDp(size)

            // ----- Assert -----
            bar shouldBe PROGRESS_BAR_HEIGHT_DP
        }

        @Test
        fun `a wider stacked widget gets a wider bar`() {
            // ----- Arrange -----
            val narrowSize = DpSize(
                width = 180.dp,
                height = 400.dp,
            )
            val wideSize = DpSize(
                width = 320.dp,
                height = 400.dp,
            )

            // ----- Act -----
            val narrow = progressBarWidthDp(narrowSize)
            val wide = progressBarWidthDp(wideSize)

            // ----- Assert -----
            wide shouldBeGreaterThan narrow
        }

        @Test
        fun `a side by side size gets a narrower bar than a stacked size of the same width`() {
            // ----- Arrange -----
            val stackedSize = DpSize(
                width = 320.dp,
                height = 320.dp,
            )
            val sideBySideSize = DpSize(
                width = 320.dp,
                height = 110.dp,
            )

            // ----- Act -----
            val stacked = progressBarWidthDp(stackedSize)
            val sideBySide = progressBarWidthDp(sideBySideSize)

            // ----- Assert -----
            sideBySide shouldBeLessThan stacked
        }
    }

    @Nested
    inner class ProgressBarWidthsDp {
        @Test
        fun `returns one width per distinct reported size`() {
            // ----- Arrange -----
            val small = DpSize(
                width = 180.dp,
                height = 400.dp,
            )
            val large = DpSize(
                width = 320.dp,
                height = 400.dp,
            )
            val sizes = listOf(small, large, small)

            // ----- Act -----
            val widths = progressBarWidthsDp(sizes)

            // ----- Assert -----
            widths shouldBe setOf(
                progressBarWidthDp(small),
                progressBarWidthDp(large),
            )
        }
    }
}
