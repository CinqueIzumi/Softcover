package nl.rhaydus.softcover.core.component.chip

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChipUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every fixture has a unique key`() {
            // ----- Arrange -----
            val keys = ChipUiModel.previews.map { it.key }

            // ----- Act -----
            val uniqueKeys = keys.toSet()

            // ----- Assert -----
            uniqueKeys.size shouldBe keys.size
        }

        @Test
        fun `every ChipVariant subtype appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedVariantTypes = setOf(
                ChipVariant.Tonal::class,
                ChipVariant.Spoiler::class,
                ChipVariant.Add::class,
                ChipVariant.AddOutlined::class,
                ChipVariant.Remove::class,
                ChipVariant.Format::class,
            )

            // ----- Act -----
            val coveredVariantTypes = ChipUiModel.previews.map { it.variant::class }.toSet()

            // ----- Assert -----
            coveredVariantTypes shouldBe expectedVariantTypes
        }

        @Test
        fun `every ChipInteraction value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedInteractions = ChipInteraction.entries.toSet()

            // ----- Act -----
            val coveredInteractions = ChipUiModel.previews.map { it.interaction }.toSet()

            // ----- Assert -----
            coveredInteractions shouldBe expectedInteractions
        }
    }
}
