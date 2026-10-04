package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover

/**
 * Renders a structured [RichTextUiModel] read-only: bold and italic runs are styled inline (the base
 * face is upright so an italic run is actually distinguishable), and a run marked as a spoiler is
 * hidden under a solid [spoilerCover] block until the reader taps that span to reveal it. This per-span
 * hiding is independent of any whole-review spoiler gate the caller wraps around it.
 *
 * Tapping a hidden spoiler reveals it and consumes the tap; tapping anywhere else invokes [onClick]
 * (when provided) so the surrounding card — e.g. the personal review card — still opens on a tap that
 * does not land on a concealed spoiler.
 *
 * Named for what it renders, not for where the text came from — nothing about formatted prose is
 * specific to a review. Reach for it for any displayed review body; never route review content through
 * `htmlToAnnotatedString` (that path is for server HTML, e.g. a book description, not for reviews).
 *
 * Text past [maxLines] ends in an ellipsis.
 */
@Composable
fun RichText(
    model: RichTextUiModel,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.editorialTypography.body.copy(fontStyle = FontStyle.Normal),
    color: Color = LocalContentColor.current,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val coverColor: Color = MaterialTheme.colorScheme.spoilerCover

    var revealed by remember(model) { mutableStateOf(emptySet<Int>()) }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val (annotated: AnnotatedString, spoilerRanges: List<IntRange>) = remember(model, revealed, coverColor) {
        buildRichText(
            model = model,
            revealed = revealed,
            coverColor = coverColor,
        )
    }

    val tapModifier: Modifier = if (spoilerRanges.isNotEmpty() || onClick != null) {
        Modifier.pointerInput(spoilerRanges, onClick) {
            detectTapGestures { position ->
                val layout = layoutResult ?: return@detectTapGestures

                val offset = layout.getOffsetForPosition(position = position)

                val index = spoilerRanges.indexOfFirst { offset in it }

                if (index >= 0 && revealed.contains(index).not()) {
                    revealed = revealed + index
                } else {
                    onClick?.invoke()
                }
            }
        }
    } else {
        Modifier
    }

    SoftcoverText(
        text = annotated,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = {
            layoutResult = it

            onTextLayout(it)
        },
        modifier = modifier.then(tapModifier),
    )
}

/** The model's styled text and, in reading order, the range each spoiler run occupies in it. */
private fun buildRichText(
    model: RichTextUiModel,
    revealed: Set<Int>,
    coverColor: Color,
): Pair<AnnotatedString, List<IntRange>> {
    val spoilerRanges = mutableListOf<IntRange>()

    val annotated = buildAnnotatedString {
        model.paragraphs.forEachIndexed { index, paragraph ->
            if (index > 0) append('\n')

            paragraph.runs.forEach { run ->
                val start = length

                append(run.text)

                val isSpoiler: Boolean = run.spoiler && run.text.isNotEmpty()

                val hidden: Boolean = isSpoiler && revealed.contains(spoilerRanges.size).not()

                addStyle(
                    style = SpanStyle(
                        fontWeight = if (run.bold) FontWeight.Bold else null,
                        fontStyle = if (run.italic) FontStyle.Italic else null,
                        color = if (hidden) Color.Transparent else Color.Unspecified,
                        background = if (hidden) coverColor else Color.Unspecified,
                    ),
                    start = start,
                    end = length,
                )

                if (isSpoiler) spoilerRanges.add(start until length)
            }
        }
    }

    return annotated to spoilerRanges
}
