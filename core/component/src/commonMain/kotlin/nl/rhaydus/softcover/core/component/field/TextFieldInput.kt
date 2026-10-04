package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.component.text.SoftcoverText

/**
 * The input every variant but [TextFieldVariant.Numeral] builds on: it owns the event wiring, the cursor
 * and the ink, and each variant supplies only its [textStyle] and [decorationBox].
 */
@Composable
internal fun TextFieldInput(
    model: TextFieldUiModel,
    textStyle: TextStyle,
    onEvent: (TextFieldEvent) -> Unit,
    focusRequester: FocusRequester,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = model.text,
        onValueChange = model.onTextChange(onEvent),
        enabled = model.enabled,
        singleLine = true,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface.forEnabled(model.enabled)),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = model.keyboardOptions(),
        keyboardActions = model.keyboardActions(onEvent),
        decorationBox = decorationBox,
        modifier = modifier
            .focusRequester(focusRequester)
            .semantics { contentDescription = model.placeholder },
    )
}

internal fun TextFieldUiModel.onTextChange(
    onEvent: (TextFieldEvent) -> Unit,
): (String) -> Unit = { text ->
    onEvent(
        TextFieldEvent.ValueChanged(
            key = key,
            text = text,
        ),
    )
}

/** Dimmed to the disabled alpha used across the component library. */
internal fun Color.forEnabled(enabled: Boolean): Color = if (enabled) this else copy(alpha = alpha * DISABLED_ALPHA)

/** Hidden from accessibility: the field announces its placeholder as its own label. */
@Composable
internal fun TextFieldPlaceholder(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    SoftcoverText(
        text = text,
        modifier = modifier.clearAndSetSemantics {},
        style = style,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f).forEnabled(enabled),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
    )
}
