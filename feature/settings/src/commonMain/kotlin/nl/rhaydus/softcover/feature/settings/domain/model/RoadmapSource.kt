package nl.rhaydus.softcover.feature.settings.domain.model

/**
 * Where a [RoadmapDocument] came from, so the screen can tell the reader when it is showing an
 * offline copy. [REMOTE] and [CACHE] both back onto the same on-disk row - see
 * `RoadmapRepositoryImpl` for which one a given read is labelled and why.
 */
enum class RoadmapSource {
    REMOTE,
    CACHE,
    BUNDLED,
}
