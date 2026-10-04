package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GalleryChoiceChipsMapperTest {
    @Nested
    inner class ThemeModeChips {
        @Test
        fun `returns one chip per ThemeMode entry in entries order`() {
            // ----- Act -----
            val result = galleryThemeModeChips(selected = null)

            // ----- Assert -----
            result.chips.map { result[it.key] } shouldBe ThemeMode.entries
        }

        @Test
        fun `marks no chip selected while the gallery follows the app setting`() {
            // ----- Act -----
            val result = galleryThemeModeChips(selected = null)

            // ----- Assert -----
            result.chips.count { it.selected } shouldBe 0
        }

        @Test
        fun `marks only the chip of the override in effect`() {
            ThemeMode.entries.forEach { override ->
                // ----- Act -----
                val result = galleryThemeModeChips(selected = override)

                // ----- Assert -----
                val selected = result.chips.filter { it.selected }
                selected shouldHaveSize 1
                result[selected.first().key] shouldBe override
            }
        }

        @Test
        fun `gives every chip a distinct key`() {
            // ----- Act -----
            val result = galleryThemeModeChips(selected = null)

            // ----- Assert -----
            result.chips.map { it.key }.toSet() shouldHaveSize ThemeMode.entries.size
        }

        @Test
        fun `resolves an unknown key to null`() {
            // ----- Act -----
            val result = galleryThemeModeChips(selected = null)

            // ----- Assert -----
            result["missing"].shouldBeNull()
        }
    }

    @Nested
    inner class FamilyChips {
        @Test
        fun `returns one chip per GalleryFamily entry in entries order`() {
            // ----- Act -----
            val result = galleryFamilyChips(selected = null)

            // ----- Assert -----
            result.chips.map { result[it.key] } shouldBe GalleryFamily.entries
        }

        @Test
        fun `marks only the chip of the selected family`() {
            GalleryFamily.entries.forEach { family ->
                // ----- Act -----
                val result = galleryFamilyChips(selected = family)

                // ----- Assert -----
                result.chips.filter { it.selected }.map { result[it.key] } shouldBe listOf(family)
            }
        }
    }

    @Nested
    inner class PaletteChips {
        @Test
        fun `returns one chip per ColorPalette entry in entries order`() {
            // ----- Act -----
            val result = galleryPaletteChips(selected = null)

            // ----- Assert -----
            result.chips.map { result[it.key] } shouldBe ColorPalette.entries
        }

        @Test
        fun `marks only the chip of the selected palette`() {
            ColorPalette.entries.forEach { palette ->
                // ----- Act -----
                val result = galleryPaletteChips(selected = palette)

                // ----- Assert -----
                result.chips.filter { it.selected }.map { result[it.key] } shouldBe listOf(palette)
            }
        }
    }
}
