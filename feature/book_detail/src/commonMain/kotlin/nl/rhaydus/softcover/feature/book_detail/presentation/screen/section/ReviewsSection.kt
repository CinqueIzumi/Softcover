package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.displayFontFamily
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRevealReviewSpoilerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

private const val REVIEW_COLLAPSED_LINES = 8

/**
 * "Voices" (The Book lens): section-opener bar + eyebrow, the headline from
 * [BookDetailUiState.reviewsSectionHeader], the community review cards, and a footer summary line
 * (`★ avg across N ratings`).
 */
@Composable
internal fun ReviewsSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    if (state.reviews.isEmpty()) return

    Spacer(modifier = Modifier.height(Spacing.section))

    Column(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
        SectionHeader(model = state.reviewsSectionHeader)

        Spacer(modifier = Modifier.height(Spacing.md))

        Column {
            state.reviews.forEachIndexed { index, review ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                ReviewCard(
                    review = review,
                    onRevealSpoilerClick = {
                        runAction(OnRevealReviewSpoilerAction(reviewId = review.id))
                    },
                )
            }

            state.reviewsSummary?.let { summary ->
                Spacer(modifier = Modifier.height(Spacing.md))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val footerStarIcon = drawableIconResource(
                        icon = SoftcoverIcon.StarFilled,
                        contentDescription = "",
                    )

                    Icon(
                        painter = footerStarIcon.getIconPainter(),
                        contentDescription = footerStarIcon.contentDescription,
                        tint = RatingGold,
                        modifier = Modifier.size(14.dp),
                    )

                    Spacer(modifier = Modifier.width(Spacing.xxs))

                    SoftcoverText(
                        text = summary,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: BookReviewUiModel,
    onRevealSpoilerClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(20.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val quoteAlpha = if (isSystemInDarkTheme()) 0.22f else 0.32f

            SoftcoverText(
                text = "“",
                style = MaterialTheme.editorialTypography.quoteGlyph.copy(
                    fontSize = 92.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = quoteAlpha),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = Spacing.md, top = Spacing.xxs),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
            ) {
                Spacer(modifier = Modifier.height(Spacing.xxl))

                // Whole-review spoiler gate (HEAD behavior): a review flagged as containing
                // spoilers withholds its body entirely behind a quiet "Show spoiler" text button —
                // never rendered underneath, unlike an inline-span reveal.
                if (review.isSpoilerGated) {
                    TextButton(onClick = onRevealSpoilerClick) {
                        SoftcoverText(text = "Show spoiler")
                    }
                } else {
                    var expanded by rememberSaveable(review.id) { mutableStateOf(false) }
                    var hasOverflow by rememberSaveable(review.id) { mutableStateOf(false) }

                    RichText(
                        model = review.body,
                        style = MaterialTheme.editorialTypography.review,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expanded) Int.MAX_VALUE else REVIEW_COLLAPSED_LINES,
                        modifier = Modifier.skeletonMask(visible = review.isLoading),
                        onTextLayout = { layout ->
                            if (expanded.not() && layout.hasVisualOverflow) {
                                hasOverflow = true
                            }
                        },
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

                Spacer(modifier = Modifier.height(Spacing.md))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RhaydusShimmerImage(
                        model = review.reviewerAvatarUrl,
                        contentDescription = "Reviewer avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .skeletonMask(
                                visible = review.isLoading,
                                shape = RoundedCornerShape(18.dp),
                            ),
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SoftcoverText(
                                text = review.reviewerName.uppercase(),
                                style = MaterialTheme.editorialTypography.eyebrowSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )

                            review.reviewedMonthYear?.let { monthYear ->
                                Spacer(modifier = Modifier.width(Spacing.xs))

                                SoftcoverText(
                                    text = monthYear,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    review.rating?.let { rating ->
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.skeletonMask(
                                visible = review.isLoading,
                                shape = RoundedCornerShape(10.dp),
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = Spacing.xs,
                                    vertical = Spacing.xxs,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val ratingBadgeIcon = drawableIconResource(
                                    icon = SoftcoverIcon.StarFilled,
                                    contentDescription = "Rating",
                                )

                                Icon(
                                    painter = ratingBadgeIcon.getIconPainter(),
                                    contentDescription = ratingBadgeIcon.contentDescription,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(14.dp),
                                )

                                Spacer(modifier = Modifier.width(Spacing.xxs))

                                SoftcoverText(
                                    text = rating,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = displayFontFamily(),
                                    ),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
