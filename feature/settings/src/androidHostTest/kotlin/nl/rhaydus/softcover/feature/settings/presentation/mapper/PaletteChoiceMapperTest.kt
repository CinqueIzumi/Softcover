package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PaletteChoiceMapperTest {
    @Nested
    inner class Selection {
        @Test
        fun `marks exactly one tile selected, the one whose palette matches selected`() {
            // ----- Arrange -----
            val palettes = ColorPalette.entries

            palettes.forEach { selectedPalette ->
                // ----- Act -----
                val result = paletteChoicesFor(selected = selectedPalette)

                // ----- Assert -----
                result.count { it.selected } shouldBe 1
                result.first { it.selected }.key shouldBe selectedPalette
            }
        }
    }

    @Nested
    inner class Enabled {
        @Test
        fun `carries enabled false onto every tile`() {
            // ----- Arrange -----
            val selected = ColorPalette.entries.first()

            // ----- Act -----
            val result = paletteChoicesFor(
                selected = selected,
                enabled = false,
            )

            // ----- Assert -----
            result.size shouldBe ColorPalette.entries.size
            result.none { it.enabled } shouldBe true
        }

        @Test
        fun `carries enabled true onto every tile`() {
            // ----- Arrange -----
            val selected = ColorPalette.entries.first()

            // ----- Act -----
            val result = paletteChoicesFor(
                selected = selected,
                enabled = true,
            )

            // ----- Assert -----
            result.all { it.enabled } shouldBe true
        }
    }
}
