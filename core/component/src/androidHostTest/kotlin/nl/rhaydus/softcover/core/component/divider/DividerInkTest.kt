package nl.rhaydus.softcover.core.component.divider

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DividerInkTest {
    @Nested
    inner class Resolve {
        // region Fixtures
        private val colorScheme = lightColorScheme(
            outlineVariant = Color(color = 0xFF111111),
        )
        private val contentColor = Color(color = 0xFF222222)
        // endregion
        @Test
        fun `Outline resolves to the colour scheme's outlineVariant`() {
            // ----- Act -----
            val color = DividerInk.Outline.resolve(
                colorScheme = colorScheme,
                contentColor = contentColor,
            )

            // ----- Assert -----
            color shouldBe colorScheme.outlineVariant
        }

        @Test
        fun `ContentTint resolves to the content colour at its alpha`() {
            // ----- Act -----
            val color = DividerInk.ContentTint(alpha = 0.25f).resolve(
                colorScheme = colorScheme,
                contentColor = contentColor,
            )

            // ----- Assert -----
            color shouldBe contentColor.copy(alpha = 0.25f)
        }
    }
}
