package nl.rhaydus.softcover.showcase.appearance.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppearanceChipsMapperTest {
    @Nested
    inner class PaletteChips {
        @Test
        fun `marks exactly the selected palette`() {
            // ----- Arrange -----
            val selected = SpinePalette.SEA

            // ----- Act -----
            val chipSet = paletteChips(selected = selected)

            // ----- Assert -----
            chipSet.chips.filter { it.selected }.map { chipSet[it.key] } shouldContainExactly listOf(selected)
            chipSet.chips.size shouldBe SpinePalette.entries.size
        }
    }

    @Nested
    inner class BrightnessChips {
        @Test
        fun `marks exactly the selected brightness`() {
            // ----- Arrange -----
            val selected = SchemeBrightness.DARK

            // ----- Act -----
            val chipSet = brightnessChips(selected = selected)

            // ----- Assert -----
            chipSet.chips.filter { it.selected }.map { chipSet[it.key] } shouldContainExactly listOf(selected)
            chipSet.chips.size shouldBe SchemeBrightness.entries.size
        }
    }
}
