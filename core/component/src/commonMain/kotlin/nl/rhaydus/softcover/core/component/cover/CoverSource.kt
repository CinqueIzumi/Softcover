package nl.rhaydus.softcover.core.component.cover

/**
 * What [Cover] loads, already resolved to a Coil-loadable model — a local file path or a remote
 * URL — with nothing left for the component to decide. The resolution ladder that picks between a
 * user's local copy, an edition's remote cover, and a fallback URL is the mapper's job
 * (`:core:uibinding`), not [Cover]'s; a `null` [CoverUiModel.source] means the ladder found nothing,
 * and [Cover] falls through to its coverless rung.
 */
sealed interface CoverSource {
    /**
     * A cover persisted on-device. [cacheKey] is the URL the file was originally downloaded from —
     * when non-null, it doubles as both the memory-cache key and the placeholder memory-cache key, so
     * a cover already warm in Coil's memory cache from an earlier remote load doesn't flash blank
     * while the local file decodes.
     */
    data class Local(
        val path: String,
        val cacheKey: String?,
    ) : CoverSource

    /**
     * A cover loaded over the network. [persistEditionId] is non-null only when a successful load
     * should be persisted to disk against that edition — the reading-session notification and other
     * non-persisting surfaces resolve to `null` here.
     */
    data class Remote(
        val url: String,
        val persistEditionId: Int?,
    ) : CoverSource
}
