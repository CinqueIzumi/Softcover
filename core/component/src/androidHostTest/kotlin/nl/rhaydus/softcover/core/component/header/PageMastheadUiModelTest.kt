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
            val coveredSizes = PageMastheadUiModel.previews.map { it.size }.toSet()

            // ----- Assert -----
            coveredSizes shouldBe expectedSizes
        }

        @Test
        fun `at least one fixture carries an eyebrow`() {
            // ----- Act -----
            val hasEyebrow = PageMastheadUiModel.previews.any { it.eyebrow != null }

            // ----- Assert -----
            hasEyebrow shouldBe true
        }

        @Test
        fun `at least one fixture omits the eyebrow`() {
            // ----- Act -----
            val hasNoEyebrow = PageMastheadUiModel.previews.any { it.eyebrow == null }

            // ----- Assert -----
            hasNoEyebrow shouldBe true
        }
    }

    @Nested
    inner class Defaults {
        @Test
        fun `size defaults to Regular`() {
            // ----- Arrange & Act -----
            val model = PageMastheadUiModel(title = "Title")

            // ----- Assert -----
            model.size shouldBe PageMastheadSize.Regular
        }

        @Test
        fun `eyebrow defaults to null`() {
            // ----- Arrange & Act -----
            val model = PageMastheadUiModel(title = "Title")

            // ----- Assert -----
            model.eyebrow shouldBe null
        }

        @Test
        fun `subtitle defaults to null`() {
            // ----- Arrange & Act -----
            val model = PageMastheadUiModel(title = "Title")

            // ----- Assert -----
            model.subtitle shouldBe null
        }
    }
}
