package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.feature.profile.presentation.screen.RatingBandCopy

// The band → copy mapping is authored here in presentation from the domain-classified RatingBand,
// mirroring the InlineErrorState precedent of keeping user-facing copy out of the domain layer.
private fun RatingBand.toCopy(): RatingBandCopy = when (this) {
    RatingBand.GENEROUS -> RatingBandCopy(
        headline = "A generous but honest hand",
        caption = "Half-stars count. Most of yours land between four and five — you round up when a book earns it.",
    )

    RatingBand.SUPERFAN -> RatingBandCopy(
        headline = "Easily, happily impressed",
        caption = "Your average sits north of four and a half — you finish what you start, and it usually earns the " +
            "top shelf.",
    )

    RatingBand.CENTRIST -> RatingBandCopy(
        headline = "You keep to the middle of the shelf",
        caption = "Your ratings cluster around three and a half — steady and considered, rarely swinging to an " +
            "extreme.",
    )

    RatingBand.EXACTING -> RatingBandCopy(
        headline = "A hard star to earn",
        caption = "Your average runs low — a five-star read is rare here, which makes the ones you give out mean " +
            "something.",
    )

    RatingBand.POLARISED -> RatingBandCopy(
        headline = "Love it or leave it",
        caption = "Your ratings split into two camps, low and high, with little in between — a book either wins " +
            "you over or it doesn't.",
    )

    RatingBand.NEW_READER -> RatingBandCopy(
        headline = "Still finding your scale",
        caption = "Not enough ratings yet to say much about your taste — a few more books will fill this in.",
    )
}

private val RATINGS_BAR_AREA_HEIGHT = 88.dp
private val RATINGS_LABEL_HEIGHT = 16.dp
private val RATINGS_BAR_MIN_HEIGHT = 10.dp
private val RATINGS_BAR_MAX_WIDTH = 14.dp
private const val RATINGS_WHOLE_STEP_WIDTH_FRACTION = 0.72f
private const val RATINGS_HALF_STEP_WIDTH_FRACTION = 0.48f
private const val RATINGS_HALF_STEP_ALPHA = 0.55f

/**
 * "How you score" — a band-driven headline and caption over a number-forward average and a half-star
 * histogram. The headline/caption pair is chosen from [RatingsDistribution.band] (average + distribution
 * shape), never from the bare average.
 */
@Composable
internal fun RatingsHistogramSection(
    ratings: RatingsDistribution,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val copy = ratings.band.toCopy()

    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "How you score",
            headline = copy.headline,
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (ratings.totalRatings == 0 && isLoading.not()) {
            Text(
                text = "Rate a few books you've finished and this fills in.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            RatingsAverageRow(
                ratings = ratings,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(24.dp))

            RatingsHistogramChart(
                buckets = ratings.halfStarBuckets,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = copy.caption,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RatingsAverageRow(
    ratings: RatingsDistribution,
    isLoading: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.Bottom) {
            StatNumber(
                model = StatNumberUiModel(
                    value = ratings.average,
                    format = StatNumberFormat.Decimal(fractionDigits = RATING_FRACTION_DIGITS),
                ),
                style = MaterialTheme.editorialTypography.statLarge.copy(
                    fontSize = 52.sp,
                    lineHeight = 52.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )

            Text(
                text = "/5",
                style = MaterialTheme.editorialTypography.headlineSmall.copy(fontSize = 19.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )

            Spacer(modifier = Modifier.width(6.dp))

            val star = drawableIconResource(
                icon = SoftcoverIcon.StarFilled,
                contentDescription = "",
            )

            Icon(
                painter = star.getIconPainter(),
                contentDescription = star.contentDescription,
                tint = RatingGold,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically),
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.height(32.dp),
        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = "AVERAGE ACROSS ${ratings.totalRatings} BOOKS",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Bars are bounded to [RATINGS_BAR_AREA_HEIGHT] by construction — every bar height is a fraction of the
 * tallest bucket, so a large absolute count never pushes a bar past its allotted area. The x-labels sit
 * in their own fixed-height slot below the bar area so a tall bar can never clip them.
 */
@Composable
private fun RatingsHistogramChart(
    buckets: List<Int>,
    modifier: Modifier = Modifier,
) {
    val maxCount = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        buckets.forEachIndexed { index, count ->
            val isWholeStep = index % 2 == 0
            val barHeight = (RATINGS_BAR_AREA_HEIGHT * (count.toFloat() / maxCount))
                .let { if (count > 0) it.coerceAtLeast(RATINGS_BAR_MIN_HEIGHT) else it }
            val widthFraction = if (isWholeStep) RATINGS_WHOLE_STEP_WIDTH_FRACTION else RATINGS_HALF_STEP_WIDTH_FRACTION

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.height(RATINGS_BAR_AREA_HEIGHT),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(widthFraction)
                            .widthIn(max = RATINGS_BAR_MAX_WIDTH)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (isWholeStep) 1f else RATINGS_HALF_STEP_ALPHA,
                                ),
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier.height(RATINGS_LABEL_HEIGHT),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (isWholeStep) {
                        Text(
                            text = "${(index / 2) + 1}★",
                            style = MaterialTheme.editorialTypography.eyebrowSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
