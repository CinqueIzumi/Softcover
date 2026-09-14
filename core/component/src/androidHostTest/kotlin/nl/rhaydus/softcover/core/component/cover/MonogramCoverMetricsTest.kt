package nl.rhaydus.softcover.core.component.cover

import androidx.compose.ui.unit.dp
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MonogramCoverMetricsTest {
    @Nested
    inner class BorderInset {
        @Test
        fun `floors borderInset at 3dp for a narrow shelf thumbnail`() {
            // ----- Arrange -----
            val width = 48.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.borderInset shouldBe 3.dp
        }

        @Test
        fun `ceils borderInset at 8dp for a wide library grid tile`() {
            // ----- Arrange -----
            val width = 300.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.borderInset shouldBe 8.dp
        }

        @Test
        fun `scales borderInset proportionally to width between the floor and ceiling`() {
            // ----- Arrange -----
            val width = 100.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.borderInset.value shouldBe (6f plusOrMinus 0.01f)
        }
    }

    @Nested
    inner class InitialFontSize {
        @Test
        fun `floors initialFontSize at 10sp for a narrow shelf thumbnail`() {
            // ----- Arrange -----
            val width = 20.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.initialFontSize.value shouldBe (10f plusOrMinus 0.01f)
        }

        @Test
        fun `ceils initialFontSize at 96sp for a very wide desktop tile`() {
            // ----- Arrange -----
            val width = 400.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.initialFontSize.value shouldBe (96f plusOrMinus 0.01f)
        }

        @Test
        fun `scales initialFontSize proportionally to width between the floor and ceiling`() {
            // ----- Arrange -----
            val width = 150.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.initialFontSize.value shouldBe (54f plusOrMinus 0.01f)
        }
    }

    @Nested
    inner class ShowFullTitle {
        @Test
        fun `hides the full title just below the 64dp floor`() {
            // ----- Arrange -----
            val width = 63.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.showFullTitle shouldBe false
        }

        @Test
        fun `shows the full title exactly at the 64dp floor`() {
            // ----- Arrange -----
            val width = 64.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.showFullTitle shouldBe true
        }

        @Test
        fun `shows the full title comfortably above the 64dp floor`() {
            // ----- Arrange -----
            val width = 200.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.showFullTitle shouldBe true
        }
    }

    @Nested
    inner class TitleFontSize {
        @Test
        fun `floors titleFontSize at 10sp for a narrow shelf thumbnail`() {
            // ----- Arrange -----
            val width = 20.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.titleFontSize.value shouldBe (10f plusOrMinus 0.01f)
        }

        @Test
        fun `ceils titleFontSize at 28sp for a wide library grid tile`() {
            // ----- Arrange -----
            val width = 300.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.titleFontSize.value shouldBe (28f plusOrMinus 0.01f)
        }

        @Test
        fun `scales titleFontSize proportionally to width between the floor and ceiling`() {
            // ----- Arrange -----
            val width = 100.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.titleFontSize.value shouldBe (15f plusOrMinus 0.01f)
        }
    }

    @Nested
    inner class TitleMaxLines {
        @Test
        fun `floors titleMaxLines at 2 for a narrow shelf thumbnail`() {
            // ----- Arrange -----
            val width = 32.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.titleMaxLines shouldBe 2
        }

        @Test
        fun `ceils titleMaxLines at 5 for a wide library grid tile`() {
            // ----- Arrange -----
            val width = 300.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.titleMaxLines shouldBe 5
        }

        @Test
        fun `rounds titleMaxLines half-up between the floor and ceiling`() {
            // ----- Arrange -----
            val width = 112.dp

            // ----- Act -----
            val result = monogramCoverMetrics(width = width)

            // ----- Assert -----
            result.titleMaxLines shouldBe 4
        }
    }
}
