package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TextFieldKeyboardOptionsTest {
    private fun model(
        variant: TextFieldVariant,
        submittable: Boolean = false,
    ) = TextFieldUiModel(
        key = "field",
        text = "",
        placeholder = "",
        variant = variant,
        submittable = submittable,
    )

    @Nested
    inner class ImeActionMapping {
        @Test
        fun `a submittable pill, outlined or hero field offers Done`() {
            // ----- Arrange -----
            val fields = listOf(
                TextFieldVariant.Pill(),
                TextFieldVariant.Outlined(),
                TextFieldVariant.Hero,
            ).map {
                model(
                    it,
                    submittable = true,
                )
            }

            // ----- Act -----
            val actions = fields.map { it.keyboardOptions().imeAction }

            // ----- Assert -----
            actions shouldBe List(3) { ImeAction.Done }
        }

        @Test
        fun `a pill, outlined or hero field that is not submittable keeps the platform default action`() {
            // ----- Arrange -----
            val fields = listOf(
                TextFieldVariant.Pill(),
                TextFieldVariant.Outlined(),
                TextFieldVariant.Hero,
            ).map { model(it) }

            // ----- Act -----
            val actions = fields.map { it.keyboardOptions().imeAction }

            // ----- Assert -----
            actions shouldBe List(3) { ImeAction.Default }
        }

        @Test
        fun `a numeral offers Done even when it is not submittable`() {
            // ----- Arrange -----
            val field = model(
                TextFieldVariant.Numeral(charCount = 2),
                submittable = false,
            )

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Done
        }
    }

    @Nested
    inner class KeyboardMapping {
        @Test
        fun `a non-numeric outlined field uses the text keyboard`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Outlined(numeric = false))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.keyboardType shouldBe KeyboardType.Text
        }

        @Test
        fun `a numeric outlined field uses the number keyboard`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Outlined(numeric = true))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.keyboardType shouldBe KeyboardType.Number
        }

        @Test
        fun `a numeral always uses the number keyboard`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Numeral(charCount = 3))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.keyboardType shouldBe KeyboardType.Number
        }

        @Test
        fun `pill and hero use the text keyboard`() {
            // ----- Arrange -----
            val pill = model(TextFieldVariant.Pill())
            val hero = model(TextFieldVariant.Hero)

            // ----- Act -----
            val pillType = pill.keyboardOptions().keyboardType
            val heroType = hero.keyboardOptions().keyboardType

            // ----- Assert -----
            pillType shouldBe KeyboardType.Text
            heroType shouldBe KeyboardType.Text
        }

        @Test
        fun `keeps the keyboard type independent of the ime action`() {
            // ----- Arrange -----
            val field = model(
                TextFieldVariant.Outlined(numeric = true),
                submittable = true,
            )

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.keyboardType shouldBe KeyboardType.Number
            options.imeAction shouldBe ImeAction.Done
        }
    }

    @Nested
    inner class KeyboardActionsMapping {
        @Test
        fun `a field that is not submittable gets the default actions`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Pill())

            // ----- Act -----
            val actions = field.keyboardActions(onEvent = {})

            // ----- Assert -----
            actions shouldBe KeyboardActions.Default
        }

        @Test
        fun `a submittable field gets its own actions`() {
            // ----- Arrange -----
            val field = model(
                TextFieldVariant.Hero,
                submittable = true,
            )

            // ----- Act -----
            val actions = field.keyboardActions(onEvent = {})

            // ----- Assert -----
            (actions == KeyboardActions.Default) shouldBe false
            (actions.onDone != null) shouldBe true
        }

        @Test
        fun `a numeral gets its own actions even when it is not submittable`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Numeral(charCount = 2))

            // ----- Act -----
            val actions = field.keyboardActions(onEvent = {})

            // ----- Assert -----
            (actions == KeyboardActions.Default) shouldBe false
        }
    }
}
