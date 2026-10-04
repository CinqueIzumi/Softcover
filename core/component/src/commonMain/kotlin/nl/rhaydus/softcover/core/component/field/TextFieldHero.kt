package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The value sets the type size rather than the type size clipping the value: it is measured against
 * the width actually available, so a long value steps down instead of wrapping. The placeholder is
 * laid out beside the field, not behind it, so the caret sits at the start of the hint rather than
 * in the middle of it.
 */
@Composable
internal fun HeroTextField(
    model: TextFieldUiModel,
    onEvent: (TextFieldEvent) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val heroStyle = MaterialTheme.editorialTypography.display

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        val fittedStyle = heroStyle.copy(
            fontSize = fittedHeroFontSize(
                text = model.text.ifEmpty { model.placeholder },
                style = heroStyle,
                availableWidth = maxWidth,
            ),
            textAlign = TextAlign.Center,
        )

        BasicTextField(
            value = model.text,
            onValueChange = { text ->
                onEvent(
                    TextFieldEvent.ValueChanged(
                        key = model.key,
                        text = text,
                    ),
                )
            },
            enabled = model.enabled,
            singleLine = true,
            textStyle = fittedStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
            keyboardOptions = model.keyboardOptions(),
            keyboardActions = KeyboardActions(onDone = { onEvent(TextFieldEvent.Submitted(key = model.key)) }),
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Free to shrink to its content: at zero width (empty field) this is the caret
                    // alone, flush against the hint.
                    Box(
                        modifier = Modifier.weight(
                            weight = 1f,
                            fill = false,
                        ),
                    ) {
                        innerTextField()
                    }

                    if (model.text.isEmpty()) {
                        SoftcoverText(
                            text = model.placeholder,
                            style = fittedStyle,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
    }
}

/**
 * The largest size at which [text] still fits [availableWidth] on one line, stepping down 1sp at a
 * time from the role's own size to a 22sp floor. 12dp is held back so the trailing caret never clips
 * at the moment the field fills up.
 */
@Composable
private fun fittedHeroFontSize(
    text: String,
    style: TextStyle,
    availableWidth: Dp,
): TextUnit {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    return remember(text, style, availableWidth, density) {
        val availablePx = with(density) { (availableWidth - 12.dp).toPx() }

        var candidate = style.fontSize
        while (candidate > 22.sp) {
            val measured = textMeasurer.measure(
                text = AnnotatedString(text = text),
                style = style.copy(fontSize = candidate),
                maxLines = 1,
                softWrap = false,
            )

            if (measured.size.width <= availablePx) break

            candidate = (candidate.value - 1f).sp
        }

        candidate
    }
}
