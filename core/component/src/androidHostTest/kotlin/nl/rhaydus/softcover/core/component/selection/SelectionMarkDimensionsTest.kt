package nl.rhaydus.softcover.core.component.selection

import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SelectionMarkDimensionsTest {
    @Nested
    inner class ForVariant {
        @Test
        fun `OnCover resolves a 26dp mark with an 18dp check icon`() {
            // ----- Arrange & Act -----
            val dimensions = SelectionMarkDimensions.forVariant(variant = SelectionMarkVariant.OnCover)

            // ----- Assert -----
            dimensions shouldBe SelectionMarkDimensions(
                size = 26.dp,
                iconSize = 18.dp,
            )
        }

        @Test
        fun `RowLeading resolves a 24dp mark with a 16dp check icon`() {
            // ----- Arrange & Act -----
            val dimensions = SelectionMarkDimensions.forVariant(variant = SelectionMarkVariant.RowLeading)

            // ----- Assert -----
            dimensions shouldBe SelectionMarkDimensions(
                size = 24.dp,
                iconSize = 16.dp,
            )
        }

        @Test
        fun `EditionCard resolves a 24dp mark with a 15dp check icon`() {
            // ----- Arrange & Act -----
            val dimensions = SelectionMarkDimensions.forVariant(variant = SelectionMarkVariant.EditionCard)

            // ----- Assert -----
            dimensions shouldBe SelectionMarkDimensions(
                size = 24.dp,
                iconSize = 15.dp,
            )
        }
    }
}
