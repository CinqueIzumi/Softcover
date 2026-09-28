package nl.rhaydus.softcover.core.component.badge

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoverOverlayUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every CoverOverlayVariant value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedVariants = CoverOverlayVariant.entries.toSet()

            // ----- Act -----
            val coveredVariants = CoverOverlayUiModel.previews.map { it.variant }.toSet()

            // ----- Assert -----
            coveredVariants shouldBe expectedVariants
        }

        @Test
        fun `both grayscale states appear across the fixtures`() {
            // ----- Arrange -----
            val expectedGrayscaleStates = setOf(true, false)

            // ----- Act -----
            val coveredGrayscaleStates = CoverOverlayUiModel.previews.map { it.grayscale }.toSet()

            // ----- Assert -----
            coveredGrayscaleStates shouldBe expectedGrayscaleStates
        }
    }
}
