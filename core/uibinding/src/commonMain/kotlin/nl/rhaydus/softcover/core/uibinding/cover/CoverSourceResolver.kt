package nl.rhaydus.softcover.core.uibinding.cover

import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.BookEdition

/**
 * Resolves which [CoverSource] a cover surface should load, preferring [edition]'s own image, then
 * [defaultEdition]'s, then [fallbackCoverUrl] — the ladder `EditionImage` walked before the
 * component-library migration, re-encoded onto [CoverSource] rather than a Coil-ready string.
 *
 * A local file always outranks any remote URL, and a persist id is only ever attached to the
 * middle rung: a local file needs no persisting, and a bare [fallbackCoverUrl] carries no edition
 * to persist against.
 */
fun resolveCoverSource(
    edition: BookEdition?,
    defaultEdition: BookEdition?,
    fallbackCoverUrl: String?,
): CoverSource? {
    val localEdition = edition?.takeIf { it.existingLocalSource() != null }
        ?: defaultEdition?.takeIf { it.existingLocalSource() != null }

    if (localEdition != null) {
        return CoverSource.Local(
            path = localEdition.existingLocalSource()!!,
            cacheKey = localEdition.url,
        )
    }

    val urlEdition = edition?.takeIf { it.url != null }
        ?: defaultEdition?.takeIf { it.url != null }

    if (urlEdition != null) {
        return CoverSource.Remote(
            url = urlEdition.url!!,
            persistEditionId = urlEdition.id,
        )
    }

    val fallback = fallbackCoverUrl ?: return null

    return CoverSource.Remote(
        url = fallback,
        persistEditionId = null,
    )
}

private fun BookEdition.existingLocalSource(): String? =
    localImageSourceOrNull(localImagePath)
