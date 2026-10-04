package nl.rhaydus.softcover.core.component.control

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ToggleUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every fixture has a unique key`() {
            // ----- Act -----
            val keys = ToggleUiModel.previews.map { it.key }

            // ----- Assert -----
            keys.distinct() shouldBe keys
        }

        @Test
        fun `fixtures cover both variants`() {
            // ----- Act -----
            val variants = ToggleUiModel.previews.map { it.variant }

            // ----- Assert -----
            variants.toSet() shouldBe ToggleVariant.entries.toSet()
        }

        @Test
        fun `fixtures cover checked, unchecked, enabled and disabled`() {
            // ----- Act -----
            val toggles = ToggleUiModel.previews

            // ----- Assert -----
            toggles.any { it.checked } shouldBe true
            toggles.any { it.checked.not() } shouldBe true
            toggles.any { it.enabled } shouldBe true
            toggles.any { it.enabled.not() } shouldBe true
        }
    }
}
