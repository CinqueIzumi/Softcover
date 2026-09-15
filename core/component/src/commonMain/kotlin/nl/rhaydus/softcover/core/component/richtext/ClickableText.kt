package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle

/**
 * A [Text] that resolves `url` string annotations in [ClickableTextUiModel.text] to taps and reports
 * them as [ClickableTextEvent.LinkClicked], so a screen never wires its own `pointerInput` /
 * `detectTapGestures` for an inline link.
 *
 * [inlineContent] is forwarded to the underlying [Text] unchanged — pass it to glue a small glyph
 * (e.g. an external-link icon) directly onto a run of text via
 * [androidx.compose.foundation.text.appendInlineContent] at the call site. It stays a parameter
 * rather than a model field because its values are composable slots, not data.
 *
 * Like `RichText`, it takes the `Text`-shaped render parameters after the model: which face prose is
 * set in belongs to the surface rather than to the words.
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

    Text(
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
