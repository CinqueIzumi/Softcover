package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.control.ThemeTilePainting
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode

class ThemeChoiceMapperTest {
    @Nested
    inner class Choices {
        @Test
        fun `returns one choice per ThemeMode entry in entries order`() {
            // ----- Act -----
            val result = themeChoicesFor(
                selected = ThemeMode.SYSTEM,
                palette = SpinePalette.DEFAULT,
                dynamicColor = false,
            )

            // ----- Assert -----
            result.map { it.mode } shouldBe ThemeMode.entries
        }
    }

    @Nested
    inner class TilePainting {
        @Test
        fun `paints the LIGHT mode's tile with the LIGHT painting`() {
            // ----- Act -----
            val result = themeChoicesFor(
                selected = ThemeMode.LIGHT,
                palette = SpinePalette.DEFAULT,
                dynamicColor = false,
            )

            // ----- Assert -----
            result.first { it.mode == ThemeMode.LIGHT }.tile.painting shouldBe ThemeTilePainting.LIGHT
        }

        @Test
        fun `paints the DARK mode's tile with the DARK painting`() {
            // ----- Act -----
            val result = themeChoicesFor(
                selected = ThemeMode.LIGHT,
                palette = SpinePalette.DEFAULT,
                dynamicColor = false,
            )

            // ----- Assert -----
            result.first { it.mode == ThemeMode.DARK }.tile.painting shouldBe ThemeTilePainting.DARK
        }

        @Test
        fun `paints the SYSTEM mode's tile with the SPLIT painting`() {
            // ----- Act -----
            val result = themeChoicesFor(
                selected = ThemeMode.LIGHT,
                palette = SpinePalette.DEFAULT,
                dynamicColor = false,
            )

            // ----- Assert -----
            result.first { it.mode == ThemeMode.SYSTEM }.tile.painting shouldBe ThemeTilePainting.SPLIT
        }
    }

    @Nested
    inner class Selection {
        @Test
        fun `marks exactly one tile selected, the one whose mode matches selected`() {
            ThemeMode.entries.forEach { selectedMode ->
                // ----- Act -----
                val result = themeChoicesFor(
                    selected = selectedMode,
                    palette = SpinePalette.DEFAULT,
                    dynamicColor = false,
                )

                // ----- Assert -----
                result.count { it.tile.selected } shouldBe 1
                result.first { it.tile.selected }.mode shouldBe selectedMode
            }
        }
    }

    @Nested
    inner class PaletteAndDynamicColor {
        @Test
        fun `passes palette and dynamicColor to every tile unchanged`() {
            // ----- Arrange -----
            val palette = SpinePalette.INK

            // ----- Act -----
            val result = themeChoicesFor(
                selected = ThemeMode.DARK,
                palette = palette,
                dynamicColor = true,
            )

            // ----- Assert -----
            result.forEach {
                it.tile.palette shouldBe palette
                it.tile.dynamicColor shouldBe true
            }
        }
    }

    @Nested
    inner class TapPayload {
        @Test
        fun `keeps the domain ThemeMode as each choice's tap payload`() {
            // ----- Act -----
            val result = themeChoicesFor(
                selected = ThemeMode.SYSTEM,
                palette = SpinePalette.DEFAULT,
                dynamicColor = false,
            )

            // ----- Assert -----
            result.map { it.mode }.toSet() shouldBe ThemeMode.entries.toSet()
        }
    }
}
