package nl.rhaydus.softcover.showcase.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShowcaseChipsMapperTest {
    @Nested
    inner class ShowcaseFamilyChips {
        @Test
        fun `marks no chip selected for a null selection`() {
            // ----- Arrange -----
            val selected: GalleryFamily? = null

            // ----- Act -----
            val result = showcaseFamilyChips(selected = selected)

            // ----- Assert -----
            result.chips shouldHaveSize GalleryFamily.entries.size
            result.chips.count { it.selected } shouldBe 0
        }

        @Test
        fun `marks exactly the chip of the selected family`() {
            // ----- Arrange -----
            val family = GalleryFamily.entries[2]

            // ----- Act -----
            val result = showcaseFamilyChips(selected = family)

            // ----- Assert -----
            val selected = result.chips.filter { it.selected }
            selected shouldHaveSize 1
            result[selected.first().key] shouldBe family
        }
    }

    @Nested
    inner class ShowcasePaletteChips {
        @Test
        fun `marks exactly the chip of the selected palette`() {
            // ----- Arrange -----
            val palette = SpinePalette.entries.last()

            // ----- Act -----
            val result = showcasePaletteChips(selected = palette)

            // ----- Assert -----
            val selected = result.chips.filter { it.selected }
            selected shouldHaveSize 1
            result[selected.first().key] shouldBe palette
        }
    }

    @Nested
    inner class ShowcaseBrightnessChips {
        @Test
        fun `marks exactly the chip of the selected brightness`() {
            // ----- Arrange -----
            val brightness = SchemeBrightness.DARK

            // ----- Act -----
            val result = showcaseBrightnessChips(selected = brightness)

            // ----- Assert -----
            val selected = result.chips.filter { it.selected }
            selected shouldHaveSize 1
            result[selected.first().key] shouldBe brightness
        }
    }

    @Nested
    inner class ShowcaseSectionHeaders {
        @Test
        fun `covers every gallery family`() {
            // ----- Arrange -----
            val expected = GalleryFamily.entries.toSet()

            // ----- Act -----
            val result = showcaseSectionHeaders()

            // ----- Assert -----
            result.keys shouldBe expected
        }
    }
}
