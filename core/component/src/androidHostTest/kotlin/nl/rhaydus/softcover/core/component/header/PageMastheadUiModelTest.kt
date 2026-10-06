package nl.rhaydus.softcover.core.component.header

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PageMastheadUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every PageMastheadSize value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedSizes = PageMastheadSize.entries.toSet()

            // ----- Act -----
            val coveredSizes = PageMastheadUiModel.previews.values.map { it.size }.toSet()

            // ----- Assert -----
            coveredSizes shouldBe expectedSizes
        }

        @Test
        fun `at least one fixture carries an eyebrow`() {
            // ----- Arrange -----
            val fixtures = PageMastheadUiModel.previews.values

            // ----- Act -----
            val hasEyebrow = fixtures.any { it.eyebrow != null }

            // ----- Assert -----
            hasEyebrow shouldBe true
        }

        @Test
        fun `at least one fixture omits the eyebrow`() {
            // ----- Arrange -----
            val fixtures = PageMastheadUiModel.previews.values

            // ----- Act -----
            val hasNoEyebrow = fixtures.any { it.eyebrow == null }

            // ----- Assert -----
            hasNoEyebrow shouldBe true
        }
    }
}
