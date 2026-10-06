package nl.rhaydus.softcover.core.component.control

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ToggleUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `fixtures cover both variants`() {
            // ----- Arrange -----
            val fixtures = ToggleUiModel.previews.values

            // ----- Act -----
            val variants = fixtures.map { it.variant }

            // ----- Assert -----
            variants.toSet() shouldBe ToggleVariant.entries.toSet()
        }

        @Test
        fun `fixtures cover checked, unchecked, enabled and disabled`() {
            // ----- Arrange -----
            val toggles = ToggleUiModel.previews.values

            // ----- Act & Assert -----
            toggles.any { it.checked } shouldBe true
            toggles.any { it.checked.not() } shouldBe true
            toggles.any { it.enabled } shouldBe true
            toggles.any { it.enabled.not() } shouldBe true
        }
    }
}
