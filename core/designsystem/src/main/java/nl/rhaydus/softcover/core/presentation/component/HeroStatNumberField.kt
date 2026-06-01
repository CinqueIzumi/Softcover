package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography

/**
 * The canonical "editable hero stat" input (DS §5): a borderless [BasicTextField] rendered in the
 * editorial `statLarge` face, cursor tinted `primary`, width sized to [charCount] so the field reads
 * as a hero number rather than a boxed form control. Reach for it wherever the user edits a single
 * number framed as a hero stat (the reading-progress sheet, Focus Mode's page entry) — never hand-roll
 * a bordered `OutlinedTextField`/`TextField` for that role.
 */
@Composable
fun HeroStatNumberField(
    value: TextFieldValue,
    charCount: Int,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusReset: () -> Unit,
    onFocusGained: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.editorialTypography.statLarge

    val width = computeHeroStatFieldWidth(textStyle = style, charCount = charCount)

    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = style.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = modifier
            .width(width = width)
            .onFocusChanged { focusState ->
                if (focusState.hasFocus.not()) {
                    onFocusReset()

                    return@onFocusChanged
                }

                onFocusGained()
            },
    )
}

/** Editorial caption rendered beneath a [HeroStatNumberField] (e.g. "of 195 pages"). */
@Composable
fun EditorialSuffix(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Approximate width for a hero stat field of [charCount] glyphs in [textStyle]. */
@Composable
fun computeHeroStatFieldWidth(
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
