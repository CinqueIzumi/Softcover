package nl.rhaydus.softcover.core.component.button

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PillButtonUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every fixture has a unique key`() {
            // ----- Arrange -----
            val keys = PillButtonUiModel.previews.map { it.key }

            // ----- Act -----
            val uniqueKeys = keys.toSet()

            // ----- Assert -----
            uniqueKeys.size shouldBe keys.size
        }

        @Test
        fun `every PillButtonTone value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedTones = PillButtonTone.entries.toSet()

            // ----- Act -----
            val coveredTones = PillButtonUiModel.previews.map { it.tone }.toSet()

            // ----- Assert -----
            coveredTones shouldBe expectedTones
        }

        @Test
        fun `every PillButtonSize value appears in at least one fixture`() {
            // ----- Arrange -----
            val expectedSizes = PillButtonSize.entries.toSet()

            // ----- Act -----
            val coveredSizes = PillButtonUiModel.previews.map { it.size }.toSet()

            // ----- Assert -----
            coveredSizes shouldBe expectedSizes
        }

        @Test
        fun `both content kinds appear in at least one fixture`() {
            // ----- Arrange -----
            val previews = PillButtonUiModel.previews

            // ----- Act -----
            val hasLabel = previews.any { it.content is PillButtonContent.Label }
            val hasIconOnly = previews.any { it.content is PillButtonContent.IconOnly }

            // ----- Assert -----
            hasLabel shouldBe true
            hasIconOnly shouldBe true
        }

        @Test
        fun `an indicator fixture exists`() {
            // ----- Arrange -----
            val previews = PillButtonUiModel.previews

            // ----- Act -----
            val hasIndicator = previews.any { (it.content as? PillButtonContent.Label)?.indicator == true }

            // ----- Assert -----
            hasIndicator shouldBe true
        }
    }
}
