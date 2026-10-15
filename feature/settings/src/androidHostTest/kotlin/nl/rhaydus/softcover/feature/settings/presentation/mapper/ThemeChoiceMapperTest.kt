package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ThemeChoiceMapperTest {
    @Nested
    inner class Selection {
        @Test
        fun `marks exactly one tile selected, the one whose mode matches selected`() {
            // ----- Arrange -----
            val modes = ThemeMode.entries

            modes.forEach { selectedMode ->
                // ----- Act -----
                val result = themeChoicesFor(
                    selected = selectedMode,
                    palette = SpinePalette.DEFAULT,
                    dynamicColor = false,
                )

                // ----- Assert -----
                result.count { it.selected } shouldBe 1
                result.first { it.selected }.key shouldBe selectedMode
            }
        }
    }

    @Nested
    inner class DynamicColor {
        @Test
        fun `dim tile is never dynamic even when dynamic colour is on`() {
            // ----- Arrange -----
            val dynamicColor = true

            // ----- Act -----
            val result = themeChoicesFor(
                selected = ThemeMode.LIGHT,
                palette = SpinePalette.DEFAULT,
                dynamicColor = dynamicColor,
            )

            // ----- Assert -----
            result.first { it.key == ThemeMode.DIM }.dynamicColor shouldBe false
        }

        @Test
        fun `other tiles carry the dynamic colour input`() {
            listOf(true, false).forEach { dynamicColor ->
                // ----- Arrange -----
                val others = ThemeMode.entries.filter { it != ThemeMode.DIM }

                // ----- Act -----
                val result = themeChoicesFor(
                    selected = ThemeMode.LIGHT,
                    palette = SpinePalette.DEFAULT,
                    dynamicColor = dynamicColor,
                )

                // ----- Assert -----
                others.forEach { mode ->
                    result.first { it.key == mode }.dynamicColor shouldBe dynamicColor
                }
            }
        }
    }
}
