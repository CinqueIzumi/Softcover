package nl.rhaydus.softcover.feature.settings.domain.model

/**
 * The parsed public roadmap, ready to render. [fetchedAtEpochMillis] is `null` only for [source]
 * [RoadmapSource.BUNDLED] - the build-time fallback carries no fetch timestamp of its own.
 */
data class RoadmapDocument(
    val blocks: List<RoadmapBlock>,
    val fetchedAtEpochMillis: Long?,
    val source: RoadmapSource,
)
