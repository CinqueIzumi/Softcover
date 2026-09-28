package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BadgeDimensionsTest {
    @Nested
    inner class ForVariant {
        @Test
        fun `Standard resolves 6dp by 2dp padding with no weight or icon override`() {
            // ----- Arrange & Act -----
            val dimensions = BadgeDimensions.forVariant(variant = BadgeVariant.Standard)

            // ----- Assert -----
            dimensions.horizontal shouldBe 6.dp
            dimensions.vertical shouldBe 2.dp
            dimensions.shape shouldBe RoundedCornerShape(6.dp)
            dimensions.fontWeight shouldBe null
            dimensions.letterSpacing shouldBe TextUnit.Unspecified
        }

        @Test
        fun `FeaturedRelease resolves 8dp by 4dp padding with no weight or icon override`() {
            // ----- Arrange & Act -----
            val dimensions = BadgeDimensions.forVariant(variant = BadgeVariant.FeaturedRelease)

            // ----- Assert -----
            dimensions.horizontal shouldBe 8.dp
            dimensions.vertical shouldBe 4.dp
            dimensions.shape shouldBe RoundedCornerShape(6.dp)
            dimensions.fontWeight shouldBe null
            dimensions.letterSpacing shouldBe TextUnit.Unspecified
        }

        @Test
        fun `GridCountdown resolves 8dp by 4dp padding, SemiBold weight and an 11dp icon`() {
            // ----- Arrange & Act -----
            val dimensions = BadgeDimensions.forVariant(variant = BadgeVariant.GridCountdown)

            // ----- Assert -----
            dimensions.horizontal shouldBe 8.dp
            dimensions.vertical shouldBe 4.dp
            dimensions.shape shouldBe RoundedCornerShape(6.dp)
            dimensions.fontWeight shouldBe FontWeight.SemiBold
            dimensions.letterSpacing shouldBe TextUnit.Unspecified
            dimensions.iconSize shouldBe 11.dp
            dimensions.iconGap shouldBe 4.dp
        }

        @Test
        fun `OwnedCover resolves 8dp by 4dp padding, an 8dp shape radius, Bold weight and a 14dp icon`() {
            // ----- Arrange & Act -----
            val dimensions = BadgeDimensions.forVariant(variant = BadgeVariant.OwnedCover)

            // ----- Assert -----
            dimensions.horizontal shouldBe 8.dp
            dimensions.vertical shouldBe 4.dp
            dimensions.shape shouldBe RoundedCornerShape(8.dp)
            dimensions.fontWeight shouldBe FontWeight.Bold
            dimensions.letterSpacing shouldBe TextUnit.Unspecified
            dimensions.iconSize shouldBe 14.dp
            dimensions.iconGap shouldBe 4.dp
        }

        @Test
        fun `TrackingTag resolves 9dp by 4dp padding, SemiBold weight and 0,6sp tracking`() {
            // ----- Arrange & Act -----
            val dimensions = BadgeDimensions.forVariant(variant = BadgeVariant.TrackingTag)

            // ----- Assert -----
            dimensions.horizontal shouldBe 9.dp
            dimensions.vertical shouldBe 4.dp
            dimensions.shape shouldBe RoundedCornerShape(percent = 50)
            dimensions.fontWeight shouldBe FontWeight.SemiBold
            dimensions.letterSpacing shouldBe 0.6.sp
        }
    }
}
