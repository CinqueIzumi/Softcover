package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun NumeralTextField(
    model: TextFieldUiModel,
    variant: TextFieldVariant.Numeral,
    onEvent: (TextFieldEvent) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.editorialTypography.headlineMedium
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current

    // The model carries only the text, so the selection is the field's own: all of it on focus
    // (typing replaces the value), none once focus leaves.
    var selection by remember { mutableStateOf(TextRange.Zero) }

    val width = remember(density, textStyle, variant.charCount) {
        with(density) {
            ((variant.charCount * textStyle.fontSize.toPx() * 0.62f) + 16.dp.toPx()).toDp()
        }
    }

    BasicTextField(
        value = TextFieldValue(
            text = model.text,
            selection = TextRange(
                start = selection.start.coerceAtMost(model.text.length),
                end = selection.end.coerceAtMost(model.text.length),
            ),
        ),
        onValueChange = { newValue ->
            selection = newValue.selection

            val digits = newValue.text.filter { it.isDigit() }

            if (digits != model.text) {
                onEvent(
                    TextFieldEvent.ValueChanged(
                        key = model.key,
                        text = digits,
                    ),
                )
            }
        },
        enabled = model.enabled,
        singleLine = true,
        textStyle = textStyle.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        keyboardOptions = model.keyboardOptions(),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onEvent(TextFieldEvent.Submitted(key = model.key))
            },
        ),
        modifier = modifier
            .width(width)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                selection = if (focusState.hasFocus) {
                    TextRange(
                        start = 0,
                        end = model.text.length,
                    )
                } else {
                    TextRange.Zero
                }
            },
    )
}
