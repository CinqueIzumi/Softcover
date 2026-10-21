package nl.rhaydus.softcover.showcase.store

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShowcaseThemeStoreTest {
    @Nested
    inner class SelectPalette {
        @Test
        fun `replaces the palette and keeps the brightness`() {
            // ----- Arrange -----
            val store = ShowcaseThemeStore()
            store.selectBrightness(SchemeBrightness.DARK)

            // ----- Act -----
            store.selectPalette(SpinePalette.SEA)

            // ----- Assert -----
            store.theme.value shouldBe ShowcaseTheme(
                palette = SpinePalette.SEA,
                brightness = SchemeBrightness.DARK,
            )
        }
    }

    @Nested
    inner class SelectBrightness {
        @Test
        fun `replaces the brightness and keeps the palette`() {
            // ----- Arrange -----
            val store = ShowcaseThemeStore()
            store.selectPalette(SpinePalette.INK)

            // ----- Act -----
            store.selectBrightness(SchemeBrightness.DARK)

            // ----- Assert -----
            store.theme.value shouldBe ShowcaseTheme(
                palette = SpinePalette.INK,
                brightness = SchemeBrightness.DARK,
            )
        }
    }
}
