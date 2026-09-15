package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette

class PaletteChoiceMapperTest {
    @Nested
    inner class Choices {
        @Test
        fun `returns one choice per ColorPalette entry in entries order`() {
            // ----- Act -----
            val result = paletteChoicesFor(selected = ColorPalette.DEFAULT)

            // ----- Assert -----
            result.map { it.colorPalette } shouldBe ColorPalette.entries
        }
    }

    @Nested
    inner class Selection {
        @Test
        fun `marks exactly one tile selected, the one whose palette matches selected`() {
            ColorPalette.entries.forEach { selectedPalette ->
                // ----- Act -----
                val result = paletteChoicesFor(selected = selectedPalette)

                // ----- Assert -----
                result.count { it.tile.selected } shouldBe 1
                result.first { it.tile.selected }.colorPalette shouldBe selectedPalette
            }
        }
    }

    @Nested
    inner class TilePalette {
        @Test
        fun `carries each choice's SpinePalette tile mapped from its ColorPalette`() {
            // ----- Act -----
            val result = paletteChoicesFor(selected = ColorPalette.DEFAULT)

            // ----- Assert -----
            result.forEach {
                it.tile.palette shouldBe it.colorPalette.toSpinePalette()
            }
        }
    }

    @Nested
    inner class TapPayload {
        @Test
        fun `keeps the domain ColorPalette as each choice's tap payload`() {
            // ----- Act -----
            val result = paletteChoicesFor(selected = ColorPalette.DEFAULT)

            // ----- Assert -----
            result.map { it.colorPalette }.toSet() shouldBe ColorPalette.entries.toSet()
        }
    }
}
