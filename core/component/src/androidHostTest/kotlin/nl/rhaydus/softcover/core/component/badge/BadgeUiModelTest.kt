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
        fun `every BadgeSize value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedSizes = BadgeSize.entries.toSet()

            // ----- Act -----
            val coveredSizes = BadgeUiModel.previews.map { it.size }.toSet()

            // ----- Assert -----
            coveredSizes shouldBe expectedSizes
        }

        @Test
        fun `at least one fixture carries a leading icon`() {
            // ----- Arrange & Act -----
            val withIcon = BadgeUiModel.previews.filter { it.icon != null }

            // ----- Assert -----
            withIcon.size shouldBe 2
        }
    }
}
