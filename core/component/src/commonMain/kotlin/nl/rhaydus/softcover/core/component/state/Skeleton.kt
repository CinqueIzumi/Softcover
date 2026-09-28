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
    when (model.variant) {
        SkeletonVariant.ExploreSectionHeader -> ExploreSectionHeaderSkeleton(modifier)
        SkeletonVariant.ExploreFeatured -> ExploreFeaturedSkeleton(modifier)
        SkeletonVariant.ExploreTrendingRail,
        SkeletonVariant.ExploreBecauseYouReadRail,
        -> ExploreRailSkeleton(modifier)
        SkeletonVariant.ExploreSeries -> ExploreSeriesSkeleton(modifier)
        SkeletonVariant.ExploreMoodTile -> ExploreMoodTileSkeleton(modifier)
        SkeletonVariant.RoadmapEntry -> RoadmapEntrySkeleton(modifier)
    }
}

@Composable
private fun ExploreSectionHeaderSkeleton(modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .shimmer(isLoading = true),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .height(22.dp)
                .fillMaxWidth(0.5f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )
    }
}

@Composable
private fun ExploreFeaturedSkeleton(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(18.dp),
    ) {
        Box(
            modifier = Modifier
                .height(12.dp)
                .width(180.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(120.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .shimmer(isLoading = true),
            )

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(90.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(isLoading = true),
                )

                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(isLoading = true),
                )

                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(isLoading = true),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .height(ButtonSize.S.height)
                .fillMaxWidth()
                .clip(RoundedCornerShape(percent = 50))
                .shimmer(isLoading = true),
        )
    }
}

/**
 * The title reserves two 20dp bars, not 16dp ones: the loaded 2-line `titleMedium` block is 48dp,
 * and splitting it across two children adds one 8dp gap, so the bars must sum to 40dp.
 */
@Composable
private fun ExploreRailSkeleton(modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(0.55f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.4f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )
    }
}

@Composable
private fun ExploreSeriesSkeleton(modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.55f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.4f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )
    }
}

@Composable
private fun ExploreMoodTileSkeleton(modifier: Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = MoodTileDimensions.minHeight)
            .clip(RoundedCornerShape(16.dp))
            .shimmer(isLoading = true),
    )
}

/** A fixed eyebrow, headline and three body lines — the roadmap's variable-length document is not mirrored. */
@Composable
private fun RoadmapEntrySkeleton(modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        RoadmapSkeletonBar(
            height = 14.dp,
            width = 120.dp,
        )

        Spacer(modifier = Modifier.height(20.dp))

        RoadmapSkeletonBar(
            height = 24.dp,
            width = 220.dp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoadmapSkeletonBar(
            height = 16.dp,
            widthFraction = 1f,
        )

        Spacer(modifier = Modifier.height(8.dp))

        RoadmapSkeletonBar(
            height = 16.dp,
            widthFraction = 0.92f,
        )

        Spacer(modifier = Modifier.height(8.dp))

        RoadmapSkeletonBar(
            height = 16.dp,
            widthFraction = 0.6f,
        )
    }
}

@Composable
private fun RoadmapSkeletonBar(
    height: Dp,
    width: Dp? = null,
    widthFraction: Float? = null,
) {
    val widthModifier = when {
        width != null -> Modifier.width(width)
        widthFraction != null -> Modifier.fillMaxWidth(widthFraction)
        else -> Modifier.fillMaxWidth()
    }

    Box(
        modifier = widthModifier
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .shimmer(isLoading = true),
    )
}
