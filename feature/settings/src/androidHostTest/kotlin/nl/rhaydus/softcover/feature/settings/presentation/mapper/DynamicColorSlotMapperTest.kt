package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.model.DynamicColorSlotUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DynamicColorSlotMapperTest {
    @Nested
    inner class Dim {
        @Test
        fun `gives the gloss when the platform supports dynamic colour`() {
            // ----- Arrange -----
            val themeMode = ThemeMode.DIM

            // ----- Act -----
            val result = dynamicColorSlotFor(
                themeMode = themeMode,
                supportsDynamicColor = true,
                useDynamicColor = false,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<DynamicColorSlotUiModel.Gloss>()
        }

        @Test
        fun `stays hidden when the platform does not support dynamic colour`() {
            // ----- Arrange -----
            val themeMode = ThemeMode.DIM

            // ----- Act -----
            val result = dynamicColorSlotFor(
                themeMode = themeMode,
                supportsDynamicColor = false,
                useDynamicColor = true,
            )

            // ----- Assert -----
            result shouldBe DynamicColorSlotUiModel.Hidden
        }
    }

    @Nested
    inner class Supported {
        @Test
        fun `gives a checked toggle for every non-Dim theme while dynamic colour is on`() {
            // ----- Arrange -----
            val themes = ThemeMode.entries.filter { it != ThemeMode.DIM }

            themes.forEach { themeMode ->
                // ----- Act -----
                val result = dynamicColorSlotFor(
                    themeMode = themeMode,
                    supportsDynamicColor = true,
                    useDynamicColor = true,
                )

                // ----- Assert -----
                result.shouldBeInstanceOf<DynamicColorSlotUiModel.Toggle>().row.checked shouldBe true
            }
        }

        @Test
        fun `gives an unchecked toggle while dynamic colour is off`() {
            // ----- Arrange -----
            val themeMode = ThemeMode.LIGHT

            // ----- Act -----
            val result = dynamicColorSlotFor(
                themeMode = themeMode,
                supportsDynamicColor = true,
                useDynamicColor = false,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<DynamicColorSlotUiModel.Toggle>().row.checked shouldBe false
        }
    }

    @Nested
    inner class Unsupported {
        @Test
        fun `gives hidden for every non-Dim theme`() {
            // ----- Arrange -----
            val themes = ThemeMode.entries.filter { it != ThemeMode.DIM }

            themes.forEach { themeMode ->
                // ----- Act -----
                val result = dynamicColorSlotFor(
                    themeMode = themeMode,
                    supportsDynamicColor = false,
                    useDynamicColor = true,
                )

                // ----- Assert -----
                result shouldBe DynamicColorSlotUiModel.Hidden
            }
        }
    }
}
