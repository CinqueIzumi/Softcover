package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WidgetBookLayoutTest {
    @Nested
    inner class IsSideBySide {
        @Test
        fun `a bucket wider than it is tall is side by side`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 320.dp,
                height = 110.dp,
            )

            // ----- Act -----
            val result = isSideBySide(size)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `a square bucket stays stacked`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 180.dp,
                height = 180.dp,
            )

            // ----- Act -----
            val result = isSideBySide(size)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `a portrait bucket stays stacked`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 110.dp,
                height = 250.dp,
            )

            // ----- Act -----
            val result = isSideBySide(size)

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class SideShowsAuthor {
        @Test
        fun `the compact currently reading bucket drops the author`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 180.dp,
                height = 110.dp,
            )

            // ----- Act -----
            val result = sideShowsAuthor(
                size = size,
                fixedRowsDp = 68,
                fontScale = 1f,
            )

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `a tall wide bucket shows the author`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 390.dp,
                height = 250.dp,
            )

            // ----- Act -----
            val result = sideShowsAuthor(
                size = size,
                fixedRowsDp = 68,
                fontScale = 1f,
            )

            // ----- Assert -----
            result shouldBe true
        }
    }

    @Nested
    inner class SideTitleLines {
        @Test
        fun `a cramped size gets one line`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 180.dp,
                height = 110.dp,
            )

            // ----- Act -----
            val lines = sideTitleLines(
                size = size,
                fixedRowsDp = 60,
                fontScale = 1f,
            )

            // ----- Assert -----
            lines shouldBe 1
        }

        @Test
        fun `a tall wide size gets more than three lines`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 390.dp,
                height = 250.dp,
            )

            // ----- Act -----
            val lines = sideTitleLines(
                size = size,
                fixedRowsDp = 40,
                fontScale = 1f,
            )

            // ----- Assert -----
            lines shouldBeGreaterThan 3
        }

        @Test
        fun `a larger font scale gets fewer lines in the same size`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 390.dp,
                height = 250.dp,
            )

            // ----- Act -----
            val normal = sideTitleLines(
                size = size,
                fixedRowsDp = 40,
                fontScale = 1f,
            )
            val scaled = sideTitleLines(
                size = size,
                fixedRowsDp = 40,
                fontScale = 1.3f,
            )

            // ----- Assert -----
            scaled shouldBeLessThan normal
        }

        @Test
        fun `the title lines and author never overflow a tall currently reading text column at a large font scale`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 390.dp,
                height = 250.dp,
            )
            val fixedRowsDp = 68
            val fontScale = 1.3f
            val worstCaseLineDp = 25
            val worstCaseAuthorDp = 20

            // ----- Act -----
            val lines = sideTitleLines(
                size = size,
                fixedRowsDp = fixedRowsDp,
                fontScale = fontScale,
            )
            val showsAuthor = sideShowsAuthor(
                size = size,
                fixedRowsDp = fixedRowsDp,
                fontScale = fontScale,
            )
            val author = if (showsAuthor) worstCaseAuthorDp else 0

            // ----- Assert -----
            showsAuthor shouldBe true
            (fixedRowsDp + lines * worstCaseLineDp + author) shouldBeLessThanOrEqual sideColumnHeightDp(size)
        }
    }

    @Nested
    inner class SideCoverHeightDp {
        @Test
        fun `the side cover is inset shorter than the text column`() {
            // ----- Arrange -----
            val size = DpSize(
                width = 320.dp,
                height = 110.dp,
            )

            // ----- Act -----
            val coverHeight = sideCoverHeightDp(size)

            // ----- Assert -----
            coverHeight shouldBeLessThan sideColumnHeightDp(size)
        }
    }

    @Nested
    inner class StackedCoverHeightDp {
        @Test
        fun `a taller eyebrow leaves a shorter cover`() {
            // ----- Arrange -----
            val short = stackedCoverHeightDp(
                heightDp = 250,
                eyebrowDp = 0,
                footDp = 36,
            )

            // ----- Act -----
            val tall = stackedCoverHeightDp(
                heightDp = 250,
                eyebrowDp = 28,
                footDp = 36,
            )

            // ----- Assert -----
            tall shouldBeLessThan short
        }

        @Test
        fun `a taller foot leaves a shorter cover`() {
            // ----- Arrange -----
            val short = stackedCoverHeightDp(
                heightDp = 250,
                eyebrowDp = 28,
                footDp = 36,
            )

            // ----- Act -----
            val tall = stackedCoverHeightDp(
                heightDp = 250,
                eyebrowDp = 28,
                footDp = 48,
            )

            // ----- Assert -----
            tall shouldBeLessThan short
        }

        @Test
        fun `chrome taller than the widget never gives a negative cover`() {
            // ----- Arrange -----
            val heightDp = 20

            // ----- Act -----
            val result = stackedCoverHeightDp(
                heightDp = heightDp,
                eyebrowDp = 28,
                footDp = 48,
            )

            // ----- Assert -----
            result shouldBe 0
        }
    }

    @Nested
    inner class TallestCoverHeightDp {
        private val sideBySide = DpSize(
            width = 320.dp,
            height = 110.dp,
        )
        private val stacked = DpSize(
            width = 110.dp,
            height = 400.dp,
        )

        @Test
        fun `is the tallest of the sizes cover slots`() {
            // ----- Arrange -----
            val short = DpSize(
                width = 110.dp,
                height = 150.dp,
            )
            val tall = DpSize(
                width = 110.dp,
                height = 300.dp,
            )

            // ----- Act -----
            val result = tallestCoverHeightDp(
                sizes = listOf(
                    short,
                    tall,
                ),
                eyebrowDp = { 10 },
                footDp = { 20 },
            )

            // ----- Assert -----
            result shouldBe stackedCoverHeightDp(
                heightDp = 300,
                eyebrowDp = 10,
                footDp = 20,
            )
        }

        @Test
        fun `a side by side size can be the tallest in a mixed list`() {
            // ----- Arrange -----
            val wideAndTall = DpSize(
                width = 500.dp,
                height = 300.dp,
            )
            val narrow = DpSize(
                width = 110.dp,
                height = 200.dp,
            )

            // ----- Act -----
            val result = tallestCoverHeightDp(
                sizes = listOf(
                    narrow,
                    wideAndTall,
                ),
                eyebrowDp = { 10 },
                footDp = { 20 },
            )

            // ----- Assert -----
            result shouldBe sideCoverHeightDp(wideAndTall)
        }

        @Test
        fun `a stacked size can be the tallest in a mixed list`() {
            // ----- Arrange -----
            val sizes = listOf(
                sideBySide,
                stacked,
            )

            // ----- Act -----
            val result = tallestCoverHeightDp(
                sizes = sizes,
                eyebrowDp = { 0 },
                footDp = { 0 },
            )

            // ----- Assert -----
            result shouldBe stackedCoverHeightDp(
                heightDp = 400,
                eyebrowDp = 0,
                footDp = 0,
            )
        }

        @Test
        fun `no sizes give the floor of one`() {
            // ----- Arrange -----
            val sizes = emptyList<DpSize>()

            // ----- Act -----
            val result = tallestCoverHeightDp(
                sizes = sizes,
                eyebrowDp = { 10 },
                footDp = { 20 },
            )

            // ----- Assert -----
            result shouldBe 1
        }
    }
}
