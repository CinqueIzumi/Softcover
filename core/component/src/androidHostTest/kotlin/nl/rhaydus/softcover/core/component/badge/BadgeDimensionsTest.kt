package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BadgeDimensionsTest {
    @Nested
    inner class PaddingFor {
        @Test
        fun `Compact resolves 6dp horizontal by 2dp vertical padding`() {
            // ----- Arrange & Act -----
            val padding = BadgeDimensions.paddingFor(size = BadgeSize.Compact)

            // ----- Assert -----
            padding shouldBe PaddingValues(
                horizontal = 6.dp,
                vertical = 2.dp,
            )
        }

        @Test
        fun `Regular resolves 8dp horizontal by 4dp vertical padding`() {
            // ----- Arrange & Act -----
            val padding = BadgeDimensions.paddingFor(size = BadgeSize.Regular)

            // ----- Assert -----
            padding shouldBe PaddingValues(
                horizontal = 8.dp,
                vertical = 4.dp,
            )
        }
    }

    @Nested
    inner class Shared {
        @Test
        fun `shape is a 6dp rounded corner shared by every size`() {
            // ----- Arrange & Act -----
            val shape = BadgeDimensions.shape

            // ----- Assert -----
            shape shouldBe RoundedCornerShape(6.dp)
        }

        @Test
        fun `iconSize and iconGap are shared by every size`() {
            // ----- Arrange & Act & Assert -----
            BadgeDimensions.iconSize shouldBe 12.dp
            BadgeDimensions.iconGap shouldBe 4.dp
        }
    }
}
