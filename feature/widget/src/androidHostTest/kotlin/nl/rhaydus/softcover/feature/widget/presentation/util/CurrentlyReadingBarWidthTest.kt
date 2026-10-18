package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.dp
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentlyReadingBarWidthTest {
    @Nested
    inner class ProgressBarWidthDp {
        @Test
        fun `the bar and label never exceed the widget width`() {
            // ----- Act & Assert -----
            for (width in WIDGET_WIDTHS_DP) {
                val bar = progressBarWidthDp(widgetWidth = width.dp)

                if (bar > PROGRESS_BAR_HEIGHT_DP) {
                    val row = bar.dp + PROGRESS_LABEL_WIDTH_DP + Spacing.xxs * 2

                    row.value.toInt() shouldBeLessThanOrEqual width
                }
            }
        }

        @Test
        fun `a widget too narrow for the row keeps a bar as wide as it is tall`() {
            // ----- Arrange -----
            val widget = 40.dp

            // ----- Act -----
            val bar = progressBarWidthDp(widgetWidth = widget)

            // ----- Assert -----
            bar shouldBe PROGRESS_BAR_HEIGHT_DP
        }

        @Test
        fun `a wider widget gets a wider bar`() {
            // ----- Arrange -----
            val narrow = progressBarWidthDp(widgetWidth = 180.dp)

            // ----- Act -----
            val wide = progressBarWidthDp(widgetWidth = 320.dp)

            // ----- Assert -----
            wide shouldBeGreaterThan narrow
        }
    }
}
