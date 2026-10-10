package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
) {
    val textStyle = MaterialTheme.editorialTypography.headlineMedium
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val onTextChange = model.onTextChange(onEvent)

    // The text round-trips through the collector, so the field keeps its own value and re-seeds
    // only when the model's text moves away from it. The selection is the field's own: all of it on
    // focus (typing replaces the value), none once focus leaves.
    var fieldValue by remember { mutableStateOf(TextFieldValue(text = model.text)) }

    if (fieldValue.text != model.text) {
        fieldValue = TextFieldValue(
            text = model.text,
            selection = TextRange(model.text.length),
        )
    }

    // 0.62 is an estimated average digit advance in em, not a measurement of the text.
    val width = remember(density, textStyle, variant.charCount) {
        with(density) {
            ((variant.charCount * textStyle.fontSize.toPx() * 0.62f) + 16.dp.toPx()).toDp()
        }
    }

    BasicTextField(
        value = fieldValue,
        onValueChange = { newValue ->
            val digits = newValue.text.filter { it.isDigit() }

            fieldValue = newValue.copy(text = digits)

            onTextChange(digits)
        },
        enabled = model.enabled,
        singleLine = true,
        textStyle = textStyle.copy(
            color = if (model.error != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface.forEnabled(model.enabled)
            },
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        keyboardOptions = model.keyboardOptions(),
        keyboardActions = model.keyboardActions(
            onEvent = onEvent,
            onDone = { focusManager.clearFocus() },
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (model.text.isEmpty()) {
                    TextFieldPlaceholder(
                        text = model.placeholder,
                        enabled = model.enabled,
                        style = textStyle,
                    )
                }

                innerTextField()
            }
        },
        modifier = Modifier
            .width(width)
            .focusRequester(focusRequester)
            .semantics { contentDescription = model.placeholder }
            .onFocusChanged { focusState ->
                fieldValue = fieldValue.copy(
                    selection = if (focusState.hasFocus) {
                        TextRange(
                            start = 0,
                            end = fieldValue.text.length,
                        )
                    } else {
                        TextRange.Zero
                    },
                )
            },
    )
}
