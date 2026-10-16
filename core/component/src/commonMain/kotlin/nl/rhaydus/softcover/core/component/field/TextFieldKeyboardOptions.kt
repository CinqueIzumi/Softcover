package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

private val TextFieldUiModel.submits: Boolean
    get() = submittable || variant is TextFieldVariant.Numeral

private val TextFieldUiModel.keyboardType: KeyboardType
    get() = when (variant) {
        is TextFieldVariant.Numeral -> KeyboardType.Number
        is TextFieldVariant.Outlined -> if (variant.numeric) KeyboardType.Number else KeyboardType.Text
        is TextFieldVariant.Pill,
        TextFieldVariant.Multiline,
        TextFieldVariant.Hero -> KeyboardType.Text
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
