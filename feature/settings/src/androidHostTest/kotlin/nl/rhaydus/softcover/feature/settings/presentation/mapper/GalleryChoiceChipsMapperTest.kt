package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GalleryChoiceChipsMapperTest {
    @Nested
    inner class ThemeModeChips {
        @Test
        fun `marks no chip selected while the gallery follows the app setting`() {
            // ----- Arrange -----
            val selected: ThemeMode? = null

            // ----- Act -----
            val result = galleryThemeModeChips(selected = selected)

            // ----- Assert -----
            result.chips.count { it.selected } shouldBe 0
        }

        @Test
        fun `marks only the chip of the override in effect`() {
            // ----- Arrange -----
            val overrides = ThemeMode.entries

            overrides.forEach { override ->
                // ----- Act -----
                val result = galleryThemeModeChips(selected = override)

                // ----- Assert -----
                val selected = result.chips.filter { it.selected }
                selected shouldHaveSize 1
                result[selected.first().key] shouldBe override
            }
        }
    }
}
