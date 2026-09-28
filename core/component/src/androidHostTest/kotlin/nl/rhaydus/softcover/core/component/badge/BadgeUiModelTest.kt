package nl.rhaydus.softcover.core.component.badge

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BadgeUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every BadgeTone value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedTones = BadgeTone.entries.toSet()

            // ----- Act -----
            val coveredTones = BadgeUiModel.previews.map { it.tone }.toSet()

            // ----- Assert -----
            coveredTones shouldBe expectedTones
        }

        @Test
        fun `every BadgeVariant value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedVariants = BadgeVariant.entries.toSet()

            // ----- Act -----
            val coveredVariants = BadgeUiModel.previews.map { it.variant }.toSet()

            // ----- Assert -----
            coveredVariants shouldBe expectedVariants
        }

        @Test
        fun `at least one fixture carries a leading icon`() {
            // ----- Arrange & Act -----
            val withIcon = BadgeUiModel.previews.filter { it.icon != null }

            // ----- Assert -----
            withIcon.size shouldBe 2
        }
    }

    @Nested
    inner class Defaults {
        @Test
        fun `variant defaults to Standard`() {
            // ----- Arrange & Act -----
            val model = BadgeUiModel(
                label = "label",
                tone = BadgeTone.Primary,
            )

            // ----- Assert -----
            model.variant shouldBe BadgeVariant.Standard
        }

        @Test
        fun `icon defaults to null`() {
            // ----- Arrange & Act -----
            val model = BadgeUiModel(
                label = "label",
                tone = BadgeTone.Primary,
            )

            // ----- Assert -----
            model.icon shouldBe null
        }
    }
}
