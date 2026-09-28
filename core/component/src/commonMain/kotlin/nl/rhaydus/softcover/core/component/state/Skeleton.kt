package nl.rhaydus.softcover.core.component.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.component.bookcard.MoodTileDimensions

/**
 * The shimmering placeholder a loading screen renders in place of content it does not have yet.
 * Every explore variant copies its loaded counterpart's footprint bar for bar, so `SkeletonCrossfade`
 * never resizes between them.
 */
@Composable
fun Skeleton(
    model: SkeletonUiModel,
    modifier: Modifier = Modifier,
) {
    val skeletonModifier = modifier.clearAndSetSemantics {
        progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }
    when (model.variant) {
        SkeletonVariant.ExploreSectionHeader -> ExploreSectionHeaderSkeleton(skeletonModifier)
        SkeletonVariant.ExploreFeatured -> ExploreFeaturedSkeleton(skeletonModifier)
        SkeletonVariant.ExploreRail -> ExploreRailSkeleton(skeletonModifier)
        SkeletonVariant.ExploreSeries -> ExploreSeriesSkeleton(skeletonModifier)
        SkeletonVariant.ExploreMoodTile -> ExploreMoodTileSkeleton(skeletonModifier)
        SkeletonVariant.RoadmapEntry -> RoadmapEntrySkeleton(skeletonModifier)
    }
}

/**
 * Every skeleton dimension in one table, plus the small vocabulary — [Extent], [Line] — those
 * dimensions are expressed in. Nested rather than top-level so the file keeps a single top-level type.
 */
private object SkeletonMetrics {
    sealed interface Extent {
        data class Fixed(val width: Dp) : Extent

        data class Fraction(val fraction: Float) : Extent
    }

    data class Line(
        val height: Dp,
        val fraction: Float,
    )

    val BarCorner: Shape = RoundedCornerShape(4.dp)
    val CoverCorner: Shape = RoundedCornerShape(6.dp)
    val Pill: Shape = RoundedCornerShape(percent = 50)
    val MoodTileCorner: Shape = RoundedCornerShape(16.dp)
    val HeaderAccentCorner: Shape = RoundedCornerShape(2.dp)
    val FeaturedContainerCorner: Shape = RoundedCornerShape(20.dp)

    const val COVER_ASPECT_RATIO = 2f / 3f
    val CoverStackGap = 8.dp

    val HeaderAccentBarHeight = 4.dp
    val HeaderAccentBarWidth = 32.dp
    val HeaderAccentGap = 12.dp
    val HeaderEyebrowHeight = 12.dp
    val HeaderEyebrowWidth = 100.dp
    val HeaderHeadlineGap = 8.dp
    val HeaderHeadlineHeight = 22.dp
    const val HEADER_HEADLINE_FRACTION = 0.5f

    val FeaturedPadding = 18.dp
    val FeaturedTitleHeight = 12.dp
    val FeaturedTitleWidth = 180.dp
    val FeaturedTitleGap = 12.dp
    val FeaturedRowGap = 8.dp
    val FeaturedBadgeHeight = 20.dp
    val FeaturedBadgeWidth = 120.dp
    val FeaturedLabelHeight = 14.dp
    val FeaturedLabelWidth = 90.dp
    val FeaturedBodyGap = 14.dp
    val FeaturedBodyRowGap = 18.dp
    val FeaturedCoverWidth = 96.dp
    val FeaturedTextGap = 10.dp
    val FeaturedHeadlineHeight = 24.dp
    val FeaturedSubline1Height = 12.dp
    const val FEATURED_SUBLINE_1_FRACTION = 0.6f
    val FeaturedSubline2Height = 12.dp
    const val FEATURED_SUBLINE_2_FRACTION = 0.85f
    val FeaturedButtonGap = 16.dp

