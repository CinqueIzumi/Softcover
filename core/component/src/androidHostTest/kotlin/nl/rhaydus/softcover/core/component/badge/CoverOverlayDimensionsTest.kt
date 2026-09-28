package nl.rhaydus.softcover.core.component.badge

import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoverOverlayDimensionsTest {
    @Nested
    inner class InsetFor {
        @Test
        fun `Compact resolves the same 6dp inset as the badge's own horizontal padding`() {
            // ----- Arrange & Act -----
            val inset = CoverOverlayDimensions.insetFor(size = BadgeSize.Compact)

            // ----- Assert -----
            inset shouldBe 6.dp
        }

        @Test
        fun `Regular resolves the same 8dp inset as the badge's own horizontal padding`() {
            // ----- Arrange & Act -----
            val inset = CoverOverlayDimensions.insetFor(size = BadgeSize.Regular)

            // ----- Assert -----
            inset shouldBe 8.dp
        }
    }
}
