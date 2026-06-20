package nl.rhaydus.softcover.core.database.mapper

import nl.rhaydus.softcover.core.database.model.EditionCachePlan
import nl.rhaydus.softcover.core.database.model.EditionImageRef
import nl.rhaydus.softcover.core.domain.model.BookEdition

/**
 * Reconciles incoming [editions] against the currently cached [existingRefs] to decide what happens
 * to each edition's locally-persisted cover. The cached file lives in the DB row, so the existing
 * ref is the source of truth: a cover is kept only while the URL it was downloaded from
 * ([EditionImageRef.localImageUrl]) still matches the edition's incoming URL. When they diverge —
 * including the legacy case where the provenance URL is unknown (`null`) but a file exists — the
 * cover is evicted: the entity's path/provenance are cleared and the orphaned on-disk path is
 * surfaced in [EditionCachePlan.stalePaths] for the caller to delete after the upsert.
 */
internal fun planEditionCaching(
    editions: List<BookEdition>,
    existingRefs: List<EditionImageRef>,
): EditionCachePlan {
    val refsById = existingRefs.associateBy { it.id }
    val stalePaths = mutableListOf<String>()

    val entities = editions.map { edition ->
        val existing = refsById[edition.id]
        val cachedPath = existing?.localImagePath

        when {
            cachedPath == null -> edition.toEntity().copy(
                localImagePath = null,
                localImageUrl = null,
            )

            existing.localImageUrl == edition.url -> edition.toEntity().copy(
                localImagePath = cachedPath,
                localImageUrl = existing.localImageUrl,
            )

            // URL changed since the file was downloaded — including the legacy case where the
            // provenance is unknown (null) but the edition now has a URL: evict and re-fetch.
            else -> {
                stalePaths += cachedPath
                edition.toEntity().copy(
                    localImagePath = null,
                    localImageUrl = null,
                )
            }
        }
    }

    return EditionCachePlan(
        entities = entities,
        stalePaths = stalePaths,
    )
}
