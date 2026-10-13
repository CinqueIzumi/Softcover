package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.UiScale
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UiScaleRowsMapperTest {
    @Nested
    inner class Selection {
        @Test
        fun `marks exactly one row selected, the one whose key matches selected`() {
            // ----- Arrange -----
            val scales = UiScale.entries

            scales.forEach { selectedScale ->
                // ----- Act -----
                val result = uiScaleRowsFor(selected = selectedScale)

                // ----- Assert -----
                result.count { it.selected } shouldBe 1
                result.first { it.selected }.key shouldBe selectedScale
            }
        }
    }
}
