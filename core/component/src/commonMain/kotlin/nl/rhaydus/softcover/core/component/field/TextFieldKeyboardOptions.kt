package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

internal fun TextFieldUiModel.keyboardOptions(): KeyboardOptions = KeyboardOptions(
    keyboardType = when (keyboard) {
        TextFieldKeyboard.Text -> KeyboardType.Text
        TextFieldKeyboard.Number -> KeyboardType.Number
    },
    imeAction = when (imeAction) {
        TextFieldImeAction.Default -> ImeAction.Default
        TextFieldImeAction.Done -> ImeAction.Done
    },
)
