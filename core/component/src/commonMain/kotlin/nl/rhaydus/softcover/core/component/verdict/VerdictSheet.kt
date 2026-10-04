package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.richtext.RichTextMark
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType
import nl.rhaydus.softcover.core.component.richtext.applyEditToMarks
import nl.rhaydus.softcover.core.component.richtext.documentToEditorBuffer
import nl.rhaydus.softcover.core.component.richtext.editorBufferToDocument
import nl.rhaydus.softcover.core.component.richtext.isRangeMarked
import nl.rhaydus.softcover.core.component.richtext.toggleMark
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight
import kotlin.math.roundToInt

private val VERDICT_CAPTIONS = listOf(
    "A hard finish.",
    "Not for me.",
    "We didn't click.",
    "Mostly patience.",
    "Fair, in the end.",
    "Quietly worthwhile.",
    "A good one.",
    "One that lingers.",
    "Hard to put down.",
    "One of the greats.",
)

/**
 * The combined "Verdict" sheet fusing a half-star rating with an optional written review — the single
 * surface for a book's verdict.
 *
 * The header and the footer are pinned around a body that scrolls itself, so the primary action is
 * never buried below the fold, however much review text or how tall the keyboard pushes the content.
 *
 * Rating and review are held as a **local draft**, seeded once from the model's `initial*` fields.
 * [StarRatingInput] only updates the draft; nothing reaches [onEvent] until the reader taps Save
 * ([VerdictSheetEvent.Saved]), so leaving with just a rating, just a review, or neither is always
 * possible.
 */