    /**
     * Two 20dp bars, not 16dp: the loaded two-line `titleMedium` block is 48dp, and splitting it
     * across two bars adds one 8dp gap, so the bars must sum to 40dp.
     */
    val RailLines = listOf(
        Line(
            height = 20.dp,
            fraction = 0.9f,
        ),
        Line(
            height = 20.dp,
            fraction = 0.55f,
        ),
        Line(
            height = 16.dp,
            fraction = 0.4f,
        ),
    )
    val SeriesLines = listOf(
        Line(
            height = 16.dp,
            fraction = 0.7f,
        ),
        Line(
            height = 16.dp,
            fraction = 0.95f,
        ),
        Line(
            height = 16.dp,
            fraction = 0.55f,
        ),
        Line(
            height = 16.dp,
            fraction = 0.4f,
        ),
    )

    val RoadmapEyebrowHeight = 14.dp
    val RoadmapEyebrowWidth = 120.dp
    val RoadmapEyebrowGap = 20.dp
    val RoadmapHeadlineHeight = 24.dp
    val RoadmapHeadlineWidth = 220.dp
    val RoadmapHeadlineGap = 16.dp
    val RoadmapBodyLineHeight = 16.dp
    val RoadmapBodyGap = 8.dp
    val RoadmapBodyFractions = listOf(1f, 0.92f, 0.6f)
}

private fun SkeletonMetrics.Extent.widthModifier(): Modifier = when (this) {
    is SkeletonMetrics.Extent.Fixed -> Modifier.width(width)
    is SkeletonMetrics.Extent.Fraction -> Modifier.fillMaxWidth(fraction)
}

@Composable
private fun SkeletonBar(
    height: Dp,
    extent: SkeletonMetrics.Extent,
    shape: Shape = SkeletonMetrics.BarCorner,
) {
    Box(
        modifier = extent.widthModifier()
            .height(height)
            .clip(shape)
            .shimmer(isLoading = true),
    )
}

@Composable
private fun CoverStackSkeleton(
    lines: List<SkeletonMetrics.Line>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SkeletonMetrics.CoverStackGap),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(SkeletonMetrics.COVER_ASPECT_RATIO)
                .clip(SkeletonMetrics.CoverCorner)
                .shimmer(isLoading = true),
        )

        lines.forEach { line ->
            SkeletonBar(
                height = line.height,
                extent = SkeletonMetrics.Extent.Fraction(line.fraction),
            )
        }
    }
}

@Composable
private fun ExploreSectionHeaderSkeleton(modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBar(
                height = SkeletonMetrics.HeaderAccentBarHeight,
                extent = SkeletonMetrics.Extent.Fixed(SkeletonMetrics.HeaderAccentBarWidth),
                shape = SkeletonMetrics.HeaderAccentCorner,
            )

            Spacer(modifier = Modifier.width(SkeletonMetrics.HeaderAccentGap))

            SkeletonBar(
                height = SkeletonMetrics.HeaderEyebrowHeight,
                extent = SkeletonMetrics.Extent.Fixed(SkeletonMetrics.HeaderEyebrowWidth),
            )
        }

        Spacer(modifier = Modifier.height(SkeletonMetrics.HeaderHeadlineGap))

        SkeletonBar(
            height = SkeletonMetrics.HeaderHeadlineHeight,
            extent = SkeletonMetrics.Extent.Fraction(SkeletonMetrics.HEADER_HEADLINE_FRACTION),
        )
    }
}

