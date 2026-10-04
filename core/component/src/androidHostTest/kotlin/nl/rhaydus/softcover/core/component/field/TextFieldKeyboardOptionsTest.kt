package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TextFieldKeyboardOptionsTest {
    private fun model(variant: TextFieldVariant) = TextFieldUiModel(
        key = "field",
        text = "",
        placeholder = "",
        variant = variant,
    )

    @Nested
    inner class ImeActionMapping {
        @Test
        fun `a submittable pill offers Done`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Pill(submittable = true))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Done
        }

        @Test
        fun `a pill that is not submittable keeps the platform default action`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Pill(submittable = false))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Default
        }

        @Test
        fun `a submittable outlined field offers Done`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Outlined(submittable = true))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Done
        }

        @Test
        fun `an outlined field that is not submittable keeps the platform default action`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Outlined(submittable = false))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Default
        }

        @Test
        fun `a submittable hero offers Done`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Hero(submittable = true))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Done
        }

        @Test
        fun `a hero that is not submittable keeps the platform default action`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Hero(submittable = false))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Default
        }

        @Test
        fun `a numeral always offers Done`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Numeral(charCount = 2))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Done
        }
    }

    @Nested
    inner class KeyboardMapping {
        @Test
        fun `maps an outlined Text keyboard to the text keyboard`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Outlined(keyboard = TextFieldKeyboard.Text))

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.keyboardType shouldBe KeyboardType.Text
        }

        @Test
        fun `maps an outlined Number keyboard to the number keyboard`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Outlined(keyboard = TextFieldKeyboard.Number))

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
            val hero = model(TextFieldVariant.Hero())

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
                TextFieldVariant.Outlined(
                    keyboard = TextFieldKeyboard.Number,
                    submittable = true,
                ),
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
            val field = model(TextFieldVariant.Pill(submittable = false))

            // ----- Act -----
            val actions = field.keyboardActions(onEvent = {})

            // ----- Assert -----
            actions shouldBe KeyboardActions.Default
        }

        @Test
        fun `a submittable field gets its own actions`() {
            // ----- Arrange -----
            val field = model(TextFieldVariant.Hero(submittable = true))

            // ----- Act -----
            val actions = field.keyboardActions(onEvent = {})

            // ----- Assert -----
            (actions == KeyboardActions.Default) shouldBe false
            (actions.onDone != null) shouldBe true
        }
    }
}
