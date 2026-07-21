package nl.rhaydus.softcover.core.designsystem.presentation.component

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import kotlin.math.roundToInt
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.VerdictSheetContext
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReviewDocument

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
 * surface both `feature/book_detail` (reopened from the book page) and `feature/reading` (raised once
 * on a genuine mark-as-read transition) host, each driven by its own TOAD state. [context] swaps copy
 * only (see the private `verdictCopyFor` below); the anatomy is identical either way.
 *
 * Rating and review are held as a **local draft** — exactly like the review editor it replaces held its
 * text/marks/spoiler flag — seeded once from [initialRating] / [initialReview] / [initialHasSpoilers].
 * [StarRatingInput] only updates the draft; nothing reaches [onSave] until the caller taps Save, so
 * leaving with just a rating, just a review, or neither is always possible.
 */
@Composable
fun VerdictSheet(
    context: VerdictSheetContext,
    bookTitle: String,
    coverEdition: BookEdition?,
    fallbackCoverUrl: String?,
    initialRating: Double?,
    initialReview: ReviewDocument,
    initialHasSpoilers: Boolean,
    canDelete: Boolean,
    onSave: (rating: Double?, review: ReviewDocument, hasSpoilers: Boolean) -> Unit,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val initialBuffer = remember { documentToEditorBuffer(document = initialReview) }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialBuffer.text,
                selection = TextRange(initialBuffer.text.length),
            ),
        )
    }

    var marks by remember { mutableStateOf(initialBuffer.marks) }

    var hasSpoilers by remember { mutableStateOf(initialHasSpoilers) }

    var draftRating by remember { mutableStateOf(initialRating) }

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

    val selection = textFieldValue.selection
    val hasSelection: Boolean = selection.collapsed.not()
    val selectionStart: Int = selection.min
    val selectionEnd: Int = selection.max

    val copy = verdictCopyFor(
        context = context,
        bookTitle = bookTitle,
    )

    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                    ),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = copy.dismissLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .pointerHandCursor()
                        .pressScaleClickable(onClick = onDismissRequest),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(state = rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    EditionImage(
                        edition = coverEdition,
                        defaultEdition = coverEdition,
                        isLoading = false,
                        coverlessTitle = bookTitle,
                        fallbackCoverUrl = fallbackCoverUrl,
                        cornerRadius = 16.dp,
                        elevation = 6.dp,
                        shadowColor = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.width(96.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                EditorialSectionHeader(
                    eyebrow = copy.eyebrow,
                    headline = copy.headline,
                    description = copy.description,
                )

                Spacer(modifier = Modifier.height(24.dp))

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

                Text(
                    text = "In your words",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            if (textFieldValue.text.isEmpty()) {
                                Text(
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

                ReviewFormattingToolbar(
                    isBold = isRangeMarked(
                        marks = marks,
                        start = selectionStart,
                        end = selectionEnd,
                        type = ReviewMarkType.BOLD,
                    ),
                    isItalic = isRangeMarked(
                        marks = marks,
                        start = selectionStart,
                        end = selectionEnd,
                        type = ReviewMarkType.ITALIC,
                    ),
                    isSpoiler = isRangeMarked(
                        marks = marks,
                        start = selectionStart,
                        end = selectionEnd,
                        type = ReviewMarkType.SPOILER,
                    ),
                    enabled = hasSelection,
                    onToggle = { type ->
                        marks = toggleMark(
                            marks = marks,
                            start = selectionStart,
                            end = selectionEnd,
                            type = type,
                        )
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select text, then tap B, i or Spoiler",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Contains spoilers",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Readers will need to tap to reveal the whole review.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = hasSpoilers,
                        onCheckedChange = { hasSpoilers = it },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 12.dp,
                        bottom = 8.dp,
                    ),
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(12.dp))

                RhaydusButton(
                    label = copy.saveLabel,
                    style = ButtonStyle.FILLED,
                    size = ButtonSize.M,
                    onClick = {
                        onSave(
                            draftRating,
                            editorBufferToDocument(
                                text = textFieldValue.text,
                                marks = marks,
                            ),
                            hasSpoilers,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (canDelete) {
                    Spacer(modifier = Modifier.height(8.dp))

                    RhaydusButton(
                        label = "Delete review",
                        style = ButtonStyle.TEXT,
                        size = ButtonSize.M,
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** The eyebrow/headline/description/dismiss/save copy that swaps per [VerdictSheetContext] (§Copy). */
private data class VerdictCopy(
    val eyebrow: String,
    val headline: String,
    val description: String?,
    val dismissLabel: String,
    val saveLabel: String,
)

private fun verdictCopyFor(
    context: VerdictSheetContext,
    bookTitle: String,
): VerdictCopy = when (context) {
    VerdictSheetContext.FINISHED -> VerdictCopy(
        eyebrow = "The last page",
        headline = "You finished $bookTitle.",
        description = "A rating and a few words, if you like — both are optional.",
        dismissLabel = "Not now",
        saveLabel = "Save",
    )

    VerdictSheetContext.EDIT -> VerdictCopy(
        eyebrow = "Your verdict",
        headline = "Your take on $bookTitle.",
        description = null,
        dismissLabel = "Cancel",
        saveLabel = "Save changes",
    )
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
                Text(
                    text = verdictScoreLineFor(score = animatedScore).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.6.sp,
                        fontFeatureSettings = "tnum",
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
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
internal fun verdictScoreOf(rating: Double?): Int? = rating?.let { (it * 2).roundToInt() }

/** One of the ten hand-written margin-note captions for [score], or the pre-rating prompt. */
internal fun verdictCaptionFor(score: Int?): String =
    score?.let { VERDICT_CAPTIONS.getOrNull(it - 1) } ?: "How was it?"

/** "Your {score} / 10", or the pre-rating "Tap the stars" prompt. */
internal fun verdictScoreLineFor(score: Int?): String =
    score?.let { "Your $it / 10" } ?: "Tap the stars"

private fun buildEditorAnnotatedString(
    text: String,
    marks: List<ReviewMark>,
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
            ReviewMarkType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)

            ReviewMarkType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)

            ReviewMarkType.SPOILER -> SpanStyle(background = spoilerHighlight)
        }

        addStyle(
            style = style,
            start = start,
            end = end,
        )
    }
}
