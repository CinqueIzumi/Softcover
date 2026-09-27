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
        fun `every ChipTone value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedTones = ChipTone.entries.toSet()

            // ----- Act -----
            val coveredTones = ChipUiModel.previews.map { it.tone }.toSet()

            // ----- Assert -----
            coveredTones shouldBe expectedTones
        }

        @Test
        fun `both selected states appear across the fixtures`() {
            // ----- Arrange -----
            val expectedSelectedStates = setOf(true, false)

            // ----- Act -----
            val coveredSelectedStates = ChipUiModel.previews.map { it.selected }.toSet()

            // ----- Assert -----
            coveredSelectedStates shouldBe expectedSelectedStates
        }

        @Test
        fun `every ChipLeading subtype appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedLeadingTypes = setOf(
                ChipLeading.Icon::class,
                ChipLeading.SpoilerToggle::class,
            )

            // ----- Act -----
            val coveredLeadingTypes = ChipUiModel.previews
                .mapNotNull { it.leading }
                .map { it::class }
                .toSet()

            // ----- Assert -----
            coveredLeadingTypes shouldBe expectedLeadingTypes
        }

        @Test
        fun `every ChipTrailing subtype appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedTrailingTypes = setOf(
                ChipTrailing.Icon::class,
                ChipTrailing.Dismiss::class,
            )

            // ----- Act -----
            val coveredTrailingTypes = ChipUiModel.previews
                .mapNotNull { it.trailing }
                .map { it::class }
                .toSet()

            // ----- Assert -----
            coveredTrailingTypes shouldBe expectedTrailingTypes
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

        @Test
        fun `every ChipSize value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedSizes = ChipSize.entries.toSet()

            // ----- Act -----
            val coveredSizes = ChipUiModel.previews.map { it.size }.toSet()

            // ----- Assert -----
            coveredSizes shouldBe expectedSizes
        }
    }

    @Nested
    inner class Defaults {
        @Test
        fun `tone defaults to Tonal`() {
            // ----- Arrange & Act -----
            val model = ChipUiModel(
                key = "key",
                label = "label",
            )

            // ----- Assert -----
            model.tone shouldBe ChipTone.Tonal
        }

        @Test
        fun `selected defaults to false`() {
            // ----- Arrange & Act -----
            val model = ChipUiModel(
                key = "key",
                label = "label",
            )

            // ----- Assert -----
            model.selected shouldBe false
        }

        @Test
        fun `size defaults to Regular`() {
            // ----- Arrange & Act -----
            val model = ChipUiModel(
                key = "key",
                label = "label",
            )

            // ----- Assert -----
            model.size shouldBe ChipSize.Regular
        }

        @Test
        fun `leading defaults to null`() {
            // ----- Arrange & Act -----
            val model = ChipUiModel(
                key = "key",
                label = "label",
            )

            // ----- Assert -----
            model.leading shouldBe null
        }

        @Test
        fun `trailing defaults to null`() {
            // ----- Arrange & Act -----
            val model = ChipUiModel(
                key = "key",
                label = "label",
            )

            // ----- Assert -----
            model.trailing shouldBe null
        }

        @Test
        fun `interaction defaults to Clickable`() {
            // ----- Arrange & Act -----
            val model = ChipUiModel(
                key = "key",
                label = "label",
            )

            // ----- Assert -----
            model.interaction shouldBe ChipInteraction.Clickable
        }

        @Test
        fun `face defaults to Plain`() {
            // ----- Arrange & Act -----
            val model = ChipUiModel(
                key = "key",
                label = "label",
            )

            // ----- Assert -----
            model.face shouldBe ChipFace.Plain
        }
    }
}
