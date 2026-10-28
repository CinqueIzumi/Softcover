package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The set of filter values actually present in the source list for a tab. The picker only ever
 * offers options that would narrow the visible set — no empty-result chips. Rating buckets are
 * derived (not raw values) because per-book ratings are continuous; the buckets express the
 * common "show me ≥X★" question instead of forcing the user to pick a precise threshold.
 */
internal data class LibraryFilterOptions(
    /** Community tags (all categories) on the tab, most used first. */
    val hardcoverTags: ImmutableList<LibraryTagOption<HardcoverTagKey>> = persistentListOf(),
    /** The user's own tags on the tab, most used first. */
    val yourTags: ImmutableList<LibraryTagOption<UserTagKey>> = persistentListOf(),
    val formats: ImmutableList<String> = persistentListOf(),
    val releaseYears: ImmutableList<Int> = persistentListOf(),
    /** Year-finished facet values, populated for the Read tab only — see [buildBookFilterOptions]. */
    val readYears: ImmutableList<Int> = persistentListOf(),
    val supportsOwnedFilter: Boolean = false,
    val ratingBuckets: ImmutableList<Double> = persistentListOf(),
) {
    val isEmpty: Boolean
        get() = hardcoverTags.isEmpty() &&
            yourTags.isEmpty() &&
            formats.isEmpty() &&
            releaseYears.isEmpty() &&
            readYears.isEmpty() &&
            supportsOwnedFilter.not() &&
            ratingBuckets.isEmpty()
}
