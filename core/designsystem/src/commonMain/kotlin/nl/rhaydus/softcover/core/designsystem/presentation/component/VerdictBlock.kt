package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.isBlank

private const val VERDICT_BLOCK_REVIEW_COLLAPSED_LINES = 8

/**
 * The book-page "Your verdict" card, replacing the old separate rating row + review card. Read-only —
 * tapping anywhere on the card (or the "Edit" link, once a verdict exists) reports [onEditClick], which
 * the caller wires to open [VerdictSheet] in `EDIT` context; rating and review are only ever changed
 * inside that sheet, never inline on the book page.
 *
 * Renders **empty** (outline stars + an invitation) when neither [rating] nor [review] carries content,
 * or **filled** — whichever of a gold star row / score line and a rendered [review] paragraph actually
 * has content, separated by a hairline when both are present — followed by a "Tap to edit" hint.
 */
@Composable
fun VerdictBlock(
    rating: Double?,
    review: ReviewDocument?,
    hasSpoilers: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayedReview = review?.takeIf { it.isBlank().not() }
    val hasVerdict = rating != null || displayedReview != null

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Your verdict".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (hasVerdict) {
                Text(
                    text = "Edit",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .pointerHandCursor()
                        .clickable(onClick = onEditClick),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            onClick = onEditClick,
        ) {
            Column(modifier = Modifier.padding(if (hasVerdict) 18.dp else 20.dp)) {
                if (hasVerdict) {
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

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = verdictScoreLineFor(score = verdictScoreOf(rating = rating)).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.6.sp,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        if (displayedReview != null) {
                            Spacer(modifier = Modifier.height(14.dp))

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    if (displayedReview != null) {
                        ReviewDocumentText(
                            document = displayedReview,
                            style = MaterialTheme.editorialTypography.bodyLarge.copy(
                                fontStyle = FontStyle.Normal,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = VERDICT_BLOCK_REVIEW_COLLAPSED_LINES,
                            overflow = TextOverflow.Ellipsis,
                            onClick = onEditClick,
                        )

                        if (hasSpoilers) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Marked as containing spoilers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tap to edit".uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Rate it and say a few words…",
                        style = MaterialTheme.editorialTypography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Out of ten, your ten — spoilers optional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
