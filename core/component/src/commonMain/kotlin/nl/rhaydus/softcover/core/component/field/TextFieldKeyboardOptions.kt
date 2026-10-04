package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

private val TextFieldUiModel.submits: Boolean
    get() = when (variant) {
        is TextFieldVariant.Pill -> variant.submittable
        is TextFieldVariant.Outlined -> variant.submittable
        is TextFieldVariant.Hero -> variant.submittable
        is TextFieldVariant.Numeral -> true
    }

private val TextFieldUiModel.keyboardType: KeyboardType
    get() = when (variant) {
        is TextFieldVariant.Numeral -> KeyboardType.Number
        is TextFieldVariant.Outlined -> when (variant.keyboard) {
            TextFieldKeyboard.Text -> KeyboardType.Text
            TextFieldKeyboard.Number -> KeyboardType.Number
        }

        is TextFieldVariant.Pill,
        is TextFieldVariant.Hero -> KeyboardType.Text
    }

internal fun TextFieldUiModel.keyboardOptions(): KeyboardOptions = KeyboardOptions(
    keyboardType = keyboardType,
    imeAction = if (submits) ImeAction.Done else ImeAction.Default,
)

/** [onDone] runs before [TextFieldEvent.Submitted] is sent. */
internal fun TextFieldUiModel.keyboardActions(
    onEvent: (TextFieldEvent) -> Unit,
    onDone: () -> Unit = {},
): KeyboardActions = if (submits) {
    KeyboardActions(
        onDone = {
            onDone()
            onEvent(TextFieldEvent.Submitted(key = key))
        },
    )
} else {
    KeyboardActions.Default
}
