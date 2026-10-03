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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.displayFontFamily
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRevealReviewSpoilerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

private const val REVIEW_COLLAPSED_LINES = 8

private const val LOADING_BODY_STAND_IN =
    "A few sentences of what a reader made of the book sit here while the reviews load, " +
        "enough to fill about four lines of the card."

/**
 * "Voices" (The Book lens): section-opener bar + eyebrow, the "What readers think" italic headline,
 * the community review cards, and a footer summary line (`★ avg across N ratings`).
 */
@Composable
internal fun ReviewsSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val isLoading = state.loadingBookDetails || state.loadingReviews

    if (isLoading.not() && state.reviews.isEmpty()) return

    Spacer(modifier = Modifier.height(36.dp))

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader(model = state.reviewsSectionHeader)

        Spacer(modifier = Modifier.height(4.dp))

        SoftcoverText(
            text = "What readers think",
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column {
            if (isLoading) {
                repeat(2) { index ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    ReviewCard(
                        review = null,
                        isSpoilerRevealed = false,
                        onRevealSpoilerClick = {},
                    )
                }
            } else {
                state.reviews.forEachIndexed { index, review ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    ReviewCard(
                        review = review,
                        isSpoilerRevealed = review.id in state.revealedSpoilerReviewIds,
                        onRevealSpoilerClick = {
                            runAction(OnRevealReviewSpoilerAction(reviewId = review.id))
                        },
                    )
                }
            }

            state.book?.let { book ->
                if (book.ratingsCount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
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

                        Spacer(modifier = Modifier.width(4.dp))

                        SoftcoverText(
                            text = "${
                                formatDecimalNumber(
                                    value = book.rating,
                                    fractionDigits = 1,
                                )
                            } across ${formatGroupedNumber(book.ratingsCount)} ratings",
                            style = MaterialTheme.editorialTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: BookReviewUiModel?,
    isSpoilerRevealed: Boolean,
    onRevealSpoilerClick: () -> Unit,
) {
    val isLoading = review == null
    val avatarUrl = review?.reviewerAvatarUrl
    val reviewerName = review?.reviewerName?.uppercase()
    val monthYear = review?.reviewedMonthYear
    val rating = review?.rating

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
                    .padding(start = 16.dp, top = 4.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Whole-review spoiler gate (HEAD behavior): a review flagged as containing
                // spoilers withholds its body entirely behind a quiet "Show spoiler" text button —
                // never rendered underneath, unlike an inline-span reveal. Reveal state is
                // transient, held by the caller per review id.
                if (review == null) {
                    SoftcoverText(
                        text = null as String?,
                        standIn = LOADING_BODY_STAND_IN,
                        style = MaterialTheme.editorialTypography.review,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else if (review.hasSpoilers && isSpoilerRevealed.not()) {
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
                        overflow = TextOverflow.Ellipsis,
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

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RhaydusShimmerImage(
                        model = avatarUrl,
                        contentDescription = "Reviewer avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .skeletonMask(
                                visible = isLoading,
                                shape = RoundedCornerShape(18.dp),
                            ),
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SoftcoverText(
                                text = reviewerName,
                                standIn = "REVIEWER NAME",
                                style = MaterialTheme.editorialTypography.eyebrowSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )

                            if (isLoading || monthYear != null) {
                                Spacer(modifier = Modifier.width(6.dp))

                                SoftcoverText(
                                    text = monthYear,
                                    standIn = "January 2025",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    if (isLoading || rating != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.skeletonMask(
                                visible = isLoading,
                                shape = RoundedCornerShape(10.dp),
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp,
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

                                Spacer(modifier = Modifier.width(4.dp))

                                SoftcoverText(
                                    text = rating?.let {
                                        formatDecimalNumber(
                                            value = it,
                                            fractionDigits = 1,
                                        )
                                    },
                                    standIn = "4.0",
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
