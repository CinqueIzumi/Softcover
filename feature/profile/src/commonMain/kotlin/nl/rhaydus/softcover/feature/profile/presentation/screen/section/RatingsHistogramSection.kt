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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution

private val RATINGS_BAR_AREA_HEIGHT = 88.dp
private val RATINGS_LABEL_HEIGHT = 16.dp
private val RATINGS_BAR_MIN_HEIGHT = 10.dp
private val RATINGS_BAR_MAX_WIDTH = 14.dp
private const val RATINGS_WHOLE_STEP_WIDTH_FRACTION = 0.72f
private const val RATINGS_HALF_STEP_WIDTH_FRACTION = 0.48f
private const val RATINGS_HALF_STEP_ALPHA = 0.55f

/**
 * "How you score" — a number-forward average and a half-star histogram, with [header] and [caption]
 * supplying the band-driven copy.
 */
@Composable
internal fun RatingsHistogramSection(
    ratings: RatingsDistribution,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(model = header)

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
                text = caption,
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

        Divider(model = DividerUiModel.Vertical(length = 32.dp))

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
