package nl.rhaydus.softcover.core.component.selection

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SelectionMarkColorsTest {
    // region Fixtures
    private val colorScheme = lightColorScheme()
    // endregion
    @Nested
    inner class Of {
        @Test
        fun `selected over an Image backdrop resolves the primary and onPrimary roles`() {
            // ----- Arrange & Act -----
            val colors = SelectionMarkColors.of(
                selected = true,
                backdrop = SelectionMarkBackdrop.Image,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe SelectionMarkColors(
                container = colorScheme.primary,
                content = colorScheme.onPrimary,
            )
        }

        @Test
        fun `selected over a Surface backdrop resolves the primary and onPrimary roles`() {
            // ----- Arrange & Act -----
            val colors = SelectionMarkColors.of(
                selected = true,
                backdrop = SelectionMarkBackdrop.Surface,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe SelectionMarkColors(
                container = colorScheme.primary,
                content = colorScheme.onPrimary,
            )
        }

        @Test
        fun `unselected over an Image backdrop resolves surface at 0,72 alpha with unspecified content`() {
            // ----- Arrange & Act -----
            val colors = SelectionMarkColors.of(
                selected = false,
                backdrop = SelectionMarkBackdrop.Image,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe SelectionMarkColors(
                container = colorScheme.surface.copy(alpha = 0.72f),
                content = Color.Unspecified,
            )
        }

        @Test
        fun `unselected over a Surface backdrop resolves surfaceContainerHigh with unspecified content`() {
            // ----- Arrange & Act -----
            val colors = SelectionMarkColors.of(
                selected = false,
                backdrop = SelectionMarkBackdrop.Surface,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe SelectionMarkColors(
                container = colorScheme.surfaceContainerHigh,
                content = Color.Unspecified,
            )
        }
    }
}
