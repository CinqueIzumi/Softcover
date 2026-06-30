package nl.rhaydus.softcover.core.database.model

/**
 * Outcome of reconciling a batch of incoming editions against what is already cached: the
 * [entities] to upsert (with stale local-cover references cleared) and the [stalePaths] — on-disk
 * cover files whose edition URL has changed and that the caller must delete after the upsert.
 */
internal data class EditionCachePlan(
    val entities: List<BookEditionEntity>,
    val stalePaths: List<String>,
)
