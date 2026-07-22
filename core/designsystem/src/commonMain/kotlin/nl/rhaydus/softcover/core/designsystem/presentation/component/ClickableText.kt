package nl.rhaydus.softcover.core.designsystem.presentation.component

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle

/**
 * A [Text] that resolves `url` string annotations in [annotatedText] to taps and forwards them to
 * [handleUrlClick]. [inlineContent] is forwarded to the underlying [Text] unchanged — pass it to glue
 * a small glyph (e.g. an external-link icon) directly onto a run of text via
 * [androidx.compose.foundation.text.appendInlineContent] at the call site; empty by default so existing
 * callers are unaffected.
 */
@Composable
fun ClickableText(
    annotatedText: AnnotatedString,
    style: TextStyle,
    handleUrlClick: (String) -> Unit,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    var textLayoutResult by remember {
        mutableStateOf<TextLayoutResult?>(null)
    }

    Text(
        text = annotatedText,
        style = style,
        inlineContent = inlineContent,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val layoutResult = textLayoutResult ?: return@detectTapGestures

                    val position = layoutResult.getOffsetForPosition(tapOffset)

                    annotatedText
                        .getStringAnnotations(
                            start = position,
                            end = position,
                        )
                        .firstOrNull()
                        ?.let { annotation -> handleUrlClick(annotation.item) }
                }
            },
        onTextLayout = { layout ->
            textLayoutResult = layout
        },
    )
}
