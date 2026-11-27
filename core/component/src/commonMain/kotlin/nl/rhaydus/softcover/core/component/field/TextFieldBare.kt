package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
internal fun BareTextField(
    model: TextFieldUiModel,
    style: TextStyle,
    onEvent: (TextFieldEvent) -> Unit,
    focusRequester: FocusRequester,
    ink: Color,
) {
    TextFieldInput(
        model = model,
        textStyle = style,
        onEvent = onEvent,
        focusRequester = focusRequester,
        ink = ink,
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (model.text.isEmpty()) {
                    TextFieldPlaceholder(
                        text = model.placeholder,
                        enabled = model.enabled,
                        style = style,
                    )
                }

                innerTextField()
            }
        },
    )
}
