package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover

/**
 * Renders a structured [RichTextUiModel] read-only: bold and italic runs are styled inline (the base
 * face is upright so an italic run is actually distinguishable), and a run marked as a spoiler is
 * hidden under a solid [spoilerCover] block until the reader taps that span to reveal it. This per-span
 * hiding is independent of any whole-review spoiler gate the caller wraps around it.
 *
 * Named for what it renders, not for where the text came from — nothing about formatted prose is
 * specific to a review. Reach for it for any displayed review body; never route review content through
 * `htmlToAnnotatedString` (that path is for server HTML, e.g. a book description, not for reviews).
 */
@Composable
fun RichText(
    model: RichTextUiModel,
    onEvent: (RichTextEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (model.variant == RichTextVariant.CommunityReview) {
        CollapsibleRichText(
            model = model,
            onEvent = onEvent,
            modifier = modifier,
        )
    } else {
        RichTextBody(
            model = model,
            maxLines = model.variant.lineCap(),
            onEvent = onEvent,
            onTextLayout = {},
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CollapsibleRichText(
    model: RichTextUiModel,
    onEvent: (RichTextEvent) -> Unit,
    modifier: Modifier,
) {
    var expanded by rememberSaveable(model) { mutableStateOf(false) }
    var hasOverflow by rememberSaveable(model) { mutableStateOf(false) }

    Column(modifier = modifier.animateContentSize(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec())) {
        RichTextBody(
            model = model,
            maxLines = if (expanded) Int.MAX_VALUE else model.variant.lineCap(),
            onEvent = onEvent,
            onTextLayout = { layout ->
                if (expanded.not() && layout.hasVisualOverflow) {
                    hasOverflow = true
                }
            },
            modifier = Modifier,
        )

        if (hasOverflow) {
            TextButton(
                onClick = { expanded = expanded.not() },
                contentPadding = PaddingValues(horizontal = 0.dp),
            ) {
                SoftcoverText(text = if (expanded) "Show less" else "Show more")
            }
        }
    }
}

@Composable
private fun RichTextBody(
    model: RichTextUiModel,
    maxLines: Int,
    onEvent: (RichTextEvent) -> Unit,
    onTextLayout: (TextLayoutResult) -> Unit,
    modifier: Modifier,
) {
    val typography = MaterialTheme.editorialTypography
    val colors = MaterialTheme.colorScheme

    val style: TextStyle = when (model.variant) {
        RichTextVariant.Body -> typography.body.copy(fontStyle = FontStyle.Normal)

        RichTextVariant.Verdict -> typography.bodyLarge.copy(fontStyle = FontStyle.Normal)

        RichTextVariant.ShareCard -> typography.body.copy(
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center,
        )

        RichTextVariant.CommunityReview -> typography.review
    }

    val color: Color = when (model.variant) {
        RichTextVariant.Body -> LocalContentColor.current

        RichTextVariant.Verdict,
        RichTextVariant.CommunityReview,
        -> colors.onSurface

        RichTextVariant.ShareCard -> colors.onSurfaceVariant
    }

    val coverColor: Color = colors.spoilerCover

    var revealed by remember(model) { mutableStateOf(emptySet<Int>()) }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val currentOnEvent by rememberUpdatedState(onEvent)

    val reportsTaps = model.variant == RichTextVariant.Verdict

    val (annotated: AnnotatedString, spoilerRanges: List<IntRange>) = remember(model, revealed, coverColor) {
        buildRichText(
            model = model,
            revealed = revealed,
            coverColor = coverColor,
        )
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
        modifier = if (spoilerRanges.isNotEmpty() || reportsTaps) {
            modifier.pointerInput(spoilerRanges) {
                detectTapGestures { position ->
                    val layout = layoutResult ?: return@detectTapGestures

                    val offset = layout.getOffsetForPosition(position = position)

                    val index = spoilerRanges.indexOfFirst { offset in it }

                    if (index >= 0 && revealed.contains(index).not()) {
                        revealed = revealed + index
                    } else {
                        currentOnEvent(RichTextEvent.Tapped)
                    }
                }
            }
        } else {
            modifier
        },
    )
}

private fun RichTextVariant.lineCap(): Int = when (this) {
    RichTextVariant.Body -> Int.MAX_VALUE
    RichTextVariant.Verdict -> 8
    RichTextVariant.ShareCard -> 5
    RichTextVariant.CommunityReview -> 8
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