@Composable
fun VerdictSheet(
    model: VerdictSheetUiModel,
    onEvent: (VerdictSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialBuffer = remember { documentToEditorBuffer(document = model.initialReview) }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialBuffer.text,
                selection = TextRange(initialBuffer.text.length),
            ),
        )
    }

    var marks by remember { mutableStateOf(initialBuffer.marks) }

    var spoilersChecked by remember { mutableStateOf(model.spoilers.checked) }

    var draftRating by remember { mutableStateOf(model.initialRating) }

    val spoilerHighlight: Color = MaterialTheme.colorScheme.spoilerEditorHighlight

    val onSurfaceColor: Color = MaterialTheme.colorScheme.onSurface

    val visualTransformation = remember(marks, spoilerHighlight) {
        VisualTransformation { input ->
            TransformedText(
                text = buildEditorAnnotatedString(
                    text = input.text,
                    marks = marks,
                    spoilerHighlight = spoilerHighlight,
                ),
                offsetMapping = OffsetMapping.Identity,
            )
        }
    }

    val spoilersRow = remember(model.spoilers, spoilersChecked) { model.spoilers.copy(checked = spoilersChecked) }

    val selection = textFieldValue.selection
    val hasSelection: Boolean = selection.collapsed.not()
    val selectionStart: Int = selection.min
    val selectionEnd: Int = selection.max

    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(
                    VerdictSheetEvent.Saved(
                        rating = draftRating,
                        review = editorBufferToDocument(
                            text = textFieldValue.text,
                            marks = marks,
                        ),
                        hasSpoilers = spoilersChecked,
                    ),
                )

                SheetScaffoldEvent.SecondaryClicked -> onEvent(VerdictSheetEvent.DeleteRequested)

                SheetScaffoldEvent.Dismissed -> onEvent(VerdictSheetEvent.Dismissed)

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                StarRatingInput(
                    rating = draftRating,
                    onRatingChange = { draftRating = it },
                    starIcon = drawableIconResource(
                        icon = SoftcoverIcon.StarFilled,
                        contentDescription = "",
                    ),
                    filledColor = RatingGold,
                    starSize = 42.dp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            VerdictScoreAndCaption(rating = draftRating)

            Spacer(modifier = Modifier.height(28.dp))

            SoftcoverText(
                text = "In your words".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics(mergeDescendants = true) { heading() },
            )

            Spacer(modifier = Modifier.height(10.dp))

            BasicTextField(
                value = textFieldValue,
                onValueChange = { updated ->
                    if (updated.text != textFieldValue.text) {
                        marks = applyEditToMarks(
                            marks = marks,
                            oldText = textFieldValue.text,
                            newText = updated.text,
                        )
                    }

                    textFieldValue = updated
                },
                textStyle = MaterialTheme.editorialTypography.body.copy(
                    color = onSurfaceColor,
                    fontStyle = FontStyle.Normal,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = visualTransformation,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(
                                horizontal = 16.dp,
                                vertical = 14.dp,
                            ),
                    ) {
                        if (textFieldValue.text.isEmpty()) {
                            SoftcoverText(
                                text = "What stayed with you? A line is plenty.",
                                style = MaterialTheme.editorialTypography.body.copy(
                                    fontStyle = FontStyle.Normal,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        innerTextField()
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            MarkToolbar(
                chips = model.markChips,
                activeMarks = RichTextMarkType.entries
                    .filter { type ->
                        isRangeMarked(
                            marks = marks,
                            start = selectionStart,
                            end = selectionEnd,
                            type = type,
                        )
                    }
                    .toSet(),
                enabled = hasSelection,
                onMarkToggled = { mark ->
                    marks = toggleMark(
                        marks = marks,
                        start = selectionStart,
                        end = selectionEnd,
                        type = mark,
                    )
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            SoftcoverText(
                text = "Select text, then tap Bold, Italic or Spoiler",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ToggleRow(
                model = spoilersRow,
                onEvent = { event ->
                    when (event) {
                        is ToggleEvent.Toggled -> spoilersChecked = event.checked
                    }
                },
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * The score line ("YOUR {score} / 10" — or the pre-rating prompt) and the editorial verdict caption
 * beneath it, crossfading with a slight rise on every rating change (~0.3s, [playDecorativeMotion]-
 * gated). [rating] is the sheet's local draft, so this tweens live as the reader drags across the stars.
 */
@Composable
private fun VerdictScoreAndCaption(
    rating: Double?,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val score = verdictScoreOf(rating = rating)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = score,
            transitionSpec = {
                if (playMotion) {
                    (
                        fadeIn(animationSpec = tween(durationMillis = 300)) +
                            slideInVertically(
                                animationSpec = tween(durationMillis = 300),
                            ) { height -> height / 4 }
                        ).togetherWith(fadeOut(animationSpec = tween(durationMillis = 300)))
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "VerdictScoreCaption",
        ) { animatedScore ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                VerdictScoreLine(score = animatedScore)

                Spacer(modifier = Modifier.height(6.dp))

                SoftcoverText(
                    text = verdictCaptionFor(score = animatedScore),
                    style = MaterialTheme.editorialTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** The 0..10 score a 0.5-stepped 0..5 star [rating] reads out as, or `null` before a rating exists. */
fun verdictScoreOf(rating: Double?): Int? = rating?.let { (it * 2).roundToInt() }

/** One of the ten hand-written margin-note captions for [score], or the pre-rating prompt. */
internal fun verdictCaptionFor(score: Int?): String =
    score?.let { VERDICT_CAPTIONS.getOrNull(it - 1) } ?: "How was it?"

/** "YOUR {score} / 10", or the pre-rating "TAP THE STARS" prompt, in the verdict's accent label style. */
@Composable
internal fun VerdictScoreLine(
    score: Int?,
    modifier: Modifier = Modifier,
) {
    SoftcoverText(
        text = (score?.let { "Your $it / 10" } ?: "Tap the stars").uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.6.sp,
            fontFeatureSettings = "tnum",
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

private fun buildEditorAnnotatedString(
    text: String,
    marks: List<RichTextMark>,
    spoilerHighlight: Color,
): AnnotatedString = buildAnnotatedString {
    append(text)

    marks.forEach { mark ->
        val start = mark.start.coerceIn(
            0,
            text.length,
        )
        val end = mark.end.coerceIn(
            start,
            text.length,
        )

        if (start >= end) return@forEach

        val style = when (mark.type) {
            RichTextMarkType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)

            RichTextMarkType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)

            RichTextMarkType.SPOILER -> SpanStyle(background = spoilerHighlight)
        }

        addStyle(
            style = style,
            start = start,
            end = end,
        )
    }
}

/**
 * One chip per mark, each selected while the whole selection already carries that mark. The row is inert
 * while [enabled] is false.
 */
@Composable
private fun MarkToolbar(
    chips: ImmutableMap<RichTextMarkType, ChipUiModel>,
    activeMarks: Set<RichTextMarkType>,
    enabled: Boolean,
    onMarkToggled: (RichTextMarkType) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        chips.forEach { (mark, chip) ->
            Chip(
                model = chip.copy(
                    selected = mark in activeMarks,
                    interaction = if (enabled) ChipInteraction.Clickable else ChipInteraction.Disabled,
                ),
                onEvent = { event ->
                    if (event is ChipEvent.Clicked) {
                        onMarkToggled(mark)
                    }
                },
            )
        }
    }
}