@Composable
private fun ExploreFeaturedSkeleton(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SkeletonMetrics.FeaturedContainerCorner)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(SkeletonMetrics.FeaturedPadding),
    ) {
        SkeletonBar(
            height = SkeletonMetrics.FeaturedTitleHeight,
            extent = SkeletonMetrics.Extent.Fixed(SkeletonMetrics.FeaturedTitleWidth),
        )

        Spacer(modifier = Modifier.height(SkeletonMetrics.FeaturedTitleGap))

        Row(
            horizontalArrangement = Arrangement.spacedBy(SkeletonMetrics.FeaturedRowGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBar(
                height = SkeletonMetrics.FeaturedBadgeHeight,
                extent = SkeletonMetrics.Extent.Fixed(SkeletonMetrics.FeaturedBadgeWidth),
                shape = SkeletonMetrics.Pill,
            )

            SkeletonBar(
                height = SkeletonMetrics.FeaturedLabelHeight,
                extent = SkeletonMetrics.Extent.Fixed(SkeletonMetrics.FeaturedLabelWidth),
            )
        }

        Spacer(modifier = Modifier.height(SkeletonMetrics.FeaturedBodyGap))

        Row(
            horizontalArrangement = Arrangement.spacedBy(SkeletonMetrics.FeaturedBodyRowGap),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(SkeletonMetrics.FeaturedCoverWidth)
                    .aspectRatio(SkeletonMetrics.COVER_ASPECT_RATIO)
                    .clip(SkeletonMetrics.BarCorner)
                    .shimmer(isLoading = true),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SkeletonMetrics.FeaturedTextGap),
            ) {
                SkeletonBar(
                    height = SkeletonMetrics.FeaturedHeadlineHeight,
                    extent = SkeletonMetrics.Extent.Fraction(1f),
                )

                SkeletonBar(
                    height = SkeletonMetrics.FeaturedSubline1Height,
                    extent = SkeletonMetrics.Extent.Fraction(SkeletonMetrics.FEATURED_SUBLINE_1_FRACTION),
                )

                SkeletonBar(
                    height = SkeletonMetrics.FeaturedSubline2Height,
                    extent = SkeletonMetrics.Extent.Fraction(SkeletonMetrics.FEATURED_SUBLINE_2_FRACTION),
                )
            }
        }

        Spacer(modifier = Modifier.height(SkeletonMetrics.FeaturedButtonGap))

        SkeletonBar(
            height = ButtonSize.S.height,
            extent = SkeletonMetrics.Extent.Fraction(1f),
            shape = SkeletonMetrics.Pill,
        )
    }
}

@Composable
private fun ExploreRailSkeleton(modifier: Modifier) {
    CoverStackSkeleton(
        lines = SkeletonMetrics.RailLines,
        modifier = modifier,
    )
}

@Composable
private fun ExploreSeriesSkeleton(modifier: Modifier) {
    CoverStackSkeleton(
        lines = SkeletonMetrics.SeriesLines,
        modifier = modifier,
    )
}

@Composable
private fun ExploreMoodTileSkeleton(modifier: Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = MoodTileDimensions.minHeight)
            .clip(SkeletonMetrics.MoodTileCorner)
            .shimmer(isLoading = true),
    )
}

/** A fixed eyebrow, headline and three body lines — the roadmap's variable-length document is not mirrored. */
@Composable
private fun RoadmapEntrySkeleton(modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonBar(
            height = SkeletonMetrics.RoadmapEyebrowHeight,
            extent = SkeletonMetrics.Extent.Fixed(SkeletonMetrics.RoadmapEyebrowWidth),
        )

        Spacer(modifier = Modifier.height(SkeletonMetrics.RoadmapEyebrowGap))

        SkeletonBar(
            height = SkeletonMetrics.RoadmapHeadlineHeight,
            extent = SkeletonMetrics.Extent.Fixed(SkeletonMetrics.RoadmapHeadlineWidth),
        )

        Spacer(modifier = Modifier.height(SkeletonMetrics.RoadmapHeadlineGap))

        Column(verticalArrangement = Arrangement.spacedBy(SkeletonMetrics.RoadmapBodyGap)) {
            SkeletonMetrics.RoadmapBodyFractions.forEach { fraction ->
                SkeletonBar(
                    height = SkeletonMetrics.RoadmapBodyLineHeight,
                    extent = SkeletonMetrics.Extent.Fraction(fraction),
                )
            }
        }
    }
}
