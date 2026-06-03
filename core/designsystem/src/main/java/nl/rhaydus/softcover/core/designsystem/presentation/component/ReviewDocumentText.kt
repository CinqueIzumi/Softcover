package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover
import nl.rhaydus.softcover.core.domain.model.ReviewDocument

/**
 * Renders a structured [ReviewDocument] read-only: bold and italic runs are styled inline (the base
 * face is upright so an italic run is actually distinguishable), and a run marked as a spoiler is
 * hidden under a solid [spoilerCover] block until the reader taps that span to reveal it. This per-span
 * hiding is independent of any whole-review spoiler gate the caller wraps around it.
 *
 * Tapping a hidden spoiler reveals it and consumes the tap; tapping anywhere else invokes [onClick]
 * (when provided) so the surrounding card — e.g. the personal review card — still opens on a tap that
 * does not land on a concealed spoiler.
 */
@Composable
fun ReviewDocumentText(
    document: ReviewDocument,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.editorialTypography.body.copy(fontStyle = FontStyle.Normal),
    color: Color = LocalContentColor.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onClick: (() -> Unit)? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val coverColor: Color = MaterialTheme.colorScheme.spoilerCover

    var revealed by remember(document) { mutableStateOf(emptySet<Int>()) }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val spoilerRanges: List<IntRange> = remember(document) { spoilerRangesOf(document = document) }

    val annotated: AnnotatedString = remember(document, revealed, coverColor) {
        buildReviewAnnotatedString(
            document = document,
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

    Text(
        text = annotated,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = {
            layoutResult = it

            onTextLayout(it)
        },
        modifier = modifier.then(tapModifier),
    )
}

private fun buildReviewAnnotatedString(
    document: ReviewDocument,
    revealed: Set<Int>,
    coverColor: Color,
): AnnotatedString = buildAnnotatedString {
    var spoilerIndex = 0

    document.paragraphs.forEachIndexed { index, paragraph ->
        if (index > 0) append('\n')

        paragraph.runs.forEach { run ->
            val start = length

            append(run.text)

            val isSpoiler: Boolean = run.spoiler && run.text.isNotEmpty()

            val hidden: Boolean = isSpoiler && revealed.contains(spoilerIndex).not()

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

            if (isSpoiler) spoilerIndex += 1
        }
    }
}

private fun spoilerRangesOf(document: ReviewDocument): List<IntRange> {
    val ranges = mutableListOf<IntRange>()

    var offset = 0

    document.paragraphs.forEachIndexed { index, paragraph ->
        if (index > 0) offset += 1

        paragraph.runs.forEach { run ->
            val end = offset + run.text.length

            if (run.spoiler && run.text.isNotEmpty()) ranges.add(offset until end)

            offset = end
        }
    }

    return ranges
}
