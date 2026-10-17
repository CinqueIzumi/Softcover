package nl.rhaydus.softcover.core.component.badge

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoverOverlayUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every CoverOverlayCorner value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedCorners = CoverOverlayCorner.entries.toSet()

            // ----- Act -----
            val coveredCorners = CoverOverlayUiModel.previews.values.map { it.corner }.toSet()

            // ----- Assert -----
            coveredCorners shouldBe expectedCorners
        }

        @Test
        fun `both grayscale states appear across the fixtures`() {
            // ----- Arrange -----
            val expectedGrayscaleStates = setOf(true, false)

            // ----- Act -----
            val coveredGrayscaleStates = CoverOverlayUiModel.previews.values.map { it.grayscale }.toSet()

            // ----- Assert -----
            coveredGrayscaleStates shouldBe expectedGrayscaleStates
        }
    }
}
