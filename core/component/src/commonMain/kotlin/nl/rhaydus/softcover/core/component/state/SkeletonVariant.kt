package nl.rhaydus.softcover.core.component.state

/**
 * The loading anatomy a [Skeleton] mirrors. [ExploreTrendingRail] and [ExploreBecauseYouReadRail]
 * render identically today but stay separate entries so tuning one rail cannot move the other.
 */
enum class SkeletonVariant {
    ExploreSectionHeader,
    ExploreFeatured,
    ExploreTrendingRail,
    ExploreBecauseYouReadRail,
    ExploreSeries,
    ExploreMoodTile,
    RoadmapEntry,
}
