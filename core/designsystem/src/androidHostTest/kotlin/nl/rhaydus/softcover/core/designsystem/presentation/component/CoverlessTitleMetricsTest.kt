package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private val BASE_FONT_SIZE = 28.sp

class CoverlessTitleMetricsTest {
    @Nested
    inner class Padding {
        @Test
        fun `caps horizontal and vertical padding at their ceilings for a library grid tile`() {
            // ----- Arrange -----
            val width = 160.dp
            val height = 240.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            result.horizontalPadding shouldBe 12.dp
            result.verticalPadding shouldBe 16.dp
        }

        @Test
        fun `floors horizontal and vertical padding for a shelf thumbnail rather than collapsing them`() {
            // ----- Arrange -----
            val width = 30.dp
            val height = 45.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            result.horizontalPadding shouldBe 4.dp
            result.verticalPadding shouldBe 4.dp
        }
    }

    @Nested
    inner class MinFontSize {
        @Test
        fun `floors minFontSize at 7sp for a narrow shelf thumbnail`() {
            // ----- Arrange -----
            val width = 48.dp
            val height = 72.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            result.minFontSize.value shouldBe (7f plusOrMinus 0.01f)
        }

        @Test
        fun `scales minFontSize proportionally to width once above the floor`() {
            // ----- Arrange -----
            val width = 300.dp
            val height = 450.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            result.minFontSize.value shouldBe (30f plusOrMinus 0.01f)
        }
    }

    @Nested
    inner class MaxFontSize {
        @Test
        fun `never exceeds baseFontSize on a very wide tile`() {
            // ----- Arrange -----
            val width = 250.dp
            val height = 300.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            (result.maxFontSize.value <= BASE_FONT_SIZE.value) shouldBe true
            (result.maxFontSize.value >= result.minFontSize.value) shouldBe true
        }

        @Test
        fun `reaches full baseFontSize at a real three-column grid tile width now that the max font ceiling fraction is raised`() {
            // ----- Arrange -----
            val width = 110.dp
            val height = 165.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            result.maxFontSize shouldBe BASE_FONT_SIZE
        }

        @Test
        fun `never drops below minFontSize on a very narrow tile`() {
            // ----- Arrange -----
            val width = 10.dp
            val height = 40.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            (result.maxFontSize.value >= result.minFontSize.value) shouldBe true
        }
    }

    @Nested
    inner class MaxLines {
        @Test
        fun `clamps to the 1-line floor when available text height collapses to zero`() {
            // ----- Arrange -----
            val width = 160.dp
            val height = 10.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            result.maxLines shouldBe 1
        }

        @Test
        fun `clamps to the 5-line cap for an absurdly tall tile`() {
            // ----- Arrange -----
            val width = 160.dp
            val height = 10000.dp

            // ----- Act -----
            val result = coverlessTitleMetrics(
                width = width,
                height = height,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            result.maxLines shouldBe 5
        }

        @Test
        fun `shrinks with available height at a fixed width`() {
            // ----- Arrange -----
            val width = 160.dp
            val shortHeight = 60.dp
            val tallHeight = 150.dp

            // ----- Act -----
            val shortResult = coverlessTitleMetrics(
                width = width,
                height = shortHeight,
                baseFontSize = BASE_FONT_SIZE,
            )
            val tallResult = coverlessTitleMetrics(
                width = width,
                height = tallHeight,
                baseFontSize = BASE_FONT_SIZE,
            )

            // ----- Assert -----
            (shortResult.maxLines < tallResult.maxLines) shouldBe true
        }
    }
}
