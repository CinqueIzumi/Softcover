package nl.rhaydus.softcover.core.component.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SkeletonUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `there is exactly one fixture per SkeletonVariant entry`() {
            // ----- Act & Assert -----
            SkeletonUiModel.previews.size shouldBe SkeletonVariant.entries.size
        }

        @Test
        fun `the fixtures cover every variant with no duplicates`() {
            // ----- Act -----
            val variants = SkeletonUiModel.previews.map { it.variant }

            // ----- Assert -----
            variants.toSet() shouldBe SkeletonVariant.entries.toSet()
            variants.distinct().size shouldBe variants.size
        }

        @Test
        fun `the fixtures follow SkeletonVariant entry order`() {
            // ----- Act -----
            val variants = SkeletonUiModel.previews.map { it.variant }

            // ----- Assert -----
            variants shouldBe SkeletonVariant.entries
        }
    }
}
