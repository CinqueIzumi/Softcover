package nl.rhaydus.softcover.feature.settings.domain.model

import kotlinx.collections.immutable.ImmutableList

/**
 * The parsed public roadmap, ready to render. [fetchedAtEpochMillis] is `null` only for [source]
 * [RoadmapSource.BUNDLED] - the build-time fallback carries no fetch timestamp of its own.
 */
data class RoadmapDocument(
    val blocks: ImmutableList<RoadmapBlock>,
    val fetchedAtEpochMillis: Long?,
    val source: RoadmapSource,
)
