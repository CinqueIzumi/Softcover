package nl.rhaydus.softcover.core.component.divider

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DividerUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `covers both horizontal and vertical variants`() {
            // ----- Arrange & Act -----
            val variants = DividerUiModel.previews.map { it::class }.toSet()

            // ----- Assert -----
            variants shouldBe setOf(DividerUiModel.Horizontal::class, DividerUiModel.Vertical::class)
        }

        @Test
        fun `covers every DividerInk variant`() {
            // ----- Arrange & Act -----
            val inkVariants = DividerUiModel.previews
                .map { model ->
                    when (model) {
                        is DividerUiModel.Horizontal -> model.ink
                        is DividerUiModel.Vertical -> model.ink
                    }
                }
                .map { it::class }
                .toSet()

            // ----- Assert -----
            inkVariants shouldBe setOf(
                DividerInk.Outline::class,
                DividerInk.ContentTint::class,
                DividerInk.OnImage::class,
            )
        }

        @Test
        fun `covers both DividerCenter variants`() {
            // ----- Arrange & Act -----
            val centerVariants = DividerUiModel.previews
                .filterIsInstance<DividerUiModel.Horizontal>()
                .mapNotNull { it.center }
                .map { it::class }
                .toSet()

            // ----- Assert -----
            centerVariants shouldBe setOf(DividerCenter.QuoteMark::class, DividerCenter.Label::class)
        }

        @Test
        fun `includes a Vertical with a non-null length`() {
            // ----- Arrange & Act -----
            val hasLength = DividerUiModel.previews
                .filterIsInstance<DividerUiModel.Vertical>()
                .any { it.length != null }

            // ----- Assert -----
            hasLength shouldBe true
        }
    }
}
