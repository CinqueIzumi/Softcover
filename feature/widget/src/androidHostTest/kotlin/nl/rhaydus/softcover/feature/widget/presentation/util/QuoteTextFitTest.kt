package nl.rhaydus.softcover.feature.widget.presentation.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteTextFitTest {
    private val lineHeightPx = 10

    // Wraps to as many lines as the font size is large, so bigger text needs more height.
    private val measurer: QuoteTextMeasurer = { fontSizeSp, _ ->
        QuoteTextMeasure(
            lineCount = fontSizeSp,
            lineHeightPx = lineHeightPx,
        )
    }

    @Nested
    inner class FitQuoteText {
        @Test
        fun `picks the largest size at which the whole quote fits`() {
            // ----- Arrange -----
            val heightPx = 150

            // ----- Act -----
            val result = fitQuoteText(
                availableWidthPx = 300,
                availableHeightPx = heightPx,
                measure = measurer,
            )

            // ----- Assert -----
            result.fontSizeSp shouldBe 15
            result.maxLines shouldBe 15
        }

        @Test
        fun `returns the maximum size when the quote fits at it`() {
            // ----- Arrange -----
            val heightPx = 1_000

            // ----- Act -----
            val result = fitQuoteText(
                availableWidthPx = 300,
                availableHeightPx = heightPx,
                measure = measurer,
            )

            // ----- Assert -----
            result.fontSizeSp shouldBe QUOTE_MAX_FONT_SIZE_SP
        }

        @Test
        fun `returns the minimum size with the lines that fit when nothing fits`() {
            // ----- Arrange -----
            val heightPx = 90

            // ----- Act -----
            val result = fitQuoteText(
                availableWidthPx = 300,
                availableHeightPx = heightPx,
                measure = measurer,
            )

            // ----- Assert -----
            result.fontSizeSp shouldBe QUOTE_MIN_FONT_SIZE_SP
            result.maxLines shouldBe 9
        }

        @Test
        fun `never allows fewer than one line`() {
            // ----- Arrange -----
            val heightPx = 3

            // ----- Act -----
            val result = fitQuoteText(
                availableWidthPx = 300,
                availableHeightPx = heightPx,
                measure = measurer,
            )

            // ----- Assert -----
            result.fontSizeSp shouldBe QUOTE_MIN_FONT_SIZE_SP
            result.maxLines shouldBe 1
        }
    }
}
