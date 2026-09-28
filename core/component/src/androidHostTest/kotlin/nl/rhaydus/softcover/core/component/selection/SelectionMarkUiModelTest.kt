package nl.rhaydus.softcover.core.component.selection

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SelectionMarkUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every SelectionMarkBackdrop value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedBackdrops = SelectionMarkBackdrop.entries.toSet()

            // ----- Act -----
            val coveredBackdrops = SelectionMarkUiModel.previews.map { it.backdrop }.toSet()

            // ----- Assert -----
            coveredBackdrops shouldBe expectedBackdrops
        }

        @Test
        fun `both selected states appear for every backdrop`() {
            // ----- Arrange -----
            val expectedSelectedStates = setOf(true, false)

            // ----- Act -----
            val coveredSelectedStatesByBackdrop = SelectionMarkUiModel.previews
                .groupBy { it.backdrop }
                .mapValues { (_, fixtures) -> fixtures.map { it.selected }.toSet() }

            // ----- Assert -----
            coveredSelectedStatesByBackdrop.values.forEach { states ->
                states shouldBe expectedSelectedStates
            }
        }
    }
}
