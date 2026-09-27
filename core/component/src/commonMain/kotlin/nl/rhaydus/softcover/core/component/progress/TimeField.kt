package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Approximate width for a hero stat field of [charCount] glyphs in [textStyle]. */
@Composable
private fun computeHeroStatFieldWidth(
    textStyle: TextStyle,
    charCount: Int,
): Dp {
    val density = LocalDensity.current

    return remember(density, textStyle, charCount) {
        with(density) {
            val fontSizeInPx = textStyle.fontSize.toPx()
            val padding = 16.dp.toPx()

            ((charCount * fontSizeInPx * 0.62f) + padding).toDp()
        }
    }
}

@Composable
internal fun TimeField(
    value: TextFieldValue,
    charCount: Int,
    textStyle: TextStyle,
    onValueChange: (TextFieldValue) -> Unit,
) {
    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val width = computeHeroStatFieldWidth(
        textStyle = textStyle,
        charCount = charCount,
    )

    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (firstTimeFocusedGained) {
                firstTimeFocusedGained = false
                if (newValue.text == value.text) return@BasicTextField
            }

            onValueChange(newValue)
        },
        singleLine = true,
        textStyle = textStyle.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier
            .width(width = width)
            .onFocusChanged { focusState ->
                if (focusState.hasFocus.not()) {
                    firstTimeFocusedGained = true
                    onValueChange(value.copy(selection = TextRange.Zero))
                    return@onFocusChanged
                }

                onValueChange(
                    value.copy(
                        selection = TextRange(
                            start = 0,
                            end = value.text.length,
                        ),
                    ),
                )
            },
    )
}
