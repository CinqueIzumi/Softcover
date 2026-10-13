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
}
