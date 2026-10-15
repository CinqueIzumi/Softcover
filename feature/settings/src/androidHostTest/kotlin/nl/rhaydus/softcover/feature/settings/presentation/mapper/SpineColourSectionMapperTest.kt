package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpineColourSectionMapperTest {
    private val paletteGloss = "palette gloss"

    @Nested
    inner class Dim {
        @Test
        fun `steps back to an inert section regardless of dynamic colour`() {
            // ----- Arrange -----
            val dynamicColorStates = listOf(true, false)

            dynamicColorStates.forEach { useDynamicColor ->
                // ----- Act -----
                val result = spineColourSectionFor(
                    themeMode = ThemeMode.DIM,
                    useDynamicColor = useDynamicColor,
                    paletteGloss = paletteGloss,
                )

                // ----- Assert -----
                result.interactive shouldBe false
                result.gloss shouldNotBe paletteGloss
            }
        }

        @Test
        fun `uses the same gloss whether or not dynamic colour is on`() {
            // ----- Arrange -----
            val themeMode = ThemeMode.DIM

            // ----- Act -----
            val withDynamic = spineColourSectionFor(
                themeMode,
                useDynamicColor = true,
                paletteGloss = paletteGloss,
            )
            val withoutDynamic = spineColourSectionFor(
                themeMode,
                useDynamicColor = false,
                paletteGloss = paletteGloss,
            )

            // ----- Assert -----
            withDynamic shouldBe withoutDynamic
        }
    }

    @Nested
    inner class DynamicColour {
        @Test
        fun `gives an interactive dynamic-colour gloss rather than the palette gloss`() {
            // ----- Arrange -----
            val themes = ThemeMode.entries.filter { it != ThemeMode.DIM }

            themes.forEach { themeMode ->
                // ----- Act -----
                val result = spineColourSectionFor(
                    themeMode = themeMode,
                    useDynamicColor = true,
                    paletteGloss = paletteGloss,
                )

                // ----- Assert -----
                result.interactive shouldBe true
                result.gloss shouldNotBe paletteGloss
            }
        }

        @Test
        fun `differs from the Dim section`() {
            // ----- Arrange -----
            val dim = spineColourSectionFor(
                ThemeMode.DIM,
                useDynamicColor = true,
                paletteGloss = paletteGloss,
            )

            // ----- Act -----
            val result = spineColourSectionFor(
                ThemeMode.LIGHT,
                useDynamicColor = true,
                paletteGloss = paletteGloss,
            )

            // ----- Assert -----
            result.gloss shouldNotBe dim.gloss
        }
    }

    @Nested
    inner class Palette {
        @Test
        fun `gives an interactive section carrying the palette gloss`() {
            // ----- Arrange -----
            val themes = ThemeMode.entries.filter { it != ThemeMode.DIM }

            themes.forEach { themeMode ->
                // ----- Act -----
                val result = spineColourSectionFor(
                    themeMode = themeMode,
                    useDynamicColor = false,
                    paletteGloss = paletteGloss,
                )

                // ----- Assert -----
                result.interactive shouldBe true
                result.gloss shouldBe paletteGloss
            }
        }
    }
}
