package nl.rhaydus.softcover.core.component.cover

import androidx.compose.ui.unit.dp
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MonogramCoverMetricsTest {
    private val widths = (1..600 step 3).map { it.dp }

    @Nested
    inner class Scaling {
        @Test
        fun `every metric grows with the width and never shrinks`() {
            // ----- Arrange -----
            val sampledWidths = widths

            // ----- Act -----
            val metrics = sampledWidths.map { monogramCoverMetrics(width = it) }

            // ----- Assert -----
            metrics.zipWithNext().forEach { (narrower, wider) ->
                wider.borderInset.value shouldBeGreaterThanOrEqualTo narrower.borderInset.value
                wider.initialFontSize.value shouldBeGreaterThanOrEqualTo narrower.initialFontSize.value
                wider.titleFontSize.value shouldBeGreaterThanOrEqualTo narrower.titleFontSize.value
                wider.titleMaxLines shouldBeGreaterThanOrEqualTo narrower.titleMaxLines
            }
        }

        @Test
        fun `a thumbnail narrower than every floor gets the same metrics as an even narrower one`() {
            // ----- Arrange -----
            val narrowWidth = 10.dp
            val narrowerWidth = 5.dp

            // ----- Act -----
            val narrow = monogramCoverMetrics(width = narrowWidth)
            val narrower = monogramCoverMetrics(width = narrowerWidth)

            // ----- Assert -----
            narrow.borderInset shouldBe narrower.borderInset
            narrow.initialFontSize shouldBe narrower.initialFontSize
            narrow.titleFontSize shouldBe narrower.titleFontSize
            narrow.titleMaxLines shouldBe narrower.titleMaxLines
        }

        @Test
        fun `a tile wider than every ceiling gets the same metrics as an even wider one`() {
            // ----- Arrange -----
            val wideWidth = 600.dp
            val widerWidth = 1200.dp

            // ----- Act -----
            val wide = monogramCoverMetrics(width = wideWidth)
            val wider = monogramCoverMetrics(width = widerWidth)

            // ----- Assert -----
            wide.borderInset shouldBe wider.borderInset
            wide.initialFontSize shouldBe wider.initialFontSize
            wide.titleFontSize shouldBe wider.titleFontSize
            wide.titleMaxLines shouldBe wider.titleMaxLines
        }

        @Test
        fun `a font size stays legible at every width`() {
            // ----- Arrange -----
            val sampledWidths = widths

            // ----- Act -----
            val metrics = sampledWidths.map { monogramCoverMetrics(width = it) }

            // ----- Assert -----
            metrics.forEach {
                it.initialFontSize.value shouldBeGreaterThanOrEqualTo 10f
                it.titleFontSize.value shouldBeGreaterThanOrEqualTo 10f
                it.titleMaxLines shouldBeGreaterThanOrEqualTo 1
            }
        }
    }

    @Nested
    inner class ShowFullTitle {
        @Test
        fun `hides the full title on a thumbnail and shows it on a tile`() {
            // ----- Arrange -----
            val thumbnailWidth = 20.dp
            val tileWidth = 200.dp

            // ----- Act -----
            val thumbnail = monogramCoverMetrics(width = thumbnailWidth)
            val tile = monogramCoverMetrics(width = tileWidth)

            // ----- Assert -----
            thumbnail.showFullTitle shouldBe false
            tile.showFullTitle shouldBe true
        }

        @Test
        fun `flips from hidden to shown exactly once as the width grows`() {
            // ----- Arrange -----
            val sampledWidths = widths

            // ----- Act -----
            val flags = sampledWidths.map { monogramCoverMetrics(width = it).showFullTitle }

            // ----- Assert -----
            flags.zipWithNext().count { (before, after) -> before != after } shouldBe 1
        }
    }
}
