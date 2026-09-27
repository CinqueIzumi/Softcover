package nl.rhaydus.softcover.core.component.chip

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChipDimensionsTest {
    @Nested
    inner class PaddingFor {
        @Test
        fun `Regular resolves 14dp horizontal by 8dp vertical padding`() {
            // ----- Arrange & Act -----
            val padding = ChipDimensions.paddingFor(size = ChipSize.Regular)

            // ----- Assert -----
            padding shouldBe PaddingValues(
                horizontal = 14.dp,
                vertical = 8.dp,
            )
        }

        @Test
        fun `Compact resolves 10dp horizontal by 6dp vertical padding`() {
            // ----- Arrange & Act -----
            val padding = ChipDimensions.paddingFor(size = ChipSize.Compact)

            // ----- Assert -----
            padding shouldBe PaddingValues(
                horizontal = 10.dp,
                vertical = 6.dp,
            )
        }
    }

    @Nested
    inner class SharedConstants {
        @Test
        fun `leading and trailing icons share one 18dp size and 8dp gap`() {
            // ----- Assert -----
            ChipDimensions.iconSize shouldBe 18.dp
            ChipDimensions.iconGap shouldBe 8.dp
        }

        @Test
        fun `the dismiss glyph uses its own 16dp size and 8dp gap`() {
            // ----- Assert -----
            ChipDimensions.dismissIconSize shouldBe 16.dp
            ChipDimensions.dismissIconGap shouldBe 8.dp
        }

        @Test
        fun `border width is 1dp and the disabled alpha is 0,45`() {
            // ----- Assert -----
            ChipDimensions.borderWidth shouldBe 1.dp
            ChipDimensions.disabledAlpha shouldBe 0.45f
        }
    }
}
