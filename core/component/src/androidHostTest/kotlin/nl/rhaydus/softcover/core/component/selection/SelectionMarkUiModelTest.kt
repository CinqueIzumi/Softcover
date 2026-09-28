package nl.rhaydus.softcover.core.component.selection

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SelectionMarkUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every SelectionMarkVariant value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedVariants = SelectionMarkVariant.entries.toSet()

            // ----- Act -----
            val coveredVariants = SelectionMarkUiModel.previews.map { it.variant }.toSet()

            // ----- Assert -----
            coveredVariants shouldBe expectedVariants
        }

        @Test
        fun `both selected states appear for every variant`() {
            // ----- Arrange -----
            val expectedSelectedStates = setOf(true, false)

            // ----- Act -----
            val coveredSelectedStatesByVariant = SelectionMarkUiModel.previews
                .groupBy { it.variant }
                .mapValues { (_, fixtures) -> fixtures.map { it.selected }.toSet() }

            // ----- Assert -----
            coveredSelectedStatesByVariant.values.forEach { states ->
                states shouldBe expectedSelectedStates
            }
        }
    }
}
