package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.domain.model.Tag

/**
 * The set of filter values actually present in the source list for a tab. The picker only ever
 * offers options that would narrow the visible set — no empty-result chips. Rating buckets are
 * derived (not raw values) because per-book ratings are continuous; the buckets express the
 * common "show me ≥X★" question instead of forcing the user to pick a precise threshold.
 */
internal data class LibraryFilterOptions(
    val tags: ImmutableList<Tag> = persistentListOf(),
    val formats: ImmutableList<String> = persistentListOf(),
    val releaseYears: ImmutableList<Int> = persistentListOf(),
    /** Year-finished facet values, populated for the Read tab only — see [buildBookFilterOptions]. */
    val readYears: ImmutableList<Int> = persistentListOf(),
    val supportsOwnedFilter: Boolean = false,
    val ratingBuckets: ImmutableList<Double> = persistentListOf(),
) {
    val isEmpty: Boolean
        get() = tags.isEmpty() &&
            formats.isEmpty() &&
            releaseYears.isEmpty() &&
            readYears.isEmpty() &&
            supportsOwnedFilter.not() &&
            ratingBuckets.isEmpty()
}
