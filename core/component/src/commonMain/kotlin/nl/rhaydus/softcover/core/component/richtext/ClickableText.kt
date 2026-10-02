package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import nl.rhaydus.softcover.core.component.text.SoftcoverText

/**
 * A [SoftcoverText] that resolves `url` string annotations in [ClickableTextUiModel.text] to taps and reports
 * them as [ClickableTextEvent.LinkClicked], so a screen never wires its own `pointerInput` /
 * `detectTapGestures` for an inline link.
 *
 * [inlineContent] stays a parameter rather than a model field because its values are composable slots, not data.
 * [style] is an R11 holdout — it belongs on [model], not on this signature.
 */
@Composable
fun ClickableText(
    model: ClickableTextUiModel,
    onEvent: (ClickableTextEvent) -> Unit,
    style: TextStyle,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    var textLayoutResult by remember {
        mutableStateOf<TextLayoutResult?>(null)
    }

    SoftcoverText(
        text = model.text,
        style = style,
        inlineContent = inlineContent,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val layoutResult = textLayoutResult ?: return@detectTapGestures

                    val position = layoutResult.getOffsetForPosition(tapOffset)

                    model.text
                        .getStringAnnotations(
                            start = position,
                            end = position,
                        )
                        .firstOrNull()
                        ?.let { annotation -> onEvent(ClickableTextEvent.LinkClicked(url = annotation.item)) }
                }
            },
        onTextLayout = { layout ->
            textLayoutResult = layout
        },
    )
}
