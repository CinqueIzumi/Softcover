package nl.rhaydus.softcover.core.component.document

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList

/**
 * Builds one [AnnotatedString] per text block so the whole run wraps as a single paragraph rather than
 * a `Row` of `Text`s. [leadingMarker] (a bullet `"•"` or an ordered `"N."`) is prepended inside a
 * [ParagraphStyle] carrying a hanging [TextIndent], so a wrapped continuation line aligns under the
 * item's own text rather than under the marker. A span with a non-null
 * [MarkdownDocumentSpanUiModel.url] is pushed as a [LinkAnnotation.Url] whose tap is reported as
 * [MarkdownDocumentEvent.LinkClicked].
 */
@Composable
internal fun markdownAnnotatedString(
    spans: ImmutableList<MarkdownDocumentSpanUiModel>,
    onEvent: (MarkdownDocumentEvent) -> Unit,
    leadingMarker: String? = null,
): AnnotatedString {
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceContainerHigh

    return buildAnnotatedString {
        if (leadingMarker != null) {
            val hangingIndent = TextIndent(
                firstLine = 0.sp,
                restLine = 20.sp,
            )

            pushStyle(style = ParagraphStyle(textIndent = hangingIndent))
            append(leadingMarker)
            append(' ')
        }

        spans.forEach { span ->
            val spanStyle = SpanStyle(
                fontWeight = if (span.bold) FontWeight.Bold else null,
                fontStyle = if (span.italic) FontStyle.Italic else null,
                fontFamily = if (span.code) FontFamily.Monospace else null,
                background = if (span.code) codeBackground else Color.Unspecified,
                color = if (span.url != null) linkColor else Color.Unspecified,
                textDecoration = if (span.url != null) TextDecoration.Underline else null,
            )

            if (span.url != null) {
                val link = LinkAnnotation.Url(
                    url = span.url,
                    linkInteractionListener = { onEvent(MarkdownDocumentEvent.LinkClicked(url = span.url)) },
                )

                withLink(link = link) {
                    withStyle(style = spanStyle) { append(span.text) }
                }
            } else {
                withStyle(style = spanStyle) { append(span.text) }
            }
        }

        if (leadingMarker != null) pop()
    }
}
