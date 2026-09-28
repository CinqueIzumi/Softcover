package nl.rhaydus.softcover.core.component.badge

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BadgeColorsTest {
    // region Fixtures
    private val colorScheme = lightColorScheme()
    // endregion
    @Nested
    inner class Of {
        @Test
        fun `Primary resolves the primary container and onPrimary content`() {
            // ----- Arrange & Act -----
            val colors = BadgeColors.of(
                tone = BadgeTone.Primary,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe BadgeColors(
                container = colorScheme.primary,
                content = colorScheme.onPrimary,
            )
        }

        @Test
        fun `PrimaryContainer resolves the primaryContainer and onPrimaryContainer roles`() {
            // ----- Arrange & Act -----
            val colors = BadgeColors.of(
                tone = BadgeTone.PrimaryContainer,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe BadgeColors(
                container = colorScheme.primaryContainer,
                content = colorScheme.onPrimaryContainer,
            )
        }

        @Test
        fun `ErrorContainer resolves the errorContainer and onErrorContainer roles`() {
            // ----- Arrange & Act -----
            val colors = BadgeColors.of(
                tone = BadgeTone.ErrorContainer,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe BadgeColors(
                container = colorScheme.errorContainer,
                content = colorScheme.onErrorContainer,
            )
        }

        @Test
        fun `Neutral resolves the surfaceContainerHigh and onSurfaceVariant roles`() {
            // ----- Arrange & Act -----
            val colors = BadgeColors.of(
                tone = BadgeTone.Neutral,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe BadgeColors(
                container = colorScheme.surfaceContainerHigh,
                content = colorScheme.onSurfaceVariant,
            )
        }

        @Test
        fun `Scrim resolves black at 0,55 alpha over a white content colour`() {
            // ----- Arrange & Act -----
            val colors = BadgeColors.of(
                tone = BadgeTone.Scrim,
                scheme = colorScheme,
            )

            // ----- Assert -----
            colors shouldBe BadgeColors(
                container = Color.Black.copy(alpha = 0.55f),
                content = Color.White,
            )
        }
    }
}
