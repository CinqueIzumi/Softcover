package nl.rhaydus.softcover.core.component.field

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TextFieldUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `fixtures cover every variant`() {
            // ----- Arrange -----
            val fixtures = TextFieldUiModel.previews.values

            // ----- Act -----
            val variants = fixtures.map { it.variant }

            // ----- Assert -----
            variants.any { it is TextFieldVariant.Pill } shouldBe true
            variants.any { it is TextFieldVariant.Outlined } shouldBe true
            variants.any { it is TextFieldVariant.Hero } shouldBe true
            variants.any { it is TextFieldVariant.Numeral } shouldBe true
        }

        @Test
        fun `fixtures cover a pill with and without a trailing button`() {
            // ----- Arrange -----
            val fixtures = TextFieldUiModel.previews.values

            // ----- Act -----
            val pills = fixtures.map { it.variant }.filterIsInstance<TextFieldVariant.Pill>()

            // ----- Assert -----
            pills.any { it.trailing != null } shouldBe true
            pills.any { it.trailing == null } shouldBe true
        }

        @Test
        fun `fixtures cover every pill size`() {
            // ----- Arrange -----
            val fixtures = TextFieldUiModel.previews.values

            // ----- Act -----
            val sizes = fixtures
                .map { it.variant }
                .filterIsInstance<TextFieldVariant.Pill>()
                .map { it.size }
                .toSet()

            // ----- Assert -----
            sizes shouldBe TextFieldPillSize.entries.toSet()
        }

        @Test
        fun `fixtures include an outlined field without a label`() {
            // ----- Arrange -----
            val fixtures = TextFieldUiModel.previews.values

            // ----- Act -----
            val outlined = fixtures
                .map { it.variant }
                .filterIsInstance<TextFieldVariant.Outlined>()

            // ----- Assert -----
            outlined.any { it.label == null } shouldBe true
            outlined.any { it.label != null } shouldBe true
        }

        @Test
        fun `fixtures include a disabled field and an empty field`() {
            // ----- Arrange -----
            val previews = TextFieldUiModel.previews.values

            // ----- Act & Assert -----
            previews.any { it.enabled.not() } shouldBe true
            previews.any { it.text.isEmpty() } shouldBe true
        }

        @Test
        fun `fixture keys are unique so events can be routed`() {
            // ----- Arrange -----
            val fixtures = TextFieldUiModel.previews.values

            // ----- Act -----
            val keys = fixtures.map { it.key }

            // ----- Assert -----
            keys.distinct() shouldBe keys
        }
    }
}
