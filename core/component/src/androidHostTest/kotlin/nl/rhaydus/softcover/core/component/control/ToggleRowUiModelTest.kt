package nl.rhaydus.softcover.core.component.control

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ToggleRowUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every fixture has a unique key`() {
            // ----- Arrange -----
            val fixtures = ToggleRowUiModel.previews.values

            // ----- Act -----
            val keys = fixtures.map { it.key }

            // ----- Assert -----
            keys.distinct() shouldBe keys
        }

        @Test
        fun `every ToggleRowVariant appears in at least one fixture`() {
            // ----- Arrange -----
            val fixtures = ToggleRowUiModel.previews.values

            // ----- Act -----
            val variants = fixtures.map { it.variant }

            // ----- Assert -----
            variants.toSet() shouldBe ToggleRowVariant.entries.toSet()
        }

        @Test
        fun `fixtures cover checked, unchecked, with and without a gloss`() {
            // ----- Arrange -----
            val rows = ToggleRowUiModel.previews.values

            // ----- Act & Assert -----
            rows.any { it.checked } shouldBe true
            rows.any { it.checked.not() } shouldBe true
            rows.any { it.gloss != null } shouldBe true
            rows.any { it.gloss == null } shouldBe true
        }
    }
}
