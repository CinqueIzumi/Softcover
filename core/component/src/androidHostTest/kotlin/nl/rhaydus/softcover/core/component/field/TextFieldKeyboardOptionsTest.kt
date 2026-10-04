package nl.rhaydus.softcover.core.component.field

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TextFieldKeyboardOptionsTest {
    private fun model(
        keyboard: TextFieldKeyboard = TextFieldKeyboard.Text,
    ) = TextFieldUiModel(
        key = "field",
        text = "",
        placeholder = "",
        variant = TextFieldVariant.Hero,
        keyboard = keyboard,
    )

    @Nested
    inner class ImeActionMapping {
        @Test
        fun `maps Default to the platform default action`() {
            // ----- Arrange -----
            val field = model().copy(imeAction = TextFieldImeAction.Default)

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Default
        }

        @Test
        fun `maps Done to the Done action`() {
            // ----- Arrange -----
            val field = model().copy(imeAction = TextFieldImeAction.Done)

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.imeAction shouldBe ImeAction.Done
        }
    }

    @Nested
    inner class KeyboardMapping {
        @Test
        fun `maps Text to the text keyboard`() {
            // ----- Arrange -----
            val field = model(keyboard = TextFieldKeyboard.Text)

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.keyboardType shouldBe KeyboardType.Text
        }

        @Test
        fun `maps Number to the number keyboard`() {
            // ----- Arrange -----
            val field = model(keyboard = TextFieldKeyboard.Number)

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.keyboardType shouldBe KeyboardType.Number
        }

        @Test
        fun `keeps the keyboard type independent of the ime action`() {
            // ----- Arrange -----
            val field = model(keyboard = TextFieldKeyboard.Number).copy(imeAction = TextFieldImeAction.Done)

            // ----- Act -----
            val options = field.keyboardOptions()

            // ----- Assert -----
            options.keyboardType shouldBe KeyboardType.Number
            options.imeAction shouldBe ImeAction.Done
        }
    }
}
