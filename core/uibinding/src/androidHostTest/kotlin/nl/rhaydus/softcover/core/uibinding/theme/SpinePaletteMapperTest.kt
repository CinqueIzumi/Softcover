package nl.rhaydus.softcover.core.uibinding.theme

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpinePaletteMapperTest {
    @Nested
    inner class NameParity {
        @Test
        fun `every ColorPalette maps to the same-named SpinePalette`() {
            ColorPalette.entries.forEach {
                // ----- Act -----
                val mapped = it.toSpinePalette()

                // ----- Assert -----
                mapped.name shouldBe it.name
            }
        }
    }

    @Nested
    inner class Bijection {
        @Test
        fun `the mapping is a bijection so the two enums cannot drift apart`() {
            // The mapper's `when` is exhaustive over `ColorPalette`, its receiver, so adding a
            // `ColorPalette` entry without a matching `SpinePalette` branch is already a compile
            // error. The reverse direction compiles happily: a `SpinePalette` entry added first,
            // with no `ColorPalette` ever mapping to it, is a look nothing can ever select. No
            // `when` can catch that — this set-equality assertion is what does.
            // ----- Act -----
            val mappedPalettes = ColorPalette.entries.map { it.toSpinePalette() }.toSet()

            // ----- Assert -----
            mappedPalettes shouldBe SpinePalette.entries.toSet()
        }
    }

    @Nested
    inner class DefaultAgreement {
        @Test
        fun `DEFAULT agrees on both sides`() {
            // ----- Act -----
            val mappedDefault = ColorPalette.DEFAULT.toSpinePalette()

            // ----- Assert -----
            mappedDefault shouldBe SpinePalette.DEFAULT
        }
    }
}
