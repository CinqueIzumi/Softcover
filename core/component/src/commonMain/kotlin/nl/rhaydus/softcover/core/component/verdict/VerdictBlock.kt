package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

private const val VERDICT_BLOCK_REVIEW_COLLAPSED_LINES = 8

/**
 * The book-page "Your verdict" card. Read-only — tapping anywhere on the card (or the "Edit" link, once
 * a verdict exists) reports [VerdictBlockEvent.EditRequested], which the caller wires to open
 * [VerdictSheet]; rating and review are only ever changed inside that sheet, never inline on the book
 * page.
 *
 * Renders **empty** (outline stars + an invitation) for [VerdictBlockUiModel.Empty], or **filled** —
 * whichever of a gold star row / score line and a rendered review paragraph the model carries,
 * separated by a hairline when both are present — followed by a "Tap to edit" hint.
 */
@Composable
fun VerdictBlock(
    model: VerdictBlockUiModel,
    onEvent: (VerdictBlockEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onEditClick = { onEvent(VerdictBlockEvent.EditRequested) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftcoverText(
                text = "Your verdict".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (model is VerdictBlockUiModel.Filled) {
                SoftcoverText(
                    text = "Edit",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            onClick = onEditClick,
        ) {
            when (model) {
                is VerdictBlockUiModel.Filled -> FilledVerdict(
                    model = model,
                    onEditClick = onEditClick,
                )

                VerdictBlockUiModel.Empty -> EmptyVerdict()
            }
        }
    }
}

@Composable
private fun FilledVerdict(
    model: VerdictBlockUiModel.Filled,
    onEditClick: () -> Unit,
) {
    val rating = model.rating
    val review = model.review

    Column(modifier = Modifier.padding(Spacing.md)) {
        if (rating != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRatingInput(
                    rating = rating,
                    onRatingChange = {},
                    enabled = false,
                    starIcon = drawableIconResource(
                        icon = SoftcoverIcon.StarFilled,
                        contentDescription = "",
                    ),
                    filledColor = RatingGold,
                    emptyColor = MaterialTheme.colorScheme.outlineVariant,
                    starSize = 22.dp,
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                VerdictScoreLine(score = verdictScoreOf(rating = rating))
            }

            if (review != null) {
                Spacer(modifier = Modifier.height(Spacing.md))

                Divider()

                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }

        if (review != null) {
            RichText(
                model = review,
                style = MaterialTheme.editorialTypography.bodyLarge.copy(
                    fontStyle = FontStyle.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = VERDICT_BLOCK_REVIEW_COLLAPSED_LINES,
                onClick = onEditClick,
            )

            if (model.hasSpoilers) {
                Spacer(modifier = Modifier.height(Spacing.sm))

                SoftcoverText(
                    text = "Marked as containing spoilers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        SoftcoverText(
            text = "Tap to edit".uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyVerdict() {
    Column(modifier = Modifier.padding(Spacing.md)) {
        StarRatingInput(
            rating = null,
            onRatingChange = {},
            enabled = false,
            starIcon = drawableIconResource(
                icon = SoftcoverIcon.StarFilled,
                contentDescription = "",
            ),
            filledColor = RatingGold,
            emptyColor = MaterialTheme.colorScheme.outlineVariant,
            starSize = 30.dp,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        SoftcoverText(
            text = "Rate it and say a few words…",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Spacing.xxs))

        SoftcoverText(
            text = "Out of ten, your ten — spoilers optional.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
