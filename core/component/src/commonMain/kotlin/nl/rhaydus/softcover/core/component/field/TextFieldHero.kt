package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The value sets the type size rather than the type size clipping the value: it is measured against
 * the width actually available, so a long value shrinks instead of wrapping. The placeholder is
 * laid out beside the field, not behind it, so the caret sits at the start of the hint rather than
 * in the middle of it. The height is the role's own line height, so the field does not resize as the
 * value shrinks.
 */
@Composable
internal fun HeroTextField(
    model: TextFieldUiModel,
    onEvent: (TextFieldEvent) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val heroStyle = MaterialTheme.editorialTypography.display
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val lineHeight = if (heroStyle.lineHeight.isSpecified) heroStyle.lineHeight else heroStyle.fontSize * 1.2f

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { lineHeight.toDp() }),
    ) {
        val shown = model.text.ifEmpty { model.placeholder }

        val fontSize = remember(shown, heroStyle, maxWidth, density) {
            fitFontSize(
                max = heroStyle.fontSize,
                floor = 22.sp,
                availablePx = with(density) { (maxWidth - 12.dp).roundToPx() },
                widthAt = { size ->
                    textMeasurer.measure(
                        text = AnnotatedString(text = shown),
                        style = heroStyle.copy(fontSize = size),
                        maxLines = 1,
                        softWrap = false,
                    ).size.width
                },
            )
        }

        val fittedStyle = heroStyle.copy(
            fontSize = fontSize,
            textAlign = TextAlign.Center,
        )

        TextFieldInput(
            model = model,
            textStyle = fittedStyle,
            onEvent = onEvent,
            focusRequester = focusRequester,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // An empty BasicTextField still claims ten characters of width; capped at the
                    // caret's width it sits flush against the hint and leaves the hint centred.
                    Box(
                        modifier = Modifier
                            .weight(
                                weight = 1f,
                                fill = false,
                            )
                            .conditional(
                                condition = model.text.isEmpty(),
                                ifTrue = { Modifier.widthIn(max = 2.dp) },
                            ),
                    ) {
                        innerTextField()
                    }

                    if (model.text.isEmpty()) {
                        TextFieldPlaceholder(
                            text = model.placeholder,
                            enabled = model.enabled,
                            style = fittedStyle,
                        )
                    }
                }
            },
        )
    }
}

/**
 * The largest size from [max] down to [floor] at which [widthAt] fits [availablePx]. It scales [max]
 * by the overflow and re-measures once, backing off a further step if the width does not scale
 * linearly with the size.
 */
internal fun fitFontSize(
    max: TextUnit,
    floor: TextUnit,
    availablePx: Int,
    widthAt: (TextUnit) -> Int,
): TextUnit {
    val widthAtMax = widthAt(max)

    if (widthAtMax <= availablePx) return max

    val estimate = (max.value * availablePx / widthAtMax).coerceIn(
        minimumValue = floor.value,
        maximumValue = max.value,
    )

    val widthAtEstimate = widthAt(estimate.sp)
    val fitted = if (widthAtEstimate <= availablePx) estimate else estimate * availablePx / widthAtEstimate

    return fitted.coerceAtLeast(floor.value).sp
}
