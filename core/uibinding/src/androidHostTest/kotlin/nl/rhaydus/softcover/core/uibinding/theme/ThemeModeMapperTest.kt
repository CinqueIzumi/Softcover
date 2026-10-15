package nl.rhaydus.softcover.core.uibinding.theme

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ThemeModeMapperTest {
    @Nested
    inner class ToSchemeBrightness {
        @Test
        fun `light ignores the system setting`() {
            // ----- Arrange -----
            val mode = ThemeMode.LIGHT

            // ----- Act -----
            val whenSystemDark = mode.toSchemeBrightness(systemInDark = true)
            val whenSystemLight = mode.toSchemeBrightness(systemInDark = false)

            // ----- Assert -----
            whenSystemDark shouldBe SchemeBrightness.LIGHT
            whenSystemLight shouldBe SchemeBrightness.LIGHT
        }

        @Test
        fun `dark ignores the system setting`() {
            // ----- Arrange -----
            val mode = ThemeMode.DARK

            // ----- Act -----
            val whenSystemDark = mode.toSchemeBrightness(systemInDark = true)
            val whenSystemLight = mode.toSchemeBrightness(systemInDark = false)

            // ----- Assert -----
            whenSystemDark shouldBe SchemeBrightness.DARK
            whenSystemLight shouldBe SchemeBrightness.DARK
        }

        @Test
        fun `dim ignores the system setting`() {
            // ----- Arrange -----
            val mode = ThemeMode.DIM

            // ----- Act -----
            val whenSystemDark = mode.toSchemeBrightness(systemInDark = true)
            val whenSystemLight = mode.toSchemeBrightness(systemInDark = false)

            // ----- Assert -----
            whenSystemDark shouldBe SchemeBrightness.DIM
            whenSystemLight shouldBe SchemeBrightness.DIM
        }

        @Test
        fun `system follows the system setting`() {
            // ----- Arrange -----
            val mode = ThemeMode.SYSTEM

            // ----- Act -----
            val whenSystemDark = mode.toSchemeBrightness(systemInDark = true)
            val whenSystemLight = mode.toSchemeBrightness(systemInDark = false)

            // ----- Assert -----
            whenSystemDark shouldBe SchemeBrightness.DARK
            whenSystemLight shouldBe SchemeBrightness.LIGHT
        }
    }
}
