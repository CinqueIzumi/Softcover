package nl.rhaydus.softcover.core.component.badge

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoverOverlayDimensionsTest {
    @Nested
    inner class ForVariant {
        @Test
        fun `ReadingStatus pins top-end with 6dp padding`() {
            // ----- Arrange & Act -----
            val dimensions = CoverOverlayDimensions.forVariant(variant = CoverOverlayVariant.ReadingStatus)

            // ----- Assert -----
            dimensions shouldBe CoverOverlayDimensions(
                alignment = Alignment.TopEnd,
                padding = 6.dp,
            )
        }

        @Test
        fun `LibraryCountdown pins top-start with 8dp padding`() {
            // ----- Arrange & Act -----
            val dimensions = CoverOverlayDimensions.forVariant(variant = CoverOverlayVariant.LibraryCountdown)

            // ----- Assert -----
            dimensions shouldBe CoverOverlayDimensions(
                alignment = Alignment.TopStart,
                padding = 8.dp,
            )
        }
    }
}
